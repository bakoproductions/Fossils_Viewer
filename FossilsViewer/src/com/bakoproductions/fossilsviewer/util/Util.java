package com.bakoproductions.fossilsviewer.util;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Vector;

public class Util {
	public static short addPointerV(int i, String[] splitted, HashMap<String, Short> uniqueFaces, short nextIndex, 
			Vector<Short> vertexPointers, Vector<Short> faces){
		Short vertexIndex = Short.valueOf(splitted[i]);
		vertexIndex --;
		
		short index;
		if(uniqueFaces.containsKey(splitted[i])){
			index = uniqueFaces.get(splitted[i]);
		}else{
			index = nextIndex;
			uniqueFaces.put(splitted[i], index);
			nextIndex++;
			
			vertexPointers.add(Short.valueOf(vertexIndex));
		}
		faces.add(index);
		
		return nextIndex;
	}
	
	public static short addPointerVT(int i, String[] splitted, HashMap<String, Short> uniqueFaces, short nextIndex, 
			Vector<Short> vertexPointers, Vector<Short> texturePointers, Vector<Short> faces){
		Short vertexIndex = Short.valueOf(splitted[i].split("/")[0]);
		Short textureIndex = Short.valueOf(splitted[i].split("/")[1]);
		vertexIndex --;
		textureIndex --;
		
		short index;
		if(uniqueFaces.containsKey(splitted[i])){
			index = uniqueFaces.get(splitted[i]);
		}else{
			index = nextIndex;
			uniqueFaces.put(splitted[i], index);
			nextIndex++;
			
			vertexPointers.add(Short.valueOf(vertexIndex));
			texturePointers.add(Short.valueOf(textureIndex));
		}
		faces.add(index);
		
		return nextIndex;
	}
	
	public static short addPointerVN(int i, String[] splitted, HashMap<String, Short> uniqueFaces, short nextIndex, 
			Vector<Short> vertexPointers, Vector<Short> normalPointers, Vector<Short> faces){
		Short vertexIndex = Short.valueOf(splitted[i].split("//")[0]);
		Short normalIndex = Short.valueOf(splitted[i].split("//")[1]);
		vertexIndex --;
		normalIndex --;
		
		short index;
		if(uniqueFaces.containsKey(splitted[i])){
			index = uniqueFaces.get(splitted[i]);
		}else{
			index = nextIndex;
			uniqueFaces.put(splitted[i], index);
			nextIndex++;
			
			vertexPointers.add(Short.valueOf(vertexIndex));
			normalPointers.add(Short.valueOf(normalIndex));
		}
		faces.add(index);
		
		return nextIndex;
	}
	
	public static short addPointerVTN(int i, String[] splitted, HashMap<String, Short> uniqueFaces, short nextIndex, 
			Vector<Short> vertexPointers, Vector<Short> normalPointers, Vector<Short> texturePointers, Vector<Short> faces){
		Short vertexIndex = Short.valueOf(splitted[i].split("/")[0]);
		Short textureIndex = Short.valueOf(splitted[i].split("/")[1]);
		Short normalIndex = Short.valueOf(splitted[i].split("/")[2]);
		vertexIndex --;
		textureIndex --;
		normalIndex --;
		
		short index;
		if(uniqueFaces.containsKey(splitted[i])){
			index = uniqueFaces.get(splitted[i]);
		}else{
			index = nextIndex;
			uniqueFaces.put(splitted[i], index);
			nextIndex++;
			
			vertexPointers.add(Short.valueOf(vertexIndex));
			texturePointers.add(Short.valueOf(textureIndex));
			normalPointers.add(Short.valueOf(normalIndex));
		}
		faces.add(index);
		
		return nextIndex;
	}
	
	public static float[] toFloatArray(FloatBuffer buffer) {
		float[] ret = new float[buffer.capacity()];
		
		while(buffer.hasRemaining()) {
			ret[buffer.position()] = buffer.get(); 
		}
		buffer.position(0);
		return ret;
	}
	
	public static int[] toIntArray(ShortBuffer buffer) {
		int[] ret = new int[buffer.capacity()];
		
		while(buffer.hasRemaining()) {
			ret[buffer.position()] = buffer.get(); 
		}
		buffer.position(0);
		return ret;
	}
}
