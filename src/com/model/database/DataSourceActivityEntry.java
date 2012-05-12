package com.model.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


public class DataSourceActivityEntry {
	// Database fields
	private SQLiteDatabase database;
	private DatabaseHelper dbHelper;
	private String[] allColumns = { DatabaseHelper.COLUMN1_ID,
									DatabaseHelper.COLUMN1_NAME,
									DatabaseHelper.COLUMN1_DATE_START,
									DatabaseHelper.COLUMN1_DURATION,
									DatabaseHelper.COLUMN1_STEPS_COUNT,
									DatabaseHelper.COLUMN1_DISTANCE,
									DatabaseHelper.COLUMN1_KCAL_COUNT};

	public DataSourceActivityEntry(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public ActivityEntry insertActivityEntry(ActivityEntry activityEntry) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN1_NAME, activityEntry.getName());
		values.put(DatabaseHelper.COLUMN1_DATE_START, activityEntry.getStartDate());
		values.put(DatabaseHelper.COLUMN1_DURATION, activityEntry.getDuration());
		values.put(DatabaseHelper.COLUMN1_STEPS_COUNT, activityEntry.getSteps());
		values.put(DatabaseHelper.COLUMN1_DISTANCE, activityEntry.getDistance());
		values.put(DatabaseHelper.COLUMN1_KCAL_COUNT, activityEntry.getKCal());
		
		long insertId = database.insert(DatabaseHelper.TABLE_ACTIVITY, 
				null,
				values);
		activityEntry.setId(insertId);
		Log.v("actE","insert ActivityEntry");
		return activityEntry;
	}

	public void deleteActivityEntry(ActivityEntry activityEntry) {
		long id = activityEntry.getId();
		System.out.println("activityEntry deleted with id: " + id);
		database.delete(DatabaseHelper.TABLE_ACTIVITY, DatabaseHelper.COLUMN1_ID
				+ " = " + id, null);
	}

	/**
	 * delete all ActivityEntryes with start date less than  tillDate.
	 * @param tillDate
	 */
	public void deleteActivityTillDate(long tillDate) {
		System.out.println("activityEntry deleted till date: " + tillDate);
		database.delete(DatabaseHelper.TABLE_ACTIVITY, DatabaseHelper.COLUMN1_DATE_START
				+ " < " + tillDate, null);
	}

	public List<ActivityEntry> getAllActivities() {
		List<ActivityEntry> activities = new ArrayList<ActivityEntry>();

		Cursor cursor = database.query(DatabaseHelper.TABLE_ACTIVITY,
				allColumns, null, null, null, null, DatabaseHelper.COLUMN2_DATE_START);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			ActivityEntry activityEntry = cursorToActivityEntry(cursor);
			activities.add(activityEntry);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return activities;
	}

	public List<ActivityEntry> getActivitiesBetweenDates(long startDate, long endDate) {
		List<ActivityEntry> activities = new ArrayList<ActivityEntry>();
		if (startDate <= endDate) {
			String whereClause = DatabaseHelper.COLUMN1_DATE_START + "> ? and "+ DatabaseHelper.COLUMN1_DATE_START +"< ?";
			Cursor cursor = database.query(DatabaseHelper.TABLE_ACTIVITY,
					allColumns, whereClause, new String[]{startDate+"", endDate+""}, null, null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				ActivityEntry activityEntry = cursorToActivityEntry(cursor);
				activities.add(activityEntry);
				cursor.moveToNext();
			}
			// Make sure to close the cursor
			cursor.close();
		}
		return activities;
	}
	
	public List<ActivityEntry> getActivitiesTillDate(long tillDate) {
		List<ActivityEntry> activities = new ArrayList<ActivityEntry>();
		String whereClause = DatabaseHelper.COLUMN1_DATE_START +"< ?";
		Cursor cursor = database.query(DatabaseHelper.TABLE_ACTIVITY,
				allColumns, whereClause, new String[]{tillDate+""}, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			ActivityEntry activityEntry = cursorToActivityEntry(cursor);
			activities.add(activityEntry);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return activities;
	}
	
	private ActivityEntry cursorToActivityEntry(Cursor cursor) {
		ActivityEntry activity = new ActivityEntry();
		activity.setId(cursor.getLong(0));
		activity.setName(cursor.getString(1));
		activity.setStartDate(cursor.getLong(2));
		activity.setDuration(cursor.getLong(3));
		activity.setSteps(cursor.getInt(4));
		activity.setDistance(cursor.getFloat(5));
		activity.setkCal(cursor.getFloat(6));
		
		return activity;
	}
}
