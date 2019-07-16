package uk.ac.ucl.isenseflu.publish;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReferenceArray;
import mockit.Deencapsulation;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ejb.TimerService;

/**
 *
 * @author David Guzman
 */
public class CallSchedulerTest {

  @Tested
  CallScheduler instance;

  @Injectable
  PublishModelScore publishModelScore;

  @Injectable
  TimerService timerService;

  @Test
  public void testSetScore() {
    String expectedScore = "1234";
    instance.setLastModelScore(expectedScore);
    AtomicReferenceArray<String> lastScore = Deencapsulation.getField(instance, "lastModelScore");
    Assertions.assertAll("lastModelScore",
            () -> Assertions.assertEquals(LocalDate.now().toString(), lastScore.get(0)),
            () -> Assertions.assertEquals(expectedScore, lastScore.get(1))
    );
  }

  @Test
  public void testCallPublisher() {
    instance.callPublisher();
    new Verifications() {
      {
        publishModelScore.publishScore(anyString);
        times = 0;
      }
    };

    instance.setLastModelScore("1234");
    instance.callPublisher();
    new Verifications() {
      {
        publishModelScore.publishScore(anyString);
        times = 1;
      }
    };

    instance.setLastModelScore("5678");
    instance.callPublisher();
    new Verifications() {
      {
        publishModelScore.publishScore(anyString);
        times = 1;
      }
    };
  }

}
