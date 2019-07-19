/*
 * i-sense flu publish: Module of the i-sense flu application used in the
 * publication of model scores on social media
 *
 * Copyright (c) 2019, UCL <https://www.ucl.ac.uk/>
 *
 * This file is part of i-sense flu publish
 *
 * i-sense flu publish is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * i-sense flu publish is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with i-sense flu publish.  If not, see <http://www.gnu.org/licenses/>.
 */

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
