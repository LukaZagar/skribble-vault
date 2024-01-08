package ws.luka.skribblevault.services;

import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import ws.luka.skribblevault.dto.request.ClientEncryptionRequest;
import ws.luka.skribblevault.dto.response.ClientResponse;

/**
 * Command design pattern
 */
public interface CommandService {
    Mono<ResponseEntity<ClientResponse>> execute(ClientEncryptionRequest clientEncryptionRequest, String keyName);
}
