package com.bakoproductions.fossilsviewer.objects;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

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
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(12);
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
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(12);
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
}

