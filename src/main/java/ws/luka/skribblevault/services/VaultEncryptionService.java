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

    public Mono<String> encryptData(EncryptDataRequest encryptDataRequest, String keyName) {
        byte[] binaryData = encryptDataRequest.getData().getBytes(StandardCharsets.UTF_8);
        String encodedString = Base64.getEncoder().encodeToString(binaryData);
        String requestBody = constructRequestBody(encodedString);

        return sendEncryptionRequest(requestBody, keyName)
                .retry(3)
                .onErrorResume(e -> {
                    log.error("Error during encryption: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Encryption failed", e));
                });
    }

    private String constructRequestBody(String encodedData) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> body = new HashMap<>();
        body.put("plaintext", encodedData);

        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            log.error("Error creating JSON body", e);
            throw new RuntimeException("Error creating JSON body", e);
        }
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