package ws.luka.skribblevault.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

/**
 * Class used for when the client wants to encrypt a file.
 * @apiNote Designed to handle large or small files via DataBufferUtils.
 */
@Data
@Getter
@Setter
@AllArgsConstructor
public class ClientEncryptFileRequest implements ClientEncryptionRequest {
    private FilePart data;

    public Mono<byte[]> getByteStream() {
        return DataBufferUtils.join(data.content())
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return bytes;
                });
    }
}
