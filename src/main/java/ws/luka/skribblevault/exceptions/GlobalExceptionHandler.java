package ws.luka.skribblevault.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ws.luka.skribblevault.dto.response.ClientErrorResponse;
import ws.luka.skribblevault.dto.response.ClientResponse;

public class GlobalExceptionHandler {
    public static ResponseEntity<ClientResponse> handleEncryptionException(Throwable e) {
        ClientResponse errorResponse;
        HttpStatus status;

        if (e instanceof EncryptionDataSizeExceededException) {
            errorResponse = new ClientErrorResponse("Data too big", HttpStatus.BAD_REQUEST.value());
            status = HttpStatus.BAD_REQUEST;
        } else if (e instanceof WebClientResponseException.NotFound) {
            errorResponse = new ClientErrorResponse("HashiCorp Vault Transit engine is not online.", HttpStatus.NOT_FOUND.value());
            status = HttpStatus.NOT_FOUND;
        } else {
            errorResponse = new ClientErrorResponse("An internal error occurred.", HttpStatus.INTERNAL_SERVER_ERROR.value());
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return ResponseEntity.status(status).body(errorResponse);
    }

}
