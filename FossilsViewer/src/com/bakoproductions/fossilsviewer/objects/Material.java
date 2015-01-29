package com.bakoproductions.fossilsviewer.objects;

import static com.bakoproductions.fossilsviewer.util.Globals.BYTES_PER_FLOAT;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

public class Material implements Parcelable{
	private String name;
	private String textureFile;
	
	private float[] ambientColor;
	private float[] diffuseColor;
	private float[] specularColor;
	
	private float alpha;
	private float shine;
	private int illumination;
	
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
		
		AssetManager assets = context.getAssets();
		
		try {
			InputStream is = assets.open(textureFile);
			return BitmapFactory.decodeStream(is);
		} catch (IOException e) {
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
	
	public static final Parcelable.Creator<Material> CREATOR = new Parcelable.Creator<Material>() {
        public Material createFromParcel(Parcel pc) {
            return new Material(pc);
        }
        public Material[] newArray(int size) {
            return new Material[size];
        }
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(textureFile);
		dest.writeFloatArray(ambientColor);
		dest.writeFloatArray(diffuseColor);
		dest.writeFloatArray(specularColor);
		dest.writeFloat(alpha);
		dest.writeFloat(shine);
		dest.writeInt(illumination);
	}
	
	public Material(Parcel parcel) {
		this.name = parcel.readString();
		this.textureFile = parcel.readString();
		this.ambientColor = parcel.createFloatArray();
		this.diffuseColor = parcel.createFloatArray();
		this.specularColor = parcel.createFloatArray();
		this.alpha = parcel.readFloat();
		this.shine = parcel.readFloat();
		this.illumination = parcel.readInt();
	}
}

