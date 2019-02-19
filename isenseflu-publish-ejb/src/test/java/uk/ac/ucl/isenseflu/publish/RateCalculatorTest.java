package uk.ac.ucl.isenseflu.publish;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ucl.isenseflu.publish.RateCalculator;

/**
 *
 * @author David Guzman
 */
public class RateCalculatorTest {

  final Double[] scores = {
    4.783023d, 4.404949d, 4.097791d, 3.962450d, 4.201026d, 3.561114d, 2.950602d, 2.789060d,
    3.176598d, 3.659054d, 3.161583d, 3.461478d, 3.089276d, 2.926677d, 3.171444d, 2.907163d,
    3.176352d, 2.440929d, 2.913349d, 3.338789d, 2.858327d, 3.071997d, 2.760835d, 2.727647d,
    2.849737d, 3.687457d, 2.918817d, 3.193272d, 3.126184d, 2.892199d
  };

  @Test
  public void testAverageChangeRate() {
    List<Double> scoreList = Arrays.asList(scores);

    Double expected = 0.04059881001399882d;

    Assertions.assertAll(
            () -> Assertions.assertEquals(expected, RateCalculator.averageChangeRate.apply(scoreList.subList(0, scoreList.size()))),
            () -> Assertions.assertEquals(expected, RateCalculator.averageChangeRate.apply(scoreList.subList(15, scoreList.size())))
    );
  }

  @Test
  public void testException() {
    List<Double> scoreList = Arrays.asList(scores).subList(0, 14);
    Throwable exception = Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {
      RateCalculator.averageChangeRate.apply(scoreList);
    });
    String expected = "Not enough items in List to perform calculation";
    Assertions.assertEquals(expected, exception.getMessage());
  }

}
