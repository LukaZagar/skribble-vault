package ws.luka.skribblevault.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VaultEncryptionResponse {
    private String request_id;
    private String lease_id;
    private boolean renewable;
    private int lease_duration;
    private Object wrap_info;
    private Object warnings;
    private Object auth;
    private Data data;

    @lombok.Data
    public static class Data {
        private String ciphertext;
        private int keyVersion;
    }
}


