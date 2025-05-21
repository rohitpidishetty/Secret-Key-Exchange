import java.util.HashMap;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.KeyPairGenerator;

public class AuthenticSource {
  protected KeyPair keyPair = null;
  protected HashMap<String, PublicKey> REG = new HashMap<>();

  AuthenticSource() throws Exception {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(512);
    keyPair = keyPairGenerator.generateKeyPair();
  }

  protected void register(PublicKey PUBLIC_KEY, String NODE) {
    REG.put(NODE, PUBLIC_KEY);
  }
}
