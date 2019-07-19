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

import java.util.logging.Logger;
import java.io.IOException;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.oauth1.AccessToken;
import org.glassfish.jersey.client.oauth1.ConsumerCredentials;
import org.glassfish.jersey.client.oauth1.OAuth1ClientSupport;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;

/**
 * Publishes a tweet with information about the latest flu score rate.
 * @author David Guzman
 */
@Stateless
@LocalBean
public class PublishModelScore {

  /**
   * The URI for publishing a new tweet. Defaults to the endpoint from Twitter
   * API. An alternative URI can be specified for further processing or testing.
   */
  private final String statusUri = PropertyReader.getFromSystemOrEnvOrElse(
    "TWITTER_STATUS_URI", "https://api.twitter.com/1.1/statuses/update.json"
  );

  /**
   * The URI for uploading a new image to include in a tweet. Defaults to the
   * endpoint from Twitter API. An alternative URI can be specified for further
   * processing or testing.
   */
  private final String mediaUri = PropertyReader.getFromSystemOrEnvOrElse(
    "TWITTER_MEDIA_URI", "https://upload.twitter.com/1.1/media/upload.json"
  );

  /**
   * The API key credentials required for calling Twitter API. Values configured
   * via system properties or environment variables.
   */
  private final ConsumerCredentials consumerCredentials =
    new ConsumerCredentials(
      PropertyReader.getFromSystemOrEnv("TWITTER_KEY"),
      PropertyReader.getFromSystemOrEnv("TWITTER_SECRET")
    );

  /**
   * The API token credentials required for calling Twitter API. Values
   * configured via system properties or environment variables.
   */
  private final AccessToken accessToken = new AccessToken(
    PropertyReader.getFromSystemOrEnv("TWITTER_TOKEN"),
    PropertyReader.getFromSystemOrEnv("TWITTER_TOKEN_SECRET")
  );

  /**
   * Feature to add support for OAuth1 client authorisation as required by
   * Twitter API.
   */
  private final Feature filterFeature = OAuth1ClientSupport
    .builder(consumerCredentials)
    .feature()
    .accessToken(accessToken)
    .build();

  /**
   * Configurable for Jersey client.
   */
  private final ClientConfig clientConfig = new ClientConfig();

  /**
   * An instance of a JAX-RS client. Defaults to Jersey.
   */
  private Client client;

  /**
   * Prepared request for calling the status endpoint in Twitter API.
   * (Publishes a new tweet).
   */
  private Invocation.Builder updateInvocationBuilder;

  /**
   * Prepared request for calling the media endpoint in Twitter API. (Uploads
   * a new image to include in a tweet).
   */
  private Invocation.Builder mediaInvocationBuilder;

  /**
   * Generates the content of a tweet using the message containing information
   * of the latest score.
   */
  @Inject
  private MessageParser messageParser;

  /**
   * Configures the JAX RS client to call both endpoints on Twitter API.
   */
  @PostConstruct
  public void init() {
    clientConfig.property(
      LoggingFeature.LOGGING_FEATURE_VERBOSITY_CLIENT,
      LoggingFeature.Verbosity.PAYLOAD_ANY
    );

    client = ClientBuilder.newBuilder()
      .withConfig(clientConfig)
      .register(filterFeature)
      .register(MultiPartFeature.class)
      .build();

    updateInvocationBuilder = client.target(statusUri)
      .queryParam("include_entities", "true")
      .request(MediaType.APPLICATION_JSON_TYPE);

    mediaInvocationBuilder = client.target(mediaUri)
      .request(MediaType.APPLICATION_JSON_TYPE);
  }

  /**
   * Publishes a tweet with a report of the latest score. Using the information
   * generated by MessageParser, uploads the chart to the media endpoint, and
   * sends request to publish a new tweet on the status endpoint.
   * @param scoreMsg  The message containing the date and value of the score.
   */
  public void publishScore(final String scoreMsg) {

    MessageParser.TweetData tweetData = messageParser.getTweetData(scoreMsg);

    final FormDataMultiPart multiPart = new FormDataMultiPart();
    StreamDataBodyPart streamBodyPart;

    try {
      streamBodyPart = new StreamDataBodyPart(
              "media",
              tweetData.getChartAsPng(),
              tweetData.getPngFilename());
    } catch (IOException ex) {
      throw new EJBException(
        "Cannot obtain InputStream from the generated chart", ex
      );
    }

    multiPart.bodyPart(streamBodyPart);
    Entity<FormDataMultiPart> entMultiPart = Entity.entity(
      multiPart, multiPart.getMediaType()
    );

    final Response responseMedia = okOrThrow(
      mediaInvocationBuilder.post(entMultiPart)
    );

    String mediaId = responseMedia.readEntity(JsonObject.class)
      .getString("media_id_string", "");

    final Form form = new Form();
    form.param("status", tweetData.getTweet());
    form.param("media_ids", mediaId);

    Entity<Form> entForm = Entity.entity(
      form, MediaType.APPLICATION_FORM_URLENCODED_TYPE
    );

    okOrThrow(updateInvocationBuilder.post(entForm));

    Logger.getLogger(PublishModelScore.class.getName())
      .log(Level.INFO, "Tweet published");
  }

  private Response okOrThrow(final Response response) {
    if (response.getStatus() != Response.Status.OK.getStatusCode()) {
      String errorEntity = null;
      if (response.hasEntity()) {
        errorEntity = response.readEntity(String.class);
      }
      throw new EJBException(
        "Request to Twitter was not successful. Response code: "
          + response.getStatus() + ", reason: "
          + response.getStatusInfo().getReasonPhrase()
          + ", entity: " + errorEntity
      );
    }
    return response;
  }

}
