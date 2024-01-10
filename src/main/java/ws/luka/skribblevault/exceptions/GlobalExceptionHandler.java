package ws.luka.skribblevault.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import ws.luka.skribblevault.dto.response.ClientErrorResponse;
import ws.luka.skribblevault.dto.response.ClientResponse;

@Slf4j
public class GlobalExceptionHandler {

    public static Mono<ClientResponse> handleException(Throwable e) {
        if (e instanceof EncryptionDataSizeExceededException) {
            return handleEncryptionDataSizeExceeded(e);
        } else if (Exceptions.isRetryExhausted(e)) {
            return handleRetryExhausted(e);
        } else {
            return handleGenericException(e);
        }
    }

    private static Mono<ClientResponse> handleEncryptionDataSizeExceeded(Throwable e) {
        log.error("Data size exceeded: {}", e.getMessage());
        return Mono.just(new ClientErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST));
    }

    private static Mono<ClientResponse> handleRetryExhausted(Throwable e) {
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        // Intentionally send the client just the retry exhausted message, we don't want to reveal internal errors
        //  that might expose libraries used, their versions, etc...
        log.error("Retry exhausted: {}", cause.getMessage());
        return Mono.just(new ClientErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private static Mono<ClientResponse> handleGenericException(Throwable e) {
        log.error("Generic error: {}", e.getMessage());
        return Mono.just(new ClientErrorResponse("An internal error occurred.", HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
