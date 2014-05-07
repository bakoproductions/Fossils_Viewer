package com.bakoproductions.fossilsviewer.util;

import java.util.Vector;

public class Triangulator {
	
	// Triangulates every convex polygon
	public static Vector<Short> triangulate(Vector<Short> polygon){
		Vector<Short> triangles=new Vector<Short>();
		for(int i=1; i<polygon.size()-1; i++){
			triangles.add(polygon.get(0));		// The vertex to start creating triangles 
			triangles.add(polygon.get(i));
			triangles.add(polygon.get(i+1));
		}
		return triangles;
	}
	
}
