package com.bakoproductions.fossilsviewer.objects;

public class BoundingSphere {
	private float[] center;
	private float diameter;
	
	public BoundingSphere(float[] center, float diameter){
		this.center = center;
		this.diameter = diameter;
	}
	
	public float[] getCenter() {
		return center;
	}
	
	public float getDiameter() {
		return diameter;
	}
}
