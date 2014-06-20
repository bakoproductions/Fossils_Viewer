package com.bakoproductions.fossilsviewer.ar;

import com.bakoproductions.fossilsviewer.ar.CustomObject;
import com.bakoproductions.fossilsviewer.ar.ARRenderer;
import com.bakoproductions.fossilsviewer.objects.Model;
import com.bakoproductions.fossilsviewer.parsers.ModelParser;
import com.bakoproductions.fossilsviewer.parsers.OBJParser;

import edu.dhbw.andar.ARToolkit;
import edu.dhbw.andar.AndARActivity;
import edu.dhbw.andar.exceptions.AndARException;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

public class ARActivity extends AndARActivity {
	CustomObject someObject;
	ARToolkit artoolkit;
	
	Model model;
	OBJParser objParser;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String file = (String) getIntent().getExtras().get("file");
		model = new Model(this);
		objParser = new OBJParser(this, file);
		int resultOBJ = objParser.parse(model);
		
		if(resultOBJ == ModelParser.IO_ERROR)
			return;
		if(resultOBJ == ModelParser.RESOURCE_NOT_FOUND_ERROR)
			return;
		
		Log.d("Bako", "model parsed");
		
		ARRenderer renderer = new ARRenderer();
		super.setNonARRenderer(renderer);
		
		try {
			artoolkit = super.getArtoolkit();
			someObject = new CustomObject("test", "android.patt", 80.0, new double[]{0,0}, model);
			artoolkit.registerARObject(someObject);
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
}
