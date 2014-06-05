package com.bakoproductions.fossilsviewer;

import com.bakoproductions.fossilsviewer.ar.ARActivity;
import com.bakoproductions.fossilsviewer.ar.CustomObject;
import com.bakoproductions.fossilsviewer.ar.ARRenderer;
import com.bakoproductions.fossilsviewer.objects.Model;
import com.bakoproductions.fossilsviewer.parsers.ModelParser;
import com.bakoproductions.fossilsviewer.parsers.OBJParser;
import com.bakoproductions.fossilsviewer.viewer.ViewerActivity;

import edu.dhbw.andar.ARToolkit;
import edu.dhbw.andar.AndARActivity;
import edu.dhbw.andar.exceptions.AndARException;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

public class MainActivity extends Activity {
	int chooser = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		String file = "iphone.obj";
		
		Intent intent;
		if(chooser == 0){
			intent = new Intent(this, ARActivity.class);
		}else{
			intent = new Intent(this, ViewerActivity.class);
		}
		intent.putExtra("file", file);
		startActivity(intent);
	}
}
