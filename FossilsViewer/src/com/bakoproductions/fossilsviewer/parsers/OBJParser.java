package com.bakoproductions.fossilsviewer.parsers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Vector;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

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
		try {
			HashMap<String, Short> uniqueFaces = new HashMap<String, Short>();
			Short nextIndex = 0;
			
			InputStream inputStream = context.getAssets().open(objFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			
			String line;
			while((line = br.readLine()) != null){
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
						vertices.add(Float.valueOf(splitted[i]));
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
		
		return NO_ERROR;
	}
}
