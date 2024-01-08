package ws.luka.skribblevault.exceptions;

public class EncryptionDataSizeExceededException extends RuntimeException{
    public EncryptionDataSizeExceededException() {
        super("Data sent to be processed is too large.");
    }
}
