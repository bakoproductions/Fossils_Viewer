package com.bakoproductions.fossilsviewer.objects;

import static com.bakoproductions.fossilsviewer.util.Globals.BYTES_PER_FLOAT;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.bakoproductions.fossilsviewer.R;


public class Material {
	String name;
	String textureFile;
	
	float[] ambientColor;
	float[] diffuseColor;
	float[] specularColor;
	
	float alpha;
	float shine;
	int illumination;
	
	public Material(String name){
		this.name=name;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float[] getAmbientColor() {
		return ambientColor;
	}
	
	public FloatBuffer getAmbientColorBuffer(){
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(ambientColor.length * BYTES_PER_FLOAT);
		byteBuffer.order(ByteOrder.nativeOrder());
		FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
		floatBuffer.put(ambientColor);
		floatBuffer.position(0);
		return floatBuffer;
	}
	
	public void setAmbientColor(float r, float g, float b) {
		ambientColor = new float[3];
		ambientColor[0] = r;
		ambientColor[1] = g;
		ambientColor[2] = b;
	}

	public float[] getDiffuseColor() {
		return diffuseColor;
	}
	
	public FloatBuffer getDiffuseColorBuffer(){
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(diffuseColor.length * BYTES_PER_FLOAT);
		byteBuffer.order(ByteOrder.nativeOrder());
		FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
		floatBuffer.put(diffuseColor);
		floatBuffer.position(0);
		return floatBuffer;
	}

	public void setDiffuseColor(float r, float g, float b) {
		diffuseColor = new float[3];
		diffuseColor[0] = r;
		diffuseColor[1] = g;
		diffuseColor[2] = b;
	}

	public float[] getSpecularColor() {
		return specularColor;
	}
	
	public FloatBuffer getSpecularColorBuffer(){
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(12);
		byteBuffer.order(ByteOrder.nativeOrder());
		FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
		floatBuffer.put(specularColor);
		floatBuffer.position(0);
		return floatBuffer;
	}

	public void setSpecularColor(float r, float g, float b) {
		specularColor = new float[3];
		specularColor[0] = r;
		specularColor[1] = g;
		specularColor[2] = b;
	}
	
	public Bitmap getBitmap(Context context){
		if(textureFile == null){
			return null;
		}
		
		Log.d("Bako", textureFile);
		AssetManager assets = context.getAssets();
		
		try {
			InputStream is = assets.open(textureFile);
			return BitmapFactory.decodeStream(is);
		} catch (IOException e) {
			Log.d("Bako", "einai null");
			return null;
		}
	}
	
	public float getAlpha() {
		return alpha;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	public float getShine() {
		return shine;
	}

	public void setShine(float shine) {
		this.shine = shine;
	}

	public int getIllumination() {
		return illumination;
	}

	public void setIllumination(int illumination) {
		this.illumination= illumination;
	}

	public String getTextureFileName() {
		return textureFile;
	}

	public void setTextureFileName(String textureFile) {
		this.textureFile = textureFile; 
	}
	
	private int getResourceId(Class<?> c) {
	    try {
	        Field idField = c.getDeclaredField(textureFile);
	        return idField.getInt(idField);
	    } catch (Exception e) {
	        return -1;
	    } 
	}
}

