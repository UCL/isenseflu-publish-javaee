package uk.ac.ucl.isenseflu.publish;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author David Guzman
 */
public class RateCalculator {

  public static Function<List<Double>, Double> averageChangeRate = (List<Double> l) -> {
    if (l.size() < 15) {
      throw new IndexOutOfBoundsException("Not enough items in List to perform calculation");
    }

    double F1 = l.subList(0, 7).stream().collect(Collectors.averagingDouble(x -> x));
    double F2 = l.subList(7, 14).stream().collect(Collectors.averagingDouble(x -> x));

    return (F1 - F2) / F2;
  };

}
