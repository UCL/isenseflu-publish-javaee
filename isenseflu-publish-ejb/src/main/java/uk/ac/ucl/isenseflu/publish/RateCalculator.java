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

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility functions to perform calculations using flu scores.
 * @author David Guzman
 */
final class RateCalculator {

  private RateCalculator() { }

  /**
   * Number of scores + 1, required to perform the calculation.
   */
  private static final int MINIMUM_SIZE_OF_SCORES_LIST = 15;

  /**
   * Size of the window used to calculate the change rate.
   */
  private static final int AVG_WINDOW_SIZE = 7;

  /**
   * Function definition for calculating the average change rate of scores,
   * comparing the last 7 scores against the previous 7 scores.
   */
  private static final Function<List<Double>, Double> FUNCTION_AVG_CHANGE_RATE =
    (List<Double> l) -> {
      if (l.size() < MINIMUM_SIZE_OF_SCORES_LIST) {
        throw new IndexOutOfBoundsException(
          "Not enough items in List to perform calculation"
        );
      }

      double f1 = l.subList(0, AVG_WINDOW_SIZE).stream()
        .collect(Collectors.averagingDouble(x -> x));
      double f2 = l.subList(AVG_WINDOW_SIZE, AVG_WINDOW_SIZE * 2).stream()
        .collect(Collectors.averagingDouble(x -> x));

      return (f1 - f2) / f2;
    };

  static Function<List<Double>, Double> averageChangeRate() {
    return FUNCTION_AVG_CHANGE_RATE;
  }

}
