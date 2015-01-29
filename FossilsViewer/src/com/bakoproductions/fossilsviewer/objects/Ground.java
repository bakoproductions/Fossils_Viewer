package com.bakoproductions.fossilsviewer.objects;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import edu.dhbw.andar.ARObject;

public class Ground extends ARObject {
	private float radious;
	
	private FloatBuffer vertexBuffer;
	private FloatBuffer normalBuffer;
	
	public Ground(String name, String patternName, double markerWidth, double[] markerCenter, BoundingSphere sphere) {
		super(name, patternName, markerWidth, markerCenter);
		
		radious = sphere.getDiameter() / 2;
	}
	
	@Override
	public void init(GL10 arg0) {
		float[] coords = new float[] {
				// Front
				-radious,	-radious,	0.1f,
                radious,	-radious,	0.1f,
               -radious,  	radious,	0.1f,
                radious,	radious,	0.1f,
               // Back
               -radious,	-radious,	-0.1f,
               -radious,	radious,	-0.1f,
               radious,		-radious,	-0.1f,
               radious,		radious,	-0.1f,
               // Left
               -radious,	-radious,	0.1f,
               -radious,	radious,	0.1f,
               -radious,	-radious,	-0.1f,
               -radious,	radious,	-0.1f,
               // Right
               radious,		-radious,	-0.1f,
               radious,		radious,	-0.1f,
               radious,		-radious,	0.1f,
               radious,		radious,	0.1f,
               // Top
               -radious,	radious,	0.1f,
               radious,		radious,	0.1f,
               -radious,	radious,	-0.1f,
               radious,		radious,	-0.1f,
               // Bottom
               -radious,	-radious,	0.1f,
               -radious,	-radious,	-0.1f,
               radious,		-radious,	0.1f,
               radious,		-radious,	-0.1f,
		};
		
		ByteBuffer vb = ByteBuffer.allocateDirect(coords.length * 4);
		vb.order(ByteOrder.nativeOrder());
	    vertexBuffer = vb.asFloatBuffer();
	    vertexBuffer.put(coords);
	    vertexBuffer.position(0);
	    
	    float normals[] =  {
                // Front
                0.0f, 0.0f,  1.0f,
                0.0f, 0.0f,  1.0f,
                0.0f, 0.0f,  1.0f,
                0.0f, 0.0f,  1.0f,
                // Back
                0.0f, 0.0f,  -1.0f,
                0.0f, 0.0f,  -1.0f,
                0.0f, 0.0f,  -1.0f,
                0.0f, 0.0f,  -1.0f,
                // Left
                -1.0f, 0.0f,  0.0f,
                -1.0f, 0.0f,  0.0f,
                -1.0f, 0.0f,  0.0f,
                -1.0f, 0.0f,  0.0f,
                // Right
                1.0f, 0.0f,  0.0f,
                1.0f, 0.0f,  0.0f,
                1.0f, 0.0f,  0.0f,
                1.0f, 0.0f,  0.0f,
                // Top
                0.0f, 1.0f,  0.0f,
                0.0f, 1.0f,  0.0f,
                0.0f, 1.0f,  0.0f,
                0.0f, 1.0f,  0.0f,
                // Bottom
                0.0f, -1.0f,  0.0f,
                0.0f, -1.0f,  0.0f,
                0.0f, -1.0f,  0.0f,
                0.0f, -1.0f,  0.0f,
        };
	    
	    ByteBuffer nb = ByteBuffer.allocateDirect(normals.length * 4);
		nb.order(ByteOrder.nativeOrder());
	    normalBuffer = nb.asFloatBuffer();
	    normalBuffer.put(normals);
	    normalBuffer.position(0);
	}
	
	public void position(GL10 gl, float[] pos, float[] rot, float scale) {
		gl.glScalef(scale, scale, scale);
		gl.glRotatef(rot[0], 1, 0, 0);
		gl.glRotatef(rot[1], 0, 1, 0);
		gl.glTranslatef(-pos[0], -pos[1], -pos[2]);
	}
	
	@Override
	public void draw(GL10 gl) {
		super.draw(gl);
		
		gl.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glNormalPointer(GL10.GL_FLOAT, 0, normalBuffer);
		
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 4, 4);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 8, 4);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 12, 4);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 16, 4);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 20, 4);
        
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
	}
}
