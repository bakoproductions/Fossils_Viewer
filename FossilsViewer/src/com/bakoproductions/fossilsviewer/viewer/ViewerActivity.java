package com.bakoproductions.fossilsviewer.viewer;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.bakoproductions.fossilsviewer.R;
import com.bakoproductions.fossilsviewer.objects.Model;
import com.bakoproductions.fossilsviewer.parsers.ModelParser;
import com.bakoproductions.fossilsviewer.parsers.OBJParser;

public class ViewerActivity extends Activity {
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
			super.onPostExecute(result);
		}		
	}
}
