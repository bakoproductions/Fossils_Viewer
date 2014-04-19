package com.bakoproductions.fossilsviewer.geometry;

/**
 * @author Michael Bakogiannis
 *
 * This class holds two public variables
 * that represent a thwo-dimensional point
 * 
 * This point used for saving the texture
 * coordinates that will be used to bind
 * a tetxure with the 3D model.
 */

public class Point2D {
	public float x;
	public float y;
	
	public Point2D(float x, float y){
		this.x = x;
		this.y = y;
	}
}
