/*
 * i-sense flu publish: Module of the i-sense flu application used in the
 * publication of model scores on social media
 *
 * Copyright (c) 2019, UCL <https://www.ucl.ac.uk/>
 *
 * This file is part of i-sense flu publish
 *
 * i-sense flu publish is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * i-sense flu publish is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with i-sense flu publish.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ucl.isenseflu.publish;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Paths;
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

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.security.auth.login.LoginException;

import static java.nio.file.Files.readAllBytes;

/**
 *
 * @author David Guzman
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SystemIT {

  private static GlassFish glassfish;
  private static CommandRunner commandRunner;
  private static CommandResult run;

  @BeforeAll
  public static void beforeAll() throws GlassFishException {
    System.out.println("=========================");
    System.out.println("BeforeAll");

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

    run = commandRunner.run(
      "set-log-attributes",
      "com.sun.enterprise.server.logging.GFFileHandler.logtoConsole=true"
    );
    System.out.println(run.getOutput());

    run = commandRunner.run(
      "set",
      "configs.config.server-config.cdi-service.enable-implicit-cdi=false"
    );

    Deployer deployer = glassfish.getDeployer();

    File war = new File(System.getProperty("mockserver"));
    deployer.deploy(war, "--name=mockserver", "--contextroot=mockserver");

    LocalTime callTime = LocalTime.now().plusSeconds(90L);
    run = commandRunner.run("create-system-properties", "API_SCORES_URI=http\\://localhost\\:8080/mockserver/flu/scores");
    run = commandRunner.run("create-system-properties", "TWITTER_STATUS_URI=http\\://localhost\\:8080/mockserver/twitter/status");
    run = commandRunner.run("create-system-properties", "TWITTER_MEDIA_URI=http\\://localhost\\:8080/mockserver/twitter/media");
    run = commandRunner.run("create-system-properties", String.format("TWITTER_SCHEDULED_FOR=%1$tH\\:%1$tM", callTime));
    run = commandRunner.run("list-system-properties");
    System.out.println(run.getOutput());

    File ear = new File(System.getProperty("app"));
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
  public void testWaitForEvent() throws InterruptedException, IOException {
    System.out.println("=========================");
    System.out.println("Wait for Event");
    String content = new String(readAllBytes(Paths.get(System.getProperty("mockserver.expectations"))));
    queryMockServer("http://localhost:8080/mockserver/expectation", content);
    Thread.sleep(70000);
    String statusReq = queryMockServer(
      "http://localhost:8080/mockserver/retrieve?type=REQUESTS&format=JSON",
      "{\"path\": \"/twitter/status\"}"
    );
    JsonReader reader = Json.createReader(new StringReader(statusReq));
    JsonObject jsonObject = reader.readArray().getJsonObject(0);
    String statusText = jsonObject.getJsonObject("body").getString("string");
    String expected = "status=Based+on+Google+searches%2C+the+estimated+flu+%28influenza-like+illness%29+rate+for+England+on+the+1st+of+January%2C+2019+was+0.123+cases+per+100%2C000+people+with+an+average+7-day+increase+rate+of+38.319%25+compared+to+the+previous+28-day+period+https%3A%2F%2Fwww.i-senseflu.org.uk%2F%3Fstart%3D2018-12-01%26end%3D2019-01-01%26resolution%3Dday%26smoothing%3D0%26id%3D3%26source%3Dtwlink+%23health+%23AI&media_ids=710511363345354753";
    Assertions.assertEquals(expected, statusText);
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
    System.out.println("=========================");
  }

  private String queryMockServer(String url, String parameters) {
    try {
      URL urlObj = new URL(url);
      HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();

      // PUT request
      con.setRequestMethod("PUT");
      con.setDoOutput(true);
      DataOutputStream wr = new DataOutputStream(con.getOutputStream());
      wr.writeBytes(parameters);
      wr.flush();
      wr.close();

      int responseCode = con.getResponseCode();
      if (responseCode != 200) return "";
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();
      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();
      return response.toString();
    } catch (IOException e ) {
      return "Request failed";
    }
  }

}
