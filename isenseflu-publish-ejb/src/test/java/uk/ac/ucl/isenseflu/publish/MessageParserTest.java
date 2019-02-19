package uk.ac.ucl.isenseflu.publish;

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
    String expected = "Based on Google searches, the estimated flu (influenza-like illness) rate for England on the 20th of May, 2018 was 2.892 cases per 100,000 people with an average 7-day increase rate of 4.06% compared to the previous 7-day period https://fludetector.cs.ucl.ac.uk/?start=2018-04-20&end=2018-05-20&resolution=day&smoothing=0&model_regions-0=7-e #health #AI";
    Assertions.assertEquals(expected, tweetData.getTweet());
  }

  @Test
  public void testGetTweetDataDates() {
    Deencapsulation.setField(instance, "fluDetectorScores", fluDetectorScores);
    Deencapsulation.setField(instance, "plotModelScore", plotModelScore);
    new Expectations() {
      {
        fluDetectorScores.getScoresForLast30Days(withInstanceOf(LocalDate.class));
        result = DATAPOINTS;
      }
    };

    String[][] assertInputs = {
      {
        "date=2018-05-20",
        "Based on Google searches, the estimated flu (influenza-like illness) rate for England on the 20th of May, 2018 was 2.892 cases per 100,000 people with an average 7-day increase rate of 4.06% compared to the previous 7-day period https://fludetector.cs.ucl.ac.uk/?start=2018-04-20&end=2018-05-20&resolution=day&smoothing=0&model_regions-0=7-e #health #AI"
      },
      {
        "date=2018-05-01",
        "Based on Google searches, the estimated flu (influenza-like illness) rate for England on the 1st of May, 2018 was 2.892 cases per 100,000 people with an average 7-day increase rate of 4.06% compared to the previous 7-day period https://fludetector.cs.ucl.ac.uk/?start=2018-04-01&end=2018-05-01&resolution=day&smoothing=0&model_regions-0=7-e #health #AI"
      },
      {
        "date=2018-05-02",
        "Based on Google searches, the estimated flu (influenza-like illness) rate for England on the 2nd of May, 2018 was 2.892 cases per 100,000 people with an average 7-day increase rate of 4.06% compared to the previous 7-day period https://fludetector.cs.ucl.ac.uk/?start=2018-04-02&end=2018-05-02&resolution=day&smoothing=0&model_regions-0=7-e #health #AI"
      },
      {
        "date=2018-05-03",
        "Based on Google searches, the estimated flu (influenza-like illness) rate for England on the 3rd of May, 2018 was 2.892 cases per 100,000 people with an average 7-day increase rate of 4.06% compared to the previous 7-day period https://fludetector.cs.ucl.ac.uk/?start=2018-04-03&end=2018-05-03&resolution=day&smoothing=0&model_regions-0=7-e #health #AI"
      },
      {
        "date=2018-05-21",
        "Based on Google searches, the estimated flu (influenza-like illness) rate for England on the 21st of May, 2018 was 2.892 cases per 100,000 people with an average 7-day increase rate of 4.06% compared to the previous 7-day period https://fludetector.cs.ucl.ac.uk/?start=2018-04-21&end=2018-05-21&resolution=day&smoothing=0&model_regions-0=7-e #health #AI"
      },
      {
        "date=2018-05-22",
        "Based on Google searches, the estimated flu (influenza-like illness) rate for England on the 22nd of May, 2018 was 2.892 cases per 100,000 people with an average 7-day increase rate of 4.06% compared to the previous 7-day period https://fludetector.cs.ucl.ac.uk/?start=2018-04-22&end=2018-05-22&resolution=day&smoothing=0&model_regions-0=7-e #health #AI"
      },
      {
        "date=2018-05-23",
        "Based on Google searches, the estimated flu (influenza-like illness) rate for England on the 23rd of May, 2018 was 2.892 cases per 100,000 people with an average 7-day increase rate of 4.06% compared to the previous 7-day period https://fludetector.cs.ucl.ac.uk/?start=2018-04-23&end=2018-05-23&resolution=day&smoothing=0&model_regions-0=7-e #health #AI"
      },
      {
        "date=2018-05-31",
        "Based on Google searches, the estimated flu (influenza-like illness) rate for England on the 31st of May, 2018 was 2.892 cases per 100,000 people with an average 7-day increase rate of 4.06% compared to the previous 7-day period https://fludetector.cs.ucl.ac.uk/?start=2018-04-30&end=2018-05-31&resolution=day&smoothing=0&model_regions-0=7-e #health #AI"
      }
    };

    Arrays.asList(assertInputs).forEach((String[] assertInput) -> {
      String msg = assertInput[0] + System.lineSeparator() + "value=2.892199";
      String result = instance.getTweetData(msg).getTweet();
      Assertions.assertEquals(assertInput[1], result);
    });
  }

}
