package com.bakoproductions.fossilsviewer.util;

import java.util.HashMap;
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
	
	// Triangulator for faces: v
	public static short triangulateV(String[] splitted, HashMap<String, Short> uniqueFaces, short nextIndex, 
			Vector<Short> vertexPointers, Vector<Short> faces){
		
		for(int i=2; i<splitted.length - 1; i++){
			nextIndex = Util.addPointerV(1, splitted, uniqueFaces, nextIndex, vertexPointers, faces);
			nextIndex = Util.addPointerV(i, splitted, uniqueFaces, nextIndex, vertexPointers, faces);
			nextIndex = Util.addPointerV(i+1, splitted, uniqueFaces, nextIndex, vertexPointers, faces);
		}
		
		return nextIndex;
	}
	
	// Triangulator for faces: v/vt
	public static short triangulateVT(String[] splitted, HashMap<String, Short> uniqueFaces, short nextIndex, 
			Vector<Short> vertexPointers, Vector<Short> texturePointers, Vector<Short> faces){
		
		for(int i=2; i<splitted.length - 1; i++){
			nextIndex = Util.addPointerVT(1, splitted, uniqueFaces, nextIndex, vertexPointers, texturePointers, faces);
			nextIndex = Util.addPointerVT(i, splitted, uniqueFaces, nextIndex, vertexPointers, texturePointers, faces);
			nextIndex = Util.addPointerVT(i+1, splitted, uniqueFaces, nextIndex, vertexPointers, texturePointers, faces);
		}
		
		return nextIndex;
	}
	
	// Triangulator for faces: v//vn
	public static short triangulateVN(String[] splitted, HashMap<String, Short> uniqueFaces, short nextIndex, 
			Vector<Short> vertexPointers, Vector<Short> normalPointers, Vector<Short> faces){
		
		for(int i=2; i<splitted.length - 1; i++){
			nextIndex = Util.addPointerVN(1, splitted, uniqueFaces, nextIndex, vertexPointers, normalPointers, faces);
			nextIndex = Util.addPointerVN(i, splitted, uniqueFaces, nextIndex, vertexPointers, normalPointers, faces);
			nextIndex = Util.addPointerVN(i+1, splitted, uniqueFaces, nextIndex, vertexPointers, normalPointers, faces);
		}
		
		return nextIndex;
	}
	
	// Triangulator for faces: v//vn
	public static short triangulateVTN(String[] splitted, HashMap<String, Short> uniqueFaces, short nextIndex, 
			Vector<Short> vertexPointers, Vector<Short> texturePointers, Vector<Short> normalPointers, Vector<Short> faces){
		
		for(int i=2; i<splitted.length - 1; i++){
			nextIndex = Util.addPointerVTN(1, splitted, uniqueFaces, nextIndex, vertexPointers, normalPointers, texturePointers, faces);
			nextIndex = Util.addPointerVTN(i, splitted, uniqueFaces, nextIndex, vertexPointers, normalPointers, texturePointers, faces);
			nextIndex = Util.addPointerVTN(i+1, splitted, uniqueFaces, nextIndex, vertexPointers, normalPointers, texturePointers, faces);
		}
		
		return nextIndex;
	}
}
