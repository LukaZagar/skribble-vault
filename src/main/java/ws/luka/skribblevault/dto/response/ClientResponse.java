package ws.luka.skribblevault.dto.response;


import org.springframework.http.HttpStatus;

/**
 * Used for when we need to forward the End user a message DTO.
 */
public interface ClientResponse {
    String getMessage();
    void setMessage(String message);

    HttpStatus getStatusCode();
    void setStatusCode(HttpStatus statusCode);
}
