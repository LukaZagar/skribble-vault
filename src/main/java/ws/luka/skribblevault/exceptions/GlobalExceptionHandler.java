package ws.luka.skribblevault.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.Exceptions;
import ws.luka.skribblevault.dto.response.ClientErrorResponse;
import ws.luka.skribblevault.dto.response.ClientResponse;

@Slf4j
public class GlobalExceptionHandler {
    public static ResponseEntity<ClientResponse> handleEncryptionException(Throwable e) {
        ClientResponse errorResponse;

        if (e instanceof EncryptionDataSizeExceededException) {
            errorResponse = new ClientErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } else if (Exceptions.isRetryExhausted(e)) {
            errorResponse = new ClientErrorResponse(e.getCause().getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            errorResponse = new ClientErrorResponse("An internal error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        log.error("An error occurred during a encryption request {}", e.getMessage());
        return ResponseEntity.status(errorResponse.getStatusCode()).body(errorResponse);
    }
}
