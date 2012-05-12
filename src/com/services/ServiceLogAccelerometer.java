package com.services;

import java.io.IOException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.model.accelerometer.AccelerometerListener;
import com.model.accelerometer.AccelerometerManager;
import com.model.processing.AccelerationProcessor;
import com.model.processing.AccelerationSample;
import com.utils.LoggerTxt;


public class ServiceLogAccelerometer extends Service  implements AccelerometerListener {

	private static String TAG = "serviceAcc";

	private static WakeLock  _wakeLock; // for running when turn off screen

	private LoggerTxt accelerationLoggerTxt;
	private AccelerationProcessor acceleratonProcessor;
	
	public void onStart(Intent intent, int startId) {
		 super.onStart(intent, startId);
		  
		 Bundle bundle = intent.getExtras();
		 String activityName = (String) bundle.getCharSequence("activityName");
		 Log.v(TAG, "onStart -- "+activityName);
		 
		 if (AccelerometerManager.isSupported()) {
			 try {		
				 AccelerometerManager.startListening(this);

				 accelerationLoggerTxt = new LoggerTxt(activityName);
				 acceleratonProcessor = new AccelerationProcessor(activityName, this);

				 //for keep running in sleepMode
				 PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);    
				 _wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag"); 
				 _wakeLock.acquire();

			 } catch(IOException e) {
				 Log.e(TAG, e.getMessage());
			 }
		 }
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		 Log.v(TAG, "onCreate");
	}


	@Override
	public void onDestroy() {
		Log.v("serviceAcc","onDestroy-------------------------------");
		if (AccelerometerManager.isListening()) {
			AccelerometerManager.stopListening();
			acceleratonProcessor.cancelAccelerationProcessor();
			_wakeLock.release();
			try {
				accelerationLoggerTxt.closeLogger();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		super.onDestroy();
	}


	/**
	 * onAccelerationChanged callback
	 */
	public void onAccelerationChanged(double timestamp, float x, float y, float z) {
		//writeToSdCard(timestamp, x, y, z);
	}

	/**
	 * LiniarizedSample callback
	 */
	public void onAccelerationChangedLiniarizedSample(long counterSample, double timestamp, float x, float y, float z) {
		
		AccelerationSample sample = new AccelerationSample(counterSample, timestamp, x, y, z);
		
		// write number of timestamp, timestamp in miliseconds, x, y, z accelerations
		if (!accelerationLoggerTxt.writeToSdCard(sample)) {
			Log.v(TAG, "Faild to write accelerationLogerTxt");
		}
		
		acceleratonProcessor.addSample(sample);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
