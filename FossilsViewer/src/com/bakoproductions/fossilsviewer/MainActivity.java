package com.bakoproductions.fossilsviewer;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

public class MainActivity extends ActionBarActivity {
	private ModelRenderer renderer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		renderer = new ModelRenderer(this);
		setContentView(renderer);
	}
	
	@Override
	protected void onPause() {
		renderer.onPause();
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		renderer.onResume();
		super.onResume();
	}
}
