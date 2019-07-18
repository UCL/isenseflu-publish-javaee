/*
 * i-sense flu publish: Module of the i-sense flu application used in the publication of model scores on social media
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

/**
 *
 * @author David Guzman
 */
public abstract class DataStubs {

  protected static final Double[] SCORES = {
    4.783023d, 4.404949d, 4.097791d, 3.962450d, 4.201026d, 3.561114d, 2.950602d, 2.789060d,
    3.176598d, 3.659054d, 3.161583d, 3.461478d, 3.089276d, 2.926677d, 3.171444d, 2.907163d,
    3.176352d, 2.440929d, 2.913349d, 3.338789d, 2.858327d, 3.071997d, 2.760835d, 2.727647d,
    2.849737d, 3.687457d, 2.918817d, 3.193272d, 3.126184d, 2.892199d
  };

  protected static final LocalDate START_DATE = LocalDate.of(2018, 4, 20);

  protected static final String JSON_RESPONSE = "{\"model_data\": [" +
    "{\"id\": 2, " +
    "\"name\": \"Google v2018.04\", " +
    "\"has_confidence_interval\": false, " +
    "\"average_score\": 8.245234945106956, " +
    "\"data_points\": [" +
    "{\"score_date\": \"2019-04-25\", \"score_value\": 5.8970798709844}, " +
    "{\"score_date\": \"2019-04-24\", \"score_value\": 6.12747053709796}, " +
    "{\"score_date\": \"2019-04-23\", \"score_value\": 6.29937691755253}, " +
    "{\"score_date\": \"2019-04-22\", \"score_value\": 6.66115709545793}, " +
    "{\"score_date\": \"2019-04-21\", \"score_value\": 7.08003368491024}, " +
    "{\"score_date\": \"2019-04-20\", \"score_value\": 7.00225285733907}, " +
    "{\"score_date\": \"2019-04-19\", \"score_value\": 7.54885199831107}, " +
    "{\"score_date\": \"2019-04-18\", \"score_value\": 8.06836655804139}, " +
    "{\"score_date\": \"2019-04-17\", \"score_value\": 8.46317798910674}, " +
    "{\"score_date\": \"2019-04-16\", \"score_value\": 9.67384636362554}, " +
    "{\"score_date\": \"2019-04-15\", \"score_value\": 9.74471644899712}, " +
    "{\"score_date\": \"2019-04-14\", \"score_value\": 9.71081369094812}, " +
    "{\"score_date\": \"2019-04-13\", \"score_value\": 9.70716522685077}, " +
    "{\"score_date\": \"2019-04-12\", \"score_value\": 9.92058147695581}, " +
    "{\"score_date\": \"2019-04-11\", \"score_value\": 9.16861542777566}, " +
    "{\"score_date\": \"2019-04-10\", \"score_value\": 9.4720884299224}, " +
    "{\"score_date\": \"2019-04-09\", \"score_value\": 9.85922407345315}, " +
    "{\"score_date\": \"2019-04-08\", \"score_value\": 9.61354702507521}, " +
    "{\"score_date\": \"2019-04-07\", \"score_value\": 9.91776583762237}, " +
    "{\"score_date\": \"2019-04-06\", \"score_value\": 8.74022552859329}, " +
    "{\"score_date\": \"2019-04-05\", \"score_value\": 7.88451158740977}, " +
    "{\"score_date\": \"2019-04-04\", \"score_value\": 8.09881865789366}, " +
    "{\"score_date\": \"2019-04-03\", \"score_value\": 7.93554952892092}, " +
    "{\"score_date\": \"2019-04-02\", \"score_value\": 7.62681247628231}, " +
    "{\"score_date\": \"2019-04-01\", \"score_value\": 7.20968167412652}, " +
    "{\"score_date\": \"2019-03-31\", \"score_value\": 7.22131169534895}, " +
    "{\"score_date\": \"2019-03-30\", \"score_value\": 8.20762591241768}, " +
    "{\"score_date\": \"2019-03-29\", \"score_value\": 8.9142602983544}, " +
    "{\"score_date\": \"2019-03-28\", \"score_value\": 8.32617989987862}, " +
    "{\"score_date\": \"2019-03-27\", \"score_value\": 7.25593958395504}]}], " +
    "\"start_date\": \"2019-03-27\", " +
    "\"end_date\": \"2019-04-25\"}";

}
