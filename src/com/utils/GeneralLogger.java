package com.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.os.Environment;
import android.util.Log;

public class GeneralLogger {

	private static String tag = "GeneralLogger";

	private final static String FOLDER = "accResultsTxt";
	
	private String fileName;
	private  OutputStreamWriter osw; // write coordinates

	public GeneralLogger(String fileName) throws IOException{
		File directory = new File(Environment.getExternalStorageDirectory() + "/" +FOLDER);
		directory.mkdir();
		String filePath = Environment.getExternalStorageDirectory() + "/" +FOLDER+ "/" + fileName + ".txt";
		FileOutputStream fos = new FileOutputStream(filePath, false); // new file
		osw = new OutputStreamWriter(fos);
	}

	public boolean writeToSdCard(String message) {
		try {  
			osw.append(message+"\n");
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

