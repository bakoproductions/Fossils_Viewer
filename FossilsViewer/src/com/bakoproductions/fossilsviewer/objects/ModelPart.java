package com.bakoproductions.fossilsviewer.objects;

import static com.bakoproductions.fossilsviewer.util.Globals.BYTES_PER_FLOAT;
import static com.bakoproductions.fossilsviewer.util.Globals.BYTES_PER_SHORT;
import static com.bakoproductions.fossilsviewer.util.Globals.THREE_DIM_ATTRS;
import static com.bakoproductions.fossilsviewer.util.Globals.TWO_DIM_ATTRS;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;

import android.opengl.GLES20;
import android.os.Parcel;
import android.os.Parcelable;

import com.bakoproductions.fossilsviewer.util.Util;


public class ModelPart implements Parcelable{
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
	
	public void bindBuffers(int tboId, int nboId, int iboId) {
		if(tboId > 0 && nboId > 0 && iboId > 0) {
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, tboId);
			GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, textureBuffer.capacity() * BYTES_PER_FLOAT, textureBuffer, GLES20.GL_STATIC_DRAW);
			
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, nboId);
			GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, normalBuffer.capacity() * BYTES_PER_FLOAT, normalBuffer, GLES20.GL_STATIC_DRAW);
			
			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, iboId);
			GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, faceBuffer.capacity() * BYTES_PER_SHORT, faceBuffer, GLES20.GL_STATIC_DRAW);
			
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
		}
	}
	
	public void buildNormalBuffer(Vector<Float> normals){
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(normalPointers.size() * BYTES_PER_FLOAT * THREE_DIM_ATTRS);
		byteBuf.order(ByteOrder.nativeOrder());
		normalBuffer = byteBuf.asFloatBuffer();
		for(int i=0; i<normalPointers.size(); i++){
			int index = normalPointers.get(i);
			
			normalBuffer.put(normals.get(index * THREE_DIM_ATTRS));
			normalBuffer.put(normals.get(index * THREE_DIM_ATTRS + 1));
			normalBuffer.put(normals.get(index * THREE_DIM_ATTRS + 2));
		}
		normalBuffer.position(0);
	}
	
	public void buildFaceBuffer(){
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(faces.size() * BYTES_PER_SHORT);
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
			int index = texturePointers.get(i);
			
			textureBuffer.put(textures.get(index * TWO_DIM_ATTRS));
			textureBuffer.put(1 - textures.get(index * TWO_DIM_ATTRS + 1));
		}
		textureBuffer.position(0);	
	}
	
	public FloatBuffer getNormalBuffer(){
		normalBuffer.position(0);
		return normalBuffer;
	}
	
	public FloatBuffer getTextureBuffer() {
		textureBuffer.position(0);
		return textureBuffer;
	}
	
	public ShortBuffer getFaceBuffer(){
		faceBuffer.position(0);
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
	
	private short[] toShortArray(int[] array){
		short[] s = new short[array.length];
		
		for (int i=0; i<s.length; i++){
			s[i] = (short) array[i];
		}
		return s;
	}
	
	public static final Parcelable.Creator<ModelPart> CREATOR = new Parcelable.Creator<ModelPart>() {
        public ModelPart createFromParcel(Parcel pc) {
            return new ModelPart(pc);
        }
        public ModelPart[] newArray(int size) {
            return new ModelPart[size];
        }
	};
	
	public ModelPart(Parcel parcel) {
		float[] normalArray = parcel.createFloatArray();
		float[] textureArray = parcel.createFloatArray();
		int[] faceArray = parcel.createIntArray();		
		material = parcel.readParcelable(Material.class.getClassLoader());
		
		ByteBuffer byteBufferNormals = ByteBuffer.allocateDirect(normalArray.length * BYTES_PER_FLOAT);
		byteBufferNormals.order(ByteOrder.nativeOrder());
		normalBuffer = byteBufferNormals.asFloatBuffer();
		normalBuffer.put(normalArray);
		normalBuffer.position(0);
		
		ByteBuffer byteBufferTextures = ByteBuffer.allocateDirect(textureArray.length * BYTES_PER_FLOAT);
		byteBufferTextures.order(ByteOrder.nativeOrder());
		textureBuffer = byteBufferTextures.asFloatBuffer();
		textureBuffer.put(textureArray);
		textureBuffer.position(0);
		
		ByteBuffer byteBufferFaces = ByteBuffer.allocateDirect(faceArray.length * BYTES_PER_SHORT);
		byteBufferFaces.order(ByteOrder.nativeOrder());
		faceBuffer = byteBufferFaces.asShortBuffer();
		faceBuffer.put(toShortArray(faceArray));
		faceBuffer.position(0);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		float[] normalArray = Util.toFloatArray(normalBuffer);
		float[] textureArray = Util.toFloatArray(textureBuffer);
		int[] faceArray = Util.toIntArray(faceBuffer);
		
		dest.writeFloatArray(normalArray);
		dest.writeFloatArray(textureArray);
		dest.writeIntArray(faceArray);
		dest.writeParcelable(material, flags);
	}
}
