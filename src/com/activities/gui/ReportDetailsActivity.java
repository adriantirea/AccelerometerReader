package com.activities.gui;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.configurations.ConfigParameters;
import com.model.accelerometer.R;
import com.model.reportProcessing.ReportProcessor;
import com.model.reportProcessing.ReportResult;

public class ReportDetailsActivity extends Activity {

	private static final String TAG = "RpDet";
	private static Context CONTEXT;

	private long startDate;
	private long endDate;
	private String[] classes_piereport;

	private ArrayAdapter<String> durationsAdapter;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.reportdetails);
		CONTEXT = this;

		Bundle extras = getIntent().getExtras();
		startDate = extras.getLong("startDate");
		endDate = extras.getLong("endDate");
		classes_piereport = extras.getStringArray("CLASSES");
	
		
		String[] infoDurations = computeDetails();
		durationsAdapter = new ArrayAdapter<String>(CONTEXT, R.layout.comand_item, infoDurations);

		ListView lv = (ListView)findViewById(R.id.durationslist);
		
		Log.v(TAG,lv + " " + durationsAdapter +" " +  infoDurations.length);
		lv.setAdapter(durationsAdapter);
		lv.setTextFilterEnabled(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		computeDetails();
	}
		
	private String[] computeDetails () { 
		ReportProcessor reportProcessor = new ReportProcessor(this, classes_piereport, ConfigParameters.STEP_TIME);
		ReportResult reportResult = reportProcessor.computeReportResult(startDate, endDate);

		double[] values = reportResult.getValues();
		long[] durations = new long[values.length];
		String[] infosDurations = new String[values.length];
		for(int i = 0; i < values.length; i++) {
			durations[i] = (long)(values[i] * ConfigParameters.STEP_TIME);
			
			String daysNO = "";
			int days = (int) (durations[i] / ConfigParameters.DAY);
			if (days > 0) {
				daysNO = days + "_days_";
			}
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			infosDurations[i] = classes_piereport[i] + "  " + daysNO + dateFormat.format(durations[i]);
			if (i == 0) {
				infosDurations[i] += "  steps_" + reportResult.getStepsRun();
				infosDurations[i] += "  "+ reportResult.getDistanceRun()+"(m)";
				if (durations[0] / (60 * 1000) > 0) {
					infosDurations[i] += "  step frequency "+(int)(reportResult.getStepsRun() / (durations[i] / (60 * 1000)))+"(steps/min)" ;
				}
				infosDurations[i] += "  "+ reportResult.getKCalRun()+"(KCal)";
			}
			if (i == 1) {
				infosDurations[i] += "  steps_" + reportResult.getStepsWalk();
				infosDurations[i] += "  "+ reportResult.getDistanceWalk()+"(m)";
				if (durations[i] / (60 * 1000) > 0) {
					infosDurations[i] += "  step_frequency "+(int)(reportResult.getStepsWalk() / (durations[i] / (60 * 1000)))+"(steps/min)";
				}
				infosDurations[i] += "  "+ reportResult.getKCalWalk()+"(KCal)";
			}
		}
		
		((TextView)findViewById(R.id.piestep)).setText(""+reportResult.getSteps());
		((TextView)findViewById(R.id.piedistance)).setText(""+reportResult.getDistance());
		if ((durations[0] + durations[1]) / (60 * 1000) > 0) {
			((TextView)findViewById(R.id.piefreq)).setText(""+reportResult.getSteps() / ((durations[0] + durations[1]) / (60 * 1000))); // (/minut)
		} else {
			((TextView)findViewById(R.id.piefreq)).setText("0"); 
		}
		((TextView)findViewById(R.id.piekcal)).setText(""+reportResult.getKCal());
		return infosDurations;
	}
}
