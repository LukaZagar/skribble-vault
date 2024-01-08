package ws.luka.skribblevault.controllers;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;
import ws.luka.skribblevault.dto.request.TransitEncryptionRequest;
import ws.luka.skribblevault.dto.response.VaultEncryptionResponse;

public interface VaultTransitClient {
    @PostExchange("/v1/transit/encrypt/{keyName}")
    Mono<VaultEncryptionResponse> encrypt(@RequestBody TransitEncryptionRequest request, @PathVariable String keyName);
}
