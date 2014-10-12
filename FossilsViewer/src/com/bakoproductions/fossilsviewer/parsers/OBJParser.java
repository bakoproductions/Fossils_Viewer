package com.bakoproductions.fossilsviewer.parsers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream.GetField;
import java.util.HashMap;
import java.util.Vector;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.bakoproductions.fossilsviewer.objects.BoundingSphere;
import com.bakoproductions.fossilsviewer.objects.Material;
import com.bakoproductions.fossilsviewer.objects.Model;
import com.bakoproductions.fossilsviewer.objects.ModelPart;
import com.bakoproductions.fossilsviewer.util.Triangulator;
import com.bakoproductions.fossilsviewer.util.Util;

import static com.bakoproductions.fossilsviewer.util.Globals.THREE_DIM_ATTRS;;

public class OBJParser implements ModelParser {
	private Context context;
	private String objFile;
	private MTLParser mtlParser;
	
	public OBJParser(Context context, String objFile){
		this.context = context;
		this.objFile = objFile;
	}
	
	@Override
	public int parse(Model model) {
		Vector<Float> vertices = new Vector<Float>();
		Vector<Float> normals = new Vector<Float>();
		Vector<Float> textures = new Vector<Float>();
		
		Vector<Short> vertexPointers = new Vector<Short>();
		Vector<Short> texturePointers = new Vector<Short>();
		Vector<Short> normalPointers = new Vector<Short>();
		Vector<Short> faces = new Vector<Short>();
		
		Vector<ModelPart> parts = new Vector<ModelPart>();
		Vector<Material> materials = new Vector<Material>();
		
		Material currentMtl = null;
		float minX = -10;
		float maxX = 10;
		
		float minY = -10;
		float maxY = 10;
		
		float minZ = -10;
		float maxZ = 10;
		try {
			HashMap<String, Short> uniqueFaces = new HashMap<String, Short>();
			Short nextIndex = 0;
			
			InputStream inputStream = context.getAssets().open(objFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			
			String line;			
			while((line = br.readLine()) != null){
				line = line.replaceFirst("^\\s+", "");
				
				//Log.d("Bako", line);
				if(line.startsWith("vn")){
					//Log.d("Bako", "vn");
					String[] splitted = line.split("[ ]+"); 
		 
					for(int i=1; i<splitted.length; i++){ 
						normals.add(Float.valueOf(splitted[i]));
					}
				}else if(line.startsWith("vt")){
					//Log.d("Bako", "vt");
					String[] splitted = line.split("[ ]+"); 
					 
					for(int i=1; i<splitted.length; i++){
						textures.add(Float.valueOf(splitted[i]));
					}
				}else if(line.startsWith("v")){
					//Log.d("Bako", "v");
					String[] splitted = line.split("[ ]+");
 
					for(int i=1; i<splitted.length; i++){
						float vertex = Float.valueOf(splitted[i]);
						
						if(i == 1){
							maxX = getMax(vertex, maxX);
							minX = getMin(vertex, minX);
						}else if(i == 2){
							maxY = getMax(vertex, maxY);
							minY = getMin(vertex, minY);
						}else if(i == 3){
							maxZ = getMax(vertex, maxZ);
							minZ = getMin(vertex, minZ);
						}
						
						vertices.add(vertex);
					}
				}else if(line.startsWith("f")){
					String[] splitted = line.split("[ ]+");
					if(splitted[1].matches("[0-9]+")){
						// f v v v ...
						if(splitted.length == 4){
							for(int i=1; i<splitted.length; i++){
								nextIndex = Util.addPointerV(i, splitted, uniqueFaces, nextIndex, vertexPointers, faces);
							}
						}else{
							nextIndex = Triangulator.triangulateV(splitted, uniqueFaces, nextIndex, vertexPointers, faces);
						}			
					}else if(splitted[1].matches("[0-9]+/[0-9]+")){
						// f v/vt ...
						if(splitted.length == 4){
							for(int i = 1; i < splitted.length; i++){								
								nextIndex = Util.addPointerVT(i, splitted, uniqueFaces, nextIndex, vertexPointers, texturePointers, faces);
							}
						}else{ // triangulate 
							nextIndex = Triangulator.triangulateVT(splitted, uniqueFaces, nextIndex, vertexPointers, texturePointers, faces);
						}
					}else if(splitted[1].matches("[0-9]+//[0-9]+")){
						//f v//vn ...
						if(splitted.length == 4){
							for(int i=1; i<splitted.length; i++){
								nextIndex = Util.addPointerVN(i, splitted, uniqueFaces, nextIndex, vertexPointers, normalPointers, faces);
							}
						}else{
							nextIndex = Triangulator.triangulateVN(splitted, uniqueFaces, nextIndex, vertexPointers, normalPointers, faces);
						}
					}else if(splitted[1].matches("[0-9]+/[0-9]+/[0-9]+")){
						//f v/vt/vn ...
						if(splitted.length == 4){
							for(int i=1; i<splitted.length; i++){
								nextIndex = Util.addPointerVTN(i, splitted, uniqueFaces, nextIndex, vertexPointers, normalPointers, texturePointers, faces);
							}
						}else{
							nextIndex = Triangulator.triangulateVTN(splitted, uniqueFaces, nextIndex, vertexPointers, texturePointers, normalPointers, faces);
						}						
					}
				}else if(line.startsWith("mtllib")){
					// start parsing the mtl file
					String mtlFile = line.split("[ ]+",2)[1];
					mtlParser = new MTLParser(context, mtlFile);
					int result = mtlParser.parse(materials);
					
					if(result != MaterialParser.NO_ERROR)
						return result;
				}else if(line.startsWith("usemtl")){
					if(faces.size() != 0){
						ModelPart modelPart = new ModelPart(faces, texturePointers, normalPointers, currentMtl);
						modelPart.buildNormalBuffer(normals);
						modelPart.buildTextureBuffer(textures);
						modelPart.buildFaceBuffer();
						parts.add(modelPart);
					}
		
					String materialName = line.split("[ ]+",2)[1];
					for(int i=0; i<materials.size(); i++){
						currentMtl = materials.get(i);
						if(currentMtl.getName().equals(materialName)){
							break;
						}
						currentMtl = null;
					}
					
					faces = new Vector<Short>();
					texturePointers = new Vector<Short>();
					normalPointers = new Vector<Short>();
				}
			}
		} catch (IOException e) {
			return IO_ERROR;
		} catch (Resources.NotFoundException nfe) {
			return RESOURCE_NOT_FOUND_ERROR;
		}
		
		if(faces.size() != 0){
			ModelPart modelPart = new ModelPart(faces, texturePointers, normalPointers, currentMtl);
			modelPart.buildNormalBuffer(normals);
			modelPart.buildTextureBuffer(textures);
			modelPart.buildFaceBuffer();
			parts.add(modelPart);
		}
		
		model.setVertices(vertices);
		model.setNormals(normals);
		model.setTextures(textures);
		model.setParts(parts);
		model.buildVertexBuffer(vertexPointers);
		
		// Create an imaginary sphere around the model
		float[] center = getCenterPoint(minX, maxX, minY, maxY, minZ, maxZ);
		float diameter = computeDiameter(minX, maxX, minY, maxY, minZ, maxZ);
		
		BoundingSphere sphere = new BoundingSphere(center, diameter);
		model.applyBoundingSphere(sphere);
		
		return NO_ERROR;
	}
	
	private float getMax(float value1, float value2){
		if(value1 >= value2)
			return value1;
		
		return value2;
	}
	
	private float getMin(float value1, float value2){
		if(value1 <= value2)
			return value1;
		
		return value2;
	}
	
	private float[] getCenterPoint(float minX, float maxX, float minY, float maxY, float minZ, float maxZ){
		float[] center = new float[3];
		
		center[0] = Math.abs(maxX - minX) / 2.0f;
		center[1] = Math.abs(maxY - minY) / 2.0f;
		center[2] = Math.abs(maxZ - minZ) / 2.0f;
		
		return center;
	}
	
	private float computeDiameter(float minX, float maxX, float minY, float maxY, float minZ, float maxZ){
		float diamX = Math.abs(maxX - minX);
		float diamY = Math.abs(maxY - minY);
		float diamZ = Math.abs(maxZ - minZ);
		
		float max = getMax(diamX, diamY);
		max = getMax(max, diamZ);
		
		return max;
	}
}
