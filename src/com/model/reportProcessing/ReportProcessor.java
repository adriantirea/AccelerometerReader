package com.model.reportProcessing;

import java.util.Date;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.model.database.ActivityCompressed;
import com.model.database.ActivityEntry;
import com.model.database.ActivityReportEntry;
import com.model.database.DataSourceActivityCompressed;
import com.model.database.DataSourceActivityEntry;
import com.model.database.DataSourceReport;
import com.model.database.DatabaseHelper;

public class ReportProcessor {

	private static final String TAG = "ReportProcessor";
	private Context CONTEXT;
	private String[] classes;
	private long stepTime;
	
	/**
	 * 
	 * @param context
	 * @param classes - Obs. last class in classes is for systemOff value.  
	 * @param stepTime
	 * @throws SQLException
	 */
	public ReportProcessor (Context context, String[] classes, long stepTime) throws SQLException {
		
		CONTEXT = context;
		this.classes = classes;
		this.stepTime = stepTime;
	}
	
	public void closeReport() {
	
	}
	
	public ReportResult computeReportResult(long startDate, long endDate) {
		DatabaseHelper dbHelper = new DatabaseHelper(CONTEXT);;
		SQLiteDatabase database = dbHelper.getWritableDatabase();;
		
		DataSourceActivityEntry dataSource = new DataSourceActivityEntry(CONTEXT);
		dataSource.open();	
		List<ActivityEntry> recentActivities = dataSource.getActivitiesBetweenDates(startDate, endDate);
		dataSource.close();
		
		double[] values = new double[classes.length];
		for (int i =0; i< values.length; i++) {
			values[i] = 0;
		}
		int stepCounterR = 0;
		int stepCounterW = 0;
		double distanceR = 0;
		double distanceW = 0;
		double kCal = 0;
		double kCalR = 0;
		double kCalW = 0;
		
		for (ActivityEntry aentry : recentActivities) {
			// determine the index from class name
			int index = indexOfElementInArray(aentry.getName(), classes);
			if (index > -1) {
				values[index] ++;
			}
			// if walk or run
			if (aentry.getName().equals(classes[0])){
				stepCounterR += aentry.getSteps();
				distanceR += aentry.getDistance();
				kCalR += aentry.getKCal();
			} else if (aentry.getName().equals(classes[1])){
				stepCounterW += aentry.getSteps();
				distanceW += aentry.getDistance();
				kCalW += aentry.getKCal();
			} else {
				kCal += aentry.getKCal();
			}
		}
		
		String t1 = DatabaseHelper.TABLE_ACTIVITY_REPORT;
		String t2 = DatabaseHelper.TABLE_ACTIVITY_COMPRESSED;
		//Cursor cursor = database.rawQuery("Select * from " + t1, null);
		
		String queryString = "SELECT t2.name, t2.value, t2.steps, t2.distance, t2.kcal FROM " + t1 +" t1 INNER JOIN " + t2 + " t2" + 
		" ON t2." + DatabaseHelper.COLUMN3_ID_REPORT + " = t1." + DatabaseHelper.COLUMN2_ID
		+
		" WHERE t1."+ DatabaseHelper.COLUMN2_DATE_START + " >= "+startDate + " and " +
		" t1."+ DatabaseHelper.COLUMN2_DATE_END + " <= " +endDate;
		
		Log.v("sql+ ", queryString);
		Cursor cursor = database.rawQuery( queryString,null);
		
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Log.v(TAG, "in data from the 2 tables");
			int index = indexOfElementInArray(cursor.getString(0), classes);
			values[index] += cursor.getInt(1);
			if (cursor.getString(0).equals(classes[0])) {
				stepCounterR += cursor.getInt(2);
				distanceR += cursor.getFloat(3);
				kCalR += cursor.getFloat(4);
			} else if (cursor.getString(0).equals(classes[1])) {
				stepCounterW += cursor.getInt(2);
				distanceW += cursor.getFloat(3);
				kCalW += cursor.getFloat(4);
			} else {
				kCal += cursor.getInt(4);
			}
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();

		// compute system off value
		if (endDate >= startDate) {
			int totalAction = 0;
			for (int i = 0; i < values.length; i++) {
				totalAction += values[i];
			}
			int totalPossibleAction = (int) ((endDate - startDate) / stepTime); 
			values[values.length-1] = totalPossibleAction - totalAction;
		}
		// end compute system off value

		ReportResult result = new ReportResult();
		result.setStartDate(new Date(startDate));
		result.setEndDate(new Date(endDate));
		result.setValues(values);
		result.setSteps(stepCounterR + stepCounterW);
		result.setStepsRun(stepCounterR );
		result.setStepsWalk(stepCounterW);
		result.setDistance((int)(distanceR + distanceW));
		result.setDistanceRun((int)distanceR);
		result.setDistanceWalk((int)distanceW);
		result.setKCal((int)(kCal + kCalR + kCalW));
		result.setKCalRun((int)kCalR);
		result.setKCalWalk((int)kCalW);
		result.setClasses(classes);
		
		dbHelper.close();
		
		return result;
	}
	
	
	/** Compress all unit action up to specified date into one Report. Obs. last class in classes is for systemOff value.   
	 * 
	 * @param tillDate - date up to which all data are grouped into a single report
	 * @param STEP_TIME - time for a unit of action. Necessary for computing systemOff value.  
	 */
	public void compressDatabaseUpToDate (long tillDate) {
		
		DataSourceActivityEntry dataSource = new DataSourceActivityEntry(CONTEXT);
		dataSource.open();	
		List<ActivityEntry> activitiesTillDate = dataSource.getActivitiesTillDate(tillDate);
		dataSource.deleteActivityTillDate(tillDate);
		dataSource.close();
		
		Log.v(TAG, activitiesTillDate.size()+" compressed");
		
		long startDate = -1;
		if (activitiesTillDate.size() > 0) {
			startDate = activitiesTillDate.get(0).getStartDate();
		}
		int[] values = new int[classes.length];
		int[] stepCounter = new int[classes.length];
		float[] kcal = new float[classes.length];
		float[] distance = new float[classes.length];
		for (int i = 0; i< values.length; i++) {
			values[i] = 0;
			stepCounter[i] = 0;
			kcal[i] = 0;
			distance[i] = 0;
		}
		for (ActivityEntry aentry : activitiesTillDate) {
			// determine the index from class name
			int index = indexOfElementInArray(aentry.getName(), classes);
			if (index > -1) {
				values[index] ++;
				stepCounter[index] += aentry.getSteps();
				kcal[index] += aentry.getKCal();
				distance[index] += aentry.getDistance();
			}
		}
		
		// compute system off value
		int totalAction = 0;
		for (int i = 0; i < values.length; i++) {
			totalAction += values[i];
		}
		int totalPossibleAction = (int) ((tillDate - startDate) / stepTime); 
		values[values.length-1] = totalPossibleAction - totalAction;
		// end compute system off value
		
		if (activitiesTillDate.size() > 0) {
			DataSourceReport reportDataSource = new DataSourceReport(CONTEXT);
			reportDataSource.open();
			ActivityReportEntry activityReport = new ActivityReportEntry();
			activityReport.setStartDate(startDate);
			activityReport.setEndDate(tillDate);
			reportDataSource.insertActivityReport(activityReport);
			reportDataSource.close();
			
			DataSourceActivityCompressed compressedDataSource = new DataSourceActivityCompressed(CONTEXT);
			compressedDataSource.open();
			for (int i = 0; i< values.length; i++) {
				ActivityCompressed activityCompressed = new ActivityCompressed();
				activityCompressed.setName(classes[i]);
				activityCompressed.setValue(values[i]);
				activityCompressed.setSteps(stepCounter[i]);
				activityCompressed.setkCal(kcal[i]);
				activityCompressed.setDistance(distance[i]);
				activityCompressed.setReportID(activityReport.getId());
				compressedDataSource.insertActivityCompressed(activityCompressed);
			}
			
			compressedDataSource.close();
		}
		
		Log.v(TAG, "end compress database till "+ tillDate);
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
