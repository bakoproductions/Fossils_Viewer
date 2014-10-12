package com.bakoproductions.fossilsviewer.util;

import android.graphics.Bitmap;

public class AssetsInfo {
	private Bitmap thumb;
	private String folderName;
	private int folderSize;
	
	private String objFilePath;
	
	public AssetsInfo() {
		
	}
	
	public AssetsInfo(Bitmap thumb, String folderName, int folderSize, String objFilePath) {
		this.thumb = thumb;
		this.folderName = folderName;
		this.folderSize = folderSize;
		this.objFilePath = objFilePath;
	}
	
	public void setThumb(Bitmap thumb) {
		this.thumb = thumb;
	}
	
	public Bitmap getThumb() {
		return thumb;
	}
	
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
	
	public String getFolderName() {
		return folderName;
	}
	
	public void setFolderSize(int folderSize) {
		this.folderSize = folderSize;
	}
	
	public int getFolderSize() {
		return folderSize;
	}
	
	public void setObjFilePath(String objFilePath) {
		this.objFilePath = objFilePath;
	}
	
	public String getObjFilePath() {
		return objFilePath;
	}
}
