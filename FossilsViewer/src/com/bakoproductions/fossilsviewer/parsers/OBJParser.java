package com.bakoproductions.fossilsviewer.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Vector;

import android.content.Context;
import android.util.Log;

import com.bakoproductions.fossilsviewer.objects.BoundingSphere;
import com.bakoproductions.fossilsviewer.objects.Material;
import com.bakoproductions.fossilsviewer.objects.Model;
import com.bakoproductions.fossilsviewer.objects.ModelPart;
import com.bakoproductions.fossilsviewer.util.Globals;
import com.bakoproductions.fossilsviewer.util.Triangulator;
import com.bakoproductions.fossilsviewer.util.Util;

public class OBJParser implements ModelParser {
	private static final String TAG = OBJParser.class.getSimpleName();
	
	private Context context;
	private String parentPath;
	private String objFile;
	private MTLParser mtlParser;
	
	public OBJParser(Context context, String parentPath, String objFile){
		this.context = context;
		this.parentPath = parentPath;
		this.objFile = objFile;
	}
	
	@Override
	public int parse(Model model) {
		Vector<Float> vertices = new Vector<Float>();
		Vector<Float> normals = new Vector<Float>();
		Vector<Float> textures = new Vector<Float>();
		Vector<Float> colors = new Vector<Float>();
		
		Vector<Short> vertexPointers = new Vector<Short>();
		Vector<Short> texturePointers = new Vector<Short>();
		Vector<Short> normalPointers = new Vector<Short>();
		Vector<Short> faces = new Vector<Short>();
		
		Vector<ModelPart> parts = new Vector<ModelPart>();
		Vector<Material> materials = new Vector<Material>();
		
		Material currentMtl = null;
		float minX = 100000;
		float maxX = -100000;
		
		float minY = 100000;
		float maxY = -100000;
		
		float minZ = 100000;
		float maxZ = -100000;
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
					
					if(splitted.length < 4)
						return WRONG_OBJ_FORMAT;
					
					for(int i=1; i <= Globals.THREE_DIM_ATTRS; i++){
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
					
					// If the line has three more attributes
					// these attributes represent colors
					if(splitted.length == 7) {
						for(int i=4; i < 7; i++){
							float color = Float.valueOf(splitted[i]);
														
							colors.add(color);
						}
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
					
					mtlFile = parentPath + "/" + mtlFile;
					mtlParser = new MTLParser(context, parentPath, mtlFile);
					int result = mtlParser.parse(materials);
					
					if(result == MaterialParser.IO_ERROR)
						Log.d(TAG, "Could not open material file: " + mtlFile);
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
		}
		
		if(faces.size() != 0){
			ModelPart modelPart = new ModelPart(faces, texturePointers, normalPointers, currentMtl);
			modelPart.buildNormalBuffer(normals);
			modelPart.buildTextureBuffer(textures);
			modelPart.buildFaceBuffer();
			parts.add(modelPart);
		}
		
		model.setVertices(vertices);
		model.setColors(colors);		
		model.setParts(parts);
		model.buildVertexBuffer(vertexPointers);
		
		// Create an imaginary sphere around the model
		float[] center = getCenterPoint(minX, maxX, minY, maxY, minZ, maxZ);
		float diameter = computeDiameter(center, vertices);
		
		Log.i("Bako", "Centroid: " + center[0] + ", " + center[1] + ", " + center[2]);
		Log.i("Bako", "Diameter: " + diameter);
		
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
		
		BigDecimal minXB = new BigDecimal(minX).setScale(6, BigDecimal.ROUND_HALF_UP);
		BigDecimal maxXB = new BigDecimal(maxX).setScale(6, BigDecimal.ROUND_HALF_UP);
		
		BigDecimal minYB = new BigDecimal(minY).setScale(6, BigDecimal.ROUND_HALF_UP);
		BigDecimal maxYB = new BigDecimal(maxY).setScale(6, BigDecimal.ROUND_HALF_UP);
		
		BigDecimal minZB = new BigDecimal(minZ).setScale(6, BigDecimal.ROUND_HALF_UP);
		BigDecimal maxZB = new BigDecimal(maxZ).setScale(6, BigDecimal.ROUND_HALF_UP);
		
		BigDecimal divisor = new BigDecimal(2).setScale(6, BigDecimal.ROUND_HALF_UP);
						
		center[0] = maxXB.add(minXB).divide(divisor).setScale(6, BigDecimal.ROUND_HALF_UP).floatValue();
		center[1] = maxYB.add(minYB).divide(divisor).setScale(6, BigDecimal.ROUND_HALF_UP).floatValue();
		center[2] = maxZB.add(minZB).divide(divisor).setScale(6, BigDecimal.ROUND_HALF_UP).floatValue();
		
		return center;
	}
	
	private float computeDiameter(float[] center, Vector<Float> vertices){
		float max = -100000000;
		
		for(int i = 0; i < vertices.size() - 2; i += 3) {
			float[] point = new float[3];
			
			point[0] = vertices.get(i);
			point[1] = vertices.get(i + 1);
			point[2] = vertices.get(i + 2);
			
			float distance = euclideanDistance(center, point);
			
			if(distance > max)
				max = distance;
		}
		
		return 2 * max;
	}
	
	private float euclideanDistance(float[] center, float[] point) {
		BigDecimal center0 = new BigDecimal(center[0]).setScale(6, BigDecimal.ROUND_HALF_UP);
		BigDecimal center1 = new BigDecimal(center[1]).setScale(6, BigDecimal.ROUND_HALF_UP);
		BigDecimal center2 = new BigDecimal(center[2]).setScale(6, BigDecimal.ROUND_HALF_UP);
		
		BigDecimal point0 = new BigDecimal(point[0]).setScale(6, BigDecimal.ROUND_HALF_UP);
		BigDecimal point1 = new BigDecimal(point[1]).setScale(6, BigDecimal.ROUND_HALF_UP);
		BigDecimal point2 = new BigDecimal(point[2]).setScale(6, BigDecimal.ROUND_HALF_UP);
		
		
		
		double sum = center0.subtract(point0).pow(2).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
		sum += center1.subtract(point1).pow(2).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
		sum += center2.subtract(point2).pow(2).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
		
		return (float) Math.sqrt(sum);
	}
}
