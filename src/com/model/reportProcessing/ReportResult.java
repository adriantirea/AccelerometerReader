package com.model.reportProcessing;

import java.util.Date;

public class ReportResult {
	
	private int steps;
	private int stepsRun;
	private int stepsWalk;
	private int distance;
	private int distanceRun;
	private int distanceWalk;
	private int kCal;
	private int kCalRun;
	private int kCalWalk;
	private Date startDate;
	private Date endDate;
	private String[] classes;
	private double[] values;
	
	public void setSteps (int steps) {
		this.steps = steps;
	}
	public int getSteps () {
		return steps;
	}
	public void setStepsWalk (int stepsWalk) {
		this.stepsWalk = stepsWalk;
	}
	public int getStepsWalk () {
		return stepsWalk;
	}
	public void setStepsRun (int stepsRun) {    
		this.stepsRun = stepsRun;
	}
	public int getStepsRun () {
		return stepsRun;
	}
	
	public void setDistance (int distance) {
		this.distance = distance;
	}
	public int getDistance () {
		return distance;
	}
	public void setDistanceRun (int distanceRun) {
		this.distanceRun = distanceRun;
	}
	public int getDistanceRun () {
		return distanceRun;
	}
	public void setDistanceWalk (int distanceWalk) {
		this.distanceWalk = distanceWalk;
	}
	public int getDistanceWalk () {
		return distanceWalk;
	}
	
	public void setKCal (int kCal) {
		this.kCal = kCal;
	}
	public int getKCal () {
		return kCal;
	}
	public void setKCalRun (int kCalRun) {
		this.kCalRun = kCalRun;
	}
	public int getKCalRun () {
		return kCalRun;
	}
	public void setKCalWalk (int kCalWalk) {
		this.kCalWalk = kCalWalk;
	}
	public int getKCalWalk () {
		return kCalWalk;
	}
	public void setStartDate (Date startDate) {
		this.startDate = startDate;
	}
	public Date getStartDate () {
		return startDate;
	}
	
	public void setEndDate (Date endDate) {
		this.endDate = endDate;
	}
	public Date getEndDate () {
		return endDate;
	}
	
	public void setClasses (String[] classes) {
		this.classes = classes;
	}
	public String[] getClasses () {
		return classes;
	}
	
	public void setValues (double[] values) {
		this.values = values;
	}
	public double[] getValues () {
		return values;
	}

}
