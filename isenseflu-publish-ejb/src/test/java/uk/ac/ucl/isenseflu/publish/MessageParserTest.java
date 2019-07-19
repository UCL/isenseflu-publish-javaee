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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static uk.ac.ucl.isenseflu.publish.DataStubs.SCORES;
import static uk.ac.ucl.isenseflu.publish.DataStubs.START_DATE;

/**
 *
 * @author David Guzman
 */
public class MessageParserTest {

  private static final List<DatapointModelScore> DATAPOINTS = new ArrayList<>();

  @Injectable
  FetchScores fluDetectorScores;

  @Injectable
  PlotModelScore plotModelScore;

  @Tested
  MessageParser instance;

  @BeforeAll
  public static void populateDatapoints() {
    AtomicInteger counter = new AtomicInteger(0);
    Arrays.asList(SCORES).forEach((Double s) -> {
      DATAPOINTS.add(new DatapointModelScore(START_DATE.plusDays(counter.getAndAdd(1)), s));
    });
  }

  @Test
  public void testGetTweetData() {
    new Expectations() {
      {
        fluDetectorScores.getScoresForLast30Days(LocalDate.of(2018, 5, 20));
        result = DATAPOINTS;
      }
    };
    String message = "date=2018-05-20" + System.lineSeparator() + "value=2.892199";
    MessageParser.TweetData tweetData = instance.getTweetData(message);
    String expected = "Based on Google searches, the estimated flu (influenza-like illness) rate for England on the 20th of May, 2018 was 2.892 cases per 100,000 people with an average 7-day increase rate of 25.59% compared to the previous 7-day period https://www.i-senseflu.org.uk/?start=2018-04-20&end=2018-05-20&resolution=day&smoothing=0&id=3&source=twlink #health #AI";
    Assertions.assertEquals(expected, tweetData.getTweet());
  }

  @Test
  public void testGetTweetDataDates() {
    new Expectations() {
      {
        fluDetectorScores.getScoresForLast30Days(withInstanceOf(LocalDate.class));
        result = DATAPOINTS;
      }
    };

    String[][] assertInputs = {
      {
        "date=2018-05-20",
        "Based on Google searches, the estimated flu (influenza-like illness) rate for England on the 20th of May, 2018 was 2.892 cases per 100,000 people with an average 7-day increase rate of 25.59% compared to the previous 7-day period https://www.i-senseflu.org.uk/?start=2018-04-20&end=2018-05-20&resolution=day&smoothing=0&id=3&source=twlink #health #AI"
      },
      {
        "date=2018-05-01",
        "Based on Google searches, the estimated flu (influenza-like illness) rate for England on the 1st of May, 2018 was 2.892 cases per 100,000 people with an average 7-day increase rate of 25.59% compared to the previous 7-day period https://www.i-senseflu.org.uk/?start=2018-04-01&end=2018-05-01&resolution=day&smoothing=0&id=3&source=twlink #health #AI"
      },
      {
        "date=2018-05-02",
        "Based on Google searches, the estimated flu (influenza-like illness) rate for England on the 2nd of May, 2018 was 2.892 cases per 100,000 people with an average 7-day increase rate of 25.59% compared to the previous 7-day period https://www.i-senseflu.org.uk/?start=2018-04-02&end=2018-05-02&resolution=day&smoothing=0&id=3&source=twlink #health #AI"
      },
      {
        "date=2018-05-03",
        "Based on Google searches, the estimated flu (influenza-like illness) rate for England on the 3rd of May, 2018 was 2.892 cases per 100,000 people with an average 7-day increase rate of 25.59% compared to the previous 7-day period https://www.i-senseflu.org.uk/?start=2018-04-03&end=2018-05-03&resolution=day&smoothing=0&id=3&source=twlink #health #AI"
      },
      {
        "date=2018-05-21",
        "Based on Google searches, the estimated flu (influenza-like illness) rate for England on the 21st of May, 2018 was 2.892 cases per 100,000 people with an average 7-day increase rate of 25.59% compared to the previous 7-day period https://www.i-senseflu.org.uk/?start=2018-04-21&end=2018-05-21&resolution=day&smoothing=0&id=3&source=twlink #health #AI"
      },
      {
        "date=2018-05-22",
        "Based on Google searches, the estimated flu (influenza-like illness) rate for England on the 22nd of May, 2018 was 2.892 cases per 100,000 people with an average 7-day increase rate of 25.59% compared to the previous 7-day period https://www.i-senseflu.org.uk/?start=2018-04-22&end=2018-05-22&resolution=day&smoothing=0&id=3&source=twlink #health #AI"
      },
      {
        "date=2018-05-23",
        "Based on Google searches, the estimated flu (influenza-like illness) rate for England on the 23rd of May, 2018 was 2.892 cases per 100,000 people with an average 7-day increase rate of 25.59% compared to the previous 7-day period https://www.i-senseflu.org.uk/?start=2018-04-23&end=2018-05-23&resolution=day&smoothing=0&id=3&source=twlink #health #AI"
      },
      {
        "date=2018-05-31",
        "Based on Google searches, the estimated flu (influenza-like illness) rate for England on the 31st of May, 2018 was 2.892 cases per 100,000 people with an average 7-day increase rate of 25.59% compared to the previous 7-day period https://www.i-senseflu.org.uk/?start=2018-04-30&end=2018-05-31&resolution=day&smoothing=0&id=3&source=twlink #health #AI"
      }
    };

    Arrays.asList(assertInputs).forEach((String[] assertInput) -> {
      String msg = assertInput[0] + System.lineSeparator() + "value=2.892199";
      String result = instance.getTweetData(msg).getTweet();
      Assertions.assertEquals(assertInput[1], result);
    });
  }

}
