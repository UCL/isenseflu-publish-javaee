package uk.ac.ucl.isenseflu.publish;

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
import java.util.stream.Collectors;
import javax.enterprise.context.Dependent;
import javax.imageio.ImageIO;
import javax.inject.Inject;

/**
 *
 * @author David Guzman
 */
@Dependent
public class MessageParser {

  private final String firstSuffix = "st of ";
  private final String secondSuffix = "nd of ";
  private final String thirdSuffix = "rd of ";

  @Inject
  private FetchScores fluDetectorScores;

  @Inject
  private PlotModelScore plotModelScore;

  private final MessageFormat tweetFormat = new MessageFormat(
          "Based on Google searches, the estimated flu (influenza-like illness) rate for England on the {0} was {1} cases per 100,000 people with an average 7-day {2} rate of {3}% compared to the previous 7-day period https://fludetector.cs.ucl.ac.uk/?start={4}&end={5}&resolution=day&smoothing=0&id=3 #health #AI");

  public TweetData getTweetData(String message) {
    Properties properties = PropertyReader.readProperties(message)
            .orElseThrow(() -> new IllegalArgumentException("Cannot read properties from message"));

    String date = properties.getProperty("date", "");
    String value = properties.getProperty("value", "");

    if (date.isEmpty() || !date.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
      throw new IllegalArgumentException("Date field not provided or date not in ISO format: date is " + date);
    }

    if (value.isEmpty() || !value.matches("\\d+(\\.\\d+)?")) {
      throw new IllegalArgumentException("Score value not provided or score value not a number: value is " + value);
    }

    LocalDate endDate = LocalDate.parse(date);
    LocalDate startDate = LocalDate.parse(date).minusMonths(1);
    String ordinal = "th of ";
    int day = endDate.getDayOfMonth();
    switch (day) {
      case 1:
      case 21:
      case 31:
        ordinal = firstSuffix;
        break;
      case 2:
      case 22:
        ordinal = secondSuffix;
        break;
      case 3:
      case 23:
        ordinal = thirdSuffix;
        break;
      default:
        break;
    }
    String formattedDate = endDate
            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.UK))
            .replaceFirst("^0", "")
            .replaceAll("-", " ")
            .replaceFirst(" ", ordinal)
            .replaceFirst("( \\d{4})$", ",$1");

    BigDecimal bd = new BigDecimal(value);
    bd = bd.setScale(3, RoundingMode.HALF_UP);

    List<DatapointModelScore> scoresList = fluDetectorScores.getScoresForLast30Days(endDate);

    BufferedImage chart = plotModelScore.createLineChart(scoresList);

    List<Double> scores = scoresList.stream()
            .map((DatapointModelScore s) -> s.getScoreValue())
            .collect(Collectors.toList());
    Double p = RateCalculator.averageChangeRate.apply(scores);
    String variation = "variation";
    if (p > 0) {
      variation = "increase";
    } else if (p < 0) {
      variation = "decrease";
    }

    Object[] toFormat = {formattedDate, bd.toPlainString(), variation, Math.abs(p * 100), startDate, endDate};
    String tweet = tweetFormat.format(toFormat);

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
