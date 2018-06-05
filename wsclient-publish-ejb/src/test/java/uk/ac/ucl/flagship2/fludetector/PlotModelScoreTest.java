package uk.ac.ucl.flagship2.fludetector;

import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import mockit.Tested;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author David Guzman
 */
public class PlotModelScoreTest {

  @Tested
  PlotModelScore instance;

  @Test
  public void testCreateLineChart() {
    List<DatapointModelScore> datapoints = new ArrayList<>();
    datapoints.add(new DatapointModelScore(LocalDate.parse("2018-05-02"), 3.089276d));
    datapoints.add(new DatapointModelScore(LocalDate.parse("2018-05-03"), 2.926677d));
    datapoints.add(new DatapointModelScore(LocalDate.parse("2018-05-04"), 3.171444d));
    datapoints.add(new DatapointModelScore(LocalDate.parse("2018-05-05"), 2.907163d));
    datapoints.add(new DatapointModelScore(LocalDate.parse("2018-05-06"), 3.176352d));
    datapoints.add(new DatapointModelScore(LocalDate.parse("2018-05-07"), 2.440929d));
    datapoints.add(new DatapointModelScore(LocalDate.parse("2018-05-08"), 2.913349d));
    datapoints.add(new DatapointModelScore(LocalDate.parse("2018-05-09"), 3.338789d));
    datapoints.add(new DatapointModelScore(LocalDate.parse("2018-05-10"), 2.858327d));
    datapoints.add(new DatapointModelScore(LocalDate.parse("2018-05-11"), 3.071997d));
    datapoints.add(new DatapointModelScore(LocalDate.parse("2018-05-12"), 2.760835d));
    datapoints.add(new DatapointModelScore(LocalDate.parse("2018-05-13"), 2.727647d));

    BufferedImage bufferedImage = instance.createLineChart(datapoints);
    Assertions.assertAll("parameters", () -> {
      Assertions.assertEquals(800, bufferedImage.getWidth());
      Assertions.assertEquals(400, bufferedImage.getHeight());
      Assertions.assertEquals(320000, bufferedImage.getData().getDataBuffer().getSize());
    });
  }

}
