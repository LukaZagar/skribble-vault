package ws.luka.skribblevault.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ws.luka.skribblevault.dto.request.ClientEncryptFileRequest;
import ws.luka.skribblevault.dto.request.ClientEncryptStringRequest;
import ws.luka.skribblevault.dto.response.VaultEncryptionResponse;
import ws.luka.skribblevault.services.VaultEncryptionService;

@RestController
@RequestMapping(value = "/api/encrypt")
@RequiredArgsConstructor
public class VaultEncryptionController {
    private final VaultEncryptionService vaultEncryptionService;

    @PostMapping
    public Mono<VaultEncryptionResponse> encryptDataString(@RequestBody ClientEncryptStringRequest request) {
        return vaultEncryptionService.encryptData(request, "usr_12345678");
    }

    @PostMapping("/file")
    public Mono<VaultEncryptionResponse> encryptDataFile(@RequestPart("data") FilePart data){
        return vaultEncryptionService.encryptData(new ClientEncryptFileRequest(data),"usr_12345678" );
    }
}
