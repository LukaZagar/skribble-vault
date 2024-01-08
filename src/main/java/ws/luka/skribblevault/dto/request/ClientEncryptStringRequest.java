package ws.luka.skribblevault.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Class sent to us when a client just wants to encrypt a simple string.
 */
@Data
@Getter
@Setter
public class ClientEncryptStringRequest implements ClientEncryptionRequest {
    private String data;

    @Override
    public Mono<byte[]> getByteStream() {
        return Mono.just(data.getBytes(StandardCharsets.UTF_8));
    }
}
