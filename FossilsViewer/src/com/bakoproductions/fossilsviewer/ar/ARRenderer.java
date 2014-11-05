package com.bakoproductions.fossilsviewer.ar;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.bakoproductions.fossilsviewer.gestures.RotationDetector;
import com.bakoproductions.fossilsviewer.gestures.TranslationDetector;

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
	private TranslationDetector translationDetector;
    private RotationDetector rotationDetector;
	
    private CustomObject customObject;
	/**
	 * Light definitions
	 */	
	
	/*private float[] ambientlight1 = {.3f, .3f, .3f, 1f};
	private float[] diffuselight1 = {.7f, .7f, .7f, 1f};
	private float[] specularlight1 = {0.6f, 0.6f, 0.6f, 1f};
	private float[] lightposition1 = {20.0f,-40.0f,100.0f,0.0f};*/
	
	private float[] ambientlight1 = {1.0f, 1.0f, 1.0f, 1f};
	private float[] diffuselight1 = {1.0f, 1.0f, 1.0f, 1f};
	private float[] specularlight1 = {1.0f, 1.0f, 1.0f, 1f};
	private float[] lightposition1 = {0.0f, 1.0f, 1.0f,0.0f};
	
	private FloatBuffer lightPositionBuffer1 =  GraphicsUtil.makeFloatBuffer(lightposition1);
	private FloatBuffer specularLightBuffer1 = GraphicsUtil.makeFloatBuffer(specularlight1);
	private FloatBuffer diffuseLightBuffer1 = GraphicsUtil.makeFloatBuffer(diffuselight1);
	private FloatBuffer ambientLightBuffer1 = GraphicsUtil.makeFloatBuffer(ambientlight1);
	
	public ARRenderer(Context context, CustomObject customObject) {
		this.customObject = customObject;
		
		scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		rotationDetector = new RotationDetector(new RotationListener());
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
		//gl.glEnable(GL10.GL_LIGHTING);
		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_AMBIENT, ambientLightBuffer1);
		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_DIFFUSE, diffuseLightBuffer1);
		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_SPECULAR, specularLightBuffer1);
		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_POSITION, lightPositionBuffer1);
		gl.glEnable(GL10.GL_LIGHT1);
		
		initGL(gl);
	}
	
	/**
	 * Called once when the OpenGL Surface was created.
	 */
	public final void initGL(GL10 gl) {
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_NORMALIZE);
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
