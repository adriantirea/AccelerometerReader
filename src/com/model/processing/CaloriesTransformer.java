package com.model.processing;

import android.util.Log;



/**
 * Computes step length based on person height and frequency of stepping. Computes volume of oxygen based on speed. 
 * Computes calories for activity of walking or running based on computed volume of oxygen consumed.
 *  
 * 
 * @author adji
 *
 */
public class CaloriesTransformer {

	float timeUnit;
	float[] stepToHeightProportion;
	float stepToHeightTimeUnit;
	float kcalOnLitre;
	float speedToKCalRunVertical;
	float speedToKCalRunHorizontal;
	float speedToKCalWalkVertical;
	float speedToKCalWalkHorizontal;
	float kCalNoAction;
	/**
	 * 
	 * @param stepToHeightProportion - relation between steps  and proportion from person height
	 * @param stepToHeightTimeUnit - timeUnit for stepToHeightProportion relation. in milliseconds  
	 * @param timeUnit - in milliseconds. in milliseconds
	 */
	public CaloriesTransformer (float[] stepToHeightProportion, float stepToHeightTimeUnit, float timeUnit, float kcalOnLitre,
			float speedToKCalRunVertical, float speedToKCalRunHorizontal, float speedToKCalWalkVertical, float speedToKCalWalkHorizontal, float kCalNoAction) {

		this.timeUnit = timeUnit;
		this.stepToHeightProportion = stepToHeightProportion;
		this.stepToHeightTimeUnit = stepToHeightTimeUnit;
		this.kcalOnLitre = kcalOnLitre;
		this.kCalNoAction = kCalNoAction;
		this.speedToKCalRunVertical =speedToKCalRunVertical;
		this.speedToKCalRunHorizontal =speedToKCalRunHorizontal;
		this.speedToKCalWalkVertical =speedToKCalWalkVertical;
		this.speedToKCalWalkHorizontal = speedToKCalWalkHorizontal;
	}

	/**
	 * Computes calories consumed by a person with 'height' performing 'steps' in unit of time specified.
	 * 
	 * @param steps - # of steps in timeUnit
	 * @param height - person height in meters
	 * @param mass - person mass in Kg
	 * @param fractionalGrade - treadmill inclination
	 * @param activity - 0 for run, 1 for walk, 2 rest
	 * @return
	 */
	public CaloriesCountResult caloriesFromSteps(float steps, float height, float mass, float fractionalGrade, int activity) {

		CaloriesCountResult result = new CaloriesCountResult();
		result.setDistance(0);
		
		float vo2 = 0; // volume of oxygen in milliliter
		if (activity == 2) {
			vo2 = kCalNoAction;
		} else {
			// compute equivalent of steps in stepToHeightProportion time dimension
			float stepsInStepToHeightTimeUnit = (float)steps * stepToHeightTimeUnit / timeUnit;
			int index = (int)stepsInStepToHeightTimeUnit;
			float proportion = 1;

			if (index >=0) {
				if (index < stepToHeightProportion.length) {
					proportion = stepToHeightProportion[index];
				} else {
					proportion = stepToHeightProportion[stepToHeightProportion.length-1];
				}
				float stepsDistance = (height * proportion) * steps;
				result.setDistance(stepsDistance);
				Log.v("kCal", stepsDistance + " stepsDistance  "+ (height * proportion) + " one step "+ + steps +" act" +activity);
				// in (meter/minute) 
				float speed = stepsDistance / (timeUnit / 60000f); 
				
				Log.v("kCal", speed + " speed m/min");
				
				if (activity == 0) { // run
					vo2 = speedToKCalRunHorizontal * speed + speedToKCalRunVertical * speed *fractionalGrade + kCalNoAction;
				} else { // walk
					vo2 = speedToKCalWalkHorizontal * speed + speedToKCalWalkVertical * speed *fractionalGrade + kCalNoAction;
				}
			}
		}
		
		float kCalPerMinute = vo2/1000 * mass * kcalOnLitre;
		
		result.setkCal(kCalPerMinute * timeUnit / 60000);
		
		return result;
	}

}
