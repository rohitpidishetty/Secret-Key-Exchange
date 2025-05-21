import java.util.Queue;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import java.security.KeyPair;
import javax.crypto.SecretKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.PrivateKey;
import javax.crypto.spec.SecretKeySpec;

public class Bob implements Runnable {
  private static PublicKey PUBLIC_KEY = null;
  private static PrivateKey PRIVATE_KEY = null;
  private static SecretKey SECRET_KEY = null;
  private static Queue<Message> alexQ = null;
  private static Queue<Message> bobQ = null;
  private static AuthenticSource source = null;

  protected static void setPublicKey(PublicKey PUBLIC_KEY) {
    Bob.PUBLIC_KEY = PUBLIC_KEY;
  }

  protected static PublicKey getPublicKey() {
    return Bob.PUBLIC_KEY;
  }

  protected static void setPrivateKey(PrivateKey PRIVATE_KEY) {
    Bob.PRIVATE_KEY = PRIVATE_KEY;
  }

  private static PrivateKey getPrivateKey() {
    return Bob.PRIVATE_KEY;
  }

  protected static void setSecretKey(SecretKey SECRET_KEY) {
    Bob.SECRET_KEY = SECRET_KEY;
  }

  private static SecretKey getSecretKey() {
    return Bob.SECRET_KEY;
  }

  Bob(KeyPair keyPair, AuthenticSource src, Queue<Message> alexQ, Queue<Message> bobQ) {
    Bob.PUBLIC_KEY = keyPair.getPublic();
    Bob.PRIVATE_KEY = keyPair.getPrivate();
    src.register(Bob.PUBLIC_KEY, "bob");
    Bob.alexQ = alexQ;
    Bob.bobQ = bobQ;
    Bob.source = src;
  }

  protected static boolean validate(byte[] signature, AuthenticSource src, byte[] dkey) throws Exception {
    Signature sg = Signature.getInstance("SHA256withRSA");
    sg.initVerify(src.REG.get("alex"));
    sg.update(keyRefactor(decrypt(dkey)));
    return sg.verify(signature);
  }

  protected static byte[] decrypt(byte[] enc) throws Exception {
    Cipher c = Cipher.getInstance("RSA/ECB/NoPadding");
    c.init(Cipher.DECRYPT_MODE, Bob.PRIVATE_KEY);
    return c.doFinal(enc);
  }

  protected static byte[] keyRefactor(byte[] UNREFINED_KEY) {
    int NonNullPointer = 0;
    while (UNREFINED_KEY[NonNullPointer] == 0)
      NonNullPointer++;
    return Arrays.copyOfRange(UNREFINED_KEY, NonNullPointer, UNREFINED_KEY.length);
  }

  protected static void showMessage(String message) {
    System.out.println(message);
  }

  protected static void receiveMessage(Queue<Message> alexQ, Queue<Message> bobQ) throws Exception {
    Cipher c = Cipher.getInstance("AES");
    c.init(Cipher.DECRYPT_MODE, Bob.SECRET_KEY);
    showMessage(new String(c.doFinal(Base64.getDecoder().decode(bobQ.poll().MESSAGE))));
    // sendReply(alexQ, bobQ); // Run Synchronous task
  }

  protected static void sendReply(Queue<Message> alexQ, Queue<Message> bobQ) throws Exception {
    Cipher c = Cipher.getInstance("AES");
    c.init(Cipher.ENCRYPT_MODE, Bob.SECRET_KEY);
    alexQ.offer(new Message(null, null,
        Base64.getEncoder().encodeToString(c.doFinal("Yes, I can meet you at Student Union".getBytes()))));
    // Alex.receiveReply(alexQ, bobQ); // Run Synchronous task
  }

  protected static void receiveKey(Queue<Message> alexQ, Queue<Message> bobQ, AuthenticSource src) throws Exception {
    Message data = bobQ.poll();
    byte[] SECRET_KEY = data.KEY;
    byte[] signature = data.SIGN;
    if (validate(signature, src, SECRET_KEY)) {
      Bob.SECRET_KEY = new SecretKeySpec(keyRefactor(decrypt(SECRET_KEY)), "AES");
      Alex.sendMessage(alexQ, bobQ);
    } else
      throw new Exception("The signature has been tampered, thus halting the communication");
  }

  protected static void await() {
    try {
      Thread.sleep(5000);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void run() {
    try {
      await();
      receiveKey(Bob.alexQ, Bob.bobQ, Bob.source);
      await();
      receiveMessage(Bob.alexQ, Bob.bobQ);
      await();
      sendReply(Bob.alexQ, Bob.bobQ);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
