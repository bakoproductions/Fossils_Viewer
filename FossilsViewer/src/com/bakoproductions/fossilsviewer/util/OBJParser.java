package com.bakoproductions.fossilsviewer.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import android.content.res.Resources;

import com.bakoproductions.fossilsviewer.objects.Material;
import com.bakoproductions.fossilsviewer.objects.Model;
import com.bakoproductions.fossilsviewer.objects.ModelPart;

public class OBJParser implements Parser {
	private String objFile;
	
	public OBJParser(String objFile){
		this.objFile = objFile;
	}
	
	@Override
	public int parse(Model model) {
		Vector<Float> vertices = new Vector<Float>();
		Vector<Float> normals = new Vector<Float>();
		Vector<Float> textures = new Vector<Float>();
		Vector<Short> faces = new Vector<Short>();
		
		Vector<Short> texturePointers=new Vector<Short>();
		Vector<Short> normalPointers=new Vector<Short>();
		
		Vector<ModelPart> parts= new Vector<ModelPart>();
		Vector<Material> materials;
		
		
		try {
			FileInputStream inputStream = new FileInputStream(objFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			
			String line;
			while((line = br.readLine()) != null){
				if(line.startsWith("v")){
					String[] splitted = line.split("[ ]+");
 
					for(int i=1; i<splitted.length; i++){
						vertices.add(Float.valueOf(splitted[i]));
					}
				}else if(line.startsWith("vn")){
					String[] splitted = line.split("[ ]+"); 
		 
					for(int i=1; i<splitted.length; i++){ 
						normals.add(Float.valueOf(splitted[i]));
					}
				}else if(line.startsWith("vt")){
					String[] splitted = line.split("[ ]+"); 
					 
					for(int i=1; i<splitted.length; i++){
						textures.add(Float.valueOf(splitted[i]));
					}
				}else if(line.startsWith("f")){
					String[] splitted = line.split("[ ]+");
					if(splitted[1].matches("[0-9]+")){
						// f v v v ...
						if(splitted.length == 4){
							for(int i=1; i<splitted.length; i++){
								Short s = Short.valueOf(splitted[i]);
								s--;
								faces.add(s);
							}
						}else{
							Vector<Short> polygon = new Vector<Short>();
							for(int i=1; i<splitted.length; i++){
								Short s = Short.valueOf(splitted[i]);
								s--;
								polygon.add(s);
							}
							faces.addAll(Triangulator.triangulate(polygon));
						}
						
					}else if(splitted[1].matches("[0-9]+/[0-9]+")){
						// f v/vt ...
						if(splitted.length == 4){
							for(int i=1; i<splitted.length; i++){
								// The first value designates a face
								Short s = Short.valueOf(splitted[i].split("/")[0]);
								s--;
								faces.add(s);
								
								// The second value designates a texture
								s = Short.valueOf(splitted[i].split("/")[1]);
								s--;
								texturePointers.add(s);
							}
						}else{
							Vector<Short> tmpFaces = new Vector<Short>();
							Vector<Short> tmpTextures = new Vector<Short>();
							for(int i=1; i<splitted.length; i++){
								Short s = Short.valueOf(splitted[i].split("/")[0]);
								s--;
								tmpFaces.add(s);
								
								s = Short.valueOf(splitted[i].split("/")[1]);
								s--;
								tmpTextures.add(s);
							}
							faces.addAll(Triangulator.triangulate(tmpFaces));
							texturePointers.addAll(Triangulator.triangulate(tmpTextures));
						}
					}else if(splitted[1].matches("[0-9]+//[0-9]+")){
						//f v//vn ...
						if(splitted.length == 4){
							for(int i=1; i<splitted.length; i++){
								Short s = Short.valueOf(splitted[i].split("//")[0]);
								s--;
								faces.add(s);
								
								s=Short.valueOf(splitted[i].split("//")[1]);
								s--;
								normalPointers.add(s);
							}
						}else{
							Vector<Short> tmpFaces = new Vector<Short>();
							Vector<Short> tmpNormals = new Vector<Short>();
							for(int i=1; i<splitted.length; i++){
								Short s = Short.valueOf(splitted[i].split("//")[0]);
								s--;
								tmpFaces.add(s);
								
								s = Short.valueOf(splitted[i].split("//")[1]);
								s--;
								tmpNormals.add(s);
							}
							faces.addAll(Triangulator.triangulate(tmpFaces));
							normalPointers.addAll(Triangulator.triangulate(tmpNormals));
						}
					}else if(splitted[1].matches("[0-9]+/[0-9]+/[0-9]+")){
						//f v/vt/vn ...
						if(splitted.length == 4){
							for(int i=1; i<splitted.length; i++){
								Short s=Short.valueOf(splitted[i].split("/")[0]);
								s--;
								faces.add(s);
								
								s=Short.valueOf(splitted[i].split("/")[1]);
								s--;
								texturePointers.add(s);
								
								s=Short.valueOf(splitted[i].split("/")[2]);
								s--;
								normalPointers.add(s);
							}
						}else{
							Vector<Short> tmpFaces = new Vector<Short>();
							Vector<Short> tmpNormals = new Vector<Short>();
							Vector<Short> tmpTextures = new Vector<Short>();
							for(int i=1; i<splitted.length; i++){
								Short s = Short.valueOf(splitted[i].split("/")[0]);
								s--;
								tmpFaces.add(s);
								
								s = Short.valueOf(splitted[i].split("/")[1]);
								s--;
								tmpTextures.add(s);
								
								s=Short.valueOf(splitted[i].split("/")[2]);
								s--;
								tmpTextures.add(s);
							}
							faces.addAll(Triangulator.triangulate(tmpFaces));
							texturePointers.addAll(Triangulator.triangulate(tmpTextures));
							normalPointers.addAll(Triangulator.triangulate(tmpNormals));
						}
						continue;
					}
				} 
			}
		} catch (IOException e) {
			return IO_ERROR;
		} catch (Resources.NotFoundException nfe) {
			return RESOURCE_NOT_FOUND_ERROR;
		}
		
		if(faces.size() != 0){
			ModelPart modelPart = new ModelPart(faces, texturePointers, normalPointers, null);
			modelPart.buildNormalBuffer(normals);
			modelPart.buildFaceBuffer();
			parts.add(modelPart);
		}
		
		if(model == null)
			model = new Model(vertices, normals, textures, parts);
		else{
			model.setVertices(vertices);
			model.setNormals(normals);
			model.setTextures(textures);
			model.setParts(parts);
		}
		
		model.buildVertexBuffer();
		
		return NO_ERROR;
	}
}
