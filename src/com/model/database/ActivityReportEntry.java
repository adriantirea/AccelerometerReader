package com.model.database;

import android.text.format.DateFormat;

public class ActivityReportEntry {

	private long id;
	private long startDate;
	private long endDate;

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	public long getStartDate() {
		return startDate;
	}
	public void setStartDate(long startDate) {
		this.startDate = startDate;
	}
	
	public long getEndDate() {
		return endDate;
	}
	public void setEndDate(long endDate) {
		this.endDate = endDate;
	}
	
	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return id + " " + DateFormat.format("dd-MM  hh:mm:ss", startDate)  + " " + DateFormat.format("dd-MM  hh:mm:ss", endDate);
	}
}
