/*
 * i-sense flu publish: Module of the i-sense flu application used in the publication of model scores on social media
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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
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
  @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/PubModelScoreQ")
  ,
  @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ReceiveModelScore implements MessageListener {

  @EJB
  private CallScheduler callScheduler;

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
      callScheduler.setLastModelScore(out);
      try {
        Logger.getLogger(ReceiveModelScore.class.getName()).log(Level.INFO, "JMS Message ID {0} has been read and sent to PublishModelScore", msg.getJMSMessageID());
      } catch (JMSException ex) {
        Logger.getLogger(ReceiveModelScore.class.getName()).log(Level.SEVERE, null, ex);
      }
    } else {
      try {
        Logger.getLogger(ReceiveModelScore.class.getName()).log(Level.WARNING, "JMS Message ID {0} is empty", msg.getJMSMessageID());
      } catch (JMSException ex) {
        Logger.getLogger(ReceiveModelScore.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }
}
