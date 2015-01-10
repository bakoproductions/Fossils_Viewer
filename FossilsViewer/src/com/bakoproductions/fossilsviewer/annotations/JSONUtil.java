package com.bakoproductions.fossilsviewer.annotations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Environment;

public class JSONUtil {
	private static final String DIRECTORY_NAME = "/FossilsViewer/";
	
	private static final String ANNOTATIONS_ARRAY = "annotations";
	private static final String ID_KEY = "id";
	private static final String X_KEY = "x";
	private static final String Y_KEY = "y";
	private static final String Z_KEY = "z";
	private static final String NX_KEY = "nx";
	private static final String NY_KEY = "ny";
	private static final String NZ_KEY = "nz";
	private static final String TITLE_KEY = "title";
	private static final String TEXT_KEY = "text";
	
	public static ArrayList<Annotation> readJSONData(String fileName) {
		File file = getFile(fileName);
		if(file == null)
			return new ArrayList<Annotation>();
		
		JSONObject object = getJSONData(file);
		if(object == null)
			return new ArrayList<Annotation>();
		
		ArrayList<Annotation> annotations = new ArrayList<Annotation>();
		try {
			JSONArray array = object.getJSONArray(ANNOTATIONS_ARRAY);
			for(int i=0;i<array.length();i++) {
				JSONObject annotationObject = array.getJSONObject(i);
				
				Annotation annotation = new Annotation();
				annotation.setId(annotationObject.getInt(ID_KEY));
				
				annotation.setX((float) annotationObject.getDouble(X_KEY));
				annotation.setY((float) annotationObject.getDouble(Y_KEY));
				annotation.setZ((float) annotationObject.getDouble(Z_KEY));
				
				annotation.setNx((float) annotationObject.getDouble(NX_KEY));
				annotation.setNy((float) annotationObject.getDouble(NY_KEY));
				annotation.setNz((float) annotationObject.getDouble(NZ_KEY));
				
				annotation.setTitle(annotationObject.getString(TITLE_KEY));
				annotation.setText(annotationObject.getString(TEXT_KEY));
				
				annotations.add(annotation);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return new ArrayList<Annotation>();
		}
		
		return annotations;
	}
	
	public static boolean writeJSONData(String fileName, ArrayList<Annotation> annotations) {
		File file = getFile(fileName);
		if(file == null)
			return false;
		
		JSONArray array = new JSONArray();
		for(Annotation annotation: annotations) {
			JSONObject object = new JSONObject();
			
			try {
				object.put(ID_KEY, annotation.getId());
				object.put(X_KEY, annotation.getX());
				object.put(Y_KEY, annotation.getY());
				object.put(Z_KEY, annotation.getZ());
				object.put(NX_KEY, annotation.getNx());
				object.put(NY_KEY, annotation.getNy());
				object.put(NZ_KEY, annotation.getNz());
				object.put(TITLE_KEY, annotation.getTitle());
				object.put(TEXT_KEY, annotation.getText());
			} catch (JSONException e) {
				e.printStackTrace();
				return false;
			}
			
			array.put(object);
		}
		
		JSONObject annotationsJSON = new JSONObject();
		try {
			annotationsJSON.put(ANNOTATIONS_ARRAY, array);
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		
		return setJSONData(file, annotationsJSON.toString());
	}
	
	private static boolean setJSONData(File file, String json) {
		try {
			FileWriter writer = new FileWriter(file);
			writer.write(json);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private static JSONObject getJSONData(File file) {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			
			while((line = br.readLine()) != null) {
				sb.append(line);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		JSONObject object = null;
		try {
			object = new JSONObject(sb.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return object;
	}
	
	private static File getFile(String fileName) {
		String externalStorage = Environment.getExternalStorageDirectory().toString();
		String directoryName = externalStorage + DIRECTORY_NAME;
		
		File directory = new File(directoryName);
		if(!directory.exists())
			directory.mkdir();
		
		File file = new File(directoryName + fileName);
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		return file;
	}
}
