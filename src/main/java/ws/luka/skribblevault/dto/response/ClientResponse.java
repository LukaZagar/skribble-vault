package ws.luka.skribblevault.dto.response;


/**
 * Used for when we need to forward the End user a message DTO.
 */
public interface ClientResponse {
    String getMessage();
    void setMessage(String message);

    Integer getStatusCode();
    void setStatusCode(Integer statusCode);
}
