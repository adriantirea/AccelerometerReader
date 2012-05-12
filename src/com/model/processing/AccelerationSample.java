package com.model.processing;


public class AccelerationSample {

	public AccelerationSample (long counterSample, double timestamp, float accX, float accY, float accZ) {
		this.counterSample = counterSample;
		this.timestamp = timestamp;
		this.accX = accX;
		this.accY = accY;
		this.accZ = accZ;
		this.acc = (float) Math.sqrt(accX * accX + accY * accY + accZ * accZ);
		this.accFiltered = acc;
		this.accFilteredHighPass = acc;
	}
	public AccelerationSample () {
		counterSample = 0;
		timestamp = 0;
		accX = 0;
		accY = 0;
		accZ = 0;
		acc = 0;
		accFiltered = 0;
		accFilteredHighPass = 0;
	}
	
	public AccelerationSample copy() {
		AccelerationSample result = new AccelerationSample(counterSample, timestamp, accX, accY, accZ);
		result.accFiltered = this.accFiltered;
		result.accFilteredHighPass = this.accFilteredHighPass;
		return result;
	}
	
	public long counterSample;
	public double timestamp;
	public float accX;
	public float accY;
	public float accZ;
	public float acc;
	public float accFiltered; 
	public float accFilteredHighPass;
}
