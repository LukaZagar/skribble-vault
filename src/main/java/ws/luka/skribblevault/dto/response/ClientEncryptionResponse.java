package ws.luka.skribblevault.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * Dto we send the user, when the encryption is successful and the accompanying data.
 */
@Data
@AllArgsConstructor
public class ClientEncryptionResponse implements ClientResponse {
    private String message;
    private HttpStatus statusCode;
    private VaultEncryptionResponse encryptionData;
}
