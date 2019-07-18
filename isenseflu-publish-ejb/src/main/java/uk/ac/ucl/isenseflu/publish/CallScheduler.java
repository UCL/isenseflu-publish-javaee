/*
 * i-sense flu publish: Module of the i-sense flu application used in the publication of model scores on social media
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
 *
 * @author David Guzman
 */
@Singleton
@LocalBean
@Startup
public class CallScheduler {

  private final String DEFAULT_TWEET_TIME = "16:00";
  private AtomicReferenceArray<String> lastModelScore;
  private AtomicReference<String> lastPublishedOn;

  @Resource
  private TimerService timerService;

  @EJB
  private PublishModelScore publishModelScore;

  @PostConstruct
  public void initialise() {
    String twitterSchedule = PropertyReader.getFromSystemOrEnv("TWITTER_SCHEDULED_FOR");
    String[] tweetTime = twitterSchedule.indexOf(":") == 2 ? twitterSchedule.split(":") : DEFAULT_TWEET_TIME.split(":");
    ScheduleExpression expression = new ScheduleExpression();
    expression.hour(tweetTime[0]);
    expression.minute(tweetTime[1]);
    timerService.createCalendarTimer(expression);
    lastModelScore = new AtomicReferenceArray<>(new String[]{"", ""});
    lastPublishedOn = new AtomicReference<>("");
  }

  @Timeout
  public void callPublisher() {
    if (!lastModelScore.get(0).equals(lastPublishedOn.get())) {
      publishModelScore.publishScore(lastModelScore.get(1));
      lastPublishedOn.set(LocalDate.now().toString());
    }
  }

  @Lock(LockType.WRITE)
  public void setLastModelScore(String score) {
    if (!score.isEmpty()) {
      lastModelScore.set(0, LocalDate.now().toString());
      lastModelScore.set(1, score);

    }
  }

}
