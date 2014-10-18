package com.bakoproductions.fossilsviewer.objects;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;

import com.bakoproductions.fossilsviewer.util.Globals;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLUtils;
import android.util.Log;
import static com.bakoproductions.fossilsviewer.util.Globals.BYTES_PER_FLOAT;
import static com.bakoproductions.fossilsviewer.util.Globals.THREE_DIM_ATTRS;;

public class Model {
	private Context context;
	
	private Vector<Float> vertices;
	private Vector<Float> normals;
	private Vector<Float> textures;
	private Vector<ModelPart> parts;
	private FloatBuffer vertexBuffer;
	
	private int[] bindedTextures;
	private BoundingSphere sphere;
	
	public Model(Context context){
		this.context = context;
	}
	
	public Model(Vector<Float> verticies, Vector<Float> normals, Vector<Float> textures, Vector<ModelPart> parts){
		this.vertices = verticies;
		this.normals = normals;
		this.textures = textures;
	}
	
	public void prepareTextures(GL10 gl){
		Material[] materials = new Material[parts.size()];
		
		int activeTextures = 0;
		for(int i=0;i<parts.size();i++){
			materials[i] = parts.get(i).getMaterial();
			if(materials[i] != null)
				activeTextures++;
		}
		
		bindedTextures = new int[activeTextures];
		gl.glGenTextures(activeTextures, bindedTextures, 0);
		int textureId = 0;
		for(int i=0;i<materials.length;i++){
			if(materials[i] == null)
				continue;
			
			Bitmap bitmap = materials[i].getBitmap(context);
			
			if(bitmap != null) {
				gl.glBindTexture(GL10.GL_TEXTURE_2D, bindedTextures[textureId]);
				gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
				gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
				
				GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
				bitmap.recycle();
				
				textureId++;
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
			gl.glDrawElements(GL10.GL_TRIANGLES, modelPart.getFacesSize(), GL10.GL_UNSIGNED_SHORT, modelPart.getFaceBuffer());
			
			gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
			gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		}
		
		//sphere.draw(gl);
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
	
	public void setNormals(Vector<Float> normals) {
		this.normals = normals;
	}
	
	public void setTextures(Vector<Float> textures) {
		this.textures = textures;
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
}
