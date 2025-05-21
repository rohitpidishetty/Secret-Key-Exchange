import java.util.Queue;
import java.util.Base64;
import javax.crypto.Cipher;
import java.security.KeyPair;
import javax.crypto.SecretKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.PrivateKey;
import javax.crypto.KeyGenerator;

public class Alex implements Runnable {
  private static PublicKey PUBLIC_KEY = null;
  private static PrivateKey PRIVATE_KEY = null;
  private static SecretKey SECRET_KEY = null;
  private static Queue<Message> alexQ = null;
  private static Queue<Message> bobQ = null;
  private static AuthenticSource source = null;

  protected static void setPublicKey(PublicKey PUBLIC_KEY) {
    Alex.PUBLIC_KEY = PUBLIC_KEY;
  }

  protected static PublicKey getPublicKey() {
    return Alex.PUBLIC_KEY;
  }

  protected static void setPrivateKey(PrivateKey PRIVATE_KEY) {
    Alex.PRIVATE_KEY = PRIVATE_KEY;
  }

  private static PrivateKey getPrivateKey() {
    return Alex.PRIVATE_KEY;
  }

  protected static void setSecretKey(SecretKey SECRET_KEY) {
    Alex.SECRET_KEY = SECRET_KEY;
  }

  private static SecretKey getSecretKey() {
    return Alex.SECRET_KEY;
  }

  protected static void init(Queue<Message> alexQ, Queue<Message> bobQ, AuthenticSource src)
      throws Exception {
    generateSeceretKey(alexQ, bobQ, src);
  }

  protected static void generateSeceretKey(Queue<Message> alexQ, Queue<Message> bobQ, AuthenticSource src)
      throws Exception {
    Alex.SECRET_KEY = KeyGenerator.getInstance("AES").generateKey();
    sendKey(Alex.SECRET_KEY, alexQ, bobQ, src);
  }

  Alex(KeyPair keyPair, AuthenticSource src, Queue<Message> alexQ, Queue<Message> bobQ) throws Exception {
    Alex.PUBLIC_KEY = keyPair.getPublic();
    Alex.PRIVATE_KEY = keyPair.getPrivate();
    src.register(Alex.PUBLIC_KEY, "alex");
    Alex.alexQ = alexQ;
    Alex.bobQ = bobQ;
    Alex.source = src;
  }

  protected static void sendKey(SecretKey SECRET_KEY, Queue<Message> alexQ, Queue<Message> bobQ, AuthenticSource src)
      throws Exception {
    byte[] signature = sign(SECRET_KEY);
    bobQ.offer(encrypt(SECRET_KEY, signature, src.REG.get("bob")));
    // Bob.receiveKey(alexQ, bobQ, src); // Run Synchronous task
  }

  protected static Message encrypt(SecretKey SECRET_KEY, byte[] signature, PublicKey PUBLIC_KEY) throws Exception {
    Cipher c = Cipher.getInstance("RSA/ECB/NoPadding");
    c.init(Cipher.ENCRYPT_MODE, PUBLIC_KEY);
    return new Message(c.doFinal(SECRET_KEY.getEncoded()), signature, null);
  }

  protected static byte[] sign(SecretKey SECRET_KEY) throws Exception {
    Signature sg = Signature.getInstance("SHA256withRSA");
    sg.initSign(Alex.PRIVATE_KEY);
    sg.update(SECRET_KEY.getEncoded());
    return sg.sign();
  }

  protected static void sendMessage(Queue<Message> alexQ, Queue<Message> bobQ) throws Exception {
    Cipher c = Cipher.getInstance("AES");
    c.init(Cipher.ENCRYPT_MODE, Alex.SECRET_KEY);
    bobQ.offer(new Message(null, null,
        Base64.getEncoder().encodeToString(c.doFinal("Let us have a meeting tomorrow at 4".getBytes()))));
    // Bob.receiveMessage(alexQ, bobQ); // Run Synchronous task
  }

  protected static void receiveReply(Queue<Message> alexQ, Queue<Message> bobQ) throws Exception {
    Cipher c = Cipher.getInstance("AES");
    c.init(Cipher.DECRYPT_MODE, Alex.SECRET_KEY);
    System.out.println(new String(c.doFinal(Base64.getDecoder().decode(alexQ.poll().MESSAGE))));
  }

  protected static void await(int milliseconds) {
    try {
      Thread.sleep(milliseconds);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void run() {
    try {
      await(5000);
      init(Alex.alexQ, Alex.bobQ, Alex.source);
      await(7000);
      sendMessage(Alex.alexQ, Alex.bobQ);
      await(9000);
      receiveReply(Alex.alexQ, Alex.bobQ);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
