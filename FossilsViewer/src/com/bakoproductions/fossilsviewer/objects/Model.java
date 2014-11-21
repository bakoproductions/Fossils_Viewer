package com.bakoproductions.fossilsviewer.objects;

import static com.bakoproductions.fossilsviewer.util.Globals.BYTES_PER_FLOAT;
import static com.bakoproductions.fossilsviewer.util.Globals.THREE_DIM_ATTRS;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.bakoproductions.fossilsviewer.util.Globals;
import com.bakoproductions.fossilsviewer.util.Util;

public class Model implements Parcelable{
	private Context context;
	
	private Vector<Float> vertices;
	private Vector<ModelPart> parts;
	private FloatBuffer vertexBuffer;
	
	private int[] bindedTextures;
	
	private int[] vbo;
	private int[] tbo;
	private int[] nbo;
	private int[] ibo;
	
	private BoundingSphere sphere;
	
	public Model(Context context){
		this.context = context;
	}
	
	public Model(Context context, Vector<Float> verticies, Vector<ModelPart> parts){
		this.context = context;
		this.vertices = verticies;
	}
	
	public int[] prepareTextures(){
		Material[] materials = new Material[parts.size()];
		int activeTextures = 0;
		for(int i=0;i<parts.size();i++){
			materials[i] = parts.get(i).getMaterial();
			if(materials[i] != null)
				activeTextures++;
		}
		
		final int[] textureHandle = new int[activeTextures];
		GLES20.glGenTextures(activeTextures, textureHandle, 0);
		
		int textureId = 0;
		for(int i=0;i<materials.length;i++) {
			if(materials[i] == null)
				continue;
			
			final BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inScaled = false;
	        
	        final Bitmap bitmap = materials[i].getBitmap(context);
	        
	        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[textureId]);
	        
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
	        
	        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
	        
	        bitmap.recycle();
	        
	        textureId++;
		}
		
		return textureHandle;
	}
	
	public void bindBuffers() {
		vbo = new int[1];
		tbo = new int[parts.size()];
		nbo = new int[parts.size()];
		ibo = new int[parts.size()];
		
		GLES20.glGenBuffers(1, vbo, 0);
		GLES20.glGenBuffers(parts.size(), tbo, 0);
		GLES20.glGenBuffers(parts.size(), nbo, 0);
		GLES20.glGenBuffers(parts.size(), ibo, 0);
		
		if(vbo[0] > 0) {
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
			GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer.capacity() * BYTES_PER_FLOAT,	vertexBuffer, GLES20.GL_STATIC_DRAW);
			
			for(int i=0;i<parts.size();i++) {
				ModelPart part = parts.get(i);
				
				part.bindBuffers(tbo[i], nbo[i], ibo[i]);
			}
		}
	}
	
	public void draw(GL10 gl){
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		
		for(int i=0; i<parts.size(); i++){
			ModelPart modelPart = parts.get(i);
			Material material = modelPart.getMaterial();
			
			int activeTextures = 0;
			if(material != null){
				FloatBuffer a = material.getAmbientColorBuffer();
				FloatBuffer d = material.getDiffuseColorBuffer();
				FloatBuffer s = material.getSpecularColorBuffer();
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, a);
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, s);
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, d);
				
				gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
				gl.glBindTexture(GL10.GL_TEXTURE_2D, bindedTextures[activeTextures]);
				activeTextures++;
			}
			
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, modelPart.getTextureBuffer());
			gl.glNormalPointer(GL10.GL_FLOAT, 0, modelPart.getNormalBuffer());
			gl.glDrawElements(GL10.GL_TRIANGLES, modelPart.getFaceBuffer().capacity(), GL10.GL_UNSIGNED_SHORT, modelPart.getFaceBuffer());
			
			gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
			gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			
		}
	}
	
	public void draw(int positionId, int normalId, int textureId) {
		if(vbo[0] > 0) {
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
			GLES20.glVertexAttribPointer(positionId, Globals.THREE_DIM_ATTRS, GLES20.GL_FLOAT, false, 0, 0);
			GLES20.glEnableVertexAttribArray(positionId);
			
			for(int i=0;i<parts.size();i++) {
				ModelPart part = parts.get(i);
				
				if(nbo[i] > 0 && tbo[i] > 0 && ibo[i] > 0) {
					GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, nbo[i]);
					GLES20.glVertexAttribPointer(normalId, Globals.THREE_DIM_ATTRS, GLES20.GL_FLOAT, false, 0, 0);
					GLES20.glEnableVertexAttribArray(normalId);
					GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
					
					GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, tbo[i]);
					GLES20.glVertexAttribPointer(textureId, Globals.TWO_DIM_ATTRS, GLES20.GL_FLOAT, false, 0, 0);
					GLES20.glEnableVertexAttribArray(textureId);
					GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
					
					GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[i]);
					GLES20.glDrawElements(GLES20.GL_TRIANGLES, part.getFaceBuffer().capacity(), GLES20.GL_UNSIGNED_SHORT, 0);
					GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
				}
			}
			
			GLES20.glDisableVertexAttribArray(normalId);
			GLES20.glDisableVertexAttribArray(textureId);
			GLES20.glDisableVertexAttribArray(positionId);
		}
	}
	
	public void buildVertexBuffer(Vector<Short> vertexPointers){
		ByteBuffer vBuf = ByteBuffer.allocateDirect(vertexPointers.size() * BYTES_PER_FLOAT * THREE_DIM_ATTRS);
		vBuf.order(ByteOrder.nativeOrder());
		vertexBuffer = vBuf.asFloatBuffer();
		for(int i=0;i<vertexPointers.size();i++){
			int index = vertexPointers.get(i);
			
			vertexBuffer.put(vertices.get(index * THREE_DIM_ATTRS));
			vertexBuffer.put(vertices.get(index * THREE_DIM_ATTRS + 1));
			vertexBuffer.put(vertices.get(index * THREE_DIM_ATTRS + 2));
		}
		vertexBuffer.position(0);
	}
	
	public void setVertices(Vector<Float> vertices) {
		this.vertices = vertices;
	}
	
	public Vector<ModelPart> getParts() {
		return parts;
	}
	
	public void setParts(Vector<ModelPart> parts) {
		this.parts = parts;
	}
	
	public void applyBoundingSphere(BoundingSphere sphere){
		this.sphere = sphere;
	}
	
	public BoundingSphere getSphere() {
		return sphere;
	}
		
	public void setContext(Context context) {
		this.context = context;
	}
	
	public FloatBuffer getVertexBuffer() {
		vertexBuffer.position(0);
		return vertexBuffer;
	}
	
	public void printVertexBuffer() {
		for(int i=0;i<vertexBuffer.capacity();i++) {
			Log.i(Model.class.getSimpleName(), "" + vertexBuffer.get(i));
		}
	}
	
	public static final Parcelable.Creator<Model> CREATOR = new Parcelable.Creator<Model>() {
        public Model createFromParcel(Parcel pc) {
            return new Model(pc);
        }
        public Model[] newArray(int size) {
            return new Model[size];
        }
	};
	
	public Model(Parcel parcel) {
		float[] vertexArray = parcel.createFloatArray();
		
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertexArray.length * BYTES_PER_FLOAT);
		byteBuffer.order(ByteOrder.nativeOrder());
		vertexBuffer = byteBuffer.asFloatBuffer();
		vertexBuffer.put(vertexArray);
		vertexBuffer.position(0);
		
		sphere = parcel.readParcelable(BoundingSphere.class.getClassLoader());
		
		ArrayList<ModelPart> partList = (ArrayList<ModelPart>) parcel.readArrayList(ModelPart.class.getClassLoader());
		parts = new Vector<ModelPart>();

		for(int i=0;i<partList.size();i++) {
			parts.add(partList.get(i));
		}
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		float[] vertexArray = Util.toFloatArray(vertexBuffer);
		//ModelPart[] partArray = parts.toArray(new ModelPart[parts.size()]);
		
		ArrayList<ModelPart> partList = new ArrayList<ModelPart>();
		for(int i=0;i<parts.size();i++) {
			partList.add(parts.get(i));
		}
		
		dest.writeFloatArray(vertexArray);
		dest.writeParcelable(sphere, flags);
		dest.writeList(partList);
	}
}
