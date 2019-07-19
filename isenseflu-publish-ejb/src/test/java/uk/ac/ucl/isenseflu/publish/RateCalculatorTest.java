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

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static uk.ac.ucl.isenseflu.publish.DataStubs.SCORES;

/**
 *
 * @author David Guzman
 */
public class RateCalculatorTest {

  @Test
  public void testAverageChangeRate() {
    List<Double> scoreList = Arrays.asList(SCORES);

    Assertions.assertEquals(0.25589737315308325d, RateCalculator.averageChangeRate().apply(scoreList));
  }

  @Test
  public void testException() {
    List<Double> scoreList = Arrays.asList(SCORES).subList(0, 14);
    Throwable exception = Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {
      RateCalculator.averageChangeRate().apply(scoreList);
    });
    String expected = "Not enough items in List to perform calculation";
    Assertions.assertEquals(expected, exception.getMessage());
  }

}
