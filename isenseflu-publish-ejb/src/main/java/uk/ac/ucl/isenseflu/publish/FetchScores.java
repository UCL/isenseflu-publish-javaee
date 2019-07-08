package uk.ac.ucl.isenseflu.publish;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import javax.ejb.EJBException;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author David Guzman
 */
@Stateless
@LocalBean
public class FetchScores {

  private final String SCORES_URI = "https://www.i-senseflu.org.uk/api/scores";
  private final Client client = ClientBuilder.newClient();

  public List<DatapointModelScore> getScoresForLast30Days(LocalDate localDate) {
    String startDate = localDate.minusDays(30).toString();
    String endDate = localDate.toString();

    final Response response = client.target(SCORES_URI)
      .queryParam("id", "3")
      .queryParam("startDate", startDate)
      .queryParam("endDate", endDate)
      .request(MediaType.APPLICATION_JSON_TYPE)
      .get();

    if (response.getStatus() != Response.Status.OK.getStatusCode()) {
      client.close();
      throw new EJBException("Request to FluDetector Scores API did not return 200/OK");
    }

    JsonArray jsonArray = response.readEntity(JsonObject.class)
      .getJsonArray("model_data");

    List<DatapointModelScore> datapoints = jsonArray
      .getJsonObject(0)
      .getJsonArray("data_points")
      .stream()
      .filter(o -> o instanceof JsonObject)
      .map(o -> (JsonObject) o)
      .map((JsonObject jsonObject) -> {
        LocalDate date = LocalDate.parse(jsonObject.getString("score_date"));
        Double value = jsonObject.getJsonNumber("score_value").doubleValue();
        return new DatapointModelScore(date, value);
      }).collect(Collectors.toList());

    return datapoints;
  }

}
