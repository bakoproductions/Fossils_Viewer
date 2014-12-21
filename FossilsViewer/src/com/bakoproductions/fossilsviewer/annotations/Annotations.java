package com.bakoproductions.fossilsviewer.annotations;

import java.util.ArrayList;

import android.content.Context;
import android.widget.Toast;

import com.bakoproductions.fossilsviewer.R;

public class Annotations {
	private String fileName;
	private ArrayList<Annotation> annotations;
	
	public Annotations(String objFileName) {
		fileName = objFileName.toString();
		if(objFileName.endsWith(".obj")) 
			fileName = objFileName.replace(".obj", "");
		
		annotations = JSONUtil.readJSONData(fileName);
	}
	
	public ArrayList<Annotation> getAnnotations() {
		return annotations;
	}
	
	public void add(Context context, Annotation annotation) {
		annotation.setId(annotations.size());
		annotations.add(annotation);
		
		boolean added = JSONUtil.writeJSONData(fileName, annotations);
		
		if(!added)
			Toast.makeText(context, R.string.toast_error_in_saving, Toast.LENGTH_SHORT).show();
	}
	
	public void edit(Context context, int id, String title, String text) {
		Annotation annotation = annotations.get(id);
		annotation.setTitle(title);
		annotation.setText(text);
		
		boolean edited = JSONUtil.writeJSONData(fileName, annotations);
		if(!edited)
			Toast.makeText(context, R.string.toast_error_in_saving, Toast.LENGTH_SHORT).show();
	}
	
	public void remove(Context context, int id) {
		for(int i=0;i<annotations.size();i++) {
			if(annotations.get(i).getId() == id) {
				annotations.remove(i);
				break;
			}
		}
		
		boolean removed = JSONUtil.writeJSONData(fileName, annotations);
		
		if(!removed)
			Toast.makeText(context, R.string.toast_error_in_saving, Toast.LENGTH_SHORT).show();
	}
}
