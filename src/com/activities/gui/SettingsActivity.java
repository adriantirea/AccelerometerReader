package com.activities.gui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.model.accelerometer.R;

public class SettingsActivity extends Activity {

	@SuppressWarnings("unused")
	private static final String TAG = "Settings";
	private static Context CONTEXT;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.settings);
		CONTEXT = this;
	}

    public void goToContacts(View view) {
    	Intent myIntent = new Intent(CONTEXT, ContactsActivity.class);
    	CONTEXT.startActivity(myIntent);
    }
 
    public void goToSync(View view) {
    	Intent myIntent = new Intent(CONTEXT, SyncActivity.class);
    	CONTEXT.startActivity(myIntent);
    }
    
    public void goToPersonalInfo(View view) {
    	Intent myIntent = new Intent(CONTEXT, PersonalInfoActivity.class);
    	CONTEXT.startActivity(myIntent);
    }
}
