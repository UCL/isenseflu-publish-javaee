/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ucl.isenseflu.publish;

import java.time.LocalDate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ucl.isenseflu.publish.DatapointModelScore;

/**
 *
 * @author David Guzman
 */
public class DatapointModelScoreTest {

  @Test
  public void testGetDate() {
    DatapointModelScore datapoint = new DatapointModelScore(LocalDate.parse("2015-02-02"), 1.001);
    Assertions.assertEquals("02-Feb-2015", datapoint.getDate());
  }

}
