package com.bakoproductions.fossilsviewer.viewer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.bakoproductions.fossilsviewer.R;
import com.bakoproductions.fossilsviewer.objects.Model;
import com.bakoproductions.fossilsviewer.parsers.ModelParser;
import com.bakoproductions.fossilsviewer.parsers.OBJParser;

public class ViewerActivity extends SherlockActivity {
	private ViewerRenderer renderer;
	private Model model;
	private OBJParser objParser;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_viewer);
		
		Bundle extras = getIntent().getExtras();
		String path = extras.getString("path");
		String file = extras.getString("file");
		
		ParsingTask task = new ParsingTask(getApplicationContext(), path, file);
		task.execute();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		if(renderer != null)
			renderer.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(renderer != null)
			renderer.onResume();
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
		MenuItem actionCloseLight = menu.findItem(R.id.action_close_light);
		MenuItem actionOpenLight = menu.findItem(R.id.action_open_light);
		
		actionCenter.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		actionUnlockTranslation.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		actionLockTranslation.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		actionCloseLight.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		actionOpenLight.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		if(renderer.isLockedTranslation()) {
			actionUnlockTranslation.setVisible(true);
			actionLockTranslation.setVisible(false);
		} else {
			actionUnlockTranslation.setVisible(false);
			actionLockTranslation.setVisible(true);
		}
		
		if(renderer.isClosedLight()) {
			actionCloseLight.setVisible(false);
			actionOpenLight.setVisible(true);
		} else {
			actionCloseLight.setVisible(true);
			actionOpenLight.setVisible(false);
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
		case R.id.action_close_light:
			renderer.setClosedLight(true);
			supportInvalidateOptionsMenu();
			break;
		case R.id.action_open_light:
			renderer.setClosedLight(false);
			supportInvalidateOptionsMenu();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class ParsingTask extends AsyncTask<Void, Void, Integer> {
		private Context context;
		private String parentPath;
		private String file;
		
		public ParsingTask(Context context, String parentPath, String file) {
			this.context = context;
			this.parentPath = parentPath;
			this.file = file;
		}
		
		@Override
		protected void onPreExecute() {
			model = new Model(context);
			objParser = new OBJParser(context, parentPath, file);
			
			super.onPreExecute();
		}
		@Override
		protected Integer doInBackground(Void... params) {
			int resultOBJ = objParser.parse(model);
			
			return resultOBJ;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			if(result == ModelParser.IO_ERROR) {
				return;
			} else if(result == ModelParser.RESOURCE_NOT_FOUND_ERROR) {
				return;
			}
			
			renderer = new ViewerRenderer(context, model);
			setContentView(renderer);
			supportInvalidateOptionsMenu();
			super.onPostExecute(result);
		}		
	}
}
