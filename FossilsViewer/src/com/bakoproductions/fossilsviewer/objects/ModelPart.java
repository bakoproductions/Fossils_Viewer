package com.bakoproductions.fossilsviewer.objects;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.util.Log;
import static com.bakoproductions.fossilsviewer.util.Globals.BYTES_PER_FLOAT;
import static com.bakoproductions.fossilsviewer.util.Globals.THREE_DIM_ATTRS;
import static com.bakoproductions.fossilsviewer.util.Globals.TWO_DIM_ATTRS;

import com.bakoproductions.fossilsviewer.R;

public class ModelPart {
	private Vector<Short> faces;
	private Vector<Short> texturePointers;
	private Vector<Short> normalPointers;
	
	private Material material;
	
	private FloatBuffer normalBuffer;
	private FloatBuffer textureBuffer;
	private ShortBuffer faceBuffer;
	
	public ModelPart(Vector<Short> faces, Vector<Short> texturePointers, Vector<Short> normalPointers, Material material){
		this.faces = faces;
		this.texturePointers = texturePointers;
		this.normalPointers = normalPointers;
		
		this.material = material;
	}
	
	public void buildNormalBuffer(Vector<Float> normals){
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(normalPointers.size() * BYTES_PER_FLOAT * THREE_DIM_ATTRS);
		byteBuf.order(ByteOrder.nativeOrder());
		normalBuffer = byteBuf.asFloatBuffer();
		for(int i=0; i<normalPointers.size(); i++){
			float x = normals.get(normalPointers.get(i) );
			float y = normals.get(normalPointers.get(i) + 1); // vale kai to THREE_DIM_ATTRS
			float z = normals.get(normalPointers.get(i) + 2);
			normalBuffer.put(x);
			normalBuffer.put(y);
			normalBuffer.put(z);
		}
		normalBuffer.position(0);
	}
	
	public void buildFaceBuffer(){
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(faces.size() * TWO_DIM_ATTRS);
		byteBuffer.order(ByteOrder.nativeOrder());
		faceBuffer = byteBuffer.asShortBuffer();
		faceBuffer.put(toShortArray(faces));
		faceBuffer.position(0);
	}
	
	public void buildTextureBuffer(Vector<Float> textures){
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(texturePointers.size() * BYTES_PER_FLOAT * TWO_DIM_ATTRS);
		byteBuffer.order(ByteOrder.nativeOrder());
		textureBuffer = byteBuffer.asFloatBuffer();

		for(int i=0; i<texturePointers.size(); i++){
			float u = textures.get(texturePointers.get(i));
			float v = textures.get(texturePointers.get(i) + 1);
			textureBuffer.put(u);
			textureBuffer.put(v);
		}
		textureBuffer.position(0);
	}
	
	public FloatBuffer getNormalBuffer(){
		return normalBuffer;
	}
	
	public FloatBuffer getTextureBuffer() {
		return textureBuffer;
	}
	
	public ShortBuffer getFaceBuffer(){
		return faceBuffer;
	}
	
	public Material getMaterial(){
		return material;
	}
	
	public int getFacesSize(){
		return faces.size();
	}
	
	private short[] toShortArray(Vector<Short> vector){
		short[] s = new short[vector.size()];
		
		for (int i=0; i<vector.size(); i++){
			s[i] = vector.get(i);
		}
		return s;
	}
}
