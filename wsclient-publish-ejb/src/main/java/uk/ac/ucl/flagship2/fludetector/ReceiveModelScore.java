package uk.ac.ucl.flagship2.fludetector;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.enterprise.event.Event;
import javax.inject.Inject;
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
  @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/PubModelScoreQ")
  ,
  @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ReceiveModelScore implements MessageListener {

  @Inject
  private Event<String> msgEvent;

  @Override
  public void onMessage(Message msg) {
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
      msgEvent.fire(out);
      try {
        Logger.getLogger(ReceiveModelScore.class.getName()).log(Level.INFO, "JMS Message ID {0} has been read and sent to PublishModelScore", msg.getJMSMessageID());
      } catch (JMSException ex) {
        Logger.getLogger(ReceiveModelScore.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }
}
