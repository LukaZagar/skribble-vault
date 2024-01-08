package ws.luka.skribblevault.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * Dto we send to the user requesting to encrypt a string, to tell them that an error has occured
 */
@Data
@AllArgsConstructor
public class ClientErrorResponse implements ClientResponse{
    private String message;
    private HttpStatus statusCode;
}
