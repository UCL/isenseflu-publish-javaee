package uk.ac.ucl.flagship2.fludetector;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;
import javax.enterprise.context.Dependent;
import javax.imageio.ImageIO;
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
          "Based on Google searches, the estimated flu (Influenza-like illness) rate for England on {0} is {1} cases per 100,000 people https://fludetector.cs.ucl.ac.uk/?start={2}&end={3}&resolution=day&smoothing=0&model_regions-0=7-e #health #AI");

  public TweetData getTweetData(String message) {
    Properties properties = PropertyReader.readProperties(message)
            .orElseThrow(() -> new IllegalArgumentException("Cannot read properties from message"));

    String date = properties.getProperty("date", "");
    String value = properties.getProperty("value", "");

    if (date.isEmpty() || !date.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
      throw new IllegalArgumentException("Date field not provided or date not in ISO format");
    }

    if (value.isEmpty() || !value.matches("\\d+(\\.\\d+)?")) {
      throw new IllegalArgumentException("Score value not provided or score value not a number");
    }

    LocalDate endDate = LocalDate.parse(date);
    LocalDate startDate = LocalDate.parse(date).minusMonths(1);
    String formattedDate = endDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.UK));

    BigDecimal bd = new BigDecimal(value);
    bd = bd.setScale(3, RoundingMode.HALF_UP);

    Object[] toFormat = {formattedDate.replaceFirst("0", "").replaceAll("-", " "), bd.toPlainString(), startDate, endDate};
    String tweet = tweetFormat.format(toFormat);

    List<DatapointModelScore> scoresList = fluDetectorScores.getScoresForLast30Days(endDate);

    BufferedImage chart = plotModelScore.createLineChart(scoresList);

    return new TweetData(tweet, chart);
  }

  public class TweetData {

    private final String tweet;
    private final BufferedImage chart;
    private final UUID chartId = UUID.randomUUID();

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

    public InputStream getChartAsPng() throws IOException {
      final ByteArrayOutputStream os = new ByteArrayOutputStream() {
        @Override
        public synchronized byte[] toByteArray() {
          return this.buf;
        }
      };
      ImageIO.write(chart, "png", os);
      return (InputStream) new ByteArrayInputStream(os.toByteArray(), 0, os.size());
    }

    public String getPngFilename() {
      return chartId + ".png";
    }

  }

}
