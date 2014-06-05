package com.bakoproductions.fossilsviewer.viewer;

import com.bakoproductions.fossilsviewer.objects.Model;
import com.bakoproductions.fossilsviewer.parsers.ModelParser;
import com.bakoproductions.fossilsviewer.parsers.OBJParser;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class ViewerActivity extends Activity {
	private ViewerRenderer renderer;
	private Model model;
	private OBJParser objParser;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String file = getIntent().getExtras().getString("file");
		
		model = new Model(this);
		objParser = new OBJParser(this, file);
		int resultOBJ = objParser.parse(model);
		
		if(resultOBJ == ModelParser.IO_ERROR)
			return;
		if(resultOBJ == ModelParser.RESOURCE_NOT_FOUND_ERROR)
			return;
		
		Log.d("Bako", "model parsed");
		
		renderer = new ViewerRenderer(this, model);
		setContentView(renderer);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		renderer.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		renderer.onResume();
	}
}
