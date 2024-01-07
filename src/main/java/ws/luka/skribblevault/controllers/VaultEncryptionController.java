package ws.luka.skribblevault.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ws.luka.skribblevault.dto.EncryptDataRequest;
import ws.luka.skribblevault.services.VaultEncryptionService;

@RestController
@RequestMapping(value = "/api/encrypt")
@RequiredArgsConstructor
public class VaultEncryptionController {
    private final VaultEncryptionService vaultEncryptionService;

    @PostMapping
    public Mono<String> encryptData(@RequestBody EncryptDataRequest request) {
        return vaultEncryptionService.encryptData(request, "usr_12345678");
    }
}
