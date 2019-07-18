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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
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

  @Mocked
  ClientBuilder clientBuilder;

  @Mocked
  Client client;

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

  @Tested
  PublishModelScore instance;

  @Test
  public void testPublishScore() throws IOException {

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
