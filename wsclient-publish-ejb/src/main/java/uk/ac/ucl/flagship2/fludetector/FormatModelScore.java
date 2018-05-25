package uk.ac.ucl.flagship2.fludetector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.Dependent;

/**
 *
 * @author David Guzman
 */
@Dependent
public class FormatModelScore {

  private final MessageFormat tweetFormat = new MessageFormat(
          "Based on Google searches, the estimated flu (Influenza-like illness) rate for England on {0} is {1} cases per 100,000 people #health #AI");

  private String date;
  private String value;

  public FormatModelScore() {
  }

  public boolean parseMessage(String message) {
    try (BufferedReader reader = new BufferedReader(new StringReader(message))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] linearr = line.split("=");
        if (linearr.length == 2) {
          if (linearr[0].equals("date")) {
            date = linearr[1];
          } else if (linearr[0].equals("value")) {
            value = linearr[1];
          } else {
            return false;
          }
          return true;
        }
      }
    } catch (IOException ex) {
      Logger.getLogger(FormatModelScore.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

}
