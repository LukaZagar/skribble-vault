package ws.luka.skribblevault.dto.request;

/**
 * Interface for handling encryption requests to the HashiCorp Vault Transit Engine.
 */
public interface TransitEncryptionRequest {
    String getPlainText();

    void setPlainText(String plainText);
}
