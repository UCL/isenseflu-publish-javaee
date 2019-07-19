/*
 * i-sense flu publish: Module of the i-sense flu application used in the
 * publication of model scores on social media
 *
 * Copyright (c) 2019, UCL <https://www.ucl.ac.uk/>
 *
 * This file is part of i-sense flu publish
 *
 * i-sense flu publish is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * i-sense flu publish is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with i-sense flu publish.  If not, see <http://www.gnu.org/licenses/>.
 */

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
 * CDI bean responsible of the generation of the content of the tweet
 * from the message containing the date and the value of the score.
 * Since Twitter offers a two-step process to include images in a tweei,
 * MessageParser uses the TweetData as a container for both the
 * message text and the image.
 *
 * @author David Guzman
 */
@Dependent
public class MessageParser {

  /**
   * Ordinal suffix to use with days ending in 1.
   */
  private final String firstSuffix = "st of ";

  /**
   * Ordinal suffix to use with days ending in 2.
   */
  private final String secondSuffix = "nd of ";

  /**
   * Ordinal suffix to use with days ending in 3.
   */
  private final String thirdSuffix = "rd of ";

  /**
   * Number of decimal places that the score should use in a tweet.
   */
  private final int scoreDecimalPlaces = 3;

  /**
   * For calculation of percentages.
   */
  private final float percent = 0.01f;

  /**
   * Divisor used in the extraction of the last digit of a day number.
   */
  private final int lastDigitInDayDivisor = 10;

  /**
   * Remainder for days ending in 1.
   */
  private final int firstOfModulus = 1;

  /**
   * Remainder for days ending in 2.
   */
  private final int secondOfModulus = 2;

  /**
   * Remainder for days ending in 3.
   */
  private final int thirdOfModulus = 3;

  /**
   * Bean responsible for the retrieval of flu scores from the i-sense flu API.
   */
  @Inject
  private FetchScores fluDetectorScores;

  /**
   * Bean responsible of the generation of the chart to include in the tweet.
   */
  @Inject
  private PlotModelScore plotModelScore;

  /**
   * Template of the tweet. It requires the following parameters, in order:
   * the date of the score, the value of the score, whether it's an increase
   * or decrease, the relative change rate, the start date of the chart, and
   * the end date of the chart.
   */
  private final MessageFormat tweetFormat = new MessageFormat(
    "Based on Google searches, the estimated flu (influenza-like illness) rate"
      + " for England on the {0} was {1} cases per 100,000 people with an "
      + "average 7-day {2} rate of {3}% compared to the previous 7-day period "
      + "https://www.i-senseflu.org.uk/?start={4}&end={5}&resolution=day&"
      + "smoothing=0&id=3&source=twlink #health #AI");

  /**
   * Generates the text of the tweet and the chart image to publish on Twitter.
   * It uses a one month period before the date of the score to provide the
   * data for the chart and rate change, calling FetchScores for the retrieval
   * of the scores for the previous month and PlotModelScore to generate the
   * chart.
   * @param message A string containing the score data in a format that can
   *                be read as Properties. The properties required are: date
   *                (in ISO format) and value (a number).
   * @return        An instance of TweetData, containing the text of the tweet
   *                and the chart image.
   */
  public TweetData getTweetData(final String message) {
    Properties properties = PropertyReader.readProperties(message)
            .orElseThrow(
              () -> new IllegalArgumentException(
                "Cannot read properties from message"
              )
            );

    String date = properties.getProperty("date", "");
    String value = properties.getProperty("value", "");

    if (date.isEmpty() || !date.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
      throw new IllegalArgumentException(
        "Date field not provided or date not in ISO format: date is "
          + date
      );
    }

    if (value.isEmpty() || !value.matches("\\d+(\\.\\d+)?")) {
      throw new IllegalArgumentException(
        "Score value not provided or score value not a number: value is "
          + value
      );
    }

    LocalDate endDate = LocalDate.parse(date);
    LocalDate startDate = LocalDate.parse(date).minusMonths(1);
    String ordinal = "th of ";
    int day = endDate.getDayOfMonth();
    switch (day % lastDigitInDayDivisor) {
      case firstOfModulus:
        ordinal = firstSuffix;
        break;
      case secondOfModulus:
        ordinal = secondSuffix;
        break;
      case thirdOfModulus:
        ordinal = thirdSuffix;
        break;
      default:
        break;
    }
    String formattedDate = endDate
            .format(DateTimeFormatter.ofLocalizedDate(
              FormatStyle.LONG).withLocale(Locale.UK)
            )
            .replaceFirst("^0", "")
            .replaceAll("-", " ")
            .replaceFirst(" ", ordinal)
            .replaceFirst("( \\d{4})$", ",$1");

    BigDecimal bd = new BigDecimal(value);
    bd = bd.setScale(scoreDecimalPlaces, RoundingMode.HALF_UP);

    List<DatapointModelScore> scoresList = fluDetectorScores
      .getScoresForLast30Days(endDate);

    BufferedImage chart = plotModelScore.createLineChart(scoresList);

    List<Double> scores = scoresList.stream()
            .map((DatapointModelScore s) -> s.getScoreValue())
            .collect(Collectors.toList());
    Double p = RateCalculator.averageChangeRate().apply(scores);
    String variation = "variation";
    if (p > 0) {
      variation = "increase";
    } else if (p < 0) {
      variation = "decrease";
    }

    Object[] toFormat = {
      formattedDate,
      bd.toPlainString(),
      variation,
      Math.abs(p / percent),
      startDate,
      endDate
    };
    String tweet = tweetFormat.format(toFormat);

    return new TweetData(tweet, chart);
  }

  public final class TweetData {

    /**
     * The generated text of the tweet.
     */
    private final String tweet;

    /**
     * The chart showing the scores for the last month of data.
     */
    private final BufferedImage chart;

    /**
     * Identifier used to name the resulting chart image.
     */
    private final UUID chartId = UUID.randomUUID();

    private TweetData(final String tweetStr, final BufferedImage chartImg) {
      this.tweet = tweetStr;
      this.chart = chartImg;
    }

    /**
     * Accesses the text of the tweet.
     * @return The text content of the tweet.
     */
    public String getTweet() {
      return tweet;
    }

    /**
     * Produces a png image of the chart.
     * @return              The chart image ss an InputStream.
     * @throws IOException  If the conversion from a BufferedImage fails.
     */
    public InputStream getChartAsPng() throws IOException {
      final ByteArrayOutputStream os = new ByteArrayOutputStream() {
        @Override
        public synchronized byte[] toByteArray() {
          return this.buf;
        }
      };
      ImageIO.write(chart, "png", os);
      return new ByteArrayInputStream(os.toByteArray(), 0, os.size());
    }

    /**
     * Appends the png extension to use with the chart image file.
     * @return The name of the chart image file.
     */
    public String getPngFilename() {
      return chartId + ".png";
    }

  }

}
