package com.bakoproductions.fossilsviewer.geometry;

/**
 * @author Michael Bakogiannis
 *
 * This class holds three public variables
 * that represent a three-dimensional point
 * int the 3D space.
 * 
 * The variables are public because this class
 * works as a simple struct.
 */


public class Point3D {
	public float x;
	public float y;
	public float z;
	
	public Point3D(float x, float y, float z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
}
