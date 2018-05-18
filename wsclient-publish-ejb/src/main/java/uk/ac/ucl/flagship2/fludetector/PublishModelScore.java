package uk.ac.ucl.flagship2.fludetector;

import java.text.MessageFormat;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.enterprise.event.Observes;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.oauth1.AccessToken;
import org.glassfish.jersey.client.oauth1.ConsumerCredentials;
import org.glassfish.jersey.client.oauth1.OAuth1ClientSupport;

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

  private final MessageFormat tweetFormat = new MessageFormat(
          "Estimated Influenza-like illness (ILI) rate in England is at {0} per 100K people");

  public void publishScore(@Observes String score) {

    System.out.println(accessToken.getToken());
    System.out.println(accessToken.getAccessTokenSecret());
    System.out.println(consumerCredentials.getConsumerKey());
    System.out.println(consumerCredentials.getConsumerSecret());

    final Feature filterFeature = OAuth1ClientSupport.builder(consumerCredentials)
            .feature()
            .accessToken(accessToken)
            .build();

    final Client client = ClientBuilder.newBuilder()
            .register(filterFeature)
            .build();

    Object[] toFormat = {score};
    String tweet = tweetFormat.format(toFormat);

    Form form = new Form();
    form.param("status", tweet);

    final Response response = client.target(STATUS_URI)
            .queryParam("include_entities", "true")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

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

  private String getFromSystemOrEnv(final String key) {
    String value = System.getProperty(key, "");
    if (value.isEmpty()) {
      value = System.getenv(key);
    }
    return (value != null) ? value : "";
  }

}
