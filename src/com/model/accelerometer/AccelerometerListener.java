package com.model.accelerometer;


public interface AccelerometerListener {
 
	public void onAccelerationChanged(double now, float x, float y, float z);
	
	public void onAccelerationChangedLiniarizedSample(long counterSample, double timestamp, float x, float y, float z);
	
	//public void onShake(float force);
 
}