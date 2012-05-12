package com.utils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.os.Environment;
import android.util.Log;


public class FileWriterARFF {
	
	private static String tag = "FileWriterARFF";
	
	private final static String FOLDER = "arff";
	
	private static final String RELATION_TAG = "@RELATION";
	private static final String ATTRIBUTE_TAG = "@ATTRIBUTE";
	private static final String ATTRIBUTE_NUMERIC_TYPE = "NUMERIC"; // use only numeric data
	private static final String ATTRIBUTE_CLASS_TYPE_START = "{"; // use only for last attribute
	private static final String ATTRIBUTE_CLASS_TYPE_END = "}"; // use only for last attribute
	private static final String DATA_TAG = "@DATA";
	private static final String MISSING_VALUE_TAG = "?";
	private static final String SEPARATOR_TAG = ", ";
	
	
	
	private static final String[] CLASSES_DEFFINITION = new String[]{"run", "walkFast", "walk", "walkSlow", "walkVerySlow"}; //
	private static final String OUTPUT_FILE = "features.arff";

	private  OutputStreamWriter osw; // write coordinates
	private String fileName;
	
	public FileWriterARFF() throws IOException{
		this(OUTPUT_FILE, "Activity128",
				new String[]{"min", "max", "range",
						"meanX", "meanY", "meanZ", "mean",
						"stdX", "stdY", "stdZ", "std", 
						"corelationXY", "corelationYZ", "corelationZX",
						"energy", "entropy",
						"class"},
				CLASSES_DEFFINITION);
	}
	
	public FileWriterARFF(String fileName, String[] attributes) throws IOException{
		this(fileName+".arff",
				"Activity128",
				attributes,
				CLASSES_DEFFINITION);
	}
	
	public FileWriterARFF(String fileName, String relation, String[] attributs, String[] classElements) throws IOException{
		this.fileName = fileName;
		File directory = new File(Environment.getExternalStorageDirectory() + "/" +FOLDER);
		directory.mkdir();
		String filePath = Environment.getExternalStorageDirectory() +"/" +FOLDER+ "/" + fileName;
		FileOutputStream fos = new FileOutputStream(filePath, false); // new file
		osw = new OutputStreamWriter(fos);
		
		
		writeLineToFile(RELATION_TAG + " " + relation);
		writeLineToFile("");

		for (int i=0; i< attributs.length - 1; i++) {
			writeLineToFile(ATTRIBUTE_TAG + " " + attributs[i] + " " + ATTRIBUTE_NUMERIC_TYPE);			
		}
		String classElems = "";
		for (String elem : classElements) {
			classElems += elem + SEPARATOR_TAG;
		}
		classElems = classElems.substring(0, classElems.length() - SEPARATOR_TAG.length());
		
		writeLineToFile(ATTRIBUTE_TAG + " " + attributs[attributs.length - 1] + " " + ATTRIBUTE_CLASS_TYPE_START + classElems + ATTRIBUTE_CLASS_TYPE_END);
		writeLineToFile(DATA_TAG);
	}	
	

	public boolean writeDataOfALine(String[] data) {
		StringBuilder line = new StringBuilder("");
		for (int i=0; i < data.length - 1; i++) {
			if (data[i] == null || "".equals(data[i])) {
				line.append(MISSING_VALUE_TAG);
			} else {
				line.append(data[i]);
			}
			line.append(SEPARATOR_TAG);
		}
		
		if (data.length > 0) {
			if (data[data.length-1] == null || "".equals(data[data.length-1])) {
				line.append(MISSING_VALUE_TAG);
			} else {
				line.append(data[data.length-1]);
			}
		}
		
		return writeLineToFile(line.toString());
	}
	
	public boolean writeCommentLineToFile(String commentMessage) {
		return writeLineToFile("% "+commentMessage);
	}
	
	private boolean writeLineToFile(String message) {
		try {  
			osw.append(message+"\n");
			osw.flush();
			return true;
		} catch(IOException e) {
			Log.e(tag, "could not write file." + e.getMessage());
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
