package com.model.accelerometer;


import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.activities.gui.AccelerometerReaderActivity;
import com.configurations.ConfigParameters;
 
/**
 * Android Accelerometer Sensor Manager Archetype
 * @author antoine vianey
 * under GPL v3 : http://www.gnu.org/licenses/gpl-3.0.html
 */
public class AccelerometerManager {
 
	@SuppressWarnings("unused")
	private static final String TAG = "AccelerometerManager"; 
    /** Accuracy configuration */
    
    private static double fixedSampleRate = ConfigParameters.FIXED_SAMPLE_RATE; // miliseconds
    // for computing the frequency of sampling
    private static double minDifTimeSamples = Double.MAX_VALUE;    
    private static double maxDifTimeSamples = Double.MIN_VALUE;
    private static double meanDifTimeSamples = 0;
    private static double counterDifTimeSamples = 0;
    
    private static double fixedSampleTime = -1;
    private static long counterSample = -1;
	
    private static double now = 0;
    private static double lastUpdate = 0;

    private static float x = 0;
    private static float y = 0;
    private static float z = 0;
    private static float lastX = 0;
    private static float lastY = 0;
    private static float lastZ = 0;
    
    
    
    
    private static Sensor sensor;
    private static SensorManager sensorManager;
    // you could use an OrientationListener array instead
    // if you plans to use more than one listener
    private static AccelerometerListener listener;
 
    /** indicates whether or not Accelerometer Sensor is supported */
    private static Boolean supported;
    /** indicates whether or not Accelerometer Sensor is running */
    private static boolean running = false;
 
    /**
     * Returns true if the manager is listening to orientation changes
     */
    public static boolean isListening() {
        return running;
    }
 
    /**
     * Unregisters listeners
     */
    public static void stopListening() {
        running = false;
        try {
            if (sensorManager != null && sensorEventListener != null) {
                sensorManager.unregisterListener(sensorEventListener);
            }
        } catch (Exception e) {}
        
         /*Log.v("AccManager","counter = "+counterDifTimeSamples
        		+"sum = "+meanDifTimeSamples
        		+" meanDifTimeSamples = "+ (meanDifTimeSamples / counterDifTimeSamples)
        		+"  minDifTimeSamples = "+minDifTimeSamples
        		+"  maxDifTimeSamples = "+maxDifTimeSamples);
        */
    }
 
    /**
     * Returns true if at least one Accelerometer sensor is available
     */
    public static boolean isSupported() {
        if (supported == null) {
            if (AccelerometerReaderActivity.getContext() != null) {
                sensorManager = (SensorManager) AccelerometerReaderActivity.getContext().
                        getSystemService(Context.SENSOR_SERVICE);
                List<Sensor> sensors = sensorManager.getSensorList(
                        Sensor.TYPE_ACCELEROMETER);
                supported = new Boolean(sensors.size() > 0);
            } else {
                supported = Boolean.FALSE;
            }
        }
        return supported;
    }
 
    /**
     * Configure the listener for shaking
     * @param threshold
     *             minimum acceleration variation for considering shaking
     * @param interval
     *             minimum interval between to shake events
     */
    public static void configure(int threshold, int interval) {
        //AccelerometerManager.threshold = threshold;
        //AccelerometerManager.interval = interval;
    }
 
    /**
     * Registers a listener and start listening
     * @param accelerometerListener
     *             callback for accelerometer events
     */
    public static void startListening(
            AccelerometerListener accelerometerListener) {
        sensorManager = (SensorManager) AccelerometerReaderActivity.getContext().
                getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(
                Sensor.TYPE_ACCELEROMETER);
        if (sensors.size() > 0) {
            sensor = sensors.get(0);
            running = sensorManager.registerListener(
                    sensorEventListener, sensor, 
                    SensorManager.SENSOR_DELAY_FASTEST);
            listener = accelerometerListener;
        }
        
        // reset infos
        minDifTimeSamples = Long.MAX_VALUE;
        maxDifTimeSamples = Double.MIN_VALUE;
        meanDifTimeSamples = 0;
        counterDifTimeSamples = 0;
        
        lastUpdate = 0;
        lastX = 0;
        lastY = 0;
        lastZ = 0;
        fixedSampleTime = -1;
        counterSample = -1;
    }
 
    /**
     * Configures threshold and interval
     * And registers a listener and start listening
     * @param accelerometerListener
     *             callback for accelerometer events
     * @param threshold
     *             minimum acceleration variation for considering shaking
     * @param interval
     *             minimum interval between to shake events
     */
    public static void startListening(
            AccelerometerListener accelerometerListener, 
            int threshold, int interval) {
        configure(threshold, interval);
        startListening(accelerometerListener);
    }
 
    /**
     * The listener that listen to events from the accelerometer listener
     */
    private static SensorEventListener sensorEventListener = 
        new SensorEventListener() {
    	
    	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
 
        public void onSensorChanged(SensorEvent event) {
            // use the event timestamp as reference in nanoseconds
            // so the manager precision won't depends 
            // on the AccelerometerListener implementation
            // processing time
            
        	
        	now = event.timestamp / 1000000.0; // transform in miliseconds
        	
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
 
            // liniarize
            if (fixedSampleTime == -1) { // begin of a new test
            	fixedSampleTime = now;
            	counterSample = 0;
            	listener.onAccelerationChangedLiniarizedSample(counterSample, fixedSampleTime, x, y, z);
            } else {
            	// liniarize for all three axes
            	float tempX, tempY, tempZ;
            	while (fixedSampleTime + fixedSampleRate <= now) {
            		fixedSampleTime += fixedSampleRate;
            		counterSample ++;
            		double liniarK = (fixedSampleTime - lastUpdate) / (now - lastUpdate);  
            		tempX = (float)(liniarK * (x - lastX) + lastX);
            		tempY = (float)(liniarK * (y - lastY) + lastY);
            		tempZ = (float)(liniarK * (z - lastZ) + lastZ);
            		listener.onAccelerationChangedLiniarizedSample(counterSample, fixedSampleTime, tempX, tempY, tempZ);
            	}
            }
            
            
            
            //computeStatisticForFrequency();
            
            // trigger change event
            listener.onAccelerationChanged(now, x, y, z);
            
            lastUpdate = now;
            lastX = x;
            lastY = y;
            lastZ = z;
        }
    };
    
    @SuppressWarnings("unused")
	private static void computeStatisticForFrequency() {
    	if (lastUpdate > 0) {
	        double diffTimestamps = now - lastUpdate;
	        if (diffTimestamps < minDifTimeSamples) {
	        	minDifTimeSamples = diffTimestamps; 
	        }
	        if (diffTimestamps > maxDifTimeSamples) {
	        	maxDifTimeSamples = diffTimestamps; 
	        }
	        meanDifTimeSamples += (diffTimestamps);
	        counterDifTimeSamples++;
    	}
    }
}



//private static float threshold     = 0.2f;
//private static int interval     = 1000;
// if not interesting in shake events
// just remove the whole if then else bloc
/*
if (lastUpdate == 0) {
    lastUpdate = now;
    lastShake = now;
    lastX = x;
    lastY = y;
    lastZ = z;
} else {
    timeDiff = now - lastUpdate;
    if (timeDiff > 0) {
        force = Math.abs(x + y + z - lastX - lastY - lastZ) 
                    / timeDiff;
        if (force > threshold) {
            if (now - lastShake >= interval) {
                // trigger shake event
                listener.onShake(force);
            }
            lastShake = now;
        }
        lastX = x;
        lastY = y;
        lastZ = z;
        lastUpdate = now;
    }
}*/