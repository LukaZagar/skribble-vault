package ws.luka.skribblevault.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import ws.luka.skribblevault.controllers.VaultTransitClient;
import ws.luka.skribblevault.dto.request.ClientEncryptionRequest;
import ws.luka.skribblevault.dto.request.TransitEncryptDataRequest;
import ws.luka.skribblevault.dto.request.TransitEncryptionRequest;
import ws.luka.skribblevault.dto.response.ClientEncryptionResponse;
import ws.luka.skribblevault.dto.response.ClientResponse;
import ws.luka.skribblevault.dto.response.VaultEncryptionResponse;
import ws.luka.skribblevault.exceptions.EncryptionDataSizeExceededException;
import ws.luka.skribblevault.exceptions.GlobalExceptionHandler;

import java.time.Duration;
import java.util.Base64;

@Slf4j
@Service
public class VaultEncryptionService implements CommandService {
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
        WebClient webClient = WebClient.builder()
                .baseUrl(String.format("%s://%s:%s", vaultScheme, vaultUrl, vaultPort))
                .defaultHeader("X-Vault-Token", vaultToken)
                .defaultHeader("Content-Type", "application/json")
                .build();

        HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build();

        this.vaultServiceProxy = proxyFactory.createClient(VaultTransitClient.class);
    }

    @Override
    public Mono<ResponseEntity<ClientResponse>> execute(ClientEncryptionRequest clientEncryptionRequest, String keyName) {
        return encryptData(clientEncryptionRequest, keyName)
                .map(response -> ResponseEntity.ok().body(response));
    }

    public Mono<ClientResponse> encryptData(ClientEncryptionRequest clientEncryptionRequest, String keyName) {
        return clientEncryptionRequest.getByteStream()
                .flatMap(this::checkByteSize) // Check if we exceeded 16 bytes
                .flatMap(this::encodeBase64Bytes)
                .flatMap(this::constructRequestBody)
                .flatMap(requestBody -> sendEncryptionRequest(requestBody, keyName))
                .flatMap(this::toClientEncryptionResponse)
                .onErrorResume(GlobalExceptionHandler::handleException)
                .retryWhen(retryPolicy()); // Separate this method since we might want to modify the policy.
    }

    private Mono<ClientResponse> toClientEncryptionResponse(VaultEncryptionResponse response) {
        return Mono.just(new ClientEncryptionResponse("Successfully encrypted", HttpStatus.OK, response));
    }

    private Retry retryPolicy() {
        return Retry.backoff(3, Duration.ofMillis(100))
                .filter(throwable -> !(throwable instanceof EncryptionDataSizeExceededException)); // Exclude exception from retries
    }

    private Mono<byte[]> checkByteSize(byte[] inputBytes) {
        if (inputBytes.length > 16) {
            return Mono.error(EncryptionDataSizeExceededException::new);
        }
        return Mono.just(inputBytes);
    }

    private Mono<String> encodeBase64Bytes(byte[] inputData) {
        return Mono.just(Base64.getEncoder().encodeToString(inputData));
    }

    private Mono<? extends TransitEncryptionRequest> constructRequestBody(String encodedData) {
        return Mono.just(new TransitEncryptDataRequest())
                .doOnNext(request -> request.setPlainText(encodedData));
    }

    private Mono<VaultEncryptionResponse> sendEncryptionRequest(TransitEncryptionRequest requestBody, String keyName) {
        return vaultServiceProxy.encrypt(requestBody, keyName);
    }
}