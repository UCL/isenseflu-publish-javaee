package uk.ac.ucl.flagship2.fludetector;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
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
          getFromSystemOrEnv("TWITTER_KEY"),
          getFromSystemOrEnv("TWITTER_SECRET")
  );

  private final AccessToken accessToken = new AccessToken(
          getFromSystemOrEnv("TWITTER_TOKEN"),
          getFromSystemOrEnv("TWITTER_TOKEN_SECRET")
  );

  @Inject
  private FormatModelScore formatter;

  private final MessageFormat tweetFormat = new MessageFormat(
          "Based on Google searches, the estimated flu (Influenza-like illness) rate for England on {0} is {1} cases per 100,000 people #health #AI");

  public void publishScore(String score) {

    Properties properties = readProperties(score).orElseThrow(() -> new EJBException("Cannot read properties from message"));

    String date = properties.getProperty("date", "");
    String value = properties.getProperty("value", "");

    if (date.isEmpty() || !date.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
      throw new EJBException("Date field was not provided or date not in ISO format");
    }

    if (value.isEmpty() || !value.matches("\\d+(\\.\\d+)?")) {
      throw new EJBException("Score value was not provided or score value is not a number");
    }

    ClientConfig clientConfig = new ClientConfig();
    clientConfig.property(LoggingFeature.LOGGING_FEATURE_VERBOSITY_CLIENT, LoggingFeature.Verbosity.PAYLOAD_ANY);

    final Feature filterFeature = OAuth1ClientSupport.builder(consumerCredentials)
            .feature()
            .accessToken(accessToken)
            .build();

    final Client client = ClientBuilder.newBuilder()
            .withConfig(clientConfig)
            .register(filterFeature)
            .build();

    Object[] toFormat = {date, value};
    String tweet = tweetFormat.format(toFormat);

    Form form = new Form();
    form.param("status", tweet);

    WebTarget target = client.target(STATUS_URI);
    WebTarget targetParams = target.queryParam("include_entities", "true");

    Invocation.Builder invocationBuilder = targetParams.request(MediaType.APPLICATION_JSON_TYPE);
    Entity ent = Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE);

    try {
      Response response = invocationBuilder.post(ent);

//    final Response response = client.target(STATUS_URI)
//            .queryParam("include_entities", "true")
//            .request(MediaType.APPLICATION_JSON_TYPE)
//            .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
      if (response.getStatus() != 200) {
        String errorEntity = null;
        if (response.hasEntity()) {
          errorEntity = response.readEntity(String.class);
        }
        throw new EJBException("Request to Twitter was not successful. Response code: "
                + response.getStatus() + ", reason: " + response.getStatusInfo().getReasonPhrase()
                + ", entity: " + errorEntity);
      }
    } catch (ProcessingException | IllegalStateException ex) {
      Logger.getLogger(getClass().getName()).log(Level.SEVERE, "EXC", ex);
    }
  }

  private String getFromSystemOrEnv(final String key) {
    String value = System.getProperty(key, "");
    if (value.isEmpty()) {
      value = System.getenv(key);
    }
    return (value != null) ? value : "";
  }

  private Optional<Properties> readProperties(String propstr) {
    final Properties p = new Properties();
    try {
      p.load(new StringReader(propstr));
    } catch (IOException ex) {
      Logger.getLogger(PublishModelScore.class.getName()).log(Level.SEVERE, null, ex);
      return Optional.empty();
    }
    return Optional.of(p);
  }

}
