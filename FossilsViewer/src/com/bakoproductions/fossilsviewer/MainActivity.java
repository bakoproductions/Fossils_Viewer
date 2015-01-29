package com.bakoproductions.fossilsviewer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.bakoproductions.fossilsviewer.adapters.ListAdapter;
import com.bakoproductions.fossilsviewer.util.AssetsInfo;
import com.bakoproductions.fossilsviewer.util.AssetsReader;
import com.bakoproductions.fossilsviewer.viewer.ViewerActivity;

public class MainActivity extends Activity {
	public static final String ROOT_FOLDER_PATH = "objects";
	private ListView list;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		AssetsLoader loader = new AssetsLoader();
		loader.execute(ROOT_FOLDER_PATH);
	}
	
	private class AssetsLoader extends AsyncTask<String, Void, Void> {
		private AssetsReader reader;
		private ListAdapter adapter;
		private LinearLayout progressLayout;
		
		@Override
		protected void onPreExecute() {
			list = (ListView) findViewById(R.id.list);
			list.setOnItemClickListener(new OnItemClicked());
			
			progressLayout = (LinearLayout) findViewById(R.id.progressViewerLayout);
			progressLayout.setVisibility(View.VISIBLE);
			list.setVisibility(View.INVISIBLE);
			
			reader = new AssetsReader(getApplicationContext());
			super.onPreExecute();
		}
		
		@Override
		protected Void doInBackground(String... paths) {
			ArrayList<AssetsInfo> info = reader.getFiles(paths[0]);
			adapter = new ListAdapter(getApplicationContext(), info);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			progressLayout.setVisibility(View.GONE);
			list.setAdapter(adapter);
			list.setVisibility(View.VISIBLE);
			super.onPostExecute(result);
		}
	}
	
	private class OnItemClicked implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Intent intent = new Intent(MainActivity.this, ViewerActivity.class);
			
			AssetsInfo info = (AssetsInfo) parent.getItemAtPosition(position);
			intent.putExtra("path", info.getParentPath());
			intent.putExtra("file", info.getObjFilePath());
			startActivity(intent);
		}
	}
}
