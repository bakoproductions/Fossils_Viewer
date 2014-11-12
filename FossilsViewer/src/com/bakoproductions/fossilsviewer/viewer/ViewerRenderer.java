package com.bakoproductions.fossilsviewer.viewer;

import static com.bakoproductions.fossilsviewer.util.Globals.BYTES_PER_FLOAT;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.util.Log;

import com.bakoproductions.fossilsviewer.objects.BoundingSphere;
import com.bakoproductions.fossilsviewer.objects.Model;

public class ViewerRenderer implements Renderer {
	private static final String TAG = ViewerRenderer.class.getSimpleName();
	private Model model;
    
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
	
	/* ======== User Interaction =========== */
	private boolean userClicked;
	private float clickX;
	private float clickY;
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
	
	public ViewerRenderer(Model model) {
		this.setModel(model);
		
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
		Log.i("Bako", "new renderer completed");
		
		lockedTranslation = false;
		closedLight = false;
		userClicked = false;
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		Log.i("Bako", "surface created");
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
		getModel().prepareTextures(gl);
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
		BoundingSphere sphere = getModel().getSphere();
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
		
		float[] center = getModel().getSphere().getCenter();
		float diameter = getModel().getSphere().getDiameter();
		
		GLU.gluLookAt(gl, -posX, -posY, diameter, -posX, -posY, 0, 0.0f, 1.0f, 0.0f);
		
		gl.glPushMatrix();		
		gl.glScalef(getScaleFactor(), getScaleFactor(), getScaleFactor());
		gl.glRotatef(getRotX(), 1, 0, 0);
		gl.glRotatef(getRotY(), 0, 1, 0);
		gl.glTranslatef(-center[0], -center[1], -center[2]);
		getModel().draw(gl);
		
		if(userClicked) {
			Log.i(TAG, "User clicked at: " + clickX + ", " + clickY);
			userClicked = false;
		}
		
		gl.glPopMatrix();
	}
	
	public void setLockedTranslation(boolean lockedTranslation) {
		this.lockedTranslation = lockedTranslation;
	}
	
	public boolean isLockedTranslation() {
		return lockedTranslation;
	}
	
	public void centerModel() {
		setPosX(0);
		setPosY(0);
	}
	
	public void setClosedLight(boolean closedLight) {
		this.closedLight = closedLight;
	}
	
	public boolean isClosedLight() {
		return closedLight;
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

	public void setModel(Model model) {
		this.model = model;
	}
	
	public void setUserClicked(boolean userClicked) {
		this.userClicked = userClicked;
	}
	
	public void setClickX(float clickX) {
		this.clickX = clickX;
	}
	
	public void setClickY(float clickY) {
		this.clickY = clickY;
	}
}
