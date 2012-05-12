package com.activities.gui.charts;

import java.util.Date;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.activities.gui.ReportDetailsActivity;
import com.configurations.ConfigParameters;
import com.model.accelerometer.R;
import com.model.reportProcessing.ReportProcessor;
import com.model.reportProcessing.ReportResult;

public class PieReportActivity extends Activity { 

	private static final int DAY = ConfigParameters.DAY;

	private static final String[] CLASSES_PIEREPORT = ConfigParameters.CLASSES_PIEREPORT;

	public static final String TYPE = "type";

	private CategorySeries mSeries = new CategorySeries("");

	private DefaultRenderer mRenderer = new DefaultRenderer();

	private GraphicalView mChartView;
	
	private int[] colors; // colors for mRenderer
	private long startDate;
	private long endDate;
	private boolean hideSystemOff;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.piereport);
		
		Spinner spinner = (Spinner) findViewById(R.id.spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.pieReport_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
		
		// create renderer of pieReport
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));
		mRenderer.setChartTitleTextSize(20);
		mRenderer.setLabelsTextSize(15);
		mRenderer.setLegendTextSize(15);
		mRenderer.setMargins(new int[] { 20, 30, 15, 0 });
		mRenderer.setZoomButtonsVisible(true);
		mRenderer.setStartAngle(0);

		colors = ConfigParameters.PIE_COLORS;
		
		for (int i = 0; i < colors.length; i++) {
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setColor(colors[i]);
			mRenderer.addSeriesRenderer(r);
		}
		
		if (savedInstanceState != null) {
			hideSystemOff = savedInstanceState.getBoolean("hideSystemOff");
			int spinnerPossition = savedInstanceState.getInt("MyInt");
			Spinner s = (Spinner) findViewById(R.id.spinner);
			s.setSelection(spinnerPossition);
		} else {
			hideSystemOff = false;
			Spinner s = (Spinner) findViewById(R.id.spinner);
			s.setSelection(0);
		}
		Log.v("PieReport", "onCreate "+hideSystemOff + endDate);
		//mSeries = buildCategoryDataset("PieReport", values, titles);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	  // Save UI state changes to the savedInstanceState.
	  // This bundle will be passed to onCreate if the process is
	  // killed and restarted.
	  savedInstanceState.putBoolean("hideSystemOff", hideSystemOff);
	  
	  Spinner s = (Spinner) findViewById(R.id.spinner);
	  savedInstanceState.putInt("spinnerPossition", s.getSelectedItemPosition());
	  
	  super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	  super.onRestoreInstanceState(savedInstanceState);
	  // Restore UI state from the savedInstanceState.
	  // This bundle has also been passed to onCreate.
	  
	}

	private void repaintPieReport () {

		// adjust SimpleSeriesRenderer
		if (hideSystemOff) {
			if (mRenderer.getSeriesRendererCount() != colors.length - 1) {
				SimpleSeriesRenderer renderer =  mRenderer.getSeriesRendererAt(mRenderer.getSeriesRendererCount() - 1);
				mRenderer.removeSeriesRenderer(renderer);
			}
		} else {
			if (mRenderer.getSeriesRendererCount() != colors.length) {
				SimpleSeriesRenderer r = new SimpleSeriesRenderer();
				r.setColor(colors[mRenderer.getSeriesRendererCount()]); // last color
				mRenderer.addSeriesRenderer(r);
			}
		}
		if (mChartView == null) {
			// add chart to layout
			LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
			mChartView = ChartFactory.getPieChartView(this, mSeries, mRenderer);
			mRenderer.setClickEnabled(true);
			mRenderer.setSelectableBuffer(10);
		
			layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT));
		} else {
			
			Log.v("PieReport", "repaint in else "+mRenderer.getSeriesRendererCount());
			mChartView.repaint();
		}
	}
	
	/** Take data from database starting with 'startDate' and computes values to be displayed on chart.
	 *  Build 'mSeries' to be displayed on chart.
	 * 
	 * @param startDate
	 */
	private void createValuesFromPeriod(long startDate, long endDate) {
		
		ReportProcessor reportProcessor = new ReportProcessor(this, CLASSES_PIEREPORT, ConfigParameters.STEP_TIME);
		ReportResult reportResult = reportProcessor.computeReportResult(startDate, endDate);
		
		((TextView)findViewById(R.id.piestep)).setText(""+reportResult.getSteps());
		
		String[] classes = CLASSES_PIEREPORT;
		double[] values = reportResult.getValues();
		if (hideSystemOff) {
			if (CLASSES_PIEREPORT.length-1 >= 0) {
				classes = new String[CLASSES_PIEREPORT.length-1];
				values = new double[CLASSES_PIEREPORT.length-1];
				for (int i = 0; i < CLASSES_PIEREPORT.length-1; i++) {
					classes[i] = CLASSES_PIEREPORT[i];
					values[i] = reportResult.getValues()[i];
				}
			}
		}
		Log.v("PieReport", hideSystemOff +"    "+classes.length + "  "+ values.length + " "+ mRenderer.getSeriesRendererCount());
		buildCategoryDataset(mSeries, "PieReport", values, classes);
	}
	
	/**
	 * Builds a category series using the provided values.
	 * 
	 * @param titles the series titles
	 * @param values the values
	 * @return the category series
	 */
	protected void buildCategoryDataset(CategorySeries categorySeries, String title, double[] values, String[] titles) {
		if (categorySeries == null) {
			categorySeries = new CategorySeries(title);
		} else {
			categorySeries.clear();
		}
		for (int i = 0; i < values.length; i++) {
			categorySeries.add(titles[i], values[i]);
		}
	}
	
	  /** First occurrence of elem in array
	   * 
	   * @param elem
	   * @param array
	   * @return index of elem or -1
	   */
	  @SuppressWarnings("unused")
	private int indexOfElementInArray(String elem, String[] array) {
		  for (int i = 0; i < array.length; i++) {
			  if (array[i].equals(elem)) {
				  return i;
			  }
		  }
		  return -1;
	  }
	  
	  
	  /** Listener for Spinner 
	   * 
	   * @author adji
	   *
	   */
	  public class MyOnItemSelectedListener implements OnItemSelectedListener {

		  public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			  
			  Toast.makeText(parent.getContext(), "Pie report period" +
					  parent.getItemAtPosition(pos).toString(), Toast.LENGTH_SHORT).show();
			  startDate = new Date().getTime();
			  endDate = new Date().getTime();
			  long timeOffset = new Date().getTimezoneOffset() * 60000L; // minutes transformed in milliseconds
			  
			  //new Date((timp - (new Date().getTimezoneOffset()) *60000L)/ DAY * DAY + new Date().getTimezoneOffset() *60000L)
			  
			  switch (pos) {
			  case 0: // today
				  endDate = new Date().getTime();
				  startDate = (endDate - timeOffset) / DAY * DAY + timeOffset;
				  break;
				  
			  case 1: // yesterday
				  endDate = (new Date().getTime() - timeOffset) / DAY * DAY + timeOffset;
				  startDate = endDate - DAY ;
				  break;
				  
			  case 2: // last week == last 7 days
				  endDate = new Date().getTime();
				  startDate = endDate - (7L * DAY);
				  break; 
				   
			  case 3: // last month == last 30 days
				  endDate = new Date().getTime();
				  startDate = endDate - (30L * DAY);
				  break;
			  case 4: // previous month
				  endDate = new Date().getTime() - (30L * DAY) ;
				  startDate = endDate - (30L * DAY);
				  break;  
			  case 5: // last year
				  endDate = new Date().getTime();
				  startDate = endDate - (365L * DAY);
				  break;  
				  
			  default:
				  break;
			  }
			  
			  PieReportActivity.this.createValuesFromPeriod(startDate, endDate);
			  PieReportActivity.this.repaintPieReport();
		  }

		    public void onNothingSelected(AdapterView<?> parent) {
		      // Do nothing.
		    }
		}
	  
	    public void onCheckboxClicked(View view) {
	    	
	    	hideSystemOff = ((CheckBox)view).isChecked();
	    	
	    	PieReportActivity.this.createValuesFromPeriod(startDate, endDate);
			PieReportActivity.this.repaintPieReport();
	    }
	    
	    public void goToDetails(View view) {
	    	Intent myIntent = new Intent(PieReportActivity.this, ReportDetailsActivity.class);
	    	myIntent.putExtra("startDate", startDate);
	    	myIntent.putExtra("endDate", endDate);
	    	myIntent.putExtra("CLASSES", CLASSES_PIEREPORT);
	    	PieReportActivity.this.startActivity(myIntent);
	    }
}
