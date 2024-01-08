package ws.luka.skribblevault.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;
import ws.luka.skribblevault.controllers.VaultTransitClient;
import ws.luka.skribblevault.dto.request.ClientEncryptionRequest;
import ws.luka.skribblevault.dto.request.TransitEncryptDataRequest;
import ws.luka.skribblevault.dto.request.TransitEncryptionRequest;
import ws.luka.skribblevault.dto.response.VaultEncryptionResponse;
import ws.luka.skribblevault.exceptions.EncryptionDataSizeExceededException;

import java.util.Base64;

@Slf4j
@Service
public class VaultEncryptionService {
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

    public Mono<VaultEncryptionResponse> encryptData(ClientEncryptionRequest clientEncryptionRequest, String keyName) {
        return clientEncryptionRequest.getByteStream()
                .filter(bytes -> bytes.length <= 16) // Remove if it's above 16 bytes as per requirements
                .switchIfEmpty(Mono.error(new EncryptionDataSizeExceededException()))
                .flatMap(this::encodeBase64Bytes)
                .flatMap(this::constructRequestBody)
                .flatMap(requestBody -> sendEncryptionRequest(requestBody, keyName))
                .onErrorResume(EncryptionDataSizeExceededException.class, e -> {
                    // Since I am not allowed to use Spring annotations / ControllerAdvice for error handling
                    //  just log the error and tell the client what the problem was.
                    // TODO simple error wrapper response entity thing ?
                    log.error("Error during encryption, data exceeded allowed limit: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Encryption failed", e));
                })
                .onErrorResume(e -> {
                    log.error("Error during encryption: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Encryption failed", e));
                })
                .retry(3);
    }

    private Mono<String> encodeBase64Bytes(byte[] inputData) {
        return Mono.just(Base64.getEncoder().encodeToString(inputData));
    }

    private Mono<TransitEncryptDataRequest> constructRequestBody(String encodedData) {
        return Mono.just(new TransitEncryptDataRequest())
                .doOnNext(request -> request.setPlainText(encodedData));
    }

    private Mono<VaultEncryptionResponse> sendEncryptionRequest(TransitEncryptionRequest requestBody, String keyName) {
        return vaultServiceProxy.encrypt(requestBody, keyName)
                .cast(VaultEncryptionResponse.class);
    }
}