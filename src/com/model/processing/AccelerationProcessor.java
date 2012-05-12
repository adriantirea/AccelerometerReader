package com.model.processing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.configurations.ConfigParameters;
import com.model.database.ActivityEntry;
import com.model.database.DataSourceActivityEntry;
import com.model.processing.ann.NeuralNetwork;
import com.model.processing.filters.Filter;
import com.model.processing.filters.HighPassFilter;
import com.model.processing.filters.SmoothFilter;
import com.utils.FileWriterARFF;

import com.activities.gui.AlertActivity;

public class AccelerationProcessor {
	
	private static final String TAG = "AccelerationProcessor";
	private Context CONTEXT;
	
	private static final int SAMPLE_PERIOD = 15; // milisec
	private static final int WINDOW_SIZE = 256;
	private static final int HALF_WINDOW_SIZE = WINDOW_SIZE /2;
	private static final int SMOOTH_TIMES = 5;
	private static final int SMOOTH_ORDER = 5;
	private static final boolean HIGH_PASS = true;
	private static final int HIGH_PASS_ORDER = 2; // out = x(i+1) - x(i-1); // not to be used yet
	
	
	private FileWriterARFF arff; // write textFile for WEKA
	private StepCounter stepCounter;
	private long stepTotalCounter = 0;
	private long stepTotalCounterHighPass = 0;
	private long stepTotalCounterFFT = 0;
	private List<AccelerationSample> window; // smooth values
	
	private DataSourceActivityEntry dataSource;
	
	private Filter[] filters;	
	
	private NeuralNetwork ann;
	
	private ActivityValidator detachedValidator;
	
	private CaloriesTransformer caloriesTransformer;
	
	private PartialStatisticsResult pastPartialResult;
	
	private String activityClass = "?"; // to be classified
	
	
	public AccelerationProcessor (String activityName, Context context) {
		
		CONTEXT = context;
		dataSource = new DataSourceActivityEntry(CONTEXT);
		dataSource.open();
		// initialize window, pastPartialResult;
		window = new ArrayList <AccelerationSample> (WINDOW_SIZE);
		for (int i=0; i< WINDOW_SIZE; i++) {
			window.add(new AccelerationSample());
		}
		
		pastPartialResult = new PartialStatisticsResult();
		
		// create filters
		if (HIGH_PASS) {
			filters = new Filter[SMOOTH_TIMES + 1];
			filters[filters.length - 1] = new HighPassFilter(HIGH_PASS_ORDER); 
		} else {
			filters = new Filter[SMOOTH_TIMES];
		}
		
		for (int i=0; i < SMOOTH_TIMES; i++) {
			filters[i] = new SmoothFilter(SMOOTH_ORDER);
		}
		
		// create arff for WEKA
		try {
			arff =  new FileWriterARFF(activityName,
					new String[] {"min", "max", "range",
					"meanX", "meanY", "meanZ", "mean",
					"stdX", "stdY", "stdZ", "std", 
					"corelationXY", "corelationYZ", "corelationZX",
					"energy", "entropy",
					"class"});
		} catch (IOException e) {
			Log.e(TAG, "FileWriterARFF not created - " + e.getMessage());
			return ;
		}
		
		stepCounter = new StepCounter(activityName, HIGH_PASS);
		stepTotalCounter = 0;
		stepTotalCounterHighPass = 0;
		stepTotalCounterFFT = 0;
		
		initializeANN();
		detachedValidator = new ActivityValidator(ConfigParameters.VALIDATOR_CONTINUOUS_THRESHOLD, ConfigParameters.VALIDATOR_DETACHED_ACTIVITY, ConfigParameters.VALIDATOR_INTERES_ACTIVITIES);
		caloriesTransformer = new CaloriesTransformer(ConfigParameters.STEP_FREQUENCY2SEC_TO_HEIGHT_PROPORTION,
				ConfigParameters.STEP_TO_HEIGHT_TIME_UNIT,
				ConfigParameters.STEP_TIME,
				ConfigParameters.KCAL_ON_LITRE,
				ConfigParameters.SPEED_TO_VO2_RUN_VERTICAL,
				ConfigParameters.SPEED_TO_VO2_RUN_HORIZONTAL,
				ConfigParameters.SPEED_TO_VO2_WALK_VERTICAL,
				ConfigParameters.SPEED_TO_VO2_WALK_HORIZONTAL,
				ConfigParameters.VO2_NO_ACTION);
	}
	
	/**
	 * close opened files
	 */
	public void cancelAccelerationProcessor() {
		try {
			arff.closeLogger();
			stepCounter.writeToLogTotalCounters(stepTotalCounter, stepTotalCounterHighPass, stepTotalCounterFFT);
			stepCounter.closeLogger();
			dataSource.close();
		} catch (IOException e) {
			Log.e(TAG, "exception close loggers " + e.getMessage());
		}
	}

	public void addSample(AccelerationSample sample) {
		//Log.v(TAG, " addSample ");
		
		// process
		AccelerationSample newFilteredSample = sample;
/*		//1. filter smooth - not used anymore
		// ------------- !! for testing. HIGH_PASS result in accFilteredHighPass!! --------------
		for (int i=0; i < filters.length; i++) {
			newFilteredSample = filters[i].filterValue(newFilteredSample);
		} //stepLogger.writeToSdCard(""+newFilteredSample.accFiltered);
		//1 end.
*/

		//2. result of filter add in window
		window.remove(0);
		window.add(newFilteredSample);
		//2. end

		//3. compute statistics for NeuralNetwork -- write in features.arff
		if (newFilteredSample.counterSample % HALF_WINDOW_SIZE == 1) { // == 1 to avoid start delay of filtering
			StatisticsResult statisticResult = computeStatisticOnWindow(window);
			
			// 3.1 call ANN clasifier
			String classifiedClass = ann.classify(statisticResult.getValues());
			// 3.1 end
			
			// 3.2 if long a rest activity => "detached"
			classifiedClass = detachedValidator.validateActivity(classifiedClass);
			// 3.2 end
			
			// 3.3 counter steps if run or walk. compute kcalories
			int steps = statisticResult.getSteps();
			CaloriesCountResult calPerActivity;
			
			SharedPreferences settings = CONTEXT.getSharedPreferences(ConfigParameters.SETTINGS_KEY, 0);
			float mass = settings.getFloat(ConfigParameters.MASS_KEY, ConfigParameters.MASS_DEFAULT);
			float height = settings.getFloat(ConfigParameters.HEIGHT_KEY, ConfigParameters.HEIGHT_DEFAULT);
			float alertTime = settings.getFloat(ConfigParameters.ALERT_NO_ACTION_KEY, ConfigParameters.ALERT_NO_ACTION_DEFAULT);
			if ("run".equals(classifiedClass)) {
				calPerActivity = caloriesTransformer.caloriesFromSteps(steps, height, mass, ConfigParameters.TREADMILL_GRADE, 0);
				stepTotalCounterFFT += steps; 
			} else if ("walk".equals(classifiedClass)) {
				calPerActivity = caloriesTransformer.caloriesFromSteps(steps, height, mass, ConfigParameters.TREADMILL_GRADE, 1);
				stepTotalCounterFFT += steps;
			} else {
				calPerActivity = caloriesTransformer.caloriesFromSteps(steps, height, mass, ConfigParameters.TREADMILL_GRADE, 2);
			}
			// 3.3 end
			
			// 3.4 fire noActivityAlarm
			Log.v(TAG," alert unit " + (int)(alertTime * ConfigParameters.HOUR / ConfigParameters.STEP_TIME));
			if (detachedValidator.fireNoActionAlarm((int)(alertTime * (ConfigParameters.HOUR / ConfigParameters.STEP_TIME)))) {
				detachedValidator.dezactivateFireNoActionAlarm();
				Log.v(TAG," should fire!");
				
				Intent myIntent = new Intent(CONTEXT, AlertActivity.class);
				myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				CONTEXT.startActivity(myIntent);
			}
			// 3.4 end
			
			// 3.5 write result to database
			ActivityEntry act = new ActivityEntry();
			act.setName(classifiedClass);
			act.setStartDate(new Date().getTime() - HALF_WINDOW_SIZE * SAMPLE_PERIOD); // milisec
			act.setDuration(HALF_WINDOW_SIZE * SAMPLE_PERIOD);
			act.setSteps(steps);
			act.setkCal(calPerActivity.getKCal());
			act.setDistance(calPerActivity.getDistance());
			dataSource.insertActivityEntry(act);
			Log.v(TAG, "insertActivityEntry");
			// 3.5 end
		}
		//3. end
		
/*		//4. step counter
		// compute only windows of WINDOW_SIZE, not overlapped - not used anymore
		if (newFilteredSample.counterSample % WINDOW_SIZE == 1) {
			int steps = stepCounter.computeSteps(window);
			if (steps > -1) {
				stepTotalCounter += steps; 
			}
			if (HIGH_PASS) {
				int stepsHighPass = stepCounter.computeStepsHighPass(window);
				if (stepsHighPass > -1) {
					stepTotalCounterHighPass += stepsHighPass; 
				}	
			}
		}
		//4. end
*/
	}	
	
	private StatisticsResult computeStatisticOnWindow(List<AccelerationSample> windowCurrent) {
		PartialStatisticsResult newPartialResult = StatisticsProcessor.computeStatisticBetweenIndexes(windowCurrent, windowCurrent.size()/2, windowCurrent.size());
		StatisticsResult sr = StatisticsProcessor.computeStatisticForWindow(windowCurrent, newPartialResult, pastPartialResult);
		pastPartialResult = newPartialResult;
		
		//-- write in the same order as attributes in arff constructor !!!
		arff.writeDataOfALine(new String[]{sr.min+"", sr.max+"", sr.range+"",
				sr.meanX+"",sr.meanY+"",sr.meanZ+"",sr.mean+"",
				sr.stdX+"",sr.stdY+"",sr.stdZ+"",sr.std+"",
				sr.corelationXY+"", sr.corelationYZ+"", sr.corelationZX+"",
				sr.energy+"", sr.entropy+"",
				activityClass
				});
		return sr;
	}
	
	public void setActivityClass (String activityClass) {
		this.activityClass = activityClass;
	}
	
	private void initializeANN() {
		double[][] level1Weights = new double[][]{
				{-4.710748528759523,-3.0895336014098853,-0.7421281155383287,-0.380124875947893,-0.09343771150419852,-1.5284356541364332,3.4852514539931922,3.8926498656789374,13.537158093195554,-7.043719952796409,-6.996719440668875,0.24226380970913933,-0.320758752939335,-7.311076005707428,4.390758213736647,-1.3288536937708681,1.2808879495383567}, 
				{-0.07091923700201998,2.5499869926853145,1.4638516887322697,-0.2237778081946888,16.55792742458851,-5.602654689834647,6.061950999512909,-8.268495097298926,-2.98840693018718,-1.6002087192034562,0.3362002792448079,0.8352995361294564,-0.9031795919701208,-0.2096854241257822,3.2649775397892338,-2.823786438518421,-7.518747677224303}, 
				{0.8934376447071982,2.1914147723636237,1.909813986298383,1.3698281518365034,-3.9678494012726113,-2.635928348289916,3.1743793150253983,-5.334504772312236,1.1421437072063514,-0.8630535649044782,-0.4435928686023062,4.889045160749173,7.144683434222729,2.621680168900969,-0.3929643329573144,1.0527191968399092,7.545487301312632}, 
				{2.3840477765883885,5.3669062182423986,2.9558409724051176,0.761409157744066,-0.34810940054346734,1.7502776474826534,-2.507698301270398,5.9759533923869546,-6.000589858664195,1.6619268332380746,-14.33112910022799,2.489538840610263,1.9167634513137428,-1.8373410039590043,1.543338199764837,0.9315139876185358,-6.284698131597335}, 
				{1.6957896021581553,-3.405578096233592,-3.1732149048165423,0.2581641448370511,10.851324661810255,2.6092505682987297,0.9721128150638788,3.7365315411956024,-7.273525213141452,3.8723327099281275,-1.3501814595498765,2.52811001786833,-0.36005632362350154,0.9677872817921651,0.8235448498038894,-1.7566256075361815,1.1202278793895337}, 
				{0.14232497742755582,-0.5573054304982035,-0.5384528664839359,-2.700395305829463,-1.1341953744093067,0.7602554392767394,-7.068773473704021,-4.415296877997879,-5.358885467777259,-1.2565558803775712,-0.031964335082808076,0.4707663844327222,0.03460501265566055,1.1440403462039122,-0.11651310436315081,-0.569076410729771,-0.6307349616952248}, 
				{-6.384871591146896,-0.7831594159755727,1.661867516936726,0.11527024816849062,0.832690895701099,1.172673875807436,-2.315819158605012,10.204218365171004,9.23138456860162,0.7241382910688807,-16.157472892039866,-1.9218579811359746,-1.1725014717358344,-0.5358003694518114,2.947035534624466,3.551876795693394,1.092926937979357}, 
				{2.099339241143832,-3.2884807375256075,-3.76989787167704,-4.524800351149317,0.7410055115891245,-6.256277160213326,4.235945929145885,0.9571281632356338,6.093181347554056,-6.9628449238992625,-7.9440153283535855,-0.5958392665511153,3.5236556714242866,-1.553969480853872,-1.2486877759582378,1.2409990505586104,-6.902237102330495}, 
				{-2.066955605683804,2.224845367475923,2.308725538256438,1.5933545921880248,-15.438311481017523,3.446314450697061,0.6651635597733626,-0.7430676643409008,-0.4436027355342434,-0.4339579823109305,3.189024642938061,-0.8197773025917765,-1.9934086910195292,0.04334030220037732,-0.35163544705415667,0.39325744998988865,-3.2403836338248664}, 
				{-3.683339085403613,2.7396154789657587,4.037807074912884,3.265923502042906,-5.384705893031416,0.524855690207762,-6.0556935368891045,2.0469487329581626,5.634516155702697,6.143985492670666,0.19972438985899327,-0.791535608124231,-3.8278140340472997,1.670749820248464,-1.2954970962142838,3.396122417881749,8.463178331592395}
				}; //
		
		double[][] level2Weights = new double[][]{
				{11.675246440962754, 7.556058956232902, 0.5722908368378841, -0.1899912310133939, -8.620142454445169, -9.111300925048548, 10.31544017801782, -11.106910915638794, 0.9196822034130985, -2.0843037355425054, -4.205382317684497}, 
				{-13.358768952348797, 7.584989295793944, 4.001532337317678, -9.873095886191646, -7.798190138478053, 9.437011494177522, -10.763592262012345, -15.398871731605274, -2.2595579177049623, 5.135733193871791, -0.8924179335813367}, 
				{-7.0873225462751615, 17.40836295823695, -7.340765342838089, 6.6164403837534325, -7.491009226383534, -0.6084573175257479, 5.364737033785186, 9.915226447411092, 11.176284040927492, -1.5329268774597233, -10.296989417952313}, 
				{2.908600653353129, -16.24885726485126, 4.135521945942276, -7.228683685744288, 8.701929317134738, -3.146674269492809, -6.194743456984481, 8.202724280874016, -8.297588001791185, -6.994446852169575, -4.346776157448324}, 
				{-8.52689515552537, -8.726729394893429, -13.188464283777236, 13.223281343120345, -3.7448958330664293, -5.254431446045068, 7.707623380861856, 6.332541147254402, -14.318160948094835, 11.738775804106504, -1.970658860080768}			};

		double[] inputsTrainingRange = new double [] {10.005102880299091,23.00916290283203,29.2474946975708,19.66494697611779,20.753757771861274,19.712199807167053,8.931472799740732,11.181956411508926,12.271713789419694,11.475648132847137,8.239381461262786,1.9764089261064126,1.9561083724873478,1.9637799799251665,10306.802398615702,48653.01544113095};
		double[] inputsTrainingMedian = new double [] {5.1246391497552395,20.85598373413086,14.709374904632568,-0.10762109560891986,-0.1485968419292476,-0.20015724748373032,13.701842948328704,5.605912886852832,6.148716545405288,5.755576107663064,4.1350393544363575,-0.0024448436105503624,0.0015969721438072737,0.006780926842583301,5177.388495749168,-24290.275570478712};
		
		String[] classes = ConfigParameters.CLASSES_ANN;
		ann = new NeuralNetwork(level1Weights, level2Weights, inputsTrainingRange, inputsTrainingMedian, classes);

	}
}