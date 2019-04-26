package uk.ac.ucl.isenseflu.publish;

import java.time.LocalDate;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author David Guzman
 */
public class FetchScoresTest {

  @Mocked
  ClientBuilder clientBuilder;

  @Mocked
  Client client;

  @Mocked
  WebTarget webTarget;

  @Mocked
  Invocation.Builder invocationBuilder;

  @Mocked
  Response response;

  @Tested
  FetchScores instance;

  @Test
  public void testParsingOfJsonArray() {
    LocalDate localDate = LocalDate.now().minusDays(3);
    JsonArray scoresArray = Json.createArrayBuilder()
      .add(Json.createObjectBuilder()
        .add("score_date", localDate.toString())
        .add("score_value", 123.4d))
      .add(Json.createObjectBuilder()
        .add("score_date", localDate.minusDays(1).toString())
        .add("score_value", 567.8d))
      .build();
    JsonArray modelDataArray = Json.createArrayBuilder()
      .add(
        Json.createObjectBuilder()
          .add("id", 1)
          .add("name", "Model name")
          .add("datapoints", scoresArray)
          .build()
      )
      .build();
    JsonObject jsonObject = Json.createObjectBuilder()
      .add("modeldata", modelDataArray)
      .build();

    new Expectations() {
      {
        response.getStatus();
        result = 200;

        response.readEntity(JsonObject.class);
        result = jsonObject;
      }
    };

    List<DatapointModelScore> scoresList = instance.getScoresForLast30Days(localDate);
    scoresList.stream().mapToDouble(o -> o.getScoreValue()).forEach(System.out::println);
    Assertions.assertEquals(2, scoresList.size());
  }

}
