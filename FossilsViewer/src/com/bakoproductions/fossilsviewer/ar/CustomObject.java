package com.bakoproductions.fossilsviewer.ar;

import javax.microedition.khronos.opengles.GL10;

import com.bakoproductions.fossilsviewer.objects.Model;

import edu.dhbw.andar.ARObject;

public class CustomObject extends ARObject {
	private Model model;
	
	/* ====== Touch events parameters ====== */
	private float scaleFactor = 1.0f;
	   
    private float posX = 0.0f;
    private float posY = 0.0f;
 
    private float rotX = 0.0f;
    private float rotY = 0.0f;
    /* ===================================== */
	
	public CustomObject(String name, String patternName, double markerWidth, double[] markerCenter, Model model) {
		super(name, patternName, markerWidth, markerCenter);

		this.model = model;
	}
	
	@Override
	public void init(GL10 gl) {
		gl.glEnable(GL10.GL_TEXTURE_2D);
		model.prepareTextures(gl);
	}
	
	@Override
	public final void draw(GL10 gl) {
		super.draw(gl);
		
		// +X --> Left to Right
		// +Y --> Back to Forward
		// +Z --> Bottom to Top
		float[] center = model.getSphere().getCenter();
		float radious = model.getSphere().getDiameter() / 2;
		
		gl.glTranslatef(0, 0, radious);
		
		gl.glScalef(scaleFactor, scaleFactor, scaleFactor);
		gl.glRotatef(rotX, 1, 0, 0);
		gl.glRotatef(rotY, 0, 0, 1);
		gl.glTranslatef(-center[0], -center[1], -center[2]);
		
		model.draw(gl);
	}
	
	public float getScaleFactor() {
		return scaleFactor;
	}
	
	public void setScaleFactor(float scaleFactor) {
		this.scaleFactor = scaleFactor;
	}
	
	public float getPosX() {
		return posX;
	}
	
	public void setPosX(float posX) {
		this.posX = posX;
	}
	
	public float getPosY() {
		return posY;
	}
	
	public void setPosY(float posY) {
		this.posY = posY;
	}
	
	public float getRotX() {
		return rotX;
	}
	
	public void setRotX(float rotX) {
		this.rotX = rotX;
	}
	
	public float getRotY() {
		return rotY;
	}
	
	public void setRotY(float rotY) {
		this.rotY = rotY;
	}
	
	public Model getModel() {
		return model;
	}
}
