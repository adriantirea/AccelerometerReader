package com.model.processing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.util.Log;

import com.utils.GeneralLogger;

public class StepCounter {
	
	private static final String TAG = "StepCounter";
	
	private static final float MAXIM_ACC_RANGE_TOTALYNOACTION_THRESHOLD = 0.1f; 
	private static final float MAXIM_ACC_RANGE_NOACTION_THRESHOLD = 0.5f;	
	private static final int MINIM_ACC_RANGE_STEP_THRESHOLD = 3; // for walk and run step counting
	private static final float STEP_THRESHOLD_FROM_RANGE = 0.1f;
	
	private static final float MAXIM_ACC_RANGE_TOTALYNOACTION_THRESHOLD_HIGHPASS = 0.1f; 
	private static final float MAXIM_ACC_RANGE_NOACTION_THRESHOLD_HIGHPASS = 0.5f;
	private static final int MINIM_ACC_RANGE_STEP_THRESHOLD_HIGHPASS = 1; // for walk and run step counting HIGH_PASS after smooth
 	private static final float STEP_THRESHOLD_FROM_RANGE_HIGHPASS = 0.1f;
	
	private static final int WINDOW_MINIM_POSSIBLE_STEP = 3;
	private static final int WINDOW_MAXIM_POSSIBLE_STEP = WINDOW_MINIM_POSSIBLE_STEP * 4;
	private static final int STEP_MAXIMUM_VARIATION_PERIOD_ON_WINDOW = 15;// * samplesPeriod = time 
	
	
	private GeneralLogger stepLogger;
	private GeneralLogger stepLoggerHighPass;
	
	private boolean highPass = false;
	
	public StepCounter (String activityName, boolean HIGH_PASS ) {
		highPass = HIGH_PASS;
		// create logger for steps
		try {
			stepLogger = new GeneralLogger(activityName);
			stepLogger.writeToSdCard("\n");
			stepLogger.writeToSdCard("----------------------------------------------");
			stepLogger.writeToSdCard(new Date().toString());
		} catch (IOException e) {
			Log.e(TAG,"exception writeSD " + e.getMessage());
		}
		if (highPass) {
			try {
				stepLoggerHighPass = new GeneralLogger(activityName + "HighPass");
				stepLoggerHighPass.writeToSdCard("\n");
				stepLoggerHighPass.writeToSdCard("---------------------------------------------- HIGHT_PASS = "+HIGH_PASS);
				stepLoggerHighPass.writeToSdCard(new Date().toString());
			} catch (IOException e) {
				Log.e(TAG,"exception writeSD " + e.getMessage());
			}
		}
	}
	
	/**
	 * 
	 * @param windowCurrent List<AccelerationSample>
	 * @return number of steps; -1 if does not pass thresholds
	 */
	public int computeSteps(List<AccelerationSample> windowCurrent) {
		
		assert(windowCurrent.size() > 1);
		//3. actualize min, max
		int steps = -1;
		AccelerationSample newFilteredSample = windowCurrent.get(windowCurrent.size()-1);
		AccelerationSample minSample = windowCurrent.get(0);
		AccelerationSample maxSample = windowCurrent.get(0);
		for (AccelerationSample accSample : windowCurrent){
			if (minSample.accFiltered > accSample.accFiltered) {
				minSample = accSample;
			}
			if (maxSample.accFiltered < accSample.accFiltered) {
				maxSample = accSample;
			}
		}
		float range = maxSample.accFiltered - minSample.accFiltered;
		if (range > MINIM_ACC_RANGE_STEP_THRESHOLD) {
			float thresholdForStep = minSample.accFiltered + STEP_THRESHOLD_FROM_RANGE * range;
			ArrayList<AccelerationSample> minims = new ArrayList<AccelerationSample>();
			for (int i = 1; i < windowCurrent.size()-1; i++) {
				AccelerationSample accSample = windowCurrent.get(i);
				if ((accSample.accFiltered < thresholdForStep) 
						&& (accSample.accFiltered <= windowCurrent.get(i-1).accFiltered) 
						&& (accSample.accFiltered < windowCurrent.get(i+1).accFiltered)) {
					minims.add(accSample);
					stepLogger.writeToSdCard("m" + i + ": " + accSample.counterSample);
				}
			}
			if ((minims.size() < WINDOW_MINIM_POSSIBLE_STEP) 
					|| (minims.size() > WINDOW_MAXIM_POSSIBLE_STEP)) {
				stepLogger.writeToSdCard("window at: " + newFilteredSample.counterSample +" does not pas WINDOW_MINIM_POSSIBLE_STEP or WINDOW_MAXIM_POSSIBLE_STEP");
			} else {
				float meanStepPeriod = minims.get(minims.size()-1).counterSample - minims.get(0).counterSample;
				meanStepPeriod /= (minims.size() - 1);
				float deviation = 0;
				for (int k = 1; k < minims.size(); k++) {
					deviation += Math.abs(meanStepPeriod - (minims.get(k).counterSample - minims.get(k-1).counterSample));
				}
				deviation /= (minims.size() - 1);
				if (deviation > STEP_MAXIMUM_VARIATION_PERIOD_ON_WINDOW) {
					stepLogger.writeToSdCard("window at: " + newFilteredSample.counterSample +" # steps = " + minims.size() + "period of steps = " + meanStepPeriod 
							+"(in samples) does not pass STEP_MAXIMUM_VARIATION_PERIOD_ON_WINDOW = " + STEP_MAXIMUM_VARIATION_PERIOD_ON_WINDOW 
							+ "deviation = " + deviation);
				} else { 
					stepLogger.writeToSdCard("window at: " + newFilteredSample.counterSample +" # steps = " + minims.size() + "period of steps = " + meanStepPeriod +"(in samples)");
					steps = minims.size();
					// number of steps holds thresholds
				}
			}
		} else {
			// test if NOACTION
			if (range < MAXIM_ACC_RANGE_TOTALYNOACTION_THRESHOLD) {
				stepLogger.writeToSdCard("window at: " + newFilteredSample.counterSample + " range = " + range + " under MAXIM_ACC_RANGE_TOTALYNOACTION_THRESHOLD = " + MAXIM_ACC_RANGE_TOTALYNOACTION_THRESHOLD);
			} else 	if (range < MAXIM_ACC_RANGE_NOACTION_THRESHOLD) {
				stepLogger.writeToSdCard("window at: " + newFilteredSample.counterSample + " range = " + range + " under MAXIM_ACC_RANGE_NOACTION_THRESHOLD = " + MAXIM_ACC_RANGE_NOACTION_THRESHOLD);					
			} else {
				stepLogger.writeToSdCard("window at: " + newFilteredSample.counterSample + " range = " + range + " NOT CLASSIFIED\n" 
						+ " between MAXIM_ACC_RANGE_NOACTION_THRESHOLD = " + MAXIM_ACC_RANGE_NOACTION_THRESHOLD 
						+ " and MINIM_ACC_RANGE_STEP_THRESHOLD = " + MINIM_ACC_RANGE_STEP_THRESHOLD);
			}
		}
		return steps;
		//3. end
	}

	/**
	 * 
	 * @param windowCurrent List<AccelerationSample>
	 * @return number of steps; -1 if does not pass thresholds
	 */
	public int computeStepsHighPass(List<AccelerationSample> windowCurrent) {
		
		assert(windowCurrent.size() > 1);
		//3. actualize min, max
		int steps = -1;
		AccelerationSample newFilteredSample = windowCurrent.get(windowCurrent.size()-1);
		AccelerationSample minSample = windowCurrent.get(0);
		AccelerationSample maxSample = windowCurrent.get(0);
		for (AccelerationSample accSample : windowCurrent) {
			if (minSample.accFilteredHighPass > accSample.accFilteredHighPass) {
				minSample = accSample;
			}
			if (maxSample.accFilteredHighPass < accSample.accFilteredHighPass) {
				maxSample = accSample;
			}
		}
		float range = maxSample.accFilteredHighPass - minSample.accFilteredHighPass;
		if (range > MINIM_ACC_RANGE_STEP_THRESHOLD_HIGHPASS) {
			float thresholdForStep = maxSample.accFilteredHighPass - STEP_THRESHOLD_FROM_RANGE_HIGHPASS * range;
			ArrayList<AccelerationSample> maxims = new ArrayList<AccelerationSample>();
			for (int i = 1; i < windowCurrent.size()-1; i++) {
				AccelerationSample accSample = windowCurrent.get(i);
				if ((accSample.accFilteredHighPass > thresholdForStep) 
						&& (accSample.accFilteredHighPass >= windowCurrent.get(i-1).accFilteredHighPass) 
						&& (accSample.accFilteredHighPass > windowCurrent.get(i+1).accFilteredHighPass)) {
					maxims.add(accSample);
					stepLoggerHighPass.writeToSdCard("m" + i + ": " + accSample.counterSample);
				}
			}
			if ((maxims.size() < WINDOW_MINIM_POSSIBLE_STEP) 
					|| (maxims.size() > WINDOW_MAXIM_POSSIBLE_STEP)) {
				stepLoggerHighPass.writeToSdCard("window at: " + newFilteredSample.counterSample +" does not pas WINDOW_MINIM_POSSIBLE_STEP or WINDOW_MAXIM_POSSIBLE_STEP");
			} else {
				float meanStepPeriod = maxims.get(maxims.size()-1).counterSample - maxims.get(0).counterSample;
				meanStepPeriod /= (maxims.size() - 1);
				float deviation = 0;
				for (int k = 1; k < maxims.size(); k++) {
					deviation += Math.abs(meanStepPeriod - (maxims.get(k).counterSample - maxims.get(k-1).counterSample));
				}
				deviation /= (maxims.size() - 1);
				if (deviation > STEP_MAXIMUM_VARIATION_PERIOD_ON_WINDOW) {
					stepLoggerHighPass.writeToSdCard("window at: " + newFilteredSample.counterSample +" # steps = " + maxims.size() + "period of steps = " + meanStepPeriod 
							+"(in samples) does not pass STEP_MAXIMUM_VARIATION_PERIOD_ON_WINDOW = " + STEP_MAXIMUM_VARIATION_PERIOD_ON_WINDOW 
							+ "deviation = " + deviation);
				} else { 
					stepLoggerHighPass.writeToSdCard("window at: " + newFilteredSample.counterSample +" # steps = " + maxims.size() + "period of steps = " + meanStepPeriod +"(in samples)");
					steps = maxims.size();
					// number of steps holds thresholds
				}
			}
		} else {
			// test if NOACTION
			if (range < MAXIM_ACC_RANGE_TOTALYNOACTION_THRESHOLD_HIGHPASS) {
				stepLoggerHighPass.writeToSdCard("window at: " + newFilteredSample.counterSample + " range = " + range + " under MAXIM_ACC_RANGE_TOTALYNOACTION_THRESHOLD_HIGHPASS = " + MAXIM_ACC_RANGE_TOTALYNOACTION_THRESHOLD_HIGHPASS);
			} else 	if (range < MAXIM_ACC_RANGE_NOACTION_THRESHOLD_HIGHPASS) {
				stepLoggerHighPass.writeToSdCard("window at: " + newFilteredSample.counterSample + " range = " + range + " under MAXIM_ACC_RANGE_NOACTION_THRESHOLD_HIGHPASS = " + MAXIM_ACC_RANGE_NOACTION_THRESHOLD_HIGHPASS);					
			} else {
				stepLoggerHighPass.writeToSdCard("window at: " + newFilteredSample.counterSample + " range = " + range + " NOT CLASSIFIED\n" 
						+ " between MAXIM_ACC_RANGE_NOACTION_THRESHOLD_HIGHPASS = " + MAXIM_ACC_RANGE_NOACTION_THRESHOLD_HIGHPASS 
						+ " and MINIM_ACC_RANGE_STEP_THRESHOLD_HIGHPASS = " + MINIM_ACC_RANGE_STEP_THRESHOLD_HIGHPASS);
			}
		}
		return steps;
		//3. end
	}
	
	public void writeToLogTotalCounters(long stepTotalCounter, long stepTotalCounterHighPass, long stepTotalCounterFFT) {
		stepLogger.writeToSdCard("-----------------------------------------");
		stepLogger.writeToSdCard("---------- stepTotalCounter = " + stepTotalCounter);
		stepLogger.writeToSdCard("---------- stepTotalCounterFFT = " + stepTotalCounterFFT);
		if (highPass) {
			stepLoggerHighPass.writeToSdCard("-----------------------------------------");
			stepLoggerHighPass.writeToSdCard("---------- stepTotalCounterHighPass = " + stepTotalCounterHighPass);
		}
	}
	
	public void closeLogger() throws IOException {
		stepLogger.closeLogger();
		if (highPass) {
			stepLoggerHighPass.closeLogger();
		}
	}
}
