import java.util.Queue;
import java.security.KeyPair;
import java.util.LinkedList;

/**
 * @author Rohit Viswakarma, Pidishetti
 * @id R11908362
 * @portfolio https://nfrac-in.web.app
 */

public class ExchangeTest {
  public static void main(String[] args) throws Exception {
    try {
      AuthenticSource source = new AuthenticSource();
      Queue<Message> alexQ = new LinkedList<>();
      Queue<Message> bobQ = new LinkedList<>();
      KeyPair alexKeyPairs = new AuthenticSource().keyPair;
      KeyPair bobKeyPairs = new AuthenticSource().keyPair;
      Alex alex = new Alex(alexKeyPairs, source, alexQ, bobQ);
      Bob bob = new Bob(bobKeyPairs, source, alexQ, bobQ);
      Alex.setPublicKey(alexKeyPairs.getPublic());
      Alex.setPrivateKey(alexKeyPairs.getPrivate());
      Bob.setPublicKey(bobKeyPairs.getPublic());
      Bob.setPrivateKey(bobKeyPairs.getPrivate());
      System.out.println("Please wait until the communication get's established!!");
      Thread ALEX = new Thread(alex);
      Thread BOB = new Thread(bob);
      ALEX.setPriority(10);
      ALEX.start();
      Thread.sleep(2000);
      BOB.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
