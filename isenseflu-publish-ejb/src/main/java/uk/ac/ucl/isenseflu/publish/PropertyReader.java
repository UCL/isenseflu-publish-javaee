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

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;
import java.util.Properties;

/**
 * Utility methods to used for passing or reading configuration properties.
 * @author David Guzman
 */
final class PropertyReader {

  private PropertyReader() { }

  /**
   * Obtains the value for a property key, first from the list of system
   * properties, if it can be found then it attempts to find the property
   * from the environment.
   * @param key The name of the property.
   * @return    The value of the property.
   */
  static String getFromSystemOrEnv(final String key) {
    String value = System.getProperty(key, "");
    if (value.isEmpty()) {
      value = System.getenv(key);
    }
    return (value != null) ? value : "";
  }

  /**
   * Obtains the value for a property key, first from the list of system
   * properties, if it can be found then it attempts to find the property
   * from the environment. If both fail, then it returns the default value
   * provided.
   * @param key           The name of the property.
   * @param defaultValue  The default value for the property.
   * @return              The value of the property.
   */
  static String getFromSystemOrEnvOrElse(
    final String key, final String defaultValue
  ) {
    String value = System.getProperty(key, "");
    if (value.isEmpty()) {
      value = System.getenv(key);
    }
    return (value != null) ? value : defaultValue;
  }

  /**
   * Reads a set of properties from a String.
   * @param propstr The string containing the properties.
   * @return        An Optional with the properties (if found).
   */
  static Optional<Properties> readProperties(final String propstr) {
    final Properties p = new Properties();
    try {
      p.load(new StringReader(propstr));
    } catch (IOException ex) {
      return Optional.empty();
    }
    return Optional.of(p);
  }

}
