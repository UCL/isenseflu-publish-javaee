package uk.ac.ucl.isenseflu.publish;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import net.ser1.stomp.Client;
import org.glassfish.embeddable.BootstrapProperties;
import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.CommandRunner;
import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javax.security.auth.login.LoginException;
//import org.mockserver.integration.ClientAndServer;

/**
 *
 * @author David Guzman
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SystemIT {

  private static GlassFish glassfish;
  private static CommandRunner commandRunner;
  private static CommandResult run;

//  private ClientAndServer mockServer;

  @BeforeAll
  public static void beforeAll() throws GlassFishException {
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

    LocalTime callTime = LocalTime.now().plusMinutes(1);
    run = commandRunner.run("create-system-properties", String.format("TWITTER_SCHEDULED_FOR=%1$tH\\:%1$tM", callTime));
    run = commandRunner.run("list-system-properties");
    System.out.println(run.getOutput());

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

    run = commandRunner.run(
      "set-log-attributes",
      "com.sun.enterprise.server.logging.GFFileHandler.logtoConsole=true"
    );
    System.out.println(run.getOutput());

    run = commandRunner.run(
      "set",
      "configs.config.server-config.cdi-service.enable-implicit-cdi=false"
    );

    File ear = new File(System.getProperty("app"));
    Deployer deployer = glassfish.getDeployer();
    deployer.deploy(ear);
  }

  @Test
  @Order(1)
  public void testCallStomp() throws IOException, LoginException, InterruptedException {
    System.out.println("=========================");
    System.out.println("Call STOMP");

    String hostname = InetAddress.getLocalHost().getHostName();
    Client stompClient = new Client(hostname, 7672, "admin", "admin");
    Map<String, String> headers = new HashMap<>();
    headers.put("receipt", "stomp-receipt-1");
    stompClient.send("/queue/PubModelScore.Q", "date=2019-01-01\nvalue=0.123", headers);
    // Wait two seconds for the receipt
    boolean receipt = stompClient.waitOnReceipt( "stomp-receipt-1", 2000 );
    Assertions.assertTrue(receipt);
    stompClient.disconnect();
  }

  @Test
  @Order(2)
  public void testWaitForEvent() throws InterruptedException {
    Thread.sleep(70000);
    System.out.println("+++++++++++=========================");
  }

  @AfterAll
  public static void afterAll() throws GlassFishException {
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
