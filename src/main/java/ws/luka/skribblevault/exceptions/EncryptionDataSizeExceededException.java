package ws.luka.skribblevault.exceptions;

public class EncryptionDataSizeExceededException extends RuntimeException{
    public EncryptionDataSizeExceededException() {
        super("Data to process is too large!");
    }
}
