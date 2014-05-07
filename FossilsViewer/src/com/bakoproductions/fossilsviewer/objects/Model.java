package com.bakoproductions.fossilsviewer.objects;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;

public class Model {
	private Vector<Float> vertices;
	private Vector<Float> normals;
	private Vector<Float> textures;
	private Vector<ModelPart> parts;
	private FloatBuffer vertexBuffer;
	
	public Model(){
		
	}
	
	public Model(Vector<Float> verticies, Vector<Float> normals, Vector<Float> textures, Vector<ModelPart> parts){
		this.vertices = verticies;
		this.normals = normals;
		this.textures = textures;
	}
	
	public void draw(GL10 gl){
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		
		for(int i=0; i<parts.size(); i++){
			ModelPart modelPart = parts.get(i);
			Material material = modelPart.getMaterial();
			if(material != null){
				FloatBuffer a = material.getAmbientColorBuffer();
				FloatBuffer d = material.getDiffuseColorBuffer();
				FloatBuffer s = material.getSpecularColorBuffer();
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, a);
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, s);
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, d);
			}
			gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
			gl.glNormalPointer(GL10.GL_FLOAT, 0, modelPart.getNormalBuffer());
			gl.glDrawElements(GL10.GL_TRIANGLES, modelPart.getFacesSize(), GL10.GL_UNSIGNED_SHORT, modelPart.getFaceBuffer());
			//gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
			//gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
			gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		}
	}
	
	public void buildVertexBuffer(){
		ByteBuffer vBuf = ByteBuffer.allocateDirect(vertices.size() * 4);
		vBuf.order(ByteOrder.nativeOrder());
		vertexBuffer = vBuf.asFloatBuffer();
		vertexBuffer.put(toFloatArray(vertices));
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
	
	public void setParts(Vector<ModelPart> parts) {
		this.parts = parts;
	}
	
	private float[] toFloatArray(Vector<Float> vector){
		float[] f;
		f = new float[vector.size()];
		for (int i=0; i<vector.size(); i++){
			f[i] = vector.get(i);
		}
		return f;
	}
}
