package com.model.processing;

public class ActivityValidator {

	/**
	 * Take decision for detached state == when a activity is continuous longer than a threshold.
	 * Check noAction alarm for continuous detached state longer than a threshold. 
	 */
	private int continuousActivity;
	private String lastActivity;
	private String[] interesActivity;
	
	private int threshold;
	private String newActivity;
	
	boolean fireOn;
	
	/**
	 * 
	 * @param threshold - # of consecutive same activity that triggers to put the newActivity instead of continuous activity.
	 * @param newActivity - activity to replace the continuous activity
	 */
	public ActivityValidator (int threshold, String newActivity, String[] interesActivity) {
		this.threshold = threshold;
		this.newActivity = newActivity;
		this.interesActivity = interesActivity;
		continuousActivity = 0;
	}
	
	public String validateActivity(String activity) {
		
		boolean isInteresActivity = false;
		boolean replaceActivity = false;
		if (activity != null) {
			for (String str : interesActivity) {
				if (activity.equals(str)) {
					isInteresActivity = true;
				}
			}
		}
		if (isInteresActivity && activity.equals(lastActivity)) {
			continuousActivity ++;
			replaceActivity = (continuousActivity > threshold);
		} else {
			continuousActivity = 0;
			fireOn = true;
		}
		lastActivity = activity;
		if (replaceActivity) {
			return newActivity;
		}
		return activity;
	}
	
	/**
	 * 
	 * @param threshold - # of continuous activities from 'interesActivity' (i.e 'sitUp', 'sitDown') that fires alarm 
	 * @return
	 */
	public boolean fireNoActionAlarm (int thresholdNoActivity) {
		return (fireOn && (continuousActivity > thresholdNoActivity));
	}
	
	public void dezactivateFireNoActionAlarm() {
		fireOn = false;
	}
}
