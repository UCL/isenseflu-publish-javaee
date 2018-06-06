package uk.ac.ucl.flagship2.fludetector;

import java.text.MessageFormat;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
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

  private final MessageFormat tweetFormat = new MessageFormat(
          "Based on Google searches, the estimated flu (Influenza-like illness) rate for England on {0} is {1} cases per 100,000 people #health #AI");

  public void publishScore(String scoreMsg) {

    Properties properties = PropertyReader.readProperties(scoreMsg)
            .orElseThrow(() -> new EJBException("Cannot read properties from message"));

    String date = properties.getProperty("date", "");
    String value = properties.getProperty("value", "");

    if (date.isEmpty() || !date.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
      throw new EJBException("Date field was not provided or date not in ISO format");
    }

    if (value.isEmpty() || !value.matches("\\d+(\\.\\d+)?")) {
      throw new EJBException("Score value was not provided or score value is not a number");
    }

    Object[] toFormat = {date, value};
    String tweet = tweetFormat.format(toFormat);

    Form form = new Form();
    form.param("status", tweet);

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
