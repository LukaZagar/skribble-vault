package ws.luka.skribblevault.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;
import ws.luka.skribblevault.controllers.VaultTransitClient;
import ws.luka.skribblevault.dto.request.ClientEncryptionRequest;
import ws.luka.skribblevault.dto.request.TransitEncryptDataRequest;
import ws.luka.skribblevault.dto.request.TransitEncryptionRequest;
import ws.luka.skribblevault.dto.response.ClientResponse;
import ws.luka.skribblevault.dto.response.VaultEncryptionResponse;
import ws.luka.skribblevault.exceptions.EncryptionDataSizeExceededException;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class VaultEncryptionService implements CommandService {
    private WebClient webClient;
    private VaultTransitClient vaultServiceProxy;


    @Value("${spring.cloud.vault.host}")
    private String vaultUrl;

    @Value("${spring.cloud.vault.port:8200}")
    private String vaultPort;

    @Value("${spring.cloud.vault.scheme}")
    private String vaultScheme;

    @Value("${spring.cloud.vault.token}")
    private String vaultToken;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl(String.format("%s://%s:%s", vaultScheme, vaultUrl, vaultPort))
                .defaultHeader("X-Vault-Token", vaultToken)
                .defaultHeader("Content-Type", "application/json")
                .build();

        HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build();

        this.vaultServiceProxy = proxyFactory.createClient(VaultTransitClient.class);
    }


    // TODO if this error occurs do not retry 3 times

    @Override
    public Mono<ResponseEntity<ClientResponse>> execute(ClientEncryptionRequest clientEncryptionRequest, String keyName) {
        return encryptData(clientEncryptionRequest, keyName)
                .map(response -> ResponseEntity.ok().body((ClientResponse) response))
                .onErrorResume(EncryptionDataSizeExceededException.class, e -> {
                    VaultEncryptionResponse errorResponse = new VaultEncryptionResponse(); // Assuming ClientResponse can represent errors
                    errorResponse.setMessage("Data too big"); // Set the error message
                    // Set additional error information in ClientResponse if needed
                    return Mono.just(ResponseEntity.badRequest().body(errorResponse));
                })
                .onErrorResume(WebClientResponseException.NotFound.class, e -> {
                    VaultEncryptionResponse errorResponse = new VaultEncryptionResponse(); // Assuming ClientResponse can represent errors
                    errorResponse.setMessage("HashiCorp Vault Transit engine is not online."); // Set the error message
                    // Set additional error information in ClientResponse if needed
                    return Mono.just(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(errorResponse));
                })
                .onErrorResume(e -> {
                    VaultEncryptionResponse errorResponse = new VaultEncryptionResponse(); // Assuming ClientResponse can represent errors
                    errorResponse.setMessage("HashiCorp Vault Transit engine is not online."); // Set the error message
                    // Handle the custom exception for errors during encryption request
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(errorResponse));
                });
    }

    public Mono<VaultEncryptionResponse> encryptData(ClientEncryptionRequest clientEncryptionRequest, String keyName) {
        return clientEncryptionRequest.getByteStream()
                .filter(bytes -> bytes.length <= 16) // Remove if it's above 16 bytes as per requirements
                .switchIfEmpty(Mono.error(new EncryptionDataSizeExceededException()))
                .flatMap(this::encodeBase64Bytes)
                .flatMap(this::constructRequestBody)
                .flatMap(requestBody -> sendEncryptionRequest(requestBody, keyName))
                .retry(3);
    }

    private Map<String, Object> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> errorAttributes = new HashMap<>();
        errorAttributes.put("message", message);
        errorAttributes.put("status", status.value());
        errorAttributes.put("error", status.getReasonPhrase());

        return errorAttributes;
    }

    private Mono<String> encodeBase64Bytes(byte[] inputData) {
        return Mono.just(Base64.getEncoder().encodeToString(inputData));
    }

    private Mono<TransitEncryptDataRequest> constructRequestBody(String encodedData) {
        return Mono.just(new TransitEncryptDataRequest())
                .doOnNext(request -> request.setPlainText(encodedData));
    }

    private Mono<VaultEncryptionResponse> sendEncryptionRequest(TransitEncryptionRequest requestBody, String keyName) {
        return vaultServiceProxy.encrypt(requestBody, keyName);
//                .map(response -> ResponseEntity.ok().body(response)) // Map to ResponseEntity
//                .onErrorMap(e -> new RuntimeException("Error sending encryption request", e));
    }
}