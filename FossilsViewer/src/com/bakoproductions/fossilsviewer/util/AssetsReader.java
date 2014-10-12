package com.bakoproductions.fossilsviewer.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.bakoproductions.fossilsviewer.MainActivity;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class AssetsReader {
	private static final String OBJ_FILE = ".obj";
	private static final String THUMB_FILE = "thumb.png";
	private AssetManager assets;
	private ArrayList<AssetsInfo> info;
	
	AssetsInfo currentInfo = null;
	
	public AssetsReader(Context context) {
		assets = context.getAssets();
	}
	
	public ArrayList<AssetsInfo> getFiles(String path) {		
		info = new ArrayList<AssetsInfo>();
		recursiveSearch(path);
		return info;
	}
	
	private boolean recursiveSearch(String path) {
		String[] list;
		try {
			list = assets.list(path);
			if(list.length > 0) {
				
				if(!path.equals(MainActivity.ROOT_FOLDER_PATH)) {
					currentInfo = new AssetsInfo();
					info.add(currentInfo);
					
					String[] name = path.split("/");
					currentInfo.setFolderName(name[name.length - 1]);
				}
				
				for(String file: list) {
					if (!recursiveSearch(path + "/" + file))
	                    return false;						
				}
			} else {
				if(path.endsWith(OBJ_FILE)) {
					currentInfo.setObjFilePath(path);
					
					int length = assets.open(path).available();
					currentInfo.setFolderSize(length);
				} else if(path.endsWith(THUMB_FILE)) {
					InputStream is = assets.open(path);
					Bitmap bitmap = BitmapFactory.decodeStream(is);
					currentInfo.setThumb(bitmap);					
				} 
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}
}
