package com.bakoproductions.fossilsviewer.objects;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.bakoproductions.fossilsviewer.R;
import static com.bakoproductions.fossilsviewer.util.Globals.BYTES_PER_FLOAT;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.opengl.GLUtils;
import android.util.Log;


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
	
	public int[] loadTexture(GL10 gl, Context context){
		if(textureFile == null)
			return null;
		
		int resId = getResourceId(textureFile, R.drawable.class);

		if(resId == -1)
			return null;
		
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
		
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
		/*
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		*/
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

		bitmap.recycle();
		
		return textures;
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
		// Remove suffix from file name
		int dotIndex = textureFile.indexOf('.');
		if(dotIndex != -1){
			this.textureFile = textureFile.substring(0, dotIndex);
		}else{
			this.textureFile = textureFile;
		}
	}
	
	/*public String toString(){
		String str=new String();
		str+="Material name: "+name;	
		str+="\nAmbient color: "+ambientColor.toString();
		str+="\nDiffuse color: "+diffuseColor.toString();	
		str+="\nSpecular color: "+specularColor.toString();
		str+="\nAlpha: "+alpha;
		str+="\nShine: "+shine;
		return str;
	}*/
	
	private static int getResourceId(String iconName, Class<?> c) {
	    try {
	        Field idField = c.getDeclaredField(iconName);
	        return idField.getInt(idField);
	    } catch (Exception e) {
	        return -1;
	    } 
	}
}

