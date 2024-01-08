package ws.luka.skribblevault.dto.request;

import reactor.core.publisher.Mono;

/**
 * The request the client sends us to forward to HashiCorp Vault Transit engine.
 */
public interface ClientEncryptionRequest {
    Mono<byte[]> getByteStream();
}
