package com.model.processing;

public class PartialStatisticsResult {
	public double min;
	public double max;
	
	public double sumAccX;
	public double sumAccY;
	public double sumAccZ;
	public double sumAcc;
	
	public double sumAccX2;
	public double sumAccY2;
	public double sumAccZ2;
	public double sumAcc2;
	
	public PartialStatisticsResult (){
		min = 0;
		max = 0;
		
		sumAccX = 0;
		sumAccY = 0;
		sumAccZ = 0;
		sumAcc = 0;
		
		sumAccX2 = 0;
		sumAccY2 = 0;
		sumAccZ2 = 0;
		sumAcc2 = 0;
	}
}
