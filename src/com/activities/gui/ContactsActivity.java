package com.activities.gui;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.model.accelerometer.R;
import com.model.database.ContactEntry;
import com.model.database.DataSourceContacts;
import com.utils.SmsSender;

public class ContactsActivity extends Activity {

	private static Context CONTEXT;
	private ArrayAdapter<ContactEntry> contactAdapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.contacts);
		CONTEXT = this;
		

		DataSourceContacts dataSource = new DataSourceContacts(CONTEXT);
		dataSource.open();
		List<ContactEntry> contacts = dataSource.getAllContacts();
		dataSource.close();

		contactAdapter = new ArrayAdapter<ContactEntry>(CONTEXT, R.layout.comand_item, contacts);
		//Collections.reverse(pastActivities); 
		if (contacts.isEmpty()) { //  if no past events 
			Toast.makeText(getApplicationContext(), "No contacts!", Toast.LENGTH_LONG).show();
		}

		ListView lv = (ListView)findViewById(R.id.contactlist);
		lv.setAdapter(contactAdapter);
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {

				AlertDialog.Builder alert = new AlertDialog.Builder(CONTEXT);
				alert.setTitle("Remove contact.");
				alert.setMessage("Do you want to remove emergency contact?");

				alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						DataSourceContacts dataSource = new DataSourceContacts(CONTEXT);
						dataSource.open();
						ContactEntry contactToDelete = contactAdapter.getItem(position);
						dataSource.deleteContact(contactToDelete);
						dataSource.close();

						contactAdapter.remove(contactToDelete);
						contactAdapter.setNotifyOnChange(true);
					}
				});

				alert.setNeutralButton("Test SMS", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// -- test sms sender
						try {
							SmsSender.sendSmsToContact("Salut accRec"+contactAdapter.getItem(position).getPhone(),
									contactAdapter.getItem(position).getPhone(),
									CONTEXT);
						} catch (Exception e) {
							Toast.makeText(getApplicationContext(), "Sms not send: "+e.getMessage(), Toast.LENGTH_SHORT).show();
							Log.e("sms", e.getMessage() + contactAdapter.getItem(position).getPhone());
						}
						// -- end test
					}
				});
				
				alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						return; // do nothing
					}
				});

				alert.show();
			}
		});
	}

	protected void onResume() {
		super.onResume();
	}

	protected void onDestroy() {
		super.onDestroy();
	}

	public void addEmergencyContact (View view) {

		EditText nameEditText = (EditText) findViewById(R.id.edittext1);
		EditText phoneEditText = (EditText) findViewById(R.id.edittext2);
		EditText emailEditText = (EditText) findViewById(R.id.edittext3);

		DataSourceContacts dataSource = new DataSourceContacts(CONTEXT);
		dataSource.open();
		ContactEntry contact = new ContactEntry();
		contact.setName(nameEditText.getText().toString());
		contact.setPhone(phoneEditText.getText().toString());
		contact.setEmail(emailEditText.getText().toString());
		contact = dataSource.insertContact(contact);
		dataSource.close();

		contactAdapter.add(contact);
		contactAdapter.setNotifyOnChange(true);


		Toast.makeText(getApplicationContext(), "Contact added." + phoneEditText.getText(), Toast.LENGTH_LONG).show();
	}
}
