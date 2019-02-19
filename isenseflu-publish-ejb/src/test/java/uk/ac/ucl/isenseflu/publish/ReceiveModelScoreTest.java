package uk.ac.ucl.isenseflu.publish;

import java.nio.charset.StandardCharsets;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ucl.isenseflu.publish.CallScheduler;
import uk.ac.ucl.isenseflu.publish.ReceiveModelScore;

/**
 *
 * @author David Guzman
 */
public class ReceiveModelScoreTest {

  @Tested
  ReceiveModelScore instance;

  @Injectable
  CallScheduler callScheduler;

  @Test
  void testOnMessageWithTextMessage(@Mocked TextMessage textMessage) throws JMSException {
    new Expectations() {
      {
        textMessage.getText();
        result = "This is a TextMessage";

        textMessage.getJMSMessageID();
        result = "123";
      }
    };
    Assertions.assertDoesNotThrow(
            () -> instance.onMessage(textMessage)
    );
  }

  @Test
  void testOnMessageWithBytesMessage(@Mocked BytesMessage bytesMessage) throws JMSException {
    byte[] bytes = "This is a BytesMessage".getBytes(StandardCharsets.UTF_8);
    new Expectations() {
      {
        bytesMessage.getBodyLength();
        result = bytes.length;

        bytesMessage.getJMSMessageID();
        result = "123";
      }
    };
    Assertions.assertDoesNotThrow(
            () -> instance.onMessage(bytesMessage)
    );
  }

  @Test
  void testOnMessageWithObjectMessage(@Mocked ObjectMessage objectMessage) {
    Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
      instance.onMessage(objectMessage);
    });
    Assertions.assertEquals(exception.getMessage(), "Message must be of type TextMessage or ByteMessage");
  }

}
