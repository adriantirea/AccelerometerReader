package com.activities.gui;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.configurations.ConfigParameters;
import com.model.accelerometer.R;
import com.model.database.ContactEntry;
import com.model.database.DataSourceContacts;
import com.utils.SmsSender;


/**
 * Disable back butto. Finish when stop alarm pressed.
 * @author adji
 *
 */
public class AlertActivity extends Activity implements LocationListener {

	private static final String TAG = "AlertActiviy";

	private static Context CONTEXT;

	private static MediaPlayer player;
	private static Vibrator vibrator;
	private static Timer timer;
	private static LocationManager locationManager;

	private static long[] pattern = { 0, 200, 300 };
	
	private boolean playerStarted = false;
	private boolean timerStarted = false;
	private boolean locationSet = false;
	
	private long latitude;
	private long longitude;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP, "bbbb");
		wl.acquire();

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
 		audio.setStreamVolume(AudioManager.STREAM_MUSIC,
 				audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
 				AudioManager.FLAG_PLAY_SOUND);


		setContentView(R.layout.alertactivity);
		CONTEXT = this;

		if (locationManager == null) {
			locationManager = (LocationManager) CONTEXT.getSystemService(Context.LOCATION_SERVICE);
		}
		if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		}
		else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		}

		if (savedInstanceState != null) {
			playerStarted = savedInstanceState.getBoolean("playerStarted");
			timerStarted = savedInstanceState.getBoolean("timerStarted");
			locationSet = savedInstanceState.getBoolean("locatioSet");
			latitude = savedInstanceState.getLong("latitude");
			longitude = savedInstanceState.getLong("longitude");
		}

		if (!playerStarted) {
			player = MediaPlayer.create(CONTEXT, R.raw.ciocarlia);
			player.setLooping(true);
			
			vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		}

		if (!timerStarted) {
			timer = new Timer();
			timer.schedule(callExtern, ConfigParameters.ALERT_ON_UNTIL_CONTACT);
			timerStarted = true;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!playerStarted && !player.isPlaying()) {
			player.start();
			playerStarted = true;
			vibrator.vibrate(pattern, 0);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		locationManager.removeUpdates(this);
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is
		// killed and restarted.
		savedInstanceState.putBoolean("playerStarted", playerStarted);
		savedInstanceState.putBoolean("timerStarted", timerStarted);
		savedInstanceState.putBoolean("locationSet", locationSet);

		savedInstanceState.putLong("latitude", latitude);
		savedInstanceState.putLong("longitude", longitude);
		
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onBackPressed() {
        //stopAlarm(findViewById(R.id.button1));
	}

	private TimerTask callExtern = new TimerTask() {

		@Override
		public void run() {
			Log.v(TAG, "make external contact");
			player.stop();
			vibrator.cancel();
			//timer.cancel();

			@SuppressWarnings("unused")
			String phoneNo = null;
			try {
				DataSourceContacts dataSource = new DataSourceContacts(CONTEXT);
				dataSource.open();
				List<ContactEntry> contacts = dataSource.getAllContacts();
				dataSource.close();

				if (contacts.isEmpty()) {  
					runOnUiThread(new Runnable() {
						public void run() {
							TextView smstext = (TextView) findViewById(R.id.smstext);
							smstext.setText("No contact !!!  no help message sent!");
						}
					});
				} else {
					SharedPreferences settings = getSharedPreferences(ConfigParameters.SETTINGS_KEY, 0);
					float alertTime = settings.getFloat(ConfigParameters.ALERT_NO_ACTION_KEY, ConfigParameters.ALERT_NO_ACTION_DEFAULT);
					String[] phoneNumbers = new String[contacts.size()];
					phoneNo = contacts.get(0).getPhone();
					int i = 0; 
					for (ContactEntry ce : contacts) {
						phoneNumbers[i] = ce.getPhone();
						i++;
					}
					String location;
					if (locationSet) {
						location = "Location of device (latitude, longitude) = (" + latitude + ", " + longitude + ").";
					} else {
						location = "Location not determined.";
					}
					SmsSender.sendSmsToContacts("Help needed! User device recorded NO ACTION for " + alertTime + " hours. " + location,
							phoneNumbers,
							CONTEXT);
					runOnUiThread(new Runnable() {
						public void run() {
							TextView smstext = (TextView) findViewById(R.id.smstext);
							smstext.setText("Help message sent!");
						}
					});
				}
			} catch (final Exception e) {
				runOnUiThread(new Runnable() {
					public void run() {
						TextView smstext = (TextView) findViewById(R.id.smstext);
						smstext.setText("Sms not send: "+e.getMessage());
					}
				});
				Log.e("sms", e.getMessage());
			}
//			if (phoneNo != null) {
//				String number = "tel:" + phoneNo;
//				Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number)); 
//				startActivity(callIntent);
//			}
		}
	};

	public void stopAlarm(View view) {
		Log.v(TAG, "stop alarm");
		player.stop();
		timer.cancel();
		vibrator.cancel();
		
		finish();
	}


	// location listener
	public void onLocationChanged(Location arg0) {
		Log.v("GPS","changed");
		latitude = (long)(arg0.getLatitude() * 1000000);
		longitude = (long)(arg0.getLongitude() * 1000000);
		locationSet = true;
	}
	public void onProviderDisabled(String provider) {
	}
	public void onProviderEnabled(String provider) {
	}
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
	
}
