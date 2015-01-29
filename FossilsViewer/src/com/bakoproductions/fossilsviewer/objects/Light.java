package com.bakoproductions.fossilsviewer.objects;

import com.bakoproductions.fossilsviewer.R;
import com.bakoproductions.fossilsviewer.shaders.ShadersUtil;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class Light {
	private float[] MVMatrix = new float[16];
	private float[] MVPMatrix = new float[16];
	private final float[] lightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};
	private final float[] lightPosInWorldSpace = new float[4];
	private final float[] lightPosInEyeSpace = new float[4];
	
	private float[] lightModelMatrix = new float[16];
	private int program;
	private boolean lightVisible = false;
	
	public Light(Context context) {
		ShadersUtil lightShaders = new ShadersUtil(context, R.raw.light_vertex_shader, R.raw.light_fragment_shader);	    
	    lightShaders.loadVertexShader();
	    lightShaders.loadFragmentShader();
	    program = lightShaders.linkShaders();
	}
	
	public void position(float[] pos) {
		Matrix.setIdentityM(lightModelMatrix, 0);
        Matrix.translateM(lightModelMatrix, 0, pos[0], pos[1], pos[2]);      
	}
	
	public void draw(int lightPosUniform, float[] viewMatrix, float[] projectionMatrix) {
		Matrix.multiplyMV(lightPosInWorldSpace, 0, lightModelMatrix, 0, lightPosInModelSpace, 0);
        Matrix.multiplyMV(lightPosInEyeSpace, 0, viewMatrix, 0, lightPosInWorldSpace, 0);
        
        GLES20.glUniform3f(lightPosUniform, lightPosInEyeSpace[0], lightPosInEyeSpace[1], lightPosInEyeSpace[2]);
        
        if(lightVisible) {
        	final int pointMVPMatrixID = GLES20.glGetUniformLocation(program, "u_MVPMatrix");
            final int pointPositionID = GLES20.glGetAttribLocation(program, "a_Position");
            
    		GLES20.glVertexAttrib3f(pointPositionID, lightPosInModelSpace[0], lightPosInModelSpace[1], lightPosInModelSpace[2]);  
    		
    		Matrix.multiplyMM(MVMatrix, 0, viewMatrix, 0, lightModelMatrix, 0);
    		Matrix.multiplyMM(MVPMatrix, 0, projectionMatrix, 0, MVMatrix, 0);
    		GLES20.glUniformMatrix4fv(pointMVPMatrixID, 1, false, MVPMatrix, 0);
    		
    		GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
    		GLES20.glDisableVertexAttribArray(pointPositionID);
        }
	}
	
	public void setLightVisible(boolean flag) {
		lightVisible = flag;
	}
}
