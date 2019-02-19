package uk.ac.ucl.flagship2.fludetector;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
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

  @Mocked
  MessageParser.TweetData tweetData;

  @Injectable
  MessageParser messageParser;

  @Test
  public void testPublishScore() throws IOException {
    Deencapsulation.setField(instance, "mediaInvocationBuilder", invocationBuilder);
    Deencapsulation.setField(instance, "updateInvocationBuilder", invocationBuilder);

    new Expectations() {
      {
        messageParser.getTweetData(anyString);
        result = tweetData;

        tweetData.getPngFilename();
        result = "test.png";

        tweetData.getChartAsPng();
        result = (InputStream) new ByteArrayInputStream(new byte[0]);

        invocationBuilder.post(entity);
        result = response;

        response.getStatus();
        result = 200;

        response.readEntity(JsonObject.class);
        result = Json.createObjectBuilder().add("media_id_string", "12345678").build();
      }
    };
    String message = "date=" + LocalDate.now().toString() + System.lineSeparator() + "value=12.3";
    instance.publishScore(message);
  }

}
