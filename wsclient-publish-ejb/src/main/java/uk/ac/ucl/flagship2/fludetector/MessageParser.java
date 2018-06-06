package uk.ac.ucl.flagship2.fludetector;

import java.awt.image.BufferedImage;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 *
 * @author David Guzman
 */
@Dependent
public class MessageParser {

  @Inject
  private FluDetectorScores fluDetectorScores;

  @Inject
  private PlotModelScore plotModelScore;

  private final MessageFormat tweetFormat = new MessageFormat(
          "Based on Google searches, the estimated flu (Influenza-like illness) rate for England on {0} is {1} cases per 100,000 people #health #AI");

  public TweetData getTweetData(String message) {
    Properties properties = PropertyReader.readProperties(message)
            .orElseThrow(() -> new IllegalArgumentException("Cannot read properties from message"));

    String date = properties.getProperty("date", "");
    String value = properties.getProperty("value", "");

    if (date.isEmpty() || !date.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
      throw new IllegalArgumentException("Date field was not provided or date not in ISO format");
    }

    if (value.isEmpty() || !value.matches("\\d+(\\.\\d+)?")) {
      throw new IllegalArgumentException("Score value was not provided or score value is not a number");
    }

    Object[] toFormat = {date, value};
    String tweet = tweetFormat.format(toFormat);

    List<DatapointModelScore> scoresList = fluDetectorScores.getScoresForLast30Days(LocalDate.parse(date));

    BufferedImage chart = plotModelScore.createLineChart(scoresList);

    return new TweetData(tweet, chart);
  }

  public class TweetData {

    private final String tweet;
    private final BufferedImage chart;

    private TweetData(String tweet, BufferedImage chart) {
      this.tweet = tweet;
      this.chart = chart;
    }

    public String getTweet() {
      return tweet;
    }

    public BufferedImage getChart() {
      return chart;
    }

  }

}
