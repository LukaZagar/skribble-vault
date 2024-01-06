package ws.luka.skribblevault.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ws.luka.skribblevault.services.VaultEncryptionService;

@RestController
@RequestMapping(value = "/api/encrypt")
@RequiredArgsConstructor
public class VaultEncryptionController {
    private final VaultEncryptionService vaultEncryptionService;

    @PostMapping
    public Mono<String> encryptData(@RequestPart("file") FilePart data) {
        return vaultEncryptionService.encryptData(data, "usr_12345678");
    }
}
