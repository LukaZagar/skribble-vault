package ws.luka.skribblevault.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Class we use to send to HashiCorp Vault Transit engine to be encrypted.
 */

@Data
public class TransitEncryptDataRequest implements TransitEncryptionRequest {
    @JsonProperty("plaintext")
    private String plainText;

    @Override
    public String getPlainText() {
        return this.plainText;
    }

    @Override
    public void setPlainText(String plainText) {
        this.plainText = plainText;
    }
}
