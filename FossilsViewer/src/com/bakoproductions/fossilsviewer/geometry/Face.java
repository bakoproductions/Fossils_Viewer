package com.bakoproductions.fossilsviewer.geometry;

/**
 * @author Michael Bakogiannis
 *
 * This class stores all the necessary
 * data that will be used in order to 
 * draw all the faces of a 3D model in
 * opengl.
 */

public class Face {
	private Point3D[] vertexes;
	private Point2D[] textures;
	private Point3D[] normals;
	
	public Face(){
		setVertexes(new Point3D[3]);
		setTextures(new Point2D[3]);
		setNormals(new Point3D[3]);
	}

	public Point3D[] getVertexes() {
		return vertexes;
	}

	public void setVertexes(Point3D[] vertexes) {
		this.vertexes = vertexes;
	}

	public Point2D[] getTextures() {
		return textures;
	}

	public void setTextures(Point2D[] textures) {
		this.textures = textures;
	}

	public Point3D[] getNormals() {
		return normals;
	}

	public void setNormals(Point3D[] normals) {
		this.normals = normals;
	}
}
