package uk.ac.ucl.flagship2.fludetector;

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;
import java.util.Properties;

/**
 *
 * @author David Guzman
 */
public class PropertyReader {

  public static String getFromSystemOrEnv(final String key) {
    String value = System.getProperty(key, "");
    if (value.isEmpty()) {
      value = System.getenv(key);
    }
    return (value != null) ? value : "";
  }

  public static Optional<Properties> readProperties(String propstr) {
    final Properties p = new Properties();
    try {
      p.load(new StringReader(propstr));
    } catch (IOException ex) {
      return Optional.empty();
    }
    return Optional.of(p);
  }
}
