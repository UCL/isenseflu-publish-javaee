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

  private final Client client = ClientBuilder.newClient();
  private final String SCORES_URI = PropertyReader.getFromSystemOrEnvOrElse(
    "API_SCORES_URI", "https://www.i-senseflu.org.uk/api/scores"
  );

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
