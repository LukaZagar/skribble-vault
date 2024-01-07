package ws.luka.skribblevault.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import ws.luka.skribblevault.dto.EncryptDataRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VaultEncryptionServiceTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testEncryptData_Success() {
        EncryptDataRequest request = new EncryptDataRequest();
        request.setData("TestData");

        webTestClient.post().uri("/api/encrypt")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.ciphertext").exists();
    }
}