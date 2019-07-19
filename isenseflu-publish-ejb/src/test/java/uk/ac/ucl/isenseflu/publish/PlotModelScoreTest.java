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
