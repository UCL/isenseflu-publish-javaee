package uk.ac.ucl.flagship2.fludetector;

import java.io.File;
import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.CommandRunner;
import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;

/**
 *
 * @author David Guzman
 */
public class SystemIT {

  private GlassFish glassfish;
  private CommandRunner commandRunner;
  private CommandResult run;

  public void testAsetup() throws GlassFishException {
    System.out.println("BeforeAll");
    glassfish = GlassFishRuntime.bootstrap().newGlassFish();
    glassfish.start();
    commandRunner = glassfish.getCommandRunner();
    run = commandRunner.run("create-jmsdest", "--desttype", "queue", "PubModelScore.Q");
    System.out.println(run.getOutput());
    run = commandRunner.run("create-jms-resource", "--restype", "javax.jms.Queue", "--property",
            "Name=PubModelScore.Q", "jms/PubModelScoreQ");
    System.out.println(run.getOutput());
    File ear = new File(System.getProperty("app"));
    Deployer deployer = glassfish.getDeployer();
    deployer.deploy(ear);
  }

  public void testSystem() throws GlassFishException {
    System.out.println("System Test");
//    GlassFishProperties glassfishProperties = new GlassFishProperties();
  }

  public void testZteardown() throws GlassFishException {
    glassfish.stop();
  }

}
