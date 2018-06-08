package uk.ac.ucl.flagship2.fludetector;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static uk.ac.ucl.flagship2.fludetector.DataStubs.SCORES;

/**
 *
 * @author David Guzman
 */
public class MessageParserTest extends DataStubs {

  private static final List<DatapointModelScore> DATAPOINTS = new ArrayList<>();

  @Tested
  MessageParser instance;

  @Injectable
  FluDetectorScores fluDetectorScores;

  @Injectable
  PlotModelScore plotModelScore;

  @BeforeAll
  public static void populateDatapoints() {
    AtomicInteger counter = new AtomicInteger(0);
    Arrays.asList(SCORES).forEach((Double s) -> {
      DATAPOINTS.add(new DatapointModelScore(START_DATE.plusDays(counter.getAndAdd(1)), s));
    });
  }

  @Test
  public void testGetTweetData() {
    Deencapsulation.setField(instance, "fluDetectorScores", fluDetectorScores);
    Deencapsulation.setField(instance, "plotModelScore", plotModelScore);
    new Expectations() {
      {
        fluDetectorScores.getScoresForLast30Days(LocalDate.of(2018, 5, 20));
        result = DATAPOINTS;
      }
    };
    String message = "date=2018-05-20" + System.lineSeparator() + "value=2.892199";
    MessageParser.TweetData tweetData = instance.getTweetData(message);
    String expected = "Based on Google searches, the estimated flu (influenza-like illness) rate for England on the 20th of May, 2018 is 2.892 cases per 100,000 people with an average 7-day increase rate of 4.06% compared to the previous 7-day period https://fludetector.cs.ucl.ac.uk/?start=2018-04-20&end=2018-05-20&resolution=day&smoothing=0&model_regions-0=7-e #health #AI";
    Assertions.assertEquals(expected, tweetData.getTweet());
  }

}
