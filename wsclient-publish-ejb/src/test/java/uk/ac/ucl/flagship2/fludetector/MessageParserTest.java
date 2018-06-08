package uk.ac.ucl.flagship2.fludetector;

import mockit.Deencapsulation;
import mockit.Injectable;
import mockit.Tested;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author David Guzman
 */
public class MessageParserTest {

  @Tested
  MessageParser instance;

  @Injectable
  FluDetectorScores fluDetectorScores;

  @Injectable
  PlotModelScore plotModelScore;

  @Test
  public void testGetTweetData() {
    Deencapsulation.setField(instance, "fluDetectorScores", fluDetectorScores);
    Deencapsulation.setField(instance, "plotModelScore", plotModelScore);
    String message = "date=2018-06-07" + System.lineSeparator() + "value=12.37688";
    MessageParser.TweetData tweetData = instance.getTweetData(message);
    String expected = "Based on Google searches, the estimated flu (Influenza-like illness) rate for England on 7 Jun 2018 is 12.377 cases per 100,000 people https://fludetector.cs.ucl.ac.uk/?start=2018-05-07&end=2018-06-07&resolution=day&smoothing=0&model_regions-0=7-e #health #AI";
    Assertions.assertEquals(expected, tweetData.getTweet());
  }

}
