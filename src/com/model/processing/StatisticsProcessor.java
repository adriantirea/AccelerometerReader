package com.model.processing;

import java.util.List;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class StatisticsProcessor {
	
	/**
	 * compute min, max, range, meanX, meanY, meanZ, mean, stdX, stdY, stdZ, std, corelationXY, corelationYZ, corelationZX - from time domain acceleration
	 * energy, entropy -  from FFT transformation, half of the elements of the real transform, the other half is symmetric.  
	 * @param window - List<AccelerationSample>
	 * @param partial1 - PartialStatisticsResult on first half of window
	 * @param partial2 - PartialStatisticsResult on second half of window
	 * @return StatisticsResult
	 */
	public static StatisticsResult computeStatisticForWindow(List<AccelerationSample> window,
			PartialStatisticsResult partial1, PartialStatisticsResult partial2) {
		
		int windowLength = window.size();
		
		StatisticsResult result = new StatisticsResult();
		// min, max, range
		if (partial1.min < partial2.min) {
			result.min = partial1.min; 
		} else {
			result.min = partial2.min;
		}
		
		if (partial1.max > partial2.max) {
			result.max = partial1.max; 
		} else {
			result.max = partial2.max;
		}
		
		result.range = result.max - result.min; 
		
		// mean
		result.mean = (partial1.sumAcc + partial2.sumAcc) / windowLength;
		double meanAccX = (partial1.sumAccX + partial2.sumAccX) / windowLength;
		double meanAccY = (partial1.sumAccY + partial2.sumAccY) / windowLength;
		double meanAccZ = (partial1.sumAccZ + partial2.sumAccZ) / windowLength;
		result.meanX = meanAccX;
		result.meanY = meanAccY;
		result.meanZ = meanAccZ;
		
		//std
		result.std = Math.sqrt((partial1.sumAcc2 + partial2.sumAcc2) / windowLength - result.mean * result.mean);
		result.stdX = Math.sqrt((partial1.sumAccX2 + partial2.sumAccX2) / windowLength - result.meanX * result.meanX);
		result.stdY = Math.sqrt((partial1.sumAccY2 + partial2.sumAccY2) / windowLength - result.meanY * result.meanY);
		result.stdZ = Math.sqrt((partial1.sumAccZ2 + partial2.sumAccZ2) / windowLength - result.meanZ * result.meanZ);
		
		// corelation between axes X, Y, Z
		double corelationXY = 0;
		double corelationYZ = 0;
		double corelationZX = 0;
		double[] fftWindow = new double[windowLength];
		int i=0;
		for (AccelerationSample sample : window) {
			corelationXY += (sample.accX - meanAccX) * (sample.accY - meanAccY);
			corelationYZ += (sample.accY - meanAccY) * (sample.accZ - meanAccZ);
			corelationZX += (sample.accZ - meanAccZ) * (sample.accX - meanAccX);
			fftWindow[i] = sample.acc;
			i++;
		}
		result.corelationXY = corelationXY / windowLength / (result.stdX * result.stdY);
		result.corelationYZ = corelationYZ / windowLength / (result.stdY * result.stdZ);
		result.corelationZX = corelationZX / windowLength / (result.stdZ * result.stdX);
		
		DoubleFFT_1D fft = new DoubleFFT_1D(windowLength);
		fft.realForward(fftWindow); // see jtransforms-2.4-doc // result in inputArray. only first half of fft. alternate re and img or rezults.
		double energy = 0;
		double entropy = 0;
		double magnitude = 0;
		@SuppressWarnings("unused")
		int indexMax = 2;
		double maxFreq= Double.MIN_VALUE;
		for (i = 2; i < windowLength; i+=2) {
			magnitude = Math.hypot(fftWindow[i], fftWindow[i+1]); 
			energy += magnitude;
			entropy += -magnitude * Math.log(magnitude);
			if(magnitude > maxFreq) {
				maxFreq = magnitude;
				indexMax = i;
			}
		}

		//double procentCutOffFromMax = (energy - 1000) * 0.3/5000 + 0.4;
		// calculat pe baza corespondentei energie -> cutoffFromMaxim 6000 -> 0.7, 1000 -> 0.4
		
		double procentCutOffFromMax = Math.min(Math.pow((energy), 1/3.0) / Math.pow(7000, 1/3.0) * 0.7 + 0.1, 1.0);
		// calculat pe baza corespondentei energie -> cutoffFromMaxim 7000 -> 0.8, 0 -> 0.1 variatie neliniara
		int indexFirstMax=2;
		for (i=2; i<windowLength; i+=2) {
			if(Math.hypot(fftWindow[i], fftWindow[i+1]) > maxFreq * procentCutOffFromMax) { 
				indexFirstMax = i;
				break;
			}
		}
		
		fftWindow[1] = 0; // value for Re[n/2] -- special case for this library
		for (i = indexFirstMax + 2; i< fftWindow.length; i+=2) {
			fftWindow[i] = 0;
			fftWindow[i+1] = 0;
		}
		fft.realInverse(fftWindow, true);
		int steps = 0;
		// fft is on entire window, while statistic is only on half of window. -> so I take into consideration the midle part of ifft
		for (i = windowLength / 4; i < (3 * windowLength / 4); i++) {
			if (fftWindow[i] > fftWindow[i-1] && fftWindow[i] > fftWindow[i+1]) {
				steps++;
			}
		}
		result.setSteps(steps);
		result.energy = energy;
		result.entropy = entropy;
		return result;
	}
	
	/**
	 * computes min, max, sumAccX, sumAccY, sumAccZ, sumAcc, sumAccX2, sumAccY2, sumAccZ2, sumAcc2,
	 * 
	 * @param window - ArrayList<AccelerationSample>
	 * @param a - start index
	 * @param b - end index
	 * @return StatisticsResult 
	 */
	
	public static PartialStatisticsResult computeStatisticBetweenIndexes(List<AccelerationSample> window,
			int indexStart, int indexStop) {
		
		assert (indexStop > indexStart && indexStart >= 0 && indexStop <= window.size());
		
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		double sumAcc = 0;
		double sumAccX = 0;
		double sumAccY = 0;
		double sumAccZ = 0;
		
		double sumAcc2 = 0;
		double sumAccX2 = 0;
		double sumAccY2 = 0;
		double sumAccZ2 = 0;
		
		for (int i = indexStart; i < indexStop; i++) {
			AccelerationSample sample = window.get(i);
			if (sample.acc < min) {
				min = sample.acc;
			} 
			if (sample.acc > max) {
				max = sample.acc;
			}
			sumAccX += sample.accX;
			sumAccY += sample.accY;
			sumAccZ += sample.accZ;
			sumAcc += sample.acc;
			
			sumAccX2 += sample.accX * sample.accX;
			sumAccY2 += sample.accY * sample.accY;
			sumAccZ2 += sample.accZ * sample.accZ;
			sumAcc2 += sample.acc * sample.acc;
		}
		
		PartialStatisticsResult result = new PartialStatisticsResult();
		result.min = min;
		result.max = max;
		result.sumAccX = sumAccX;
		result.sumAccY = sumAccY;
		result.sumAccZ = sumAccZ;
		result.sumAcc = sumAcc;
		
		result.sumAccX2 = sumAccX2;
		result.sumAccY2 = sumAccY2;
		result.sumAccZ2 = sumAccZ2;
		result.sumAcc2 = sumAcc2;
		
		return result;
	}
}
