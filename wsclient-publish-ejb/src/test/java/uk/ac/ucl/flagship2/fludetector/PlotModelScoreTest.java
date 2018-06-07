package uk.ac.ucl.flagship2.fludetector;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
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
public class PlotModelScoreTest {

  private static final List<DatapointModelScore> DATAPOINTS = new ArrayList<>();

  private static final Double[] SCORES = {
    4.783023d, 4.404949d, 4.097791d, 3.962450d, 4.201026d, 3.561114d, 2.950602d, 2.789060d,
    3.176598d, 3.659054d, 3.161583d, 3.461478d, 3.089276d, 2.926677d, 3.171444d, 2.907163d,
    3.176352d, 2.440929d, 2.913349d, 3.338789d, 2.858327d, 3.071997d, 2.760835d, 2.727647d,
    2.849737d, 3.687457d, 2.918817d, 3.193272d, 3.126184d, 2.892199d
  };

  private static final LocalDate START_DATE = LocalDate.of(2018, 4, 20);

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
      Assertions.assertEquals(800, bufferedImage.getWidth());
      Assertions.assertEquals(400, bufferedImage.getHeight());
      Assertions.assertEquals(320000, bufferedImage.getData().getDataBuffer().getSize());
    });
  }

  @Tag("pngfile")
  @Test
  public void testPngFile() throws IOException {
    BufferedImage bufferedImage = instance.createLineChart(DATAPOINTS);
    ImageIO.write(bufferedImage, "png", new File("saved.png"));
  }

}
