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
//  private CommandRunner commandRunner;
//  private CommandResult run;
//  private ClientAndServer mockServer;

  public void testAsetup() throws GlassFishException {
    System.out.println("BeforeAll");
//    mockServer = ClientAndServer.startClientAndServer(1080);

    GlassFishProperties glassfishProperties = new GlassFishProperties();
    glassfishProperties.setInstanceRoot(System.getProperty("gfroot") + "/glassfish/domains/domain1");
    glassfish = GlassFishRuntime.bootstrap().newGlassFish(glassfishProperties);
    glassfish.start();
    File ear = new File(System.getProperty("app"));
    Deployer deployer = glassfish.getDeployer();
    deployer.deploy(ear);
  }

  public void testSystem() throws LoginException, IOException {
    System.out.println("System Test");
    Client stompClient = new Client("localhost", 7672, "admin", "admin");
    stompClient.disconnect();
  }

  public void testZteardown() throws GlassFishException {
    glassfish.stop();
//    mockServer.stop();
  }

}
