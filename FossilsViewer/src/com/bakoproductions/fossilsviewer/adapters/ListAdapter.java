package com.bakoproductions.fossilsviewer.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bakoproductions.fossilsviewer.R;
import com.bakoproductions.fossilsviewer.util.AssetsInfo;

public class ListAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<AssetsInfo> info;
	
	public ListAdapter(Context context, ArrayList<AssetsInfo> info) {
		this.context = context;
		this.info = info;
	}
	
	@Override
	public int getCount() {
		return info.size();
	}

	@Override
	public Object getItem(int position) {
		return info.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		
		if(convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.list_row, null);
		} else {
			view = convertView;
		}
		
		ImageView thumb = (ImageView) view.findViewById(R.id.thumb);
		TextView objectName = (TextView) view.findViewById(R.id.objectName);
		TextView objectSize = (TextView) view.findViewById(R.id.objectSize);
		
		AssetsInfo info = (AssetsInfo) getItem(position);
		
		Bitmap bitmap = info.getThumb();
		if(bitmap == null) {
			thumb.setImageResource(R.drawable.ic_default_thumb);
		} else {
			thumb.setImageBitmap(bitmap);
		}
		
		objectName.setText(info.getFolderName());
		objectSize.setText(getFileSize(info.getFolderSize()));
		
		return view;
	}
	
	private String getFileSize(int size) {
		StringBuilder sb = new StringBuilder();
		
		if(size < 1024) {
			sb.append(size);
			sb.append(" " + "b");
		} else if(size < 1048576) {
			sb.append((int)(size / 1024.0));
			sb.append(" " + "Kb");
		} else {
			sb.append((int) (size / 1048576.0));
			sb.append(" " + "Mb");
		}
		
		return sb.toString();
	}

}
