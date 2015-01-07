package com.bakoproductions.fossilsviewer.ar;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.bakoproductions.fossilsviewer.gestures.RotationDetector;
import com.bakoproductions.fossilsviewer.objects.BoundingSphere;

import edu.dhbw.andar.AndARRenderer;
import edu.dhbw.andar.interfaces.OpenGLRenderer;
import edu.dhbw.andar.util.GraphicsUtil;
/**
 * A custom OpenGL renderer, that gets registered to the {@link AndARRenderer}.
 * It allows you to draw non Augmented Reality stuff, and setup the OpenGL
 * environment.
 * @author tobi
 *
 */
public class ARRenderer implements OpenGLRenderer {
	private ScaleGestureDetector scaleDetector;
    private RotationDetector rotationDetector;
	
    private CustomObject customObject;
	
    private float[] ambientlight = {1.0f, 1.0f, 1.0f, 1f};
	private float[] diffuselight = {1.0f, 1.0f, 1.0f, 1f};
	private float[] specularlight = {1.0f, 1.0f, 1.0f, 1f};
	private float[] lightposition;
	
	private FloatBuffer lightPositionBuffer;
	private FloatBuffer specularLightBuffer;
	private FloatBuffer diffuseLightBuffer;
	private FloatBuffer ambientLightBuffer;
    
	public ARRenderer(Context context, CustomObject customObject) {
		this.customObject = customObject;
		
		scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		rotationDetector = new RotationDetector(new RotationListener());
		
		BoundingSphere sphere = customObject.getModel().getSphere();
		float[] center = sphere.getCenter();
		float diameter = sphere.getDiameter();
		
		lightposition = new float[] {center[0], center[1] - diameter, center[2], 1.0f};
		
		lightPositionBuffer =  GraphicsUtil.makeFloatBuffer(lightposition);
		specularLightBuffer = GraphicsUtil.makeFloatBuffer(specularlight);
		diffuseLightBuffer = GraphicsUtil.makeFloatBuffer(diffuselight);
		ambientLightBuffer = GraphicsUtil.makeFloatBuffer(ambientlight);
	}
	/**
	 * Do non Augmented Reality stuff here. Will be called once after all AR objects have
	 * been drawn. The transformation matrices may have to be reset.
	 */
	public final void draw(GL10 gl) {
	}


	/**
	 * Directly called before each object is drawn. Used to setup lighting and
	 * other OpenGL specific things.
	 */
	public final void setupEnv(GL10 gl) {
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_AMBIENT, ambientLightBuffer);
		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_DIFFUSE, diffuseLightBuffer);
		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_SPECULAR, specularLightBuffer);
		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_POSITION, lightPositionBuffer);
		gl.glEnable(GL10.GL_LIGHT1);
		
		initGL(gl);
	}

	@Override
	public void initGL(GL10 gl) {
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_NORMALIZE);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);  
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		rotationDetector.onTouchEvent(event);
		scaleDetector.onTouchEvent(event);
		return true;
	}
	
	private class RotationListener implements RotationDetector.OnRotationListener{
		@Override
		public void onRotation(RotationDetector rotationDetector, float x, float y) {
			if(!scaleDetector.isInProgress()){
				final float dx = x - rotationDetector.getLastTouchX();
				final float dy = y - rotationDetector.getLastTouchY();
				
				float rotX = customObject.getRotX();
				float rotY = customObject.getRotY();
				
				rotX += dy * RotationDetector.ROTATION_SCALE;
				rotY += dx * RotationDetector.ROTATION_SCALE;
				
				if(rotX >= 360.0f || rotX <= -360.0f) {
					rotX = 0.0f;
				}
				
				if(rotY >= 360.0f || rotY <= -360.0f) {
					rotY = 0.0f;
				}
				
				customObject.setRotX(rotX);
				customObject.setRotY(rotY);
			}
		}
	}
	
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
	    @Override
	    public boolean onScale(ScaleGestureDetector detector) {
	    	if(!rotationDetector.isInProgress()) {
	    		float scaleFactor = customObject.getScaleFactor();
	    		
		        scaleFactor *= detector.getScaleFactor();
		        scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 50.0f));
		        
		        customObject.setScaleFactor(scaleFactor);
		        return true;
	    	}
	    	return false;
	    }
	}

}
