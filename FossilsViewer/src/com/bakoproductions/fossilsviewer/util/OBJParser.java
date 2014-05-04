package com.bakoproductions.fossilsviewer.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;

import com.bakoproductions.fossilsviewer.geometry.Face;
import com.bakoproductions.fossilsviewer.geometry.Point2D;
import com.bakoproductions.fossilsviewer.geometry.Point3D;
import com.bakoproductions.fossilsviewer.objects.Model;

public class OBJParser implements Parser {
	private Context context;
	private int objResource;
	private int mtlResource;
	
	public OBJParser(Context context, int objResource, int mtlResource){
		this.context = context;
		this.objResource = objResource;
		this.mtlResource = mtlResource;
	}
	
	@Override
	public int parse(Model model) {
		InputStream inputStream = context.getResources().openRawResource(objResource);
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		
		ArrayList<Point3D> verticies = new ArrayList<Point3D>();
		ArrayList<Point3D> normals = new ArrayList<Point3D>();
		ArrayList<Point2D> textures = new ArrayList<Point2D>();
		ArrayList<Face> faces = new ArrayList<Face>();
		
		try {
			String line;
			while((line = br.readLine()) != null){
				if(line.startsWith("v ")){
					String[] vertexStr = line.split("\\s+", 3);
					
					Point3D point = new Point3D(
							Float.parseFloat(vertexStr[0]),
							Float.parseFloat(vertexStr[1]),
							Float.parseFloat(vertexStr[2]));
					
					verticies.add(point);
				}else if(line.startsWith("vn ")){
					String[] normalStr = line.split("\\s+", 3);
					
					Point3D point = new Point3D(
							Float.parseFloat(normalStr[0]),
							Float.parseFloat(normalStr[1]),
							Float.parseFloat(normalStr[2]));
					
					normals.add(point);
				}else if(line.startsWith("vt ")){
					String[] textureStr = line.split("\\s+", 3);
					
					Point2D point = new Point2D(
							Float.parseFloat(textureStr[0]),
							Float.parseFloat(textureStr[1]));
					
					textures.add(point);
				}else if(line.startsWith("f ")){
					String[] vertexesStr = line.split("\\s+", 3);
		
					Face face = new Face();
					
					for(int i=0; i<3; i++){
						String[] vertexStr = vertexesStr[i].split("\\s+", 3);
						
						int vertexPos = Integer.parseInt(vertexStr[0]);
						face.getVertices().add(verticies.get(vertexPos - 1));
						
						int texturePos = Integer.parseInt(vertexStr[1]);
						face.getTextures().add(textures.get(texturePos - 1));
						
						int normalPos = Integer.parseInt(vertexStr[2]);
						face.getNormals().add(normals.get(normalPos - 1));
					}
					
					faces.add(face);
				}
			}
		} catch (IOException e) {
			return IO_ERROR;
		} catch (Resources.NotFoundException nfe) {
			return RESOURCE_NOT_FOUND_ERROR;
		}
		return NO_ERROR;
	}
}
