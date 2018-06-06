package uk.ac.ucl.flagship2.fludetector;

import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.inject.Inject;
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

/**
 *
 * @author David Guzman
 */
@Stateless
@LocalBean
public class PublishModelScore {

  private final String STATUS_URI = "https://api.twitter.com/1.1/statuses/update.json";

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
            .build();

    updateInvocationBuilder = client.target(STATUS_URI)
            .queryParam("include_entities", "true").request(MediaType.APPLICATION_JSON_TYPE);
  }

  public void publishScore(String scoreMsg) {

    MessageParser.TweetData tweetData = messageParser.getTweetData(scoreMsg);

    Form form = new Form();
    form.param("status", tweetData.getTweet());

    Entity ent = Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE);

    Response response = updateInvocationBuilder.post(ent);

    if (response.getStatus() != 200) {
      String errorEntity = null;
      if (response.hasEntity()) {
        errorEntity = response.readEntity(String.class);
      }
      throw new EJBException("Request to Twitter was not successful. Response code: "
              + response.getStatus() + ", reason: " + response.getStatusInfo().getReasonPhrase()
              + ", entity: " + errorEntity);
    }

  }

}
