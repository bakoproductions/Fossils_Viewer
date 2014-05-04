package com.bakoproductions.fossilsviewer.objects;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;

import static com.bakoproductions.fossilsviewer.util.Globals.BYTES_PER_FLOAT;
import static com.bakoproductions.fossilsviewer.util.Globals.THREE_DIM_ATTRS;;

public class ModelPart {
	private Vector<Short> faces;
	private Vector<Short> texturePointers;
	private Vector<Short> normalPointers;
	
	private Material material;
	
	private FloatBuffer normalBuffer;
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
			float x = normals.get(normalPointers.get(i) * THREE_DIM_ATTRS);
			float y = normals.get(normalPointers.get(i) * THREE_DIM_ATTRS + 1);
			float z = normals.get(normalPointers.get(i) * THREE_DIM_ATTRS + 2);
			normalBuffer.put(x);
			normalBuffer.put(y);
			normalBuffer.put(z);
		}
		normalBuffer.position(0);
	}
	
	public void buildFaceBuffer(){
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(faces.size() * 2);
		byteBuffer.order(ByteOrder.nativeOrder());
		faceBuffer = byteBuffer.asShortBuffer();
		faceBuffer.put(toShortArray(faces));
		faceBuffer.position(0);
	}
	
	public FloatBuffer getNormalBuffer(){
		return normalBuffer;
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
