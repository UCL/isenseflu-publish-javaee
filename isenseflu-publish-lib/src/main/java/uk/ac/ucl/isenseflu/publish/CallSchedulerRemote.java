package uk.ac.ucl.isenseflu.publish;

import javax.ejb.Remote;
import javax.ejb.ScheduleExpression;

@Remote
public interface CallSchedulerRemote {

  void setTimer(final ScheduleExpression scheduleExpression);

}
