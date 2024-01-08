package ws.luka.skribblevault.services;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import ws.luka.skribblevault.dto.request.ClientEncryptStringRequest;


/**
 * Very basic black box test that we can run during development to make sure nothing broke. Not for use in prod.
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VaultEncryptionServiceTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("Test encryption of simple string should be successful.")
    public void testEncryptString_Success() {
        ClientEncryptStringRequest request = new ClientEncryptStringRequest();
        request.setData("TestData");

        webTestClient.post().uri("/api/encrypt")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.ciphertext").exists()
                .jsonPath("$.data.ciphertext").isNotEmpty();
    }

    @Test
    @DisplayName("Test encryption of string, should fail due to being too large.")
    public void testEncryptString_Fail_DataTooBig() {
        ClientEncryptStringRequest request = new ClientEncryptStringRequest();
        request.setData("Extremely long string that should result in a exception.");

        webTestClient.post().uri("/api/encrypt")
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.statusCode").exists()
                .jsonPath("$.statusCode").isEqualTo("BAD_REQUEST");
    }

    @Test
    @DisplayName("Test should successfully encrypt a file")
    public void testEncryptFile_Success() {
        ClassPathResource resource = new ClassPathResource("aes-key.bin");

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("data", resource);

        webTestClient.post().uri("/api/encrypt/file")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.ciphertext").exists()
                .jsonPath("$.data.ciphertext").isNotEmpty();
    }
}