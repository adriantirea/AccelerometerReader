package com.model.processing.filters;

import com.model.processing.AccelerationSample;


public interface Filter {

	public AccelerationSample filterValue(AccelerationSample newValue);
	
}
