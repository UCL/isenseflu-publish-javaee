package uk.ac.ucl.flagship2.fludetector;

import java.time.LocalDate;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import org.junit.jupiter.api.Test;

/**
 *
 * @author David Guzman
 */
public class PublishModelScoreTest {

  @Tested
  PublishModelScore instance;

  @Mocked
  Invocation.Builder invocationBuilder;

  @Mocked
  Entity entity;

  @Mocked
  Response response;

  @Test
  public void testPublishScore() {
    Deencapsulation.setField(instance, "updateInvocationBuilder", invocationBuilder);

    new Expectations() {
      {
        invocationBuilder.post(entity);
        result = response;

        response.getStatus();
        result = 200;
      }
    };
    String message = "date=" + LocalDate.now().toString() + System.lineSeparator() + "value=12.3";
    instance.publishScore(message);
  }

}
