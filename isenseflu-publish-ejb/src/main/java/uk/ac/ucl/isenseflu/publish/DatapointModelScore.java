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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

/**
 * Represents the coordinates of a point in the time series of flu scores.
 * @author David Guzman
 */
public class DatapointModelScore {

  /**
   * The date of the score.
   */
  private final LocalDate scoreDate;

  /**
   * The value of the score.
   */
  private final Double scoreValue;

  /**
   * Creates a data point in the time series.
   * @param date The date of the score.
   * @param value The value of the score.
   */
  public DatapointModelScore(final LocalDate date, final Double value) {
    this.scoreDate = date;
    this.scoreValue = value;
  }

  /**
   * Converts and formats the date of the score to String before returning it.
   * It uses a medium text style (for example 10 Jan, 2019)
   * @return The date of the score as a formatted String.
   */
  public String getDate() {
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter
      .ofLocalizedDate(FormatStyle.MEDIUM);
    return scoreDate.format(dateTimeFormatter);
  }

  /**
   * Returns the date of the score for this data point with no transformation.
   * @return The date of the score as a LocalDate.
   */
  public LocalDate getLocalDate() {
    return scoreDate;
  }

  /**
   * Returns the value of the score for this data point.
   * @return The value of the score.
   */
  public Double getScoreValue() {
    return scoreValue;
  }

}
