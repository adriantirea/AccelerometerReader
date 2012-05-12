package com.model.processing.filters;

import com.model.processing.AccelerationSample;


/**
 * 
 * @author adji
 * Insert values one by one and receive the result of filter for each step. 
 * Result is the mean of 'length' consecutive points.  
 */

public class SmoothFilter implements Filter {
	private AccelerationSample[] values;
	private int length;
	private int index;
	private float smoothResult;
	
	public SmoothFilter (int length) {
		index = 0;
		smoothResult = 0;
		if (length > 0) {
			this.length = length;
		} else {
			this.length = 0;
		}
		values = new AccelerationSample[length];
		for (int i=0; i < length; i++) {
			values[i] = new AccelerationSample(); 
		}
	}
	
	public AccelerationSample filterValue(AccelerationSample newValue) {
		smoothResult += (newValue.accFiltered - values[index].accFiltered) / length;
		values[index] = newValue;
		index = ( ++index ) % length;
		
		AccelerationSample result = values[(index + length/2) % length].copy();
		result.accFiltered = smoothResult;
		return result;
	}
}
