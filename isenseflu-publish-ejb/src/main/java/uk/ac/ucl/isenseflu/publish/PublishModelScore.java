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
 *
 * @author David Guzman
 */
@Stateless
@LocalBean
public class PublishModelScore {

  private final String STATUS_URI = PropertyReader.getFromSystemOrEnvOrElse(
    "TWITTER_STATUS_URI", "https://api.twitter.com/1.1/statuses/update.json"
  );
  private final String MEDIA_URI = PropertyReader.getFromSystemOrEnvOrElse(
    "TWITTER_MEDIA_URI", "https://upload.twitter.com/1.1/media/upload.json"
  );

  private final ConsumerCredentials consumerCredentials = new ConsumerCredentials(
          PropertyReader.getFromSystemOrEnv("TWITTER_KEY"),
          PropertyReader.getFromSystemOrEnv("TWITTER_SECRET")
  );

  private final AccessToken accessToken = new AccessToken(
          PropertyReader.getFromSystemOrEnv("TWITTER_TOKEN"),
          PropertyReader.getFromSystemOrEnv("TWITTER_TOKEN_SECRET")
  );

  private final Feature filterFeature = OAuth1ClientSupport.builder(consumerCredentials)
          .feature()
          .accessToken(accessToken)
          .build();

  private final ClientConfig clientConfig = new ClientConfig();

  private Client client;
  private Invocation.Builder updateInvocationBuilder;
  private Invocation.Builder mediaInvocationBuilder;

  @Inject
  private MessageParser messageParser;

  @PostConstruct
  public void init() {
    clientConfig.property(
            LoggingFeature.LOGGING_FEATURE_VERBOSITY_CLIENT, LoggingFeature.Verbosity.PAYLOAD_ANY
    );

    client = ClientBuilder.newBuilder()
            .withConfig(clientConfig)
            .register(filterFeature)
            .register(MultiPartFeature.class)
            .build();

    updateInvocationBuilder = client.target(STATUS_URI)
            .queryParam("include_entities", "true").request(MediaType.APPLICATION_JSON_TYPE);

    mediaInvocationBuilder = client.target(MEDIA_URI).request(MediaType.APPLICATION_JSON_TYPE);
  }

  public void publishScore(String scoreMsg) {

    MessageParser.TweetData tweetData = messageParser.getTweetData(scoreMsg);

    final FormDataMultiPart multiPart = new FormDataMultiPart();
    StreamDataBodyPart streamBodyPart;

    try {
      streamBodyPart = new StreamDataBodyPart(
              "media",
              tweetData.getChartAsPng(),
              tweetData.getPngFilename());
    } catch (IOException ex) {
      throw new EJBException("Cannot obtain InputStream from the generated chart", ex);
    }

    multiPart.bodyPart(streamBodyPart);
    Entity<FormDataMultiPart> entMultiPart = Entity.entity(multiPart, multiPart.getMediaType());

    final Response responseMedia = OkOrThrow(mediaInvocationBuilder.post(entMultiPart));

    String media_id = responseMedia.readEntity(JsonObject.class).getString("media_id_string", "");

    final Form form = new Form();
    form.param("status", tweetData.getTweet());
    form.param("media_ids", media_id);

    Entity<Form> entForm = Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE);

    OkOrThrow(updateInvocationBuilder.post(entForm));

    Logger.getLogger(PublishModelScore.class.getName()).log(Level.INFO, "Tweet published");
  }

  private Response OkOrThrow(Response response) {
    if (response.getStatus() != 200) {
      String errorEntity = null;
      if (response.hasEntity()) {
        errorEntity = response.readEntity(String.class);
      }
      throw new EJBException("Request to Twitter was not successful. Response code: "
              + response.getStatus() + ", reason: " + response.getStatusInfo().getReasonPhrase()
              + ", entity: " + errorEntity);
    }
    return response;
  }

}
