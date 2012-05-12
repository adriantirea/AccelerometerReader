package com.services;

import java.util.Date;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.configurations.ConfigParameters;
import com.model.reportProcessing.ReportProcessor;

/**
 * Compress the data from 'activity' table in 'report' table
 * @author adji
 *
 */

public class ServiceDatabaseCompressor extends Service {

	private static final String[] CLASSES = ConfigParameters.CLASSES_PIEREPORT;
	
	@Override
	public void onCreate () {
		Log.v("service", "om create s");

		// Start up the thread running the service.  Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block.
		Thread thr = new Thread(null, mTask, "AlarmService_Service");
		thr.start();
	}

	@Override
	public void onDestroy() {
		Log.v("service", "onDestroy s");
		// Cancel the notification -- we use the same ID that we had used to start it

		// Tell the user we stopped.
		//Toast.makeText(this, "stoped in onDestroy repeted service", Toast.LENGTH_SHORT).show();
	}

	/**
	 * The function that runs in our worker thread
	 */
	Runnable mTask = new Runnable() {
		public void run() {
			Log.v("service", "runable DB compressor");

			ReportProcessor reportProcessor = new ReportProcessor(ServiceDatabaseCompressor.this, CLASSES, ConfigParameters.STEP_TIME);
			reportProcessor.compressDatabaseUpToDate(new Date().getTime() - 60*60*1000); // all till last hour////always have the last hour detailed
			ServiceDatabaseCompressor.this.stopSelf();
		}
	};

	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
