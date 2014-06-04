package com.bakoproductions.fossilsviewer;

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

public class MainActivity extends AndARActivity {
	CustomObject someObject;
	ARToolkit artoolkit;
	
	Model model;
	OBJParser objParser;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		model = new Model(this);
		objParser = new OBJParser(this, "iphone.obj");
		int resultOBJ = objParser.parse(model);
		
		if(resultOBJ == ModelParser.IO_ERROR)
			return;
		if(resultOBJ == ModelParser.RESOURCE_NOT_FOUND_ERROR)
			return;
		
		Log.d("Bako", "model parsed");
		
		CustomRenderer renderer = new CustomRenderer();
		super.setNonARRenderer(renderer);
		
		try {
			artoolkit = super.getArtoolkit();
			someObject = new CustomObject(this, "test", "android.patt", 80.0, new double[]{0,0}, model);
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
