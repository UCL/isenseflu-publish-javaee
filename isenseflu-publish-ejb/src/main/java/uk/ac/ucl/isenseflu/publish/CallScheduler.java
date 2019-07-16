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
