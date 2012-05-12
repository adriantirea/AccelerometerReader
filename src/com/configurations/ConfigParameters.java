package com.configurations;

import org.achartengine.chart.PointStyle;

import android.graphics.Color;

public interface ConfigParameters {

	/** Period of sampling row acceleration.  In milliseconds. Acceleration readings are transformed at this fixed rate of sampling */
	double FIXED_SAMPLE_RATE = 15;
	
	/** In milliseconds = period of classification. every STEP_TIME identify activity. */
	long STEP_TIME = 1920;  
	int HOUR = 3600 * 1000;
	int DAY = 24 * HOUR;
	
	/** ActivityValidator - replace continuous activities 'VALIDATOR_INTERES_ACTIVITIES' with 'VALIDATOR_DETACHED_ACTIVITY' if longer than 'VALIDATOR_CONTINUOUS_THRESHOLD' */ 
	String[] VALIDATOR_INTERES_ACTIVITIES = new String[]{"sitUp", "sitDown"};
	int VALIDATOR_CONTINUOUS_THRESHOLD = 909; // in STEP_TIME. 909 = 30 minute
	String VALIDATOR_DETACHED_ACTIVITY = "detached"; 
	
	/** Classes of activity to be identified. */
	String[] CLASSES_ANN = new String[]{"run", "walk", "sitUp", "sitDown", "other"};
	String[] CLASSES = new String[]{"run", "walk", "sitUp", "sitDown", "other", "detached"};
	int[] COLORS = new int[] { Color.GREEN, Color.BLUE,Color.CYAN, Color.MAGENTA, Color.RED, Color.GRAY};
	PointStyle[] STYLES = new PointStyle[] { PointStyle.CIRCLE, PointStyle.DIAMOND, PointStyle.POINT, PointStyle.SQUARE, PointStyle.TRIANGLE, PointStyle.X };
	String[] CLASSES_PIEREPORT = new String[]{"run", "walk", "sitUp", "sitDown", "other", "detached", "systemOff"};
	int[] PIE_COLORS = new int[] { Color.GREEN, Color.BLUE,Color.CYAN, Color.MAGENTA, Color.RED, Color.GRAY, Color.BLACK };
	
	
	/** Period of time for detailed statistics report. Presents last RECENT_PAST milliseconds.*/
	int RECENT_PAST = (int)(1/1.0 * ConfigParameters.HOUR);
	/** Compress activities from 'GROUPING_STEP' by majority voting. in miliseconds*/
	int GROUPING_STEP = 60 * 1000; // 1 minute

	/** CaloriesTransformer parameters */
	/** index is the number of steps per 2 seconds. value at index is the proportion of height that transforms height to step length*/
	float[] STEP_FREQUENCY2SEC_TO_HEIGHT_PROPORTION = new float[]{0, 1f/5, 1f/6, 1f/3, 1f/2, (float)(1/1.2), 1, 1.1f, 1.2f, 1.2f, 1.2f};
	float STEP_TO_HEIGHT_TIME_UNIT = 2000; // miliseconds = 2 sec
	float KCAL_ON_LITRE = 5;
	float SPEED_TO_VO2_RUN_VERTICAL = 0.9f;
	float SPEED_TO_VO2_RUN_HORIZONTAL = 0.2f;
	float SPEED_TO_VO2_WALK_VERTICAL = 1.8f;
	float SPEED_TO_VO2_WALK_HORIZONTAL = 0.1f;
	float VO2_NO_ACTION = 3.5f; // ml/kg/min
	
	float TREADMILL_GRADE = 0f; // threadmill inclination. 
	
	
	
	/** Settings keyWords - for SharedPreferences storage*/
	String SETTINGS_KEY = "settings_key";
	String MASS_KEY = "mass";
	String HEIGHT_KEY = "height";
	String ALERT_NO_ACTION_KEY = "noActionTime";
	/** Default values for settings*/
	float MASS_DEFAULT = 70f; /** kg */
	float HEIGHT_DEFAULT = 1.7f; /** meters */
	float ALERT_NO_ACTION_DEFAULT = 0.007f; /** hours */
	
	
	/** Time for alarm until contact SMS and CallHelp */
	long ALERT_ON_UNTIL_CONTACT = 30000L; // 5 min
}
