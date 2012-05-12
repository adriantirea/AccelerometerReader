package com.activities.gui;

import java.io.File;

import com.model.accelerometer.R;
import com.model.database.DatabaseHelper;
import com.services.ServiceDatabaseCompressor;
import com.services.ServiceLogAccelerometer;
import com.syncserver.SyncServer;
import com.utils.DirectoryDeleter;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SyncActivity extends Activity {

	private static final String TAG = "Sync";
	private static Context CONTEXT;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.sync);
		CONTEXT = this;
	}
	
	// delete buttons - temporary usage
    public void  deleteSampleFile (View view) {
    	AlertDialog.Builder alert = new AlertDialog.Builder(CONTEXT);
    	alert.setTitle("Delete folder '/accSamplesTxt' '/accResultsTxt' '/arff' that holds row acc data.");
    	alert.setMessage("Do you want to delete '/accSamplesTxt'?");
    	 
    	alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog, int whichButton) {
    		
    		stopRunningServices();
    		
    		String filePath = Environment.getExternalStorageDirectory()+"/accSamplesTxt";
    		File file = new File(filePath);
    		DirectoryDeleter.deleteDirectory(file);
    		
     		filePath = Environment.getExternalStorageDirectory()+"/accResultsTxt";
    		file = new File(filePath);
    		DirectoryDeleter.deleteDirectory(file);
    		
    		filePath = Environment.getExternalStorageDirectory()+"/arff";
    		file = new File(filePath);
    		DirectoryDeleter.deleteDirectory(file);
    		
    		Log.v(TAG, "accSamplesTxt/, accResultsTxt/, arff/ deleted");
    	  }
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    	  public void onClick(DialogInterface dialog, int whichButton) {
    		  return; // do not start accelerometer recording
    	  }
    	});
    	alert.show();
    }
    
    public void deleteDatabase (View view) {
    	
    	AlertDialog.Builder alert = new AlertDialog.Builder(CONTEXT);
    	alert.setTitle("Delete activities log");
    	alert.setMessage("Do you want to delete 'sql'?");
    	 
    	alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog, int whichButton) {
	        // stop accelerometer recording
    		stopRunningServices();	    
            CONTEXT.deleteDatabase(DatabaseHelper.DATABASE_NAME);
            Log.v(TAG, "deleteDB");
    	  }
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    	  public void onClick(DialogInterface dialog, int whichButton) {
    		  return; // do not start accelerometer recording
    	  }
    	});
    	alert.show();
    }
	
	
	public void createAccount (View view) {
		AlertDialog.Builder alert = new AlertDialog.Builder(CONTEXT);
    	alert.setTitle("Register account.");
    	alert.setMessage("Enter a username, password and email to register a new account.");
    	    	
    	LinearLayout lila1= new LinearLayout(this);
    	lila1.setOrientation(1); //1 is for vertical orientation
    	final EditText input1 = new EditText(this); 
    	final EditText input2 = new EditText(this);
    	final EditText input3 = new EditText(this);
    	lila1.addView(input1);
    	lila1.addView(input2);
    	lila1.addView(input3);
    	alert.setView(lila1);

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog, int whichButton) {
    		
    		String username = input1.getText().toString();
    		String pass = input2.getText().toString();
    		String email = input3.getText().toString();
    		
    		if (!("".equals(username) ||"".equals(pass) || "".equals(email))) {
    			Toast.makeText(getApplicationContext(),SyncServer.createAccount(username, pass, email),  Toast.LENGTH_LONG).show();;    			
    		} else {
     			Toast.makeText(getApplicationContext(), "Must provide username, password, email!", Toast.LENGTH_LONG).show();
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
	
    public void importReports(View view) {

    	AlertDialog.Builder alert = new AlertDialog.Builder(CONTEXT);
    	alert.setTitle("Register account.");
    	alert.setMessage("Enter a username, password and email to register a new account.");
    	    	
    	LinearLayout lila1= new LinearLayout(this);
    	lila1.setOrientation(1); //1 is for vertical orientation
    	final EditText input1 = new EditText(this); 
    	final EditText input2 = new EditText(this);
    	lila1.addView(input1);
    	lila1.addView(input2);
    	alert.setView(lila1);

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog, int whichButton) {
    		
    		String username = input1.getText().toString();
    		String pass = input2.getText().toString();
    		
    		if (!("".equals(username) ||"".equals(pass))) {
    			String response = SyncServer.importReports(username, pass, CONTEXT);
    	    	if ("OK".equals(response)) {
    	    		Toast.makeText(CONTEXT, "Import done", Toast.LENGTH_SHORT).show();
    	    	} else {
    	    		Toast.makeText(CONTEXT, "Import failed:" + response, Toast.LENGTH_LONG).show();
    	    	}
    		} else {
     			Toast.makeText(getApplicationContext(), "Must provide username, password!", Toast.LENGTH_LONG).show();
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
    public void exportReports(View view) {
    	AlertDialog.Builder alert = new AlertDialog.Builder(CONTEXT);
    	alert.setTitle("Register account.");
    	alert.setMessage("Enter a username, password and email to register a new account.");
    	    	
    	LinearLayout lila1= new LinearLayout(this);
    	lila1.setOrientation(1); //1 is for vertical orientation
    	final EditText input1 = new EditText(this); 
    	final EditText input2 = new EditText(this);
    	lila1.addView(input1);
    	lila1.addView(input2);
    	alert.setView(lila1);

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog, int whichButton) {
    		
    		String username = input1.getText().toString();
    		String pass = input2.getText().toString();
    		
    		if (!("".equals(username) ||"".equals(pass))) {
    			String response = SyncServer.exportReports(username, pass, CONTEXT); 
    			if ("OK".equals(response)) {
    	    		Toast.makeText(CONTEXT, "Export done", Toast.LENGTH_SHORT).show();
    	    	} else {
    	    		Toast.makeText(CONTEXT, "Export failed: " + response, Toast.LENGTH_SHORT).show();
    	    	}
    		} else {
     			Toast.makeText(getApplicationContext(), "Must provide username, password!", Toast.LENGTH_LONG).show();
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
