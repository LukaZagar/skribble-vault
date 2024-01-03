package ws.luka.skribblevault.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class EncryptDataRequest {
    private byte[] data;
}
