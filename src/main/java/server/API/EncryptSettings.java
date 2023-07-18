package server.API;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class EncryptSettings {
    private char[] key;
    private byte[] salt;
    private int iterationCount;
    private int keyLength;
    private byte[] vector;
}
