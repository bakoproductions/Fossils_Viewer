package com.bakoproductions.fossilsviewer.viewer;

import static com.bakoproductions.fossilsviewer.util.Globals.BYTES_PER_FLOAT;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.bakoproductions.fossilsviewer.ar.ARActivity;
import com.bakoproductions.fossilsviewer.gestures.ClickDetector;
import com.bakoproductions.fossilsviewer.gestures.LongPressDetector;
import com.bakoproductions.fossilsviewer.gestures.RotationDetector;
import com.bakoproductions.fossilsviewer.gestures.TranslationDetector;
import com.bakoproductions.fossilsviewer.objects.BoundingSphere;
import com.bakoproductions.fossilsviewer.objects.Model;

public class ViewerRenderer extends GLSurfaceView implements Renderer {
	private static final String TAG = ViewerRenderer.class.getSimpleName();
	private Model model;
	private Context context;
	
	private ClickDetector clickDetector;
	private LongPressDetector longPressDetector;
	private ScaleGestureDetector scaleDetector;
	private TranslationDetector translationDetector;
    private RotationDetector rotationDetector;
    
    private boolean lockedTranslation;
    private boolean closedLight;
    
    /* ====== Touch events parameters ====== */
    private float scaleFactor = 1.0f;
	   
    private float posX = 0.0f;
    private float posY = 0.0f;
 
    private float rotX = 0.0f;
    private float rotY = 0.0f;
    /* ===================================== */
	
	/* = Perspective projection parameters = */
	private float zNear = 1.0f;
	private float zFar;
	/* ===================================== */
	
	/* ============= Lights ================ */
	private float[] lightAmbient;
	private float[] lightDiffuse;
	private float[] lightSpecular;
	private float[] lightPosition;
	private float[] lightDirection;
	
	private FloatBuffer lightAmbientBuffer;
	private FloatBuffer lightDiffuseBuffer;
	private FloatBuffer lightSpecularBuffer;
	
	private FloatBuffer lightPositionBuffer;
	private FloatBuffer lightDirectionBuffer;
	/* ===================================== */
	
	public ViewerRenderer(Context context, Model model) {
		super(context);
		
		this.context = context;
		this.model = model;
		this.setRenderer(this);
		
		lightAmbient = new float[]{0.1f, 0.1f, 0.1f, 1.0f};
		lightDiffuse = new float[]{0.2f, 0.2f, 0.2f, 1.0f};
		lightSpecular = new float[]{0.7f, 0.7f, 0.7f, 1.0f};
		
		float[] center = model.getSphere().getCenter();
		float diameter = model.getSphere().getDiameter();
		
		lightPosition = new float[]{0, 0, diameter, 1.0f};
		lightDirection = new float[]{center[0], center[1], center[2], 1.0f};
		
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
		
		byteBuf = ByteBuffer.allocateDirect(lightSpecular.length * BYTES_PER_FLOAT);
		byteBuf.order(ByteOrder.nativeOrder());
		lightSpecularBuffer = byteBuf.asFloatBuffer();
		lightSpecularBuffer.put(lightSpecular);
		lightSpecularBuffer.position(0);
		
		byteBuf = ByteBuffer.allocateDirect(lightPosition.length * BYTES_PER_FLOAT);
		byteBuf.order(ByteOrder.nativeOrder());
		lightPositionBuffer = byteBuf.asFloatBuffer();
		lightPositionBuffer.put(lightPosition);
		lightPositionBuffer.position(0);
		
		byteBuf = ByteBuffer.allocateDirect(lightDirection.length * BYTES_PER_FLOAT);
		byteBuf.order(ByteOrder.nativeOrder());
		lightDirectionBuffer = byteBuf.asFloatBuffer();
		lightDirectionBuffer.put(lightDirection);
		lightDirectionBuffer.position(0);
		
		lockedTranslation = false;
		closedLight = false;
		
		clickDetector = new ClickDetector(new ClickListener());
		longPressDetector = new LongPressDetector(new LongPressListener());
		scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		translationDetector = new TranslationDetector(new TranslationListener());
		rotationDetector = new RotationDetector(new RotationListener());
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {	
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbientBuffer);		
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuseBuffer);	
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, lightSpecularBuffer);	
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPositionBuffer);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPOT_DIRECTION, lightDirectionBuffer);
		//gl.glLightf(GL10.GL_LIGHT0, GL10.GL_SPOT_CUTOFF, 45.0f);
		//gl.glEnable(GL10.GL_LIGHT0);
		
		// Load the texture
		gl.glClearColor(0.9f, 0.9f, 0.9f, 1.0f); 	
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
		
		gl.glMatrixMode(GL10.GL_MODELVIEW); 	//Select The Modelview Matrix
		gl.glLoadIdentity(); 
	}

	@Override
	public void onDrawFrame(GL10 gl) {		
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		
		if(closedLight)
			gl.glDisable(GL10.GL_LIGHTING);
		else
			gl.glEnable(GL10.GL_LIGHTING);
		
		gl.glLoadIdentity();					
		
		float[] center = model.getSphere().getCenter();
		float diameter = model.getSphere().getDiameter();
		
		GLU.gluLookAt(gl, -posX, -posY, diameter, -posX, -posY, 0, 0.0f, 1.0f, 0.0f);
		
		gl.glPushMatrix();		
		gl.glScalef(scaleFactor, scaleFactor, scaleFactor);
		gl.glRotatef(rotX, 1, 0, 0);
		gl.glRotatef(rotY, 0, 1, 0);
		gl.glTranslatef(-center[0], -center[1], -center[2]);
		model.draw(gl);
		gl.glPopMatrix();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		clickDetector.onTouchEvent(event);
		longPressDetector.onTouchEvent(event);
		rotationDetector.onTouchEvent(event);
		scaleDetector.onTouchEvent(event);
		translationDetector.onTouchEvent(event);
		return true;
	}
	
	public void setLockedTranslation(boolean lockedTranslation) {
		this.lockedTranslation = lockedTranslation;
	}
	
	public boolean isLockedTranslation() {
		return lockedTranslation;
	}
	
	public void centerModel() {
		posX = 0;
		posY = 0;
	}
	
	public void setClosedLight(boolean closedLight) {
		this.closedLight = closedLight;
	}
	
	public boolean isClosedLight() {
		return closedLight;
	}
	
	private class ClickListener implements ClickDetector.OnClickListener {
		@Override
		public void onClick(ClickDetector clickDetector, int x, int y) {
			if(!longPressDetector.isInProgress()) { 
				Intent intent = new Intent(context, ARActivity.class);
				intent.putExtra("model", model);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
		}
	}
	
	private class LongPressListener implements LongPressDetector.OnLongClickListener {
		@Override
		public void onLongClick(LongPressDetector longPressDetector, float x, float y) {
			Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(100);
			Log.i(TAG, "X: " + x + ", Y:" + y);
		}
	}
	
	private class TranslationListener implements TranslationDetector.OnTranslationListener {
		@Override
		public void onTranslation(TranslationDetector translationDetector, float x, float y) {
			if(x < 0 || y < 0)
				return;
			
			if(lockedTranslation) {
				return;
			}
			
			if(!scaleDetector.isInProgress() && !rotationDetector.isInProgress()){
				final float dx = x - translationDetector.getLastTouchX();
				final float dy = y - translationDetector.getLastTouchY();
				
				posX += dx/100;
				posY -= dy/100;
								
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
				
				if(rotX >= 360.0f || rotX <= -360.0f) {
					rotX = 0.0f;
				}
				
				if(rotY >= 360.0f || rotY <= -360.0f) {
					rotY = 0.0f;
				}
				
				invalidate();
			}
		}
	}
	
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
	    @Override
	    public boolean onScale(ScaleGestureDetector detector) {
	    	if(!rotationDetector.isInProgress()) {
		        scaleFactor *= detector.getScaleFactor();
		        
		        scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));
	
		        invalidate();
		        return true;
	    	}
	    	return false;
	    }
	}
}
