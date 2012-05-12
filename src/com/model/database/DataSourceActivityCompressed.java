package com.model.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


public class DataSourceActivityCompressed {
	// Database fields
	private SQLiteDatabase database;
	private DatabaseHelper dbHelper;
	private String[] allColumns = { DatabaseHelper.COLUMN3_ID,
									DatabaseHelper.COLUMN3_NAME,
									DatabaseHelper.COLUMN3_VALUE,
									DatabaseHelper.COLUMN3_STEPS_COUNT,
									DatabaseHelper.COLUMN3_DISTANCE,
									DatabaseHelper.COLUMN3_KCAL_COUNT,
									DatabaseHelper.COLUMN3_ID_REPORT};

	public DataSourceActivityCompressed(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public ActivityCompressed insertActivityCompressed(ActivityCompressed activityCompressed) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN3_NAME, activityCompressed.getName());
		values.put(DatabaseHelper.COLUMN3_VALUE, activityCompressed.getValue());
		values.put(DatabaseHelper.COLUMN3_STEPS_COUNT, activityCompressed.getSteps());
		values.put(DatabaseHelper.COLUMN3_DISTANCE, activityCompressed.getDistance());
		values.put(DatabaseHelper.COLUMN3_KCAL_COUNT, activityCompressed.getKCal());
		values.put(DatabaseHelper.COLUMN3_ID_REPORT, activityCompressed.getReportIdD());
		
		long insertId = database.insert(DatabaseHelper.TABLE_ACTIVITY_COMPRESSED, 
				null,
				values);
		activityCompressed.setId(insertId);
		
		Log.v("act compress", "insert compress "+ activityCompressed);
		return activityCompressed;
	}

	public void deleteActivityCompressed(ActivityCompressed activityCompressed) {
		long id = activityCompressed.getId();
		System.out.println("ActivityCompressed deleted with id: " + id);
		database.delete(DatabaseHelper.TABLE_ACTIVITY_COMPRESSED, DatabaseHelper.COLUMN3_ID
				+ " = " + id, null);
	}

	public List<ActivityCompressed> getAllActivities() {
		List<ActivityCompressed> activities = new ArrayList<ActivityCompressed>();

		Cursor cursor = database.query(DatabaseHelper.TABLE_ACTIVITY_COMPRESSED,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			ActivityCompressed activityCompressed = cursorToActivityCompressed(cursor);
			activities.add(activityCompressed);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return activities;
	}

	public List<ActivityCompressed> getAllActivitiesForReportID(long reportID) {
		List<ActivityCompressed> activities = new ArrayList<ActivityCompressed>();

		Cursor cursor = database.query(DatabaseHelper.TABLE_ACTIVITY_COMPRESSED,
				allColumns, DatabaseHelper.COLUMN3_ID_REPORT + "=?", new String[]{reportID+""}
				, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			ActivityCompressed activityCompressed = cursorToActivityCompressed(cursor);
			activities.add(activityCompressed);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return activities;
	}
	
	private ActivityCompressed cursorToActivityCompressed(Cursor cursor) {
		ActivityCompressed activity = new ActivityCompressed();
		activity.setId(cursor.getLong(0));
		activity.setName(cursor.getString(1));
		activity.setValue(cursor.getInt(2));
		activity.setSteps(cursor.getInt(3));
		activity.setDistance(cursor.getFloat(4));
		activity.setkCal(cursor.getFloat(5));
		return activity;
	}
}
