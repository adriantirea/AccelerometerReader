package com.model.processing;

public class StatisticsResult {

	public double min;
	public double max;
	public double range;
	
	public double meanX;
	public double meanY;
	public double meanZ;
	public double mean;
	
	// standardDeviation
	public double stdX; 
	public double stdY; 
	public double stdZ;
	public double std; 
	
	public double corelationXY;
	public double corelationYZ;
	public double corelationZX;
	
	public double energy;
	public double entropy;
	
	private int steps;
	
	public double[] getValues() {
		return new double[]{min, max, range, meanX, meanY, meanZ, mean, stdX, stdY, stdZ, std, corelationXY, corelationYZ, corelationZX, energy, entropy};
	}
	
	public int getSteps() {
		return steps;
	}
	
	public void setSteps(int steps) {
		this.steps = steps;
	}
}
