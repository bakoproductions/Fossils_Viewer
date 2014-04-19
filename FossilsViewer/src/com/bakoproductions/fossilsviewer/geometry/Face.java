package com.bakoproductions.fossilsviewer.geometry;

import java.util.ArrayList;

/**
 * @author Michael Bakogiannis
 *
 * This class stores all the necessary
 * data that will be used in order to 
 * draw all the faces of a 3D model in
 * opengl.
 */

public class Face {
	private ArrayList<Point3D> vertices;
	private ArrayList<Point2D> textures;
	private ArrayList<Point3D> normals;
	
	public Face(){
		vertices = new ArrayList<Point3D>();
		textures = new ArrayList<Point2D>();
		normals = new ArrayList<Point3D>();
	}

	public ArrayList<Point3D> getVertices() {
		return vertices;
	}

	public void setVertices(ArrayList<Point3D> vertexes) {
		this.vertices = vertexes;
	}

	public ArrayList<Point2D> getTextures() {
		return textures;
	}

	public void setTextures(ArrayList<Point2D> textures) {
		this.textures = textures;
	}

	public ArrayList<Point3D> getNormals() {
		return normals;
	}

	public void setNormals(ArrayList<Point3D> normals) {
		this.normals = normals;
	}
}
