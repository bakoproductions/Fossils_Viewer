package com.bakoproductions.fossilsviewer.objects;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.bakoproductions.fossilsviewer.R;
import com.bakoproductions.fossilsviewer.shaders.ShadersUtil;

public class Line {
	private FloatBuffer vertexBuffer;
	private int program;
	private float[] color;
	private float[] lineCoords;
	
	public Line(Context context, float[] P1, float[] P2){
		color = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
	    lineCoords = new float[P1.length + P2.length];
	    for(int i=0;i<lineCoords.length/2;i++) {
	    	lineCoords[i] = P1[i];
	    }
	    for(int i=lineCoords.length/2;i<lineCoords.length;i++) {
	    	lineCoords[i] = P2[i - (lineCoords.length/2)];
	    }
	    
	    ByteBuffer bb = ByteBuffer.allocateDirect(lineCoords.length * 4);
	    bb.order(ByteOrder.nativeOrder());
	    vertexBuffer = bb.asFloatBuffer();
	    vertexBuffer.put(lineCoords);
	    vertexBuffer.position(0);

	    ShadersUtil lineShaders = new ShadersUtil(context, R.raw.line_vertex_shader, R.raw.line_fragment_shader);	    
	    lineShaders.loadVertexShader();
	    lineShaders.loadFragmentShader();
	    program = lineShaders.linkShaders();
	}
	
	public void position(float[] modelMatrix, float[] pos, float[] rot, float scale) {
		Matrix.setIdentityM(modelMatrix, 0);
        Matrix.scaleM(modelMatrix, 0, scale, scale, scale);
        Matrix.rotateM(modelMatrix, 0, rot[0], 1, 0, 0);
        Matrix.rotateM(modelMatrix, 0, rot[1], 0, 1, 0);
        Matrix.translateM(modelMatrix, 0, pos[0], pos[1], pos[2]);
	}
	
	public void draw(float[] modelMatrix, float[] viewMatrix, float[] projectionMatrix, float[] MVMatrix, float[] MVPMatrix) {
	    GLES20.glUseProgram(program);
	    
	    Matrix.multiplyMM(MVMatrix, 0, viewMatrix, 0, modelMatrix, 0);
 		Matrix.multiplyMM(MVPMatrix, 0, projectionMatrix, 0, MVMatrix, 0);
 		
	    int positionId = GLES20.glGetAttribLocation(program, "a_Position");
	    int colorId = GLES20.glGetUniformLocation(program, "u_Color");
	    int MVPMatrixId = GLES20.glGetUniformLocation(program, "u_MVPMatrix");
	    
	    GLES20.glEnableVertexAttribArray(positionId);
	    GLES20.glVertexAttribPointer(positionId, 3, GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer);   
	    GLES20.glUniform4fv(colorId, 1, color, 0);	    
	    GLES20.glUniformMatrix4fv(MVPMatrixId, 1, false, MVPMatrix, 0);
	    
	    GLES20.glDrawArrays(GLES20.GL_LINES, 0, lineCoords.length / 3);
	    
	    GLES20.glDisableVertexAttribArray(positionId);
	}
	
	public void setColor(float red, float green, float blue, float opacity) {
		color[0] = red;
		color[1] = green;
		color[2] = blue;
		color[3] = opacity;
	}
}
