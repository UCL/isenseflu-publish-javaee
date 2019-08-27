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

import java.io.StringReader;
import java.time.LocalDate;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
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

import static uk.ac.ucl.isenseflu.publish.DataStubs.JSON_RESPONSE;

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
  public void testParsingFromJsonString() {
    LocalDate localDate = LocalDate.of(2019, 4, 26);
    JsonReader jsonParser = Json.createReader(new StringReader(JSON_RESPONSE));
    JsonObject jsonObject = jsonParser.readObject();

    new Expectations() {{
      response.getStatus();
      result = 200;

      response.readEntity(JsonObject.class);
      result = jsonObject;
    }};

    List<DatapointModelScore> scoresList = instance.getScoresForLast56Days(localDate);

    Assertions.assertAll(
      () -> Assertions.assertEquals(30, scoresList.size()),
      () -> Assertions.assertEquals(5.8970798709844d, scoresList.get(0).getScoreValue()),
      () -> Assertions.assertEquals(7.25593958395504d, scoresList.get(29).getScoreValue())
    );
  }

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
          .add("data_points", scoresArray)
          .build()
      )
      .build();
    JsonObject jsonObject = Json.createObjectBuilder()
      .add("model_data", modelDataArray)
      .build();

    new Expectations() {
      {
        response.getStatus();
        result = 200;

        response.readEntity(JsonObject.class);
        result = jsonObject;
      }
    };

    List<DatapointModelScore> scoresList = instance.getScoresForLast56Days(localDate);
    Assertions.assertEquals(2, scoresList.size());
  }

}
