public class Message {
  byte[] KEY = null;
  byte[] SIGN = null;
  String MESSAGE = null;

  Message(byte[] KEY, byte[] SIGN, String MESSAGE) {
    this.KEY = KEY;
    this.SIGN = SIGN;
    this.MESSAGE = MESSAGE;
  }
}
