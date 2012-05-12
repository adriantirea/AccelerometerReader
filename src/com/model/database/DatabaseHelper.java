package com.model.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String TABLE_ACTIVITY = "activity";
	public static final String COLUMN1_ID = "id";
	public static final String COLUMN1_NAME = "name";
	public static final String COLUMN1_DATE_START = "startDate";
	public static final String COLUMN1_DURATION = "duration";
	public static final String COLUMN1_STEPS_COUNT = "steps";
	public static final String COLUMN1_DISTANCE = "distance";
	public static final String COLUMN1_KCAL_COUNT = "kcal";
	

	public static final String TABLE_ACTIVITY_REPORT = "activityReport";
	public static final String COLUMN2_ID = "id";
	public static final String COLUMN2_DATE_START = "startDate";
	public static final String COLUMN2_DATE_END = "endDate";

	public static final String TABLE_ACTIVITY_COMPRESSED = "activityCompressed";
	public static final String COLUMN3_ID = "id";
	public static final String COLUMN3_NAME = "name";
	public static final String COLUMN3_VALUE = "value";
	public static final String COLUMN3_STEPS_COUNT = "steps";
	public static final String COLUMN3_DISTANCE = "distance";
	public static final String COLUMN3_KCAL_COUNT = "kcal";
	public static final String COLUMN3_ID_REPORT = "idreport";
	
	
	public static final String TABLE_CONTACTS = "contacts";
	public static final String CONTACTS_ID = "id";
	public static final String CONTACTS_NAME = "name";
	public static final String CONTACTS_PHONE = "phone";
	public static final String CONTACTS_EMAIL = "email";
	
	
	public static final String DATABASE_NAME = "activityRecog.db";
	private static final int DATABASE_VERSION = 6;

	// Database creation sql statement
	private static final String DATABASE_CREATE_TABLE_ACTIVITY = "create table "
		+ TABLE_ACTIVITY + "( " 
		+ COLUMN1_ID + " integer primary key autoincrement, " 
		+ COLUMN1_NAME + " text not null, "
		+ COLUMN1_DATE_START + " text not null, "
		+ COLUMN1_DURATION + " text not null, "
		+ COLUMN1_STEPS_COUNT + " integer, "
		+ COLUMN1_DISTANCE + " float, "
		+ COLUMN1_KCAL_COUNT + " float"
		+");";

	private static final String DATABASE_CREATE_TABLE_ACTIVITY_REPORT = "create table "
		+ TABLE_ACTIVITY_REPORT + "( " 
		+ COLUMN2_ID + " integer primary key autoincrement, " 
		+ COLUMN2_DATE_START + " text not null, "
		+ COLUMN2_DATE_END + " text not null"
		+");";
	
	private static final String DATABASE_CREATE_TABLE_ACTIVITY_COMPRESSED = "create table "
		+ TABLE_ACTIVITY_COMPRESSED + "( " 
		+ COLUMN3_ID + " integer primary key autoincrement, " 
		+ COLUMN3_NAME + " text not null, "
		+ COLUMN3_VALUE + " integer, "
		+ COLUMN3_STEPS_COUNT + " integer, "
		+ COLUMN3_DISTANCE + " float, "
		+ COLUMN3_KCAL_COUNT + " float, "
		+ COLUMN3_ID_REPORT + " integer, "
		+ "FOREIGN KEY (" + COLUMN3_ID_REPORT + ") REFERENCES " + TABLE_ACTIVITY_REPORT + " (" + COLUMN2_ID + ")"
		+");";
	
	
	private static final String DATABASE_CREATE_TABLE_CONTACTS = "create table "
		+ TABLE_CONTACTS + "( " 
		+ CONTACTS_ID + " integer primary key autoincrement, " 
		+ CONTACTS_NAME + " text, "
		+ CONTACTS_PHONE + " text, "
		+ CONTACTS_EMAIL + " text "
		+");";
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		Log.v("sql","onCreate");
		database.execSQL(DATABASE_CREATE_TABLE_ACTIVITY);
		database.execSQL(DATABASE_CREATE_TABLE_ACTIVITY_REPORT);
		database.execSQL(DATABASE_CREATE_TABLE_ACTIVITY_COMPRESSED);
		
		database.execSQL(DATABASE_CREATE_TABLE_CONTACTS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DatabaseHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITY);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITY_REPORT);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITY_COMPRESSED);
		
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
		onCreate(db);
	}
}
