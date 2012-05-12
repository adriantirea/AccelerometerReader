package com.model.database;

import android.text.format.DateFormat;

public class ActivityEntry {
	
	private long id;
	private String name;
	private long startDate;
	private long duration;
	private int steps;
	private float distance;
	private float kCal;
	
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

	public long getStartDate() {
		return startDate;
	}
	public void setStartDate(long startDate) {
		this.startDate = startDate;
	}
	
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
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
	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return id + " " + name + " " + DateFormat.format("dd-MM  hh:mm:ss", startDate)  + " " + duration + " " + steps + " "+ kCal + " "+ distance;
	}
}
