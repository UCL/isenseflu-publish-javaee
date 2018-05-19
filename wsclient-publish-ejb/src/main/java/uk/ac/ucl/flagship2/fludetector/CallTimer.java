/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ucl.flagship2.fludetector;

import java.io.Serializable;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 *
 * @author david
 */
@Singleton
@LocalBean
@Startup
public class CallTimer {

  private final long INTERVAL_MIN = 10000;

  @Resource
  private TimerService timerService;

  @Inject
  private Event<String> callTwitter;

  @PostConstruct
  public void initialise() {
    timerService.createTimer(0, INTERVAL_MIN, "TWITTER TEST");
  }

  @Timeout
  public void fireEvent(Timer timer) {
    timerInfoToString(timer.getInfo()).ifPresent((String s) -> {

      callTwitter.fire(String.valueOf(timer.getNextTimeout().getTime()));

    });

  }

  private Optional<String> timerInfoToString(final Serializable input) {
    if (input instanceof String) {
      return Optional.of((String) input);
    } else {
      return Optional.empty();
    }
  }

}
