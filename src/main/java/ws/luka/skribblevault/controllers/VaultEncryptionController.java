package ws.luka.skribblevault.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ws.luka.skribblevault.dto.request.ClientEncryptFileRequest;
import ws.luka.skribblevault.dto.request.ClientEncryptStringRequest;
import ws.luka.skribblevault.dto.response.ClientResponse;
import ws.luka.skribblevault.services.VaultEncryptionService;

@RestController
@RequestMapping(value = "/api/encrypt")
@RequiredArgsConstructor
public class VaultEncryptionController {
    private final VaultEncryptionService vaultEncryptionService;

    @PostMapping
    public Mono<ResponseEntity<ClientResponse>> encryptDataString(@RequestBody ClientEncryptStringRequest request) {
        return vaultEncryptionService.execute(request, "usr_12345678");
    }

    @PostMapping("/file")
    public Mono<ResponseEntity<ClientResponse>> encryptDataFile(@RequestPart("data") FilePart data){
        return vaultEncryptionService.execute(new ClientEncryptFileRequest(data),"usr_12345678" );
    }
}
