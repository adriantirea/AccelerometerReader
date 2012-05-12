package com.activities.gui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.model.accelerometer.R;
import com.configurations.ConfigParameters;

public class PersonalInfoActivity extends Activity {

	private static final String TAG = "PersInfo";
	private static Context CONTEXT;

	private EditText massET;
	private EditText heightET;
	private EditText alertET;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.personalinfo);
		CONTEXT = this;
		
		massET = (EditText) findViewById(R.id.edittext1);
		heightET = (EditText) findViewById(R.id.edittext2);
		alertET = (EditText) findViewById(R.id.edittext3);
		
		SharedPreferences settings = getSharedPreferences(ConfigParameters.SETTINGS_KEY, 0);
		float mass = settings.getFloat(ConfigParameters.MASS_KEY, ConfigParameters.MASS_DEFAULT);
		float height = settings.getFloat(ConfigParameters.HEIGHT_KEY, ConfigParameters.HEIGHT_DEFAULT);
		float alertTime = settings.getFloat(ConfigParameters.ALERT_NO_ACTION_KEY, ConfigParameters.ALERT_NO_ACTION_DEFAULT);
		
		massET.setText(mass+"");
		heightET.setText((height * 100) +"");
		alertET.setText(alertTime+"");
	}
	
	public void saveParameters(View view) {
		try {
			float mass = Float.parseFloat(massET.getText().toString());
			float height = Float.parseFloat(heightET.getText().toString());
			float alertTime = Float.parseFloat(alertET.getText().toString());
			
			SharedPreferences settings = getSharedPreferences(ConfigParameters.SETTINGS_KEY, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putFloat(ConfigParameters.MASS_KEY, mass);
			editor.putFloat(ConfigParameters.HEIGHT_KEY, height/100); // from cm to m
			editor.putFloat(ConfigParameters.ALERT_NO_ACTION_KEY, alertTime);

			// Commit the edits!
			editor.commit();
		} catch (NumberFormatException e) {
			Log.e(TAG, e.getMessage());
			Toast.makeText(CONTEXT, "Format of parameters incorect. Must be numbers!", Toast.LENGTH_LONG).show();
		}
		Log.v(TAG,"save params");
	}
	
	public void resetParameters(View view) {
		SharedPreferences settings = getSharedPreferences(ConfigParameters.SETTINGS_KEY, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.clear();
		editor.commit();
		
		massET.setText(ConfigParameters.MASS_DEFAULT + "");
		heightET.setText((ConfigParameters.HEIGHT_DEFAULT * 100) + "");
		alertET.setText(ConfigParameters.ALERT_NO_ACTION_DEFAULT + "");
	}
}
