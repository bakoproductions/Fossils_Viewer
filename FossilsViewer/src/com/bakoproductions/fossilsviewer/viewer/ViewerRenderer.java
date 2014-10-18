package com.bakoproductions.fossilsviewer.viewer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.bakoproductions.fossilsviewer.gestures.RotationDetector;
import com.bakoproductions.fossilsviewer.gestures.RotationDetector.OnRotationListener;
import com.bakoproductions.fossilsviewer.gestures.TranslationDetector;
import com.bakoproductions.fossilsviewer.objects.BoundingSphere;
import com.bakoproductions.fossilsviewer.objects.Material;
import com.bakoproductions.fossilsviewer.objects.Model;
import com.bakoproductions.fossilsviewer.objects.ModelPart;
import com.bakoproductions.fossilsviewer.parsers.MTLParser;
import com.bakoproductions.fossilsviewer.parsers.ModelParser;
import com.bakoproductions.fossilsviewer.parsers.OBJParser;

import static com.bakoproductions.fossilsviewer.util.Globals.BYTES_PER_FLOAT;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class ViewerRenderer extends GLSurfaceView implements Renderer{
	private Model model;
	private Context context;
	
	private ScaleGestureDetector scaleDetector;
	private TranslationDetector translationDetector;
    private RotationDetector rotationDetector;
    
    private float posX = 0.0f;
    private float posY = 0.0f;
    /* ====== Touch events parameters ====== */
    private float scaleFactor = 1.0f;
	
    private float transX = 0.0f;
    private float transY = 0.0f;
    private float rotX = 0.0f;
    private float rotY = 0.0f;
    /* ===================================== */
	
	/* = Perspective projection parameters = */
	private float zNear = 0.1f;
	private float zFar;
	/* ===================================== */
	
	/* ============= Lights ================ */
	private float[] lightAmbient = {0.8f, 0.8f, 0.8f, 1.0f};
	private float[] lightDiffuse = {0.8f, 0.8f, 0.8f, 1.0f};
	private float[] lightPosition = {0.0f, 2.0f, 2.0f, 1.0f};
	private FloatBuffer lightAmbientBuffer;
	private FloatBuffer lightDiffuseBuffer;
	private FloatBuffer lightPositionBuffer;
	/* ===================================== */
	
	public ViewerRenderer(Context context) {
		super(context);
		
		this.context = context;
		this.setRenderer(this);
		
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(lightAmbient.length * BYTES_PER_FLOAT);
		byteBuf.order(ByteOrder.nativeOrder());
		lightAmbientBuffer = byteBuf.asFloatBuffer();
		lightAmbientBuffer.put(lightAmbient);
		lightAmbientBuffer.position(0);
		
		byteBuf = ByteBuffer.allocateDirect(lightDiffuse.length * BYTES_PER_FLOAT);
		byteBuf.order(ByteOrder.nativeOrder());
		lightDiffuseBuffer = byteBuf.asFloatBuffer();
		lightDiffuseBuffer.put(lightDiffuse);
		lightDiffuseBuffer.position(0);
		
		byteBuf = ByteBuffer.allocateDirect(lightPosition.length * BYTES_PER_FLOAT);
		byteBuf.order(ByteOrder.nativeOrder());
		lightPositionBuffer = byteBuf.asFloatBuffer();
		lightPositionBuffer.put(lightPosition);
		lightPositionBuffer.position(0);
	}
	
	public ViewerRenderer(Context context, Model model) {
		super(context);
	
		this.model = model;
		this.setRenderer(this);
		//this.requestFocus();
		//this.setFocusableInTouchMode(true);
		
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(lightAmbient.length * BYTES_PER_FLOAT);
		byteBuf.order(ByteOrder.nativeOrder());
		lightAmbientBuffer = byteBuf.asFloatBuffer();
		lightAmbientBuffer.put(lightAmbient);
		lightAmbientBuffer.position(0);
		
		byteBuf = ByteBuffer.allocateDirect(lightDiffuse.length * BYTES_PER_FLOAT);
		byteBuf.order(ByteOrder.nativeOrder());
		lightDiffuseBuffer = byteBuf.asFloatBuffer();
		lightDiffuseBuffer.put(lightDiffuse);
		lightDiffuseBuffer.position(0);
		
		byteBuf = ByteBuffer.allocateDirect(lightPosition.length * BYTES_PER_FLOAT);
		byteBuf.order(ByteOrder.nativeOrder());
		lightPositionBuffer = byteBuf.asFloatBuffer();
		lightPositionBuffer.put(lightPosition);
		lightPositionBuffer.position(0);
				
		scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		translationDetector = new TranslationDetector(new TranslationListener());
		rotationDetector = new RotationDetector(new RotationListener());
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		//gl.glEnable(GL10.GL_LIGHTING);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbientBuffer);		
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuseBuffer);		
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPositionBuffer);	
		gl.glEnable(GL10.GL_LIGHT0);
		
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		// Load the texture
		gl.glClearColor(1.0f, 1.0f, 1.0f, 0.5f); 	
		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glShadeModel(GL10.GL_SMOOTH);					
		gl.glEnable(GL10.GL_DEPTH_TEST); 			
		gl.glDepthFunc(GL10.GL_LEQUAL); 		
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
		model.prepareTextures(gl);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if(height == 0) { 						//Prevent A Divide By Zero By
			height = 1; 						//Making Height Equal One
		}

		gl.glViewport(0, 0, width, height); 	//Reset The Current Viewport
		gl.glMatrixMode(GL10.GL_PROJECTION); 	//Select The Projection Matrix
		gl.glLoadIdentity(); 					//Reset The Projection Matrix

		//Calculate The Aspect Ratio Of The Window
		BoundingSphere sphere = model.getSphere();
		float diameter = sphere.getDiameter();
		zFar = zNear + diameter;
		
		GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, zNear, zFar*5.0f);
		//gl.glFrustumf(-width, width, -height, height, zNear, zFar+50.0f);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW); 	//Select The Modelview Matrix
		gl.glLoadIdentity(); 
	}

	@Override
	public void onDrawFrame(GL10 gl) {		
		BoundingSphere sphere = model.getSphere();
		float[] center = sphere.getCenter();
		
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();					
		
		gl.glClearColor(0.9f, 0.9f, 0.9f, 1.0f);
		GLU.gluLookAt(gl, 0, 0, sphere.getDiameter(), 0, 0, 0, 0.0f, 1.0f, 0.0f);
		//gl.glPushMatrix();
		
		
		gl.glScalef(scaleFactor, scaleFactor, scaleFactor);
		
		gl.glTranslatef(-posX , -posY , 0);
		
		gl.glRotatef(rotX, 1, 0, 0);
		gl.glRotatef(rotY, 0, 1, 0);
		
		gl.glTranslatef(posX, posY, 0);
		
		model.draw(gl);
		//gl.glPopMatrix();
		gl.glLoadIdentity();
		
		posX += transX;
		posY += transY;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		rotationDetector.onTouchEvent(event);
		scaleDetector.onTouchEvent(event);
		translationDetector.onTouchEvent(event);
		return true;
	}
	
	private class TranslationListener implements TranslationDetector.OnTranslationListener{
		@Override
		public void onTranslation(TranslationDetector translationDetector, float x, float y) {
			if(!scaleDetector.isInProgress()){
				final float dx = x - translationDetector.getLastTouchX();
				final float dy = y - translationDetector.getLastTouchY();
				
				transX += dx/100;
				transY -= dy/100;
				
				invalidate();
			}
		}
	}
	
	private class RotationListener implements RotationDetector.OnRotationListener{
		@Override
		public void onRotation(RotationDetector rotationDetector, float x, float y) {
			if(!scaleDetector.isInProgress()){
				final float dx = x - rotationDetector.getLastTouchX();
				final float dy = y - rotationDetector.getLastTouchY();
				
				rotX += dy * RotationDetector.ROTATION_SCALE;
				rotY += dx * RotationDetector.ROTATION_SCALE;
				
				invalidate();
			}
		}
	}
	
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
	    @Override
	    public boolean onScale(ScaleGestureDetector detector) {
	        scaleFactor *= detector.getScaleFactor();
	        
	        scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));

	        invalidate();
	        return true;
	    }
	}
}
