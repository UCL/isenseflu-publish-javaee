package uk.ac.ucl.flagship2.fludetector;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 *
 * @author David Guzman <d.guzman at ucl.ac.uk>
 */
@MessageDriven(activationConfig = {
  @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/PubModelScoreQ"),
  @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ReceiveModelScore implements MessageListener {

  @Override
  public void onMessage(Message msg) {
    System.out.println("Got message " + msg);
    String out = "";
    if (msg instanceof TextMessage) {
      TextMessage txt = (TextMessage) msg;
      try {
        out = txt.getText();
      } catch (JMSException ex) {
        throw new RuntimeException(ex);
      }
    } else if (msg instanceof BytesMessage) {
      Logger.getLogger(ReceiveModelScore.class.getName())
              .log(Level.WARNING, "Messages should be sent as UTF-8 text, content-length header should not be present");
      BytesMessage byteMessage = (BytesMessage) msg;
      byte[] byteData = null;
      try {
        byteData = new byte[(int) byteMessage.getBodyLength()];
        byteMessage.readBytes(byteData);
        byteMessage.reset();
        out = new String(byteData);
      } catch (JMSException ex) {
        throw new RuntimeException(ex);
      }
    } else {
      throw new IllegalArgumentException("Message must be of type TextMessage or ByteMessage");
    }
    if (!out.isEmpty()) {
      System.out.println(out);
    }
  }
}
