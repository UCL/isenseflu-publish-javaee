package uk.ac.ucl.isenseflu.publish;

import java.time.LocalDate;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ucl.isenseflu.publish.DatapointModelScore;
import uk.ac.ucl.isenseflu.publish.FluDetectorScores;

/**
 *
 * @author David Guzman
 */
public class FluDetectorScoresTest {

  @Tested
  FluDetectorScores instance;

  @Mocked
  Client client;

  @Mocked
  WebTarget webTarget;

  @Mocked
  Invocation.Builder invocationBuilder;

  @Mocked
  Response response;

  @Mocked
  JsonObject jsonObject;

  @Test
  public void testParsingOfJsonArray() {
    Deencapsulation.setField(instance, "client", client);
    LocalDate localDate = LocalDate.now().minusDays(3);
    JsonArray scoresArray = Json.createArrayBuilder()
            .add(Json.createObjectBuilder()
                    .add("date", localDate.toString())
                    .add("score", 123.4d))
            .add(Json.createObjectBuilder()
                    .add("date", localDate.minusDays(1).toString())
                    .add("score", 567.8d))
            .build();

    new Expectations() {
      {
        client.target(anyString);
        result = webTarget;

        webTarget.queryParam(anyString, anyString);
        result = webTarget;

        webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        result = invocationBuilder;

        invocationBuilder.get();
        result = response;

        response.getStatus();
        result = 200;

        response.readEntity(JsonObject.class);
        result = jsonObject;

        jsonObject.getJsonArray("scores");
        result = scoresArray;
      }
    };
    List<DatapointModelScore> scoresList = instance.getScoresForLast30Days(localDate);
    Assertions.assertEquals(2, scoresList.size());
  }

}
