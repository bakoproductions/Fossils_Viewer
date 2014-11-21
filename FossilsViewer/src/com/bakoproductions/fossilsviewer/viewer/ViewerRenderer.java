package com.bakoproductions.fossilsviewer.viewer;

import static com.bakoproductions.fossilsviewer.util.Globals.BYTES_PER_FLOAT;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.Log;

import com.bakoproductions.fossilsviewer.R;
import com.bakoproductions.fossilsviewer.objects.BoundingSphere;
import com.bakoproductions.fossilsviewer.objects.Model;
import com.bakoproductions.fossilsviewer.objects.ModelPart;
import com.bakoproductions.fossilsviewer.shaders.ShadersUtil;
import com.bakoproductions.fossilsviewer.util.MathHelper;

public class ViewerRenderer implements Renderer {
	private static final String TAG = ViewerRenderer.class.getSimpleName();
	
	private Context context;
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
	
	/*
	 * Matrices
	 */
	private float[] modelMatrix = new float[16];
	private float[] viewMatrix = new float[16];
	private float[] projectionMatrix = new float[16];
	private float[] temporaryMatrix = new float[16];
	private float[] lightModelMatrix = new float[16];
	private float[] MVPMatrix = new float[16];
	private int[] viewport;
	
	/*
	 * Point Information
	 */
	private float[] pointMVPMatrix = new float[16];
	private float[] pointModelMatrix = new float[16];
	private final float[] pointPosInWorldSpace = new float[4];
	private final float[] pointPosInEyeSpace = new float[4];
	
	/*
	 * Light Information
	 */
	private float[] lightColor;
	private final float[] lightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};
	private final float[] lightPosInWorldSpace = new float[4];
	private final float[] lightPosInEyeSpace = new float[4];
	
	/*
	 * Shader variable pointers
	 */
	int mvpMatrixUniform;
	private int mvMatrixUniform;
	private int textureUniform;
	private int lightPosUniform;

	/** OpenGL handles to our program attributes. */
	private int positionAttribute;
	private int normalAttribute;
	private int textCoordinateAttribute;
	private int lightColorAttribute;
	
	private int pointPositionAttribute;
	private int pointMVPMatrixUniform;
	
	private int[] textureData;
	private int vertexProgramID;
	private int pointProgramID;
	
	private FloatBuffer pointsBuffer = null;
	
	public ViewerRenderer(Context context, Model model) {
		this.context = context;
		this.setModel(model);
		
		lockedTranslation = false;
		closedLight = false;
		userClicked = false;
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);
		
		GLES20.glDisable(GLES20.GL_CULL_FACE);
		GLES20.glCullFace(GLES20.GL_BACK);
		GLES20.glFrontFace(GLES20.GL_CW);
	    GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		
	    GLES20.glEnable(GLES20.GL_TEXTURE_2D);
	    
		// Position the eye behind the origin.
	    final float eyeX = -posX;
	    final float eyeY = -posY;
	    final float eyeZ = model.getSphere().getDiameter();
	 
	    // We are looking toward the distance
	    final float lookX = -posX;
	    final float lookY = -posY;
	    final float lookZ = 0;
	 
	    // Set our up vector. This is where our head would be pointing were we holding the camera.
	    final float upX = 0.0f;
	    final float upY = 1.0f;
	    final float upZ = 0.0f;
		
	    Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
	    
	    ShadersUtil perVertexShaders = new ShadersUtil(context, R.raw.texture_light_vertex_shader, R.raw.texture_light_fragment_shader);	    
	    perVertexShaders.loadVertexShader();
	    perVertexShaders.loadFragmentShader();
	    vertexProgramID = perVertexShaders.linkShaders();
	    
		textureData = model.prepareTextures();
		model.bindBuffers();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if(height == 0) { 						//Prevent A Divide By Zero By
			height = 1; 						//Making Height Equal One
		}
		
		GLES20.glViewport(0, 0, width, height);
		viewport = new int[] {0, 0, width, height};
		
		//Calculate The Aspect Ratio Of The Window
		BoundingSphere sphere = getModel().getSphere();
		float diameter = sphere.getDiameter();
		zFar = zNear + diameter;
		
		final float ratio = (float) width / height;
		final float left  = -ratio;
		final float right = ratio;
		final float bottom = -1.0f;
		final float top = 1.0f;
		final float near = zNear;
		final float far = zFar*5.0f;
		
		Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far);
	}

	@Override
	public void onDrawFrame(GL10 gl) {		
		GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		GLES20.glUseProgram(vertexProgramID);
		getVariablesFromShaders();
                
		float[] center = model.getSphere().getCenter();
        float diameter = model.getSphere().getDiameter();
        Matrix.setLookAtM(viewMatrix, 0, -posX, -posY, diameter, -posX, -posY, 0.0f, 0.0f, 1.0f, 0.0f);
        activateTextures();
        positionLight(diameter);

        float[] P1 = new float[3];
    	float[] P2 = new float[3];
        if(userClicked)  	
        	createRay(P1, P2);
        
        positionModel(center);
        model.draw(positionAttribute, normalAttribute, textCoordinateAttribute);
        
        if(userClicked) {
        	findIntersections(P1, P2);
	        userClicked = false;
        }
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
	
	private void drawLight() {
		final int pointMVPMatrixID = GLES20.glGetUniformLocation(pointProgramID, "u_MVPMatrix");
        final int pointPositionID = GLES20.glGetAttribLocation(pointProgramID, "a_Position");
        
		GLES20.glVertexAttrib3f(pointPositionID, lightPosInModelSpace[0], lightPosInModelSpace[1], lightPosInModelSpace[2]);

        GLES20.glDisableVertexAttribArray(pointPositionID);  
		
		Matrix.multiplyMM(MVPMatrix, 0, viewMatrix, 0, lightModelMatrix, 0);
		Matrix.multiplyMM(MVPMatrix, 0, projectionMatrix, 0, MVPMatrix, 0);
		GLES20.glUniformMatrix4fv(pointMVPMatrixID, 1, false, MVPMatrix, 0);
		
		// Draw the point.
		GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
	}
	
	private void getVariablesFromShaders() {
		mvpMatrixUniform = GLES20.glGetUniformLocation(vertexProgramID, ShadersUtil.MVP_MATRIX_UNIFORM);
        mvMatrixUniform = GLES20.glGetUniformLocation(vertexProgramID, ShadersUtil.MV_MATRIX_UNIFORM);
        textureUniform = GLES20.glGetUniformLocation(vertexProgramID, ShadersUtil.TEXTURE_UNIFORM);
        lightPosUniform = GLES20.glGetUniformLocation(vertexProgramID, ShadersUtil.LIGHT_POS_UNIFORM);
        
        positionAttribute = GLES20.glGetAttribLocation(vertexProgramID, ShadersUtil.POSITION_ATTRIBUTE);
        textCoordinateAttribute = GLES20.glGetAttribLocation(vertexProgramID, ShadersUtil.TEXTURE_COORDINATE_ATTRIBUTE);
        normalAttribute = GLES20.glGetAttribLocation(vertexProgramID, ShadersUtil.NORMAL_ATTRIBUTE);
        lightColorAttribute = GLES20.glGetAttribLocation(vertexProgramID, ShadersUtil.LIGHT_COLOR_ATTRIBUTE);
	}
	
	private void activateTextures() {
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureData[0]);
        GLES20.glUniform1i(textureUniform, 0);
	}
	
	private void positionLight(float diameter) {
		Matrix.setIdentityM(lightModelMatrix, 0);
        Matrix.translateM(lightModelMatrix, 0, 0.0f, 0.0f, diameter);      
        Matrix.multiplyMV(lightPosInWorldSpace, 0, lightModelMatrix, 0, lightPosInModelSpace, 0);
        Matrix.multiplyMV(lightPosInEyeSpace, 0, viewMatrix, 0, lightPosInWorldSpace, 0);
        
        GLES20.glUniform3f(lightPosUniform, lightPosInEyeSpace[0], lightPosInEyeSpace[1], lightPosInEyeSpace[2]);
	}
	
	private void positionModel(float[] center) {
		Matrix.setIdentityM(modelMatrix, 0);
        Matrix.scaleM(modelMatrix, 0, getScaleFactor(), getScaleFactor(), getScaleFactor());
        Matrix.rotateM(modelMatrix, 0, getRotX(), 1, 0, 0);
        Matrix.rotateM(modelMatrix, 0, getRotY(), 0, 1, 0);
        Matrix.translateM(modelMatrix, 0, -center[0], -center[1], -center[2]);
        
        Matrix.multiplyMM(MVPMatrix, 0, viewMatrix, 0, modelMatrix, 0);
     	GLES20.glUniformMatrix4fv(mvMatrixUniform, 1, false, MVPMatrix, 0);
 		Matrix.multiplyMM(temporaryMatrix, 0, projectionMatrix, 0, MVPMatrix, 0);
 		System.arraycopy(temporaryMatrix, 0, MVPMatrix, 0, 16);
		GLES20.glUniformMatrix4fv(mvpMatrixUniform, 1, false, MVPMatrix, 0);
	}
	
	private void findIntersections(float[] P1, float[] P2) {
		ArrayList<float[]> points = new ArrayList<float[]>();  
    	FloatBuffer vertexBuffer = model.getVertexBuffer();
        for(ModelPart part: model.getParts()) {
        	ShortBuffer faceBuffer = part.getFaceBuffer();
        	//Log.i(TAG, "-------- " + faceBuffer.capacity());
        	for(int i=0;i<faceBuffer.capacity()/3;i++) {
        		//Log.i(TAG, "Index: " + i + ", value: " + faceBuffer.get(i));
        		int index1 = faceBuffer.get(i*3);
        		int index2 = faceBuffer.get(i*3 + 1);
        		int index3 = faceBuffer.get(i*3 + 2);
        		
        		float[] v1 = new float[3];
        		v1[0] = vertexBuffer.get(index1*3);
        		v1[1] = vertexBuffer.get(index1*3 + 1);
        		v1[2] = vertexBuffer.get(index1*3 + 2);
        		
        		float[] v2 = new float[3];
        		v2[0] = vertexBuffer.get(index2*3);
        		v2[1] = vertexBuffer.get(index2*3 + 1);
        		v2[2] = vertexBuffer.get(index2*3 + 2);
        		
        		float[] v3 = new float[3];
        		v3[0] = vertexBuffer.get(index3*3);
        		v3[1] = vertexBuffer.get(index3*3 + 1);
        		v3[2] = vertexBuffer.get(index3*3 + 2);
        		
        		float[] hit = new float[3];
        		if(intersects(v1, v2, v3, P1, P2, hit)) {
        			Log.i(TAG, "Hits at: " + hit[0] + ", " + hit[1] + ", " + hit[2]);
        			points.add(hit);
        		}
        	}
        }
	}
	
	private void createRay(float[] P1, float[] P2) {
		float[] temp = new float[4];
		float[] temp2 = new float[4];
		
		float[] MVMatrix = new float[16];
		Matrix.multiplyMM(MVMatrix, 0, viewMatrix, 0, modelMatrix, 0);
		
		int result = GLU.gluUnProject(clickX, (float) viewport[3] - clickY, 0.0f, MVMatrix, 0, projectionMatrix, 0, viewport, 0, temp, 0);
		Matrix.multiplyMV(temp2, 0, MVMatrix, 0, temp, 0);
		if(result == GL10.GL_TRUE) {
			P1[0] = temp2[0] / temp2[3];
			P1[1] = temp2[1] / temp2[3];
            P1[2] = temp2[2] / temp2[3];
		}
		
		result = GLU.gluUnProject(clickX, clickY, 1.0f, MVMatrix, 0, projectionMatrix, 0, viewport, 0, temp, 0);
		Matrix.multiplyMV(temp2, 0, MVMatrix, 0, temp, 0);
		if(result == GL10.GL_TRUE) {
			P2[0] = temp2[0] / temp2[3];
			P2[1] = temp2[1] / temp2[3];
            P2[2] = temp2[2] / temp2[3];
		}		
	}
	
	private boolean intersects(float[] v1, float[] v2, float[] v3, float[] P1, float[] P2, float[] hit) {
		float[] normal = new float[3];
		float[] intersectPos = new float[3];
				
		normal = MathHelper.crossProduct(MathHelper.minus(v2, v1), MathHelper.minus(v3, v1));
		
		float dist1 = MathHelper.dot(MathHelper.minus(P1, v1), normal);
		float dist2 = MathHelper.dot(MathHelper.minus(P2, v1), normal);
		
		// line doesn't cross the triangle
		if((dist1 * dist2) >= 0.0f)
			return false;
		
		// line and plane are parallel
		if(dist1 == dist2) 
			return false;
		
		intersectPos = MathHelper.add(v1, MathHelper.scalarProduct((-dist1/(dist2-dist1)), MathHelper.minus(v2, v1)));
		
		float[] test = new float[3];
		test = MathHelper.crossProduct(normal, MathHelper.minus(v2, v1));
		if(MathHelper.dot(test, MathHelper.minus(intersectPos, v1)) < 0.0f)
			return false;
		
		test = MathHelper.crossProduct(normal, MathHelper.minus(v3, v2));
		if(MathHelper.dot(test, MathHelper.minus(intersectPos, v2)) < 0.0f)
			return false;
		
		test = MathHelper.crossProduct(normal, MathHelper.minus(v1, v3));
		if(MathHelper.dot(test, MathHelper.minus(intersectPos, v1)) < 0.0f)
			return false;
		
		hit[0] = intersectPos[0];
		hit[1] = intersectPos[1];
		hit[2] = intersectPos[2];
		
		return true;
	}
}
