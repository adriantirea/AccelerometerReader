package com.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.Date;

import com.model.processing.AccelerationSample;


import android.os.Environment;
import android.util.Log;

public class LoggerTxt {
	private final static String tag = "LoggerTxt";
	
	private final static String FOLDER = "accSamplesTxt";
	
	private String fileName;
	private  OutputStreamWriter osw; // write coordinates
	
	public LoggerTxt(String fileName) throws IOException{
		File directory = new File(Environment.getExternalStorageDirectory() + "/" +FOLDER);
		directory.mkdir();
		String filePath = Environment.getExternalStorageDirectory() +"/" + FOLDER+ "/" + fileName + ".txt";
		FileOutputStream fos = new FileOutputStream(filePath, false); // new file
		osw = new OutputStreamWriter(fos);
		
    	osw.append("--------------- "+ fileName +" ---------------\n");
    	osw.append(new Date().toString() + "\n");
	    osw.flush();
 		
		Log.v("testNull", osw.toString());
	}
	
	 public boolean writeToSdCard(AccelerationSample sample) {
	    	try {  
		        osw.append(sample.counterSample + "\t" + new DecimalFormat("#.###").format(sample.timestamp)+ 
		        		"\t" + sample.accX + "\t" + sample.accY + "\t" + sample.accZ + "\t" +sample.acc+"\n");
		        osw.flush();
		        return true;
		    } catch(IOException e) {
		    	Log.e(tag,"exception writeSD" + e.getMessage());
		    	return false;
		    }
	   }
	
	 public void closeLogger() throws IOException {
		 osw.close();
	 }
	 
	 public String getFileName () {
	 	return fileName;
	 }
}
