package com.model.database;

public class ActivityCompressed {
	
	private long id;
	private String name;
	private int value;
	private int steps;
	private float distance;
	private float kCal;
	private long reportID;
		
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public void setValue(int value) {
		this.value = value;
	}
	public int getValue() {
		return value;
	}
	
	public int getSteps() {
		return steps;
	}
	public void setSteps(int steps) {
		this.steps = steps;
	}
	
	public float getDistance() {
		return distance;
	}
	public void setDistance(float distance) {
		this.distance = distance;
	}
	
	public float getKCal() {
		return kCal;
	}
	public void setkCal(float kCal) {
		this.kCal = kCal;
	}
	
	public long getReportIdD() {
		return reportID;
	}
	public void setReportID(long reportID) {
		this.reportID = reportID;
	}
	
	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return id + " " + name + " " + value + " " + steps+ " "+ distance + " " + kCal;
	}
}
