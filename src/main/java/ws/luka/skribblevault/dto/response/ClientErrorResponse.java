package ws.luka.skribblevault.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientErrorResponse implements ClientResponse{
    private String message;
    private Integer statusCode;
}
