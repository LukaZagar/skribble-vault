import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;

@Service
public class VaultEncryptionService {

    private static final Logger logger = LoggerFactory.getLogger(VaultEncryptionService.class);
    private final WebClient webClient;

    @Value("${vault.url}")
    private String vaultUrl;

    @Value("${vault.token}")
    private String vaultToken;

    public VaultEncryptionService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(vaultUrl).build();
    }

    public Mono<String> encryptData(byte[] data, String keyName) {
        String encodedData = Base64.getEncoder().encodeToString(data);
        String requestBody = constructRequestBody(encodedData);

        return sendEncryptionRequest(requestBody, keyName)
                .retry(3) // Retrying 3 times in case of error
                .onErrorResume(e -> {
                    logger.error("Error during encryption: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Encryption failed", e));
                });
    }

    private String constructRequestBody(String encodedData) {
        return String.format("{\"plaintext\":\"%s\"}", encodedData);
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
