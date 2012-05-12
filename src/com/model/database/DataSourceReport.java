package com.model.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


public class DataSourceReport {
	// Database fields
	private SQLiteDatabase database;
	private DatabaseHelper dbHelper;
	private String[] allColumns = { DatabaseHelper.COLUMN2_ID,
									DatabaseHelper.COLUMN2_DATE_START,
									DatabaseHelper.COLUMN2_DATE_END};

	public DataSourceReport(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public ActivityReportEntry insertActivityReport(ActivityReportEntry activityReport) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN2_DATE_START, activityReport.getStartDate());
		values.put(DatabaseHelper.COLUMN2_DATE_END, activityReport.getEndDate());
		
		long insertId = database.insert(DatabaseHelper.TABLE_ACTIVITY_REPORT, 
				null,
				values);
		activityReport.setId(insertId);
		
		Log.v("reprt s", "insert report "+ activityReport);
		return activityReport;
	}

	public void deleteActivityReport(ActivityReportEntry activityReport) {
		long id = activityReport.getId();
		System.out.println("Report deleted with id: " + id);
		database.delete(DatabaseHelper.TABLE_ACTIVITY_REPORT, DatabaseHelper.COLUMN2_ID
				+ " = " + id, null);
	}

	public List<ActivityReportEntry> getAllActivityReports() {
		List<ActivityReportEntry> activities = new ArrayList<ActivityReportEntry>();

		Cursor cursor = database.query(DatabaseHelper.TABLE_ACTIVITY_REPORT,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			ActivityReportEntry reportEntry = cursorToActivityReportEntry(cursor);
			activities.add(reportEntry);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return activities;
	}

	public List<ActivityReportEntry> getActivityReportsBetweenDates(long startDate, long endDate) {
		List<ActivityReportEntry> activities = new ArrayList<ActivityReportEntry>();
		if (startDate <= endDate) {
			String whereClause = DatabaseHelper.COLUMN2_DATE_START + "> ? and "+ DatabaseHelper.COLUMN2_DATE_START +"< ?";
			Cursor cursor = database.query(DatabaseHelper.TABLE_ACTIVITY_REPORT,
					allColumns, whereClause, new String[]{startDate+"", endDate+""}, null, null, null);
//database.
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				ActivityReportEntry reportEntry = cursorToActivityReportEntry(cursor);
				activities.add(reportEntry);
				cursor.moveToNext();
			}
			// Make sure to close the cursor
			cursor.close();
		}
		return activities;
	}
	
	private ActivityReportEntry cursorToActivityReportEntry(Cursor cursor) {
		ActivityReportEntry activity = new ActivityReportEntry();
		activity.setId(cursor.getLong(0));
		activity.setStartDate(cursor.getLong(1));
		activity.setEndDate(cursor.getLong(2));
		return activity;
	}
}
