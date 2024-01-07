package ws.luka.skribblevault.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ws.luka.skribblevault.dto.EncryptDataRequest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class VaultEncryptionService {
    private WebClient webClient;

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
        this.webClient = WebClient.builder().baseUrl(String.format("%s://%s:%s", vaultScheme, vaultUrl, vaultPort))
                .build();
    }

    // TODO Handle if the user uploads a AES256 key.
    // TODO Handle if a user uploads just a byte array

    public Mono<String> encryptData(EncryptDataRequest encryptDataRequest, String keyName) {
        return getBytesFromString(encryptDataRequest.getData())
                .flatMap(this::encodeBase64Bytes)
                .flatMap(this::constructRequestBody)
                .flatMap(requestBody -> sendEncryptionRequest(requestBody, keyName))
                .retry(3)
                .onErrorResume(e -> {
                    log.error("Error during encryption: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Encryption failed", e));
                });
    }

    private Mono<byte[]> getBytesFromString(String input) {
        return Mono.just(input.getBytes(StandardCharsets.UTF_8));
    }

    private Mono<String> encodeBase64Bytes(byte[] inputData) {
        return Mono.just(Base64.getEncoder().encodeToString(inputData));
    }

    private Mono<String> constructRequestBody(String encodedData) {
        return Mono.fromCallable(() -> {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> body = new HashMap<>();
            body.put("plaintext", encodedData);
            return objectMapper.writeValueAsString(body);
        }).onErrorMap(JsonProcessingException.class, e -> {
            log.error("Error creating JSON body", e);
            return new RuntimeException("Error creating JSON body", e);
        });
    }

    private Mono<String> sendEncryptionRequest(String requestBody, String keyName) {
        return webClient.post()
                .uri("/v1/transit/encrypt/" + keyName)
                .header("X-Vault-Token", vaultToken)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class);
    }
}