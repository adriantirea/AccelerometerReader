package com.model.processing.filters;

import com.model.processing.AccelerationSample;


/**
 * 
 * @author adji
 * Insert values one by one and receive the result of filter for each step. 
 * Result is the difference of last point and the previous 'length' point.  
 */

public class HighPassFilter implements Filter {
	private AccelerationSample[] values;
	private int length;
	private int index;
	
	public HighPassFilter (int length) {
		assert (length > 0);
		index = 0;
		if (length > 0) {
			this.length = length;
		}
		values = new AccelerationSample[length];
		for (int i=0; i < length; i++) {
			values[i] = new AccelerationSample(); 
		}
	}
	
	/**
	 * !! for now its result is put in accFilteredHighPass  
	 */
	public AccelerationSample filterValue(AccelerationSample newValue) { 
		float highPassResult = (newValue.accFiltered - values[index].accFiltered);
		values[index] = newValue;
		
		AccelerationSample result = values[(index + length/2) % length].copy();
		result.accFilteredHighPass = highPassResult;
		
		index = ( ++index ) % length;
		return result;
	}
}
