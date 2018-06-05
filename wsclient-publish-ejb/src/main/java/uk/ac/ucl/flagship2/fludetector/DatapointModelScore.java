/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ucl.flagship2.fludetector;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

/**
 *
 * @author David Guzman
 */
public class DatapointModelScore {

  private final LocalDate date;
  private final Double scoreValue;

  public DatapointModelScore(LocalDate date, Double scoreValue) {
    this.date = date;
    this.scoreValue = scoreValue;
  }

  public String getDate() {
    return date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
  }

  public LocalDate getLocalDate() {
    return date;
  }

  public Double getScoreValue() {
    return scoreValue;
  }

}
