package ws.luka.skribblevault.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.codec.multipart.FilePart;

@Data
@Getter
@Setter
public class EncryptDataRequest {
    private FilePart data;
}
