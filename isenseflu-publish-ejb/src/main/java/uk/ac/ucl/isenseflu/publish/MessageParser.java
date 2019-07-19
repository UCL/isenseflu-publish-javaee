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
 *
 * @author David Guzman
 */
@Dependent
public class MessageParser {

  private final String firstSuffix = "st of ";
  private final String secondSuffix = "nd of ";
  private final String thirdSuffix = "rd of ";
  private final int scoreDecimalPlaces = 3;
  private final float percent = 0.01f;
  private final int lastDigitInDayDivisor = 10;
  private final int firstOfModulus = 1;
  private final int secondOfModulus = 2;
  private final int thirdOfModulus = 3;

  @Inject
  private FetchScores fluDetectorScores;

  @Inject
  private PlotModelScore plotModelScore;

  private final MessageFormat tweetFormat = new MessageFormat(
    "Based on Google searches, the estimated flu (influenza-like illness) rate"
      + " for England on the {0} was {1} cases per 100,000 people with an "
      + "average 7-day {2} rate of {3}% compared to the previous 7-day period "
      + "https://www.i-senseflu.org.uk/?start={4}&end={5}&resolution=day&"
      + "smoothing=0&id=3&source=twlink #health #AI");

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
    Double p = RateCalculator.averageChangeRate.apply(scores);
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

    private final String tweet;
    private final BufferedImage chart;
    private final UUID chartId = UUID.randomUUID();

    private TweetData(final String tweetStr, final BufferedImage chartImg) {
      this.tweet = tweetStr;
      this.chart = chartImg;
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
      return new ByteArrayInputStream(os.toByteArray(), 0, os.size());
    }

    public String getPngFilename() {
      return chartId + ".png";
    }

  }

}
