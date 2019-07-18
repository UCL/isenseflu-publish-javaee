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
 *
 * @author David Guzman
 */
@Dependent
public class PlotModelScore {

  private final static int WIDTH = 1024;
  private final static int HEIGHT = 512;
  private final String TITLE = "Daily flu (influenza-like illness) rates for England";
  private final String SERIES = "Google v2.018.04/England";
  private final String XLABEL = "Date";
  private final String YLABEL = "ILI Rate";

  private final TimeSeries timeSeries = new TimeSeries(SERIES);
  private final TimeSeriesCollection dataset = new TimeSeriesCollection();
  private final XYItemRenderer renderer = new XYSplineRenderer(6);

  private final Font titleFont = new Font("Verdana", Font.TRUETYPE_FONT, 22);
  private final Font axisFont = new Font("Verdana", Font.TRUETYPE_FONT, 20);
  private final Font tickFont = new Font("Verdana", Font.TRUETYPE_FONT, 18);

  private final Paint lineColour = new Color(51, 102, 204);
  private final Stroke lineStroke = new BasicStroke(3);
  private final Shape lineShape = new Ellipse2D.Double();
  private final DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

  @PostConstruct
  public void init() {
    renderer.setSeriesPaint(0, lineColour);
    renderer.setSeriesStroke(0, lineStroke);
    renderer.setSeriesShape(0, lineShape);
  }

  public BufferedImage createLineChart(List<DatapointModelScore> datapoints) {

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

    JFreeChart chart = ChartFactory.createTimeSeriesChart(TITLE, XLABEL, YLABEL, dataset);

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
    dateAxis.setUpperMargin(0.1);
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
