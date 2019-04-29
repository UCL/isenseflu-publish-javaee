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

    Assertions.assertEquals(0.25589737315308325d, RateCalculator.averageChangeRate.apply(scoreList));
  }

  @Test
  public void testException() {
    List<Double> scoreList = Arrays.asList(SCORES).subList(0, 14);
    Throwable exception = Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {
      RateCalculator.averageChangeRate.apply(scoreList);
    });
    String expected = "Not enough items in List to perform calculation";
    Assertions.assertEquals(expected, exception.getMessage());
  }

}
