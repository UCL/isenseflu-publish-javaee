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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 * CDI bean reaponsible of the generation of the chart image to share in social
 * media.
 * @author David Guzman
 */
@Dependent
public class PlotModelScore {

  /**
   * The width of the chart image in pixels.
   */
  private static final int WIDTH = 1024;

  /**
   * The height of the chart image in pixels.
   */
  private static final int HEIGHT = 512;

  /**
   * The margin to add to the top of the x-axis.
   */
  private static final float XAXIS_MARGIN = 0.1f;

  /**
   * The title of the chart.
   */
  private static final String TITLE =
    "Daily flu (influenza-like illness) rates for England";

  /**
   * The name of the time series.
   */
  private static final String SERIES = "Google v2.018.04/England";

  /**
   * The label in the x-axis.
   */
  private static final String XLABEL = "Date";

  /**
   * The label in the y-axis.
   */
  private static final String YLABEL = "ILI Rate";

  /**
   * The time series for the model data. Expected to be in days.
   */
  private final TimeSeries timeSeries = new TimeSeries(SERIES);

  /**
   * A collection of scores to be used in the time series.
   */
  private final TimeSeriesCollection dataset = new TimeSeriesCollection();

  /**
   * Renderer connecting points with natural cubic splines.
   */
  private final XYItemRenderer renderer = new XYSplineRenderer(6);

  /**
   * Font type to use for the title of the chart.
   */
  private final Font titleFont = new Font("Verdana", Font.TRUETYPE_FONT, 22);

  /**
   * Font type to use for the axes.
   */
  private final Font axisFont = new Font("Verdana", Font.TRUETYPE_FONT, 20);

  /**
   * Font type to use for the tick labels.
   */
  private final Font tickFont = new Font("Verdana", Font.TRUETYPE_FONT, 18);

  /**
   * The colour to use for the chart line. A shade of blue.
   */
  private final Paint lineColour = new Color(51, 102, 204);

  /**
   * The stroke type to use for the chart line. A solid stroke with a width
   * set to 3.
   */
  private final Stroke lineStroke = new BasicStroke(3);

  /**
   * The type of line to use. An elliptical curve with a double precision.
   */
  private final Shape lineShape = new Ellipse2D.Double();

  /**
   * Format to use with the dates in the x-axis.
   */
  private final DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

  /**
   * Configures the renderer with the colour, stroke and shape of the chart
   * line.
   */
  @PostConstruct
  public void init() {
    renderer.setSeriesPaint(0, lineColour);
    renderer.setSeriesStroke(0, lineStroke);
    renderer.setSeriesShape(0, lineShape);
  }

  /**
   * Generates the chart image of a time series (one month of data, resolution
   * set to days.
   * @param datapoints  The data points for a one-month period.
   * @return            The chart image,
   */
  public BufferedImage createLineChart(
    final List<DatapointModelScore> datapoints
  ) {

    timeSeries.clear();
    datapoints.forEach((DatapointModelScore d) -> {
      RegularTimePeriod timePeriod = new Day(
              d.getLocalDate().getDayOfMonth(),
              d.getLocalDate().getMonthValue(),
              d.getLocalDate().getYear());
      timeSeries.add(timePeriod, d.getScoreValue());
    });

    dataset.removeAllSeries();
    dataset.addSeries(timeSeries);

    JFreeChart chart = ChartFactory.createTimeSeriesChart(
      TITLE, XLABEL, YLABEL, dataset
    );

    chart.getTitle().setFont(titleFont);
    chart.setBackgroundPaint(Color.WHITE);

    XYPlot plot = (XYPlot) chart.getPlot();
    plot.setBackgroundPaint(Color.WHITE);
    plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
    plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
    plot.setOutlinePaint(Color.WHITE);
    plot.setRenderer(renderer);

    DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
    dateAxis.setDateFormatOverride(dateFormat);
    dateAxis.setUpperMargin(XAXIS_MARGIN);
    dateAxis.setLabelFont(axisFont);
    dateAxis.setTickLabelFont(tickFont);

    ValueAxis valueAxis = plot.getRangeAxis();
    valueAxis.setLowerBound(0);
    valueAxis.setLabelFont(axisFont);
    valueAxis.setTickLabelFont(tickFont);

    chart.removeLegend();

    return chart.createBufferedImage(WIDTH, HEIGHT);

  }

}
