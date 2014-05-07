package com.bakoproductions.fossilsviewer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.bakoproductions.fossilsviewer.objects.Model;
import com.bakoproductions.fossilsviewer.util.OBJParser;
import com.bakoproductions.fossilsviewer.util.Parser;

import static com.bakoproductions.fossilsviewer.util.Globals.BYTES_PER_FLOAT;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLSurfaceView.Renderer;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class ModelRenderer extends GLSurfaceView implements Renderer{
	private OBJParser parser;
	private Model model;
	
	/* Rotation values */
	private float xrot;					//X Rotation
	private float yrot;					//Y Rotation

	/* Rotation speed values */
	
	private float xspeed;				//X Rotation Speed ( NEW )
	private float yspeed;				//Y Rotation Speed ( NEW )
	
	private float z = 50.0f;
	
	private float oldX;
    private float oldY;
	private final float TOUCH_SCALE = 0.4f;		//Proved to be good for normal rotation ( NEW )
	
	private float[] lightAmbient = {1.0f, 1.0f, 1.0f, 1.0f};
	private float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
	private float[] lightPosition = {0.0f, -3.0f, 2.0f, 1.0f};
	private FloatBuffer lightAmbientBuffer;
	private FloatBuffer lightDiffuseBuffer;
	private FloatBuffer lightPositionBuffer;
	
	public ModelRenderer(Context context) {
		super(context);
		
		parser = new OBJParser(context, "cube.obj");
		int result = parser.parse(model);
		
		if(result == Parser.IO_ERROR)
			return;
		
		if(result == Parser.RESOURCE_NOT_FOUND_ERROR)
			return;

		this.setRenderer(this);
		this.requestFocus();
		this.setFocusableInTouchMode(true);
		
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

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbientBuffer);		
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuseBuffer);		
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPositionBuffer);	
		gl.glEnable(GL10.GL_LIGHT0);
									
		
		gl.glShadeModel(GL10.GL_SMOOTH); 			
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); 	
		gl.glClearDepthf(1.0f); 					
		gl.glEnable(GL10.GL_DEPTH_TEST); 			
		gl.glDepthFunc(GL10.GL_LEQUAL); 		
	
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
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
		GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, 0.1f, 500.0f);

		gl.glMatrixMode(GL10.GL_MODELVIEW); 	//Select The Modelview Matrix
		gl.glLoadIdentity(); 
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		//Clear Screen And Depth Buffer
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);	
		gl.glLoadIdentity();					
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glTranslatef(0.0f, -1.2f, -z);	//Move down 1.2 Unit And Into The Screen 6.0
		gl.glRotatef(xrot, 1.0f, 0.0f, 0.0f);	//X
		gl.glRotatef(yrot, 0.0f, 1.0f, 0.0f);	//Y
		model.draw(gl);						//Draw the square
		gl.glLoadIdentity();
		
		xrot += xspeed;
		yrot += yspeed;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//
		float x = event.getX();
        float y = event.getY();
        
        //If a touch is moved on the screen
        if(event.getAction() == MotionEvent.ACTION_MOVE) {
        	//Calculate the change
        	float dx = x - oldX;
	        float dy = y - oldY;
        	//Define an upper area of 10% on the screen
        	int upperArea = this.getHeight() / 10;
        	
        	//Zoom in/out if the touch move has been made in the upper
        	if(y < upperArea) {
        		z -= dx * TOUCH_SCALE / 2;
        	
        	//Rotate around the axis otherwise
        	} else {        		
    	        xrot += dy * TOUCH_SCALE;
    	        yrot += dx * TOUCH_SCALE;
        	}        
        
        //A press on the screen
        } else if(event.getAction() == MotionEvent.ACTION_UP) {


        }
        
        //Remember the values
        oldX = x;
        oldY = y;
        
        //We handled the event
		return true;
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		//
		if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			
		} else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			
		} else if(keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			z -= 3;
			
		} else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			z += 3;
			
		} else if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {

		}

		//We handled the event
		return true;
	}
}
