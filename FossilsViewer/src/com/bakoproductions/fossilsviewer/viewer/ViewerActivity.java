package com.bakoproductions.fossilsviewer.viewer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ConfigurationInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.bakoproductions.fossilsviewer.R;
import com.bakoproductions.fossilsviewer.ar.ARActivity;
import com.bakoproductions.fossilsviewer.objects.Model;
import com.bakoproductions.fossilsviewer.parsers.ModelParser;
import com.bakoproductions.fossilsviewer.parsers.OBJParser;

public class ViewerActivity extends SherlockActivity {
	private ViewerGLSurfaceView glSurfaceView;
	private ViewerRenderer renderer;
	private String file;
	private Model model;
	private Model pushPin;
	
	private boolean modelParsed;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_viewer);
		
		final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
        
        if(!supportsEs2) {
        	Toast.makeText(this, R.string.toast_phone_not_supports_es2, Toast.LENGTH_SHORT).show();
        	return;
        }
		
		glSurfaceView = (ViewerGLSurfaceView) findViewById(R.id.glSurfaceView);
		modelParsed = false;
		
		// If retain the instance of the activity
		// try to retrieve the object
		if(savedInstanceState != null) {
			model = savedInstanceState.getParcelable("model");
			
			if(savedInstanceState.containsKey("file"))
				file = savedInstanceState.getString("file");
			
			if(savedInstanceState.containsKey("pushPin"))
				pushPin = savedInstanceState.getParcelable("pushPin");
			
			if(model != null) {			
				modelParsed = true;
				renderer = new ViewerRenderer(this, file, model, pushPin);
				glSurfaceView.setEGLConfigChooser(true);
				glSurfaceView.start(renderer);	
				
				renderer.setHandler(glSurfaceView.getDialogHandler());
				renderer.setLockedTranslation(savedInstanceState.getBoolean("lockedTranslation"));
								
				renderer.setPosX(savedInstanceState.getFloat("posX"));
				renderer.setPosY(savedInstanceState.getFloat("posY"));
				
				renderer.setRotX(savedInstanceState.getFloat("rotX"));
				renderer.setRotY(savedInstanceState.getFloat("rotY"));
				
				renderer.setScaleFactor(savedInstanceState.getFloat("scaleFactor"));
				renderer.setDisplayedAnnotation(savedInstanceState.getInt("displayedAnnotation"));
				supportInvalidateOptionsMenu();
				return;
			}
		}
		
		// If the object couldn't be retrieved
		// or this is the first time we open the activity
		// try to read the object from the 'file'
		Bundle extras = getIntent().getExtras();
		String path = extras.getString("path");
		file = extras.getString("file");
		
		ParsingTask task = new ParsingTask(getApplicationContext(), path, file);
		task.execute();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		if(modelParsed == true)
			glSurfaceView.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(modelParsed == true)
			glSurfaceView.onResume();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		if(model != null)
			outState.putParcelable("model", model);
		
		if(file != null)
			outState.putString("file", file);
	
		if(pushPin != null)
			outState.putParcelable("pushPin", pushPin);
		
		if(renderer != null) {
			outState.putBoolean("lockedTranslation", renderer.isLockedTranslation());
			
			outState.putFloat("posX", renderer.getPosX());
			outState.putFloat("posY", renderer.getPosY());
			
			outState.putFloat("rotX", renderer.getRotX());
			outState.putFloat("rotY", renderer.getRotY());
			
			outState.putFloat("scaleFactor", renderer.getScaleFactor());
			outState.putInt("displayedAnnotation", renderer.getDisplayedAnnotation());
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(renderer == null)
			return false;
		
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.viewer_activity_menu, menu);
	
		MenuItem actionCenter = menu.findItem(R.id.action_center);
		MenuItem actionUnlockTranslation = menu.findItem(R.id.action_unlock_translation);
		MenuItem actionLockTranslation = menu.findItem(R.id.action_lock_translation);
		MenuItem actionOpenAr = menu.findItem(R.id.action_ar);
		
		actionCenter.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		actionUnlockTranslation.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		actionLockTranslation.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		actionOpenAr.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		if(renderer.isLockedTranslation()) {
			actionUnlockTranslation.setVisible(true);
			actionLockTranslation.setVisible(false);
		} else {
			actionUnlockTranslation.setVisible(false);
			actionLockTranslation.setVisible(true);
		}
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(renderer == null)
			return false;
		
		switch (item.getItemId()) {
		case R.id.action_center:
			renderer.centerModel();
			break;
		case R.id.action_lock_translation:
			renderer.setLockedTranslation(true);
			supportInvalidateOptionsMenu();
			break;
		case R.id.action_unlock_translation:
			renderer.setLockedTranslation(false);
			supportInvalidateOptionsMenu();
			break;
		case R.id.action_ar:
			Intent intent = new Intent(this, ARActivity.class);
			intent.putExtra("model", renderer.getModel());
			intent.putExtra("file", renderer.getFilePath());
			
			startActivity(intent);
			supportInvalidateOptionsMenu();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class ParsingTask extends AsyncTask<Void, Void, Integer> {
		private LinearLayout loadingLayout;
		private Context context;
		private String parentPath;
		private String file;
		private OBJParser modelParser;
		private OBJParser pushPinParser;
		
		public ParsingTask(Context context, String parentPath, String file) {
			this.context = context;
			this.parentPath = parentPath;
			this.file = file;
		}
		
		@Override
		protected void onPreExecute() {
			lockScreenOrientation();
			
			modelParsed = false;
			
			loadingLayout = (LinearLayout) findViewById(R.id.progressViewerLayout);
			loadingLayout.setVisibility(View.VISIBLE);
			glSurfaceView.setVisibility(View.INVISIBLE);
			
			model = new Model(context);
			pushPin = new Model(context);
			modelParser = new OBJParser(context, parentPath, file);
			pushPinParser = new OBJParser(context, "extra/pushpin", "extra/pushpin/pushpin.obj");
			super.onPreExecute();
		}
		@Override
		protected Integer doInBackground(Void... params) {
			int resultOBJ = modelParser.parse(model);
			pushPinParser.parse(pushPin);
			return resultOBJ;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			if(result == ModelParser.IO_ERROR) {
				return;
			} else if(result == ModelParser.RESOURCE_NOT_FOUND_ERROR) {
				return;
			}
			
			modelParsed = true;
			renderer = new ViewerRenderer(ViewerActivity.this, file, model, pushPin);
			renderer.setHandler(glSurfaceView.getDialogHandler());
			glSurfaceView.setEGLConfigChooser(true);
			glSurfaceView.start(renderer);
			
			loadingLayout.setVisibility(View.GONE);
			glSurfaceView.setVisibility(View.VISIBLE);
			
			supportInvalidateOptionsMenu();
			unlockScreenOrientation();
			super.onPostExecute(result);
		}		
	}
	
	private void lockScreenOrientation() {
	    int currentOrientation = getResources().getConfiguration().orientation;
	    if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    } else {
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	    }
	}
	
	private void unlockScreenOrientation() {
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}
}
