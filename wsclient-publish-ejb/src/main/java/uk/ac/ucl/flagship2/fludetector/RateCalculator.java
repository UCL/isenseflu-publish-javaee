package uk.ac.ucl.flagship2.fludetector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 *
 * @author David Guzman
 */
public class RateCalculator {

  public static Function<List<Double>, Double> averageChangeRate = (List<Double> l) -> {
    if (l.size() < 15) {
      throw new IndexOutOfBoundsException("Not enough items in List to perform calculation");
    }

    List<Double> tempList = new ArrayList<>();
    l.forEach(d -> {
      tempList.add(d);
    });

    Collections.reverse(tempList);
    double F1 = tempList.subList(0, 7).stream().mapToDouble(md -> md).average().getAsDouble();
    double F2 = tempList.subList(7, 14).stream().mapToDouble(md -> md).average().getAsDouble();

    return (F1 - F2) / F2;
  };

}
