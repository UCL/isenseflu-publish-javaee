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
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerService;

/**
 * CallScheduler is responsible for the configuration of the EJB Timer service
 * and triggering the event required to publish the model score as managed
 * by PublishModelScore.
 *
 * @author David Guzman
 */
@Singleton
@LocalBean
@Startup
public class CallScheduler {

  /**
   * Default time when CallScheduler will trigger the publishing of the score.
   * Can be overridden with the system property or environment variable
   * TWITTER_SCHEDULED_FOR
   */
  private final String defaultTweetTime = "16:00";

  /**
   * Array to contain the date of the last score published, and the value of
   * the score.
   */
  private AtomicReferenceArray<String> lastModelScore;

  /**
   * Stores the date (as a String) when the last tweet was published.
   */
  private AtomicReference<String> lastPublishedOn;

  /**
   * EJB container-managed service used to invoke #callPublisher at a
   * specific time, as configured in #initialise.
   */
  @Resource
  private TimerService timerService;

  /**
   * Bean responsible for coordinating the process for publishing the
   * score on social media.
   */
  @EJB
  private PublishModelScore publishModelScore;

  /**
   * Configures the EJB timer service, setting the time at which the
   * #callPublisher method will the triggered.
   */
  @PostConstruct
  public void initialise() {
    String twitterScheduledFor = PropertyReader
      .getFromSystemOrEnv("TWITTER_SCHEDULED_FOR");
    String[] tweetTimeHourMin = twitterScheduledFor.indexOf(":") == 2
      ? twitterScheduledFor.split(":") : defaultTweetTime.split(":");
    ScheduleExpression expression = new ScheduleExpression();
    expression.hour(tweetTimeHourMin[0]);
    expression.minute(tweetTimeHourMin[1]);
    timerService.createCalendarTimer(expression);
    lastModelScore = new AtomicReferenceArray<>(new String[]{"", ""});
    lastPublishedOn = new AtomicReference<>("");
  }

  /**
   * Sends the latest score data to PublishModelScore. Called by the
   * EJB timer service.
   */
  @Timeout
  public void callPublisher() {
    if (!lastModelScore.get(0).equals(lastPublishedOn.get())) {
      publishModelScore.publishScore(lastModelScore.get(1));
      lastPublishedOn.set(LocalDate.now().toString());
    }
  }

  /**
   * Allows the message-driven bean ReceiveModelScore to set the date of
   * reception and the score message to be passed on to PublishModelScore.
   * @param score The message containing the score data.
   */
  @Lock(LockType.WRITE)
  public void setLastModelScore(final String score) {
    if (!score.isEmpty()) {
      lastModelScore.set(0, LocalDate.now().toString());
      lastModelScore.set(1, score);
    }
  }

}
