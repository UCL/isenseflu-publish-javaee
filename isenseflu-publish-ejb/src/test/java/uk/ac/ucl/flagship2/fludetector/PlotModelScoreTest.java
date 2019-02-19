package uk.ac.ucl.flagship2.fludetector;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import mockit.Tested;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 *
 * @author David Guzman
 */
public class PlotModelScoreTest extends DataStubs {

  private static final List<DatapointModelScore> DATAPOINTS = new ArrayList<>();

  @Tested
  PlotModelScore instance;

  @BeforeAll
  public static void populateDatapoints() {
    AtomicInteger counter = new AtomicInteger(0);
    Arrays.asList(SCORES).forEach((Double s) -> {
      DATAPOINTS.add(new DatapointModelScore(START_DATE.plusDays(counter.getAndAdd(1)), s));
    });
  }

  @Test
  public void testCreateLineChart() {
    BufferedImage bufferedImage = instance.createLineChart(DATAPOINTS);
    Assertions.assertAll("parameters", () -> {
      Assertions.assertEquals(1024, bufferedImage.getWidth());
      Assertions.assertEquals(512, bufferedImage.getHeight());
      Assertions.assertEquals(524288, bufferedImage.getData().getDataBuffer().getSize());
    });
  }

  @Tag("pngfile")
  @Test
  public void testPngFile() throws IOException {
    BufferedImage bufferedImage = instance.createLineChart(DATAPOINTS);
    ImageIO.write(bufferedImage, "png", new File("saved.png"));
  }

}
