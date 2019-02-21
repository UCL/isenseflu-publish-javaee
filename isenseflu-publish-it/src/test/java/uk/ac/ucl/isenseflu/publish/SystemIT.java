package uk.ac.ucl.isenseflu.publish;

import java.io.File;
import java.io.IOException;

import net.ser1.stomp.Client;
import org.glassfish.embeddable.*;

import javax.security.auth.login.LoginException;
//import org.mockserver.integration.ClientAndServer;

/**
 *
 * @author David Guzman
 */
public class SystemIT {

  private GlassFish glassfish;
  private CommandRunner commandRunner;
  private CommandResult run;
//  private ClientAndServer mockServer;

  public void testAsetup() throws GlassFishException {
    System.out.println("=========================");
    System.out.println("BeforeAll");
//    mockServer = ClientAndServer.startClientAndServer(1080);

    BootstrapProperties bootstrapProperties = new BootstrapProperties();
    bootstrapProperties.setInstallRoot(System.getProperty("gfroot"));
    GlassFishRuntime glassFishRuntime = GlassFishRuntime.bootstrap(bootstrapProperties);

    GlassFishProperties glassfishProperties = new GlassFishProperties();
    glassfishProperties.setInstanceRoot(System.getProperty("gfroot") + "/domains/domain1");
    glassfish = glassFishRuntime.newGlassFish(glassfishProperties);
    glassfish.start();

    commandRunner = glassfish.getCommandRunner();
    run = commandRunner.run("create-jmsdest", "--desttype", "queue", "PubModelScore.Q");
    System.out.println(run.getOutput());

    run = commandRunner.run(
      "create-jms-resource",
      "--restype",
      "javax.jms.Queue",
      "--property" ,
      "Name=PubModelScore.Q",
      "jms/PubModelScoreQ"
    );
    System.out.println(run.getOutput());

    File ear = new File(System.getProperty("app"));
    Deployer deployer = glassfish.getDeployer();
    deployer.deploy(ear);
  }

  public void testSystem() throws LoginException, IOException {
    System.out.println("=========================");
    System.out.println("System Test");
    Client stompClient = new Client("localhost", 7672, "admin", "admin");
    stompClient.send("/queue/PubModelScore.Q", "date=2019-01-01\nvalue=0.123");
    stompClient.disconnect();


  }

  public void testZteardown() throws GlassFishException {
    System.out.println("=========================");
    System.out.println("AfterAll");
    commandRunner = glassfish.getCommandRunner();
    run = commandRunner.run("flush-jmsdest", "--desttype", "queue", "PubModelScore.Q");
    System.out.println(run.getOutput());
    run = commandRunner.run("delete-jmsdest", "--desttype", "queue", "PubModelScore.Q");
    System.out.println(run.getOutput());
    glassfish.stop();
//    mockServer.stop();
    System.out.println("=========================");
  }

}
