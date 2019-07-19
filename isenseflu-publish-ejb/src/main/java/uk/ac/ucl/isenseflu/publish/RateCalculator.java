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
 *
 * @author David Guzman
 */
final class RateCalculator {

  private RateCalculator() { }

  static Function<List<Double>, Double> averageChangeRate = (List<Double> l) -> {
    if (l.size() < 15) {
      throw new IndexOutOfBoundsException("Not enough items in List to perform calculation");
    }

    double F1 = l.subList(0, 7).stream().collect(Collectors.averagingDouble(x -> x));
    double F2 = l.subList(7, 14).stream().collect(Collectors.averagingDouble(x -> x));

    return (F1 - F2) / F2;
  };

}
