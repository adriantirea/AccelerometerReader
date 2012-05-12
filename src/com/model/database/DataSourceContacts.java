package com.model.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


public class DataSourceContacts {

	// Database fields
	private SQLiteDatabase database;
	private DatabaseHelper dbHelper;
	private String[] allColumns = { DatabaseHelper.CONTACTS_ID,
									DatabaseHelper.CONTACTS_NAME,
									DatabaseHelper.CONTACTS_PHONE,
									DatabaseHelper.CONTACTS_EMAIL};

	public DataSourceContacts(Context context) {
		
		dbHelper = new DatabaseHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public ContactEntry insertContact(ContactEntry contact) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.CONTACTS_NAME, contact.getName());
		values.put(DatabaseHelper.CONTACTS_PHONE, contact.getPhone());
		values.put(DatabaseHelper.CONTACTS_EMAIL, contact.getEmail());
		
		long insertId = database.insert(DatabaseHelper.TABLE_CONTACTS, 
				null,
				values);
		contact.setId(insertId);
		return contact;
	}

	public void deleteContact(ContactEntry contact) {
		long id = contact.getId();
		System.out.println("Contact deleted with id: " + id);
		database.delete(DatabaseHelper.TABLE_CONTACTS, DatabaseHelper.CONTACTS_ID
				+ " = " + id, null);
	}

	public List<ContactEntry> getAllContacts() {
		List<ContactEntry> activities = new ArrayList<ContactEntry>();

		Cursor cursor = database.query(DatabaseHelper.TABLE_CONTACTS,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			ContactEntry contact = cursorToContactEntry(cursor);
			activities.add(contact);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return activities;
	}

	private ContactEntry cursorToContactEntry(Cursor cursor) {
		ContactEntry contact = new ContactEntry();
		contact.setId(cursor.getLong(0));
		contact.setName(cursor.getString(1));
		contact.setPhone(cursor.getString(2));
		contact.setEmail(cursor.getString(3));
		return contact;
	}
}

