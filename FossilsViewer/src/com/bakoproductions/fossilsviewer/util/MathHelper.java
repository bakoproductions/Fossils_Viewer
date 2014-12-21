package com.bakoproductions.fossilsviewer.util;


public class MathHelper {
	public static final int X = 0;
    public static final int Y = 1;
    public static final int Z = 2;
    
	public static float dot(float[] u,float[] v) {
        return ((u[X] * v[X]) + (u[Y] * v[Y]) + (u[Z] * v[Z]));
    }
	
    public static float[] sub(float[] u, float[] v){
        return new float[]{u[X]-v[X],u[Y]-v[Y],u[Z]-v[Z]};
    }
    
    public static float[] add(float[] u, float[] v){
        return new float[]{u[X]+v[X],u[Y]+v[Y],u[Z]+v[Z]};
    }

    public static float[] scalarProduct(float r, float[] u){
        return new float[]{u[X]*r,u[Y]*r,u[Z]*r};
    }

    public static float[] crossProduct(float[] u, float[] v){
        return new float[]{(u[Y]*v[Z]) - (u[Z]*v[Y]),(u[Z]*v[X]) - (u[X]*v[Z]),(u[X]*v[Y]) - (u[Y]*v[X])};
    }

    public static float length(float[] u){
        return (float) Math.abs(Math.sqrt((u[X] * u[X]) + (u[Y] * u[Y]) + (u[Z] * u[Z])));
    }
    
    public static float distance(float[] u, float[] v) {
    	return (float) Math.sqrt(((u[X] - v[X]) * (u[X] - v[X])) + ((u[Y] - v[Y]) * (u[Y] - v[Y])) + ((u[Z] - v[Z]) * (u[Z] - v[Z])));
    }
   
   public static float[] pointOnLine(float[] p, float[] vector, float len) {
	   // point = p + vector*len, p: start point, vector: vector of the line, len: distance 
	   float[] length = scalarProduct(len, vector);
	   
	   return add(p, length);
   }
    
    public static float[] normalize(float[] u) {
    	float len = length(u);
    	
    	float[] ret = new float[3];
    	ret[X] = u[X] / len;
    	ret[Y] = u[Y] / len;
    	ret[Z] = u[Z] / len;
    	
    	return ret;
    }
    
    public static float[] getLineVector(float[] P1, float[] P2) {
    	float[] ret = new float[3];
    	
    	ret[X] = P2[X] - P1[X];
    	ret[Y] = P2[Y] - P1[Y];
    	ret[Z] = P2[Z] - P1[Z];
    	
    	return ret;
    }
    
    public static float[] findProjection(float[] P1, float[] P2, float[] center) {
    	float[] v = getLineVector(P1, P2);
    	v = normalize(v);
    	float[] u = getLineVector(P1, center);
    	
    	float puvLen = dot(v, u) / length(v);
    	float[] puv = scalarProduct(puvLen, v);
    	
    	return add(P1, puv);
    }
}
