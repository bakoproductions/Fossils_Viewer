package com.bakoproductions.fossilsviewer.ar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import com.bakoproductions.fossilsviewer.objects.Ground;
import com.bakoproductions.fossilsviewer.objects.Model;
import com.bakoproductions.fossilsviewer.parsers.OBJParser;

import edu.dhbw.andar.ARToolkit;
import edu.dhbw.andar.AndARActivity;
import edu.dhbw.andar.exceptions.AndARException;

public class ARActivity extends AndARActivity {
	CustomObject object;
	Ground ground;
	
	ARRenderer renderer;
	ARToolkit artoolkit;
	
	Model model;
	Model pushPin;
	String filePath;
	OBJParser objParser;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		if(intent == null)
			return;
		
		model = intent.getParcelableExtra("model");
		model.setContext(this);
		
		if(model == null)
			return;
		
		object = new CustomObject("test", "android.patt", 80.0, new double[]{0,0}, model);
		
		renderer = new ARRenderer(this, object);
		super.setNonARRenderer(renderer);
		
		try {
			artoolkit = super.getArtoolkit();
			artoolkit.registerARObject(object);
		} catch (AndARException e) {
			e.printStackTrace();
		}
		startPreview();		
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		Log.e("AndAR EXCEPTION", ex.getMessage());
		finish();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(renderer == null)
			return false;
		
		return renderer.onTouchEvent(event);
	}
}
