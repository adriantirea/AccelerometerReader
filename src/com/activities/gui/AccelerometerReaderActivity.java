 package com.activities.gui;

import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.activities.gui.charts.DetailedStatisticActivity;
import com.activities.gui.charts.PieReportActivity;
import com.model.accelerometer.R;
import com.model.database.ContactEntry;
import com.model.database.DataSourceContacts;
import com.services.ServiceDatabaseCompressor;
import com.services.ServiceLogAccelerometer;

public class AccelerometerReaderActivity extends Activity {
 
	private static final String TAG = "AccelerometerReaderActivity";
	
    private static Context CONTEXT;

    private String labelSamples = "";
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        CONTEXT = this;
    }

    protected void onResume() {
        super.onResume();
    }
 
    protected void onDestroy() {
        super.onDestroy();
    }
 
    public static Context getContext() {
        return CONTEXT;
    }
    

//// - start / stop buttons actions
    public void startRecordAccelerometer(View view) {
    	AlertDialog.Builder alert = new AlertDialog.Builder(CONTEXT);
    	alert.setTitle("Label samples.");
    	alert.setMessage("Enter a label for next samples session.");
    	 
    	final EditText input = new EditText(CONTEXT);
    	alert.setView(input);

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog, int whichButton) {
    		
    		// write label and date to samplesFile.txt
    		labelSamples = input.getText().toString();
    		Log.v("Activity - start button", "write date when started with label : " + labelSamples +" in accSamplesTxt/"+labelSamples+".txt");
    		
    		if (!"".equals(labelSamples)) {
    			try {
    				// start accelerometer recording		
    				Intent svc = new Intent(CONTEXT, ServiceLogAccelerometer.class);
    				Bundle bundle = new Bundle();
    				bundle.putString("activityName", labelSamples); // input from user
    				svc.putExtras(bundle);
    				startService(svc);

    				// We want the alarm to go off 10 min seconds from now.
    				PendingIntent mAlarmSender = PendingIntent.getService(CONTEXT,
    			                0, new Intent(CONTEXT, ServiceDatabaseCompressor.class), 0);
    	            long firstTime = SystemClock.elapsedRealtime();
    				AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
    				am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
    						firstTime, 60*60*1000, mAlarmSender); 

    				// Tell the user about what we did.
    				Toast.makeText(CONTEXT, "repeating_scheduled", Toast.LENGTH_LONG).show();
    				
    				Log.v("Activity - start button", "service stated");
    			} catch (Exception e) {
    				Log.e("Activity - start button", "service creation problem", e);
    			}
     		} else {
     			Toast.makeText(getApplicationContext(), "Activity must has a name!", Toast.LENGTH_LONG).show();
     		}
    	  }
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    	  public void onClick(DialogInterface dialog, int whichButton) {
    		  return; // do not start accelerometer recording
    	  }
    	});

    	alert.show();
    }
    
    public void stopRecordAccelerometer(View view) {
    	
    	stopRunningServices();
        // Tell the user about what we did.
        Toast.makeText(CONTEXT, "services stopped: accReader, dbCompressor", Toast.LENGTH_LONG).show();
    }
 
    public void goToSettings(View view) {
    	Intent myIntent = new Intent(CONTEXT, SettingsActivity.class);
    	CONTEXT.startActivity(myIntent);
    }
    // view charts and activity report buttons
    public void viewPastActivities (View view) {
    	Intent myIntent = new Intent(CONTEXT, ActivityLog.class);
    	CONTEXT.startActivity(myIntent);
    }
    
    public void chartPastActivities (View view) {
    	Intent myIntent = new Intent(CONTEXT, DetailedStatisticActivity.class);
    	CONTEXT.startActivity(myIntent);
    }
    
    public void chartPieActivities (View view) {
    	Intent myIntent = new Intent(CONTEXT, PieReportActivity.class);
    	CONTEXT.startActivity(myIntent);
    }
    
    public void callHelp (View view) {
    	DataSourceContacts dataSource = new DataSourceContacts(CONTEXT);
		dataSource.open();
		List<ContactEntry> contacts = dataSource.getAllContacts();
		dataSource.close(); 
		if (contacts.size() > 0) {
			for (ContactEntry ce : contacts) {
				String phoneNo = ce.getPhone();
				String number = "tel:" + phoneNo;
				Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number)); 
				startActivity(callIntent);
				Log.v(TAG, "call help to first no "+ number);
			}
		} else {
			Toast.makeText(CONTEXT, "Your contact list is empty!", Toast.LENGTH_LONG);
		}
    }
    
    
    
    private void stopRunningServices () {
    	Intent svc = new Intent(CONTEXT, ServiceLogAccelerometer.class);
        stopService(svc);
        
        // And cancel the alarm.
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        PendingIntent mAlarmSender = PendingIntent.getService(CONTEXT,
                0, new Intent(CONTEXT, ServiceDatabaseCompressor.class), 0);
        am.cancel(mAlarmSender);
        
        Log.v(TAG + " - stop button", "services stoped");
    }
}
