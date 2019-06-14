package uk.ac.ucl.isenseflu.publish;

import javax.ejb.EJB;

public class AppClient {

  @EJB
  private CallSchedulerRemote callSchedulerRemote;
}
