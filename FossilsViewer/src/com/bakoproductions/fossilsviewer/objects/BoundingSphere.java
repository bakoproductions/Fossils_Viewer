package com.bakoproductions.fossilsviewer.objects;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class BoundingSphere {
	private float[] center;
	private float diameter;
	
	private float radius;
	private FloatBuffer sphereVertex;
	private int points;
	
	double dTheta = 25 * (Math.PI/180);
    double dPhi = dTheta;
	
	public BoundingSphere(float[] center, float diameter){
		this.center = center;
		this.diameter = diameter;
		
		radius = this.diameter / 2;
		
		ByteBuffer bb = ByteBuffer.allocateDirect(4 * 3 * (int)Math.ceil(((Math.PI*2)/dTheta)) * (int)Math.ceil((Math.PI*2) / dPhi));
		sphereVertex = bb.asFloatBuffer();
		sphereVertex.position(0);
        points = build();
	}
	
	public float[] getCenter() {
		return center;
	}
	
	public float getDiameter() {
		return diameter;
	}
	
	public void draw(GL10 gl) {
		gl.glFrontFace(GL10.GL_CW);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, sphereVertex);

        gl.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
        gl.glDrawArrays(GL10.GL_POINTS, 0, points);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	}
	
	private int build() {
        int points = 0;

        for(double phi = -(Math.PI); phi <= Math.PI; phi+=dPhi) {
            //for each stage calculating the slices
            for(double theta = 0.0; theta <= (Math.PI * 2); theta+=dTheta) {
                sphereVertex.put((float) (2000 * Math.sin(phi) * Math.cos(theta)) );
                sphereVertex.put((float) (2000 * Math.sin(phi) * Math.sin(theta)) );
                sphereVertex.put((float) (2000 * Math.cos(phi)) );
                points++;
            }
        }
        sphereVertex.position(0);
        return points;
	}
}
