package com.activities.gui.charts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.util.MathHelper;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.configurations.ConfigParameters;
import com.model.accelerometer.R;
import com.model.database.ActivityEntry;
import com.model.database.DataSourceActivityEntry;

public class DetailedStatisticActivity extends Activity  {

	private static final int RECENT_PAST = ConfigParameters.RECENT_PAST; // define the most detailed period of activity 

	private static final int STEP = ConfigParameters.GROUPING_STEP;// in milliseconds
	private static final int STEP_BASE = (int)ConfigParameters.STEP_TIME;// in milliseconds

	private static final String[] CLASSES = ConfigParameters.CLASSES;


	public static final String TYPE = "type";

	private XYMultipleSeriesDataset mSeries = new XYMultipleSeriesDataset();

	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

	private GraphicalView mChartView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timelinereport);

		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));
		mRenderer.setChartTitleTextSize(20);
		mRenderer.setLabelsTextSize(15);
		mRenderer.setLegendTextSize(15);
		mRenderer.setMargins(new int[] { 20, 30, 15, 0 });
		mRenderer.setZoomButtonsVisible(true);
		mRenderer.setStartAngle(90);

		DataSourceActivityEntry dataSource = new DataSourceActivityEntry(this);
		dataSource.open();
		List<ActivityEntry> pastActivities = //dataSource.getAllActivities();
			dataSource.getActivitiesBetweenDates(new Date().getTime() - RECENT_PAST, new Date().getTime());
		dataSource.close();
		
		
		String[] titles = CLASSES;
		int points = RECENT_PAST / STEP;
		long now = new Date().getTime();
		// x axes
		List<Date[]> x = new ArrayList<Date[]>();
		Date[] dates = new Date[points];
		for (int j = 0; j < points; j++) {
			dates[j] = new Date(now - (points - j) * STEP);
		}
		for (int i = 0; i < titles.length; i++) {
			x.add(dates);
		}
		// y axes
		List<double[]> values = new ArrayList<double[]>();
		for (int i = 0; i < CLASSES.length; i++) {
			values.add(new double[points]);
		}

		// !! pay atention when fall !!
		int[] histogram = new int[CLASSES.length + 1]; // last class is for absence of activity
		int dbIndex = 0;
		for (int i = 0; i< points; i++) {
			for (int j = 0; j < CLASSES.length+1; j++) {
				histogram[j] = 0;  
			}
			int k = 0;
			while (k < STEP / STEP_BASE) {
				if (dbIndex < pastActivities.size()) {
					if (pastActivities.get(dbIndex).getStartDate() < (now - RECENT_PAST + i * STEP + k * STEP_BASE)) {
						int index = indexOfElementInArray(pastActivities.get(dbIndex).getName(), CLASSES);
						if (index >-1) { 
							histogram[index] ++;
						} 
						dbIndex ++;
					} else {
						histogram[CLASSES.length] ++; // no data for this time
					}
				} else {
					histogram[CLASSES.length] ++; // no data for this time
				}
				k++;
			}

			int maxClass = 0;
			for (int j = 0; j < CLASSES.length+1 ; j++) {
				if (histogram[j] > histogram[maxClass]) {
					maxClass = j;
				}
			}
			for (int j = 0; j < CLASSES.length; j++) {
				values.get(j)[i] = MathHelper.NULL_VALUE;
			}
			if (maxClass < CLASSES.length) {
				values.get(maxClass)[i] = maxClass;
			}
		}
		/////
		int[] colors = ConfigParameters.COLORS;
		PointStyle[] styles = ConfigParameters.STYLES;
		mRenderer = buildRenderer(colors, styles);
		int length = mRenderer.getSeriesRendererCount();
		for (int i = 0; i < length; i++) {
			((XYSeriesRenderer) mRenderer.getSeriesRendererAt(i)).setFillPoints(true);
		}
		setChartSettings(mRenderer, "Activity timeline", "Time", "Activity", 
				x.get(0)[0].getTime(), x.get(0)[points - 1].getTime(), -0.3, 5.3, Color.LTGRAY, Color.LTGRAY);
		mRenderer.setXLabels(10);
		mRenderer.setYLabels(1);

		for (int i = 0; i < CLASSES.length; i++) {
			mRenderer.addYTextLabel(i, CLASSES[i]);
		}

		mRenderer.setShowGrid(true);
		mRenderer.setXLabelsAlign(Align.CENTER);
		mRenderer.setYLabelsAlign(Align.RIGHT);

		mRenderer.setZoomButtonsVisible(true); // adji

		Log.v("ttt", "" + titles.length +" "+ x.size() +" "+ values.size() +" "+x.get(0).length+" "+values.get(1).length+" "+values.get(2).length+" "+values.get(3).length+" "+values.get(4).length);

		mSeries = buildDateDataset(titles, x, values);
	}


	@Override
	protected void onResume() {
		super.onResume();
		if (mChartView == null) {
			LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
			mChartView = ChartFactory.getTimeChartView(this, mSeries, mRenderer,"mm:ss" );
			mRenderer.setClickEnabled(true);
			mRenderer.setSelectableBuffer(10);

			layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT));
		} else {
			mChartView.repaint();
		}
	}


	/**
	 * Builds an XY multiple series renderer.
	 * 
	 * @param colors the series rendering colors
	 * @param styles the series point styles
	 * @return the XY multiple series renderers
	 */
	protected XYMultipleSeriesRenderer buildRenderer(int[] colors, PointStyle[] styles) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		setRenderer(renderer, colors, styles);
		return renderer;
	}

	protected void setRenderer(XYMultipleSeriesRenderer renderer, int[] colors, PointStyle[] styles) {
		renderer.setAxisTitleTextSize(16);
		renderer.setChartTitleTextSize(20);
		renderer.setLabelsTextSize(15);
		renderer.setLegendTextSize(15);
		renderer.setPointSize(5f);
		renderer.setMargins(new int[] { 20, 30, 15, 20 });
		int length = colors.length;
		for (int i = 0; i < length; i++) {
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(colors[i]);
			r.setPointStyle(styles[i]);
			renderer.addSeriesRenderer(r);
		}
	}

	/**
	 * Sets a few of the series renderer settings.
	 * 
	 * @param renderer the renderer to set the properties to
	 * @param title the chart title
	 * @param xTitle the title for the X axis
	 * @param yTitle the title for the Y axis
	 * @param xMin the minimum value on the X axis
	 * @param xMax the maximum value on the X axis
	 * @param yMin the minimum value on the Y axis
	 * @param yMax the maximum value on the Y axis
	 * @param axesColor the axes color
	 * @param labelsColor the labels color
	 */
	protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
			String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor,
			int labelsColor) {
		renderer.setChartTitle(title);
		renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
		renderer.setXAxisMin(xMin);
		renderer.setXAxisMax(xMax);
		renderer.setYAxisMin(yMin);
		renderer.setYAxisMax(yMax);
		renderer.setAxesColor(axesColor);
		renderer.setLabelsColor(labelsColor);
	}

	/**
	 * Builds an XY multiple time dataset using the provided values.
	 * 
	 * @param titles the series titles
	 * @param xValues the values for the X axis
	 * @param yValues the values for the Y axis
	 * @return the XY multiple time dataset
	 */
	protected XYMultipleSeriesDataset buildDateDataset(String[] titles, List<Date[]> xValues,
			List<double[]> yValues) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		int length = titles.length;
		for (int i = 0; i < length; i++) {
			TimeSeries series = new TimeSeries(titles[i]);
			Date[] xV = xValues.get(i);
			double[] yV = yValues.get(i);
			int seriesLength = xV.length;
			for (int k = 0; k < seriesLength; k++) {
				series.add(xV[k], yV[k]);
			}
			dataset.addSeries(series);
		}
		return dataset;
	}

	/** First occurrence of elem in array
	 * 
	 * @param elem
	 * @param array
	 * @return index of elem or -1
	 */
	private int indexOfElementInArray(String elem, String[] array) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(elem)) {
				return i;
			}
		}
		return -1;
	}

}
