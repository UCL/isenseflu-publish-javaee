package uk.ac.ucl.flagship2.fludetector;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 *
 * @author David Guzman
 */
@Singleton
@LocalBean
@Startup
public class CallScheduler {

  private AtomicReferenceArray<String> lastModelScore;
  private AtomicReference<String> lastPublishedOn;

  @EJB
  private PublishModelScore publishModelScore;

  @PostConstruct
  public void initialise() {
    lastModelScore = new AtomicReferenceArray<>(new String[]{"", ""});
    lastPublishedOn = new AtomicReference<>("");
  }

  @Schedule(hour = "15")
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
