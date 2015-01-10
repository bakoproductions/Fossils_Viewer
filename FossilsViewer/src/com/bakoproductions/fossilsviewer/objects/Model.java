package com.bakoproductions.fossilsviewer.objects;

import static com.bakoproductions.fossilsviewer.util.Globals.BYTES_PER_FLOAT;
import static com.bakoproductions.fossilsviewer.util.Globals.THREE_DIM_ATTRS;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Parcel;
import android.os.Parcelable;

import com.bakoproductions.fossilsviewer.R;
import com.bakoproductions.fossilsviewer.shaders.ShadersUtil;
import com.bakoproductions.fossilsviewer.util.Globals;
import com.bakoproductions.fossilsviewer.util.MathHelper;
import com.bakoproductions.fossilsviewer.util.Util;

public class Model implements Parcelable{
	private Context context;
	
	/*
	 * GLSL variables
	 */
	private int mvpMatrixUniform;
	private int mvMatrixUniform;
	private int textureUniform;
	private int lightPosUniform;
	
	private int positionAttribute;
	private int normalAttribute;
	private int textCoordinateAttribute;
	private int colorAttribute;
	
	
	private int program;
	
	/*
	 * Drawing variables;
	 */
	private Vector<Float> vertices;
	private Vector<Float> colors;
	private Vector<ModelPart> parts;
	
	private FloatBuffer vertexBuffer;
	private FloatBuffer colorBuffer;
	
	private int[] textureData;
	
	private int[] vbo;
	private int[] cbo;
	private int[] tbo;
	private int[] nbo;
	private int[] ibo;
	
	private BoundingSphere sphere;
	
	public Model(Context context){
		this.context = context;
	}
	
	public Model(Context context, Vector<Float> verticies, Vector<ModelPart> parts){
		this.context = context;
		this.vertices = verticies;
	}
	
	public void prepareTextures(){
		Material[] materials = new Material[parts.size()];
		int activeTextures = 0;
		for(int i=0;i<parts.size();i++){
			materials[i] = parts.get(i).getMaterial();
			if(materials[i] != null)
				activeTextures++;
		}
		
		textureData = new int[activeTextures];
		GLES20.glGenTextures(activeTextures, textureData, 0);
		
		int textureId = 0;
		for(int i=0;i<materials.length;i++) {
			if(materials[i] == null)
				continue;
			
			final BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inScaled = false;
	        
	        final Bitmap bitmap = materials[i].getBitmap(context);
	        
	        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureData[textureId]);
	        
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
	        
	        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
	        
	        bitmap.recycle();
	        
	        textureId++;
		}
	}
	
	public void prepareTextures(GL10 gl){
		Material[] materials = new Material[parts.size()];
		int activeTextures = 0;
		for(int i=0;i<parts.size();i++){
			materials[i] = parts.get(i).getMaterial();
			if(materials[i] != null)
				activeTextures++;
		}
		
		textureData = new int[activeTextures];
		gl.glGenTextures(activeTextures, textureData, 0);
		int textureId = 0;
		for(int i=0;i<materials.length;i++){
			if(materials[i] == null)
				continue;

			Bitmap bitmap = materials[i].getBitmap(context);
			if(bitmap != null) {
				gl.glBindTexture(GL10.GL_TEXTURE_2D, textureData[textureId]);
				gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
				gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
				
				GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
				bitmap.recycle();
				
				textureId++;
			}
		}
	}
	
	public void bindBuffers() {
		ShadersUtil textureLightShaders = new ShadersUtil(context, R.raw.texture_light_vertex_shader, R.raw.texture_light_fragment_shader);	    
	    textureLightShaders.loadVertexShader();
	    textureLightShaders.loadFragmentShader();
	    program = textureLightShaders.linkShaders();
	    
		vbo = new int[1];
		GLES20.glGenBuffers(1, vbo, 0);
		
		cbo = new int[1];
		GLES20.glGenBuffers(1, cbo, 0);
		
		tbo = new int[parts.size()];
		GLES20.glGenBuffers(parts.size(), tbo, 0);
		
		nbo = new int[parts.size()];
		GLES20.glGenBuffers(parts.size(), nbo, 0);
		
		ibo = new int[parts.size()];
		GLES20.glGenBuffers(parts.size(), ibo, 0);
		
		if(vbo[0] > 0) {
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
			GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer.capacity() * BYTES_PER_FLOAT,	vertexBuffer, GLES20.GL_STATIC_DRAW);
			
			if(colorBuffer != null && cbo[0] > 0) {
				GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, cbo[0]);
				GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, colorBuffer.capacity() * BYTES_PER_FLOAT, colorBuffer, GLES20.GL_STATIC_DRAW);
			}
			
			for(int i=0;i<parts.size();i++) {
				ModelPart part = parts.get(i);
				
				part.bindBuffers(tbo[i], nbo[i], ibo[i]);
			}
		}
	}
	
	/**
     * @deprecated
     * This kind of rendering used to work in 
     * the old fixed pipeline. It uses the re-
     * ference to GL10
     */
	
	public void draw(GL10 gl){
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		
		for(int i=0; i<parts.size(); i++){
			ModelPart modelPart = parts.get(i);
			Material material = modelPart.getMaterial();
			
			int activeTextures = 0;
			if(material != null){
				FloatBuffer a = material.getAmbientColorBuffer();
				FloatBuffer d = material.getDiffuseColorBuffer();
				FloatBuffer s = material.getSpecularColorBuffer();
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, a);
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, s);
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, d);
				
				gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
				gl.glBindTexture(GL10.GL_TEXTURE_2D, textureData[activeTextures]);
				activeTextures++;
			}
			
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, modelPart.getTextureBuffer());
			gl.glNormalPointer(GL10.GL_FLOAT, 0, modelPart.getNormalBuffer());
			gl.glDrawElements(GL10.GL_TRIANGLES, modelPart.getFaceBuffer().capacity(), GL10.GL_UNSIGNED_SHORT, modelPart.getFaceBuffer());
			
			gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
			gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		}
	}
	
	public void position(float[] modelMatrix, float[] pos, float[] rot, float scale) {
        Matrix.scaleM(modelMatrix, 0, scale, scale, scale);
        Matrix.rotateM(modelMatrix, 0, rot[0], 1, 0, 0);
        Matrix.rotateM(modelMatrix, 0, rot[1], 0, 1, 0);
        Matrix.translateM(modelMatrix, 0, pos[0], pos[1], pos[2]);
	}
	
	public void alignToNormal(float[] modelMatrix, float[] position, float[] normal) {
		float l = (float) Math.sqrt((normal[1] * normal[1]) + (normal[2] * normal[2]));
		float length = MathHelper.length(normal);
		
		float[] invA = new float[16];
		invA[0] = l / length;
		invA[1] = 0;
		invA[2] = normal[0] / length;
		invA[3] = 0;
		
		invA[4] = - ((normal[0] * normal[1]) / (l * length));
		invA[5] = normal[2] / l;
		invA[6] = normal[1] / length;
		invA[7] = 0;
		
		invA[8] = - ((normal[0] * normal[2]) / (l * length));
		invA[9] = - (normal[1] / l);
		invA[10] = normal[2] / length;
		invA[11] = 0;
		
		invA[12] = 0;
		invA[13] = 0;
		invA[14] = 0;
		invA[15] = 1;
		
		Matrix.translateM(modelMatrix, 0, position[0], position[1], position[2]);
		
		float[] temp = new float[16];
		System.arraycopy(modelMatrix, 0, temp, 0, modelMatrix.length);
		Matrix.multiplyMM(modelMatrix, 0, temp, 0, invA, 0);
		
		Matrix.rotateM(modelMatrix, 0, 90.0f, 1, 0, 0);
	}
	
	public void alignToNormalManual(float[] modelMatrix, float[] position, float[] normal) {
		if(normal[0] == 0 && normal[1] == 0) {
			// without this check, division with n0 and n1 would return NaN
			// (0, 0, n2) means that the normal is perpendicular to the z axis
			
			Matrix.translateM(modelMatrix, 0, position[0], position[1], position[2]);
			if(normal[2] > 0) {
				// Rx(90)
				Matrix.rotateM(modelMatrix, 0, 90, 1, 0, 0);
			} else if(normal[2] < 0) {
				// Rx(-90)
				Matrix.rotateM(modelMatrix, 0, -90, 1, 0, 0);
			}
		} else {
			float l = (float) Math.sqrt((normal[0] * normal[0]) + (normal[1] * normal[1]));
			float length = MathHelper.length(normal);
			
			float[] A = new float[16];
			A[0] = normal[1] / l;
			A[1] = normal[0]/ length;
			A[2] = - ((normal[0] * normal[2]) / (l * length));
			A[3] = 0;
			
			A[4] = - (normal[0] / l);
			A[5] = normal[1] / length;
			A[6] = - ((normal[2] * normal[1]) / (l * length));
			A[7] = 0;
			
			A[8] = 0;
			A[9] = normal[2] / length;
			A[10] = l / length;
			A[11] = 0;
			
			A[12] = 0;
			A[13] = 0;
			A[14] = 0;
			A[15] = 1;
			
			float[] invA = new float[16];
			Matrix.invertM(invA, 0, A, 0);
			
			Matrix.translateM(modelMatrix, 0, position[0], position[1], position[2]);
			
			float[] temp = new float[16];
			System.arraycopy(modelMatrix, 0, temp, 0, modelMatrix.length);
			Matrix.multiplyMM(modelMatrix, 0, temp, 0, invA, 0);
		}
	}
	
	public void scale(float[] modelMatrix, float scale) {
		Matrix.scaleM(modelMatrix, 0, scale, scale, scale);
	}
	
	public void rotate(float[] modelMatrix, float[] rot) {
		Matrix.rotateM(modelMatrix, 0, rot[0], 1, 0, 0);
        Matrix.rotateM(modelMatrix, 0, rot[1], 0, 1, 0);
	}
	
	public void draw(float[] modelMatrix, float[] viewMatrix, float[] projectionMatrix, float[] MVMatrix, float[] MVPMatrix) {
		GLES20.glUseProgram(program);
		
	    mvpMatrixUniform = GLES20.glGetUniformLocation(program, ShadersUtil.MVP_MATRIX_UNIFORM);
	    mvMatrixUniform = GLES20.glGetUniformLocation(program, ShadersUtil.MV_MATRIX_UNIFORM);
        textureUniform = GLES20.glGetUniformLocation(program, ShadersUtil.TEXTURE_UNIFORM);
        lightPosUniform = GLES20.glGetUniformLocation(program, ShadersUtil.LIGHT_POS_UNIFORM);
        
        positionAttribute = GLES20.glGetAttribLocation(program, ShadersUtil.POSITION_ATTRIBUTE);
        textCoordinateAttribute = GLES20.glGetAttribLocation(program, ShadersUtil.TEXTURE_COORDINATE_ATTRIBUTE);
        colorAttribute = GLES20.glGetAttribLocation(program, ShadersUtil.COLOR_ATTRIBUTE);
        normalAttribute = GLES20.glGetAttribLocation(program, ShadersUtil.NORMAL_ATTRIBUTE);
		
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        if(textureData != null && textureData.length > 0) {
	        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureData[0]);
        }
        GLES20.glUniform1i(textureUniform, 0);
        
		Matrix.multiplyMM(MVMatrix, 0, viewMatrix, 0, modelMatrix, 0);
     	GLES20.glUniformMatrix4fv(mvMatrixUniform, 1, false, MVMatrix, 0);
 		Matrix.multiplyMM(MVPMatrix, 0, projectionMatrix, 0, MVMatrix, 0);
		GLES20.glUniformMatrix4fv(mvpMatrixUniform, 1, false, MVPMatrix, 0);
		
		if(vbo[0] > 0) {
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
			GLES20.glVertexAttribPointer(positionAttribute, Globals.THREE_DIM_ATTRS, GLES20.GL_FLOAT, false, 0, 0);
			GLES20.glEnableVertexAttribArray(positionAttribute);
			
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, cbo[0]);
			GLES20.glVertexAttribPointer(colorAttribute, Globals.THREE_DIM_ATTRS, GLES20.GL_FLOAT, false, 0, 0);
			GLES20.glEnableVertexAttribArray(colorAttribute);
			
			
			for(int i=0;i<parts.size();i++) {
				ModelPart part = parts.get(i);
				
				if(nbo[i] > 0 && tbo[i] > 0 && ibo[i] > 0) {
					GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, nbo[i]);
					GLES20.glVertexAttribPointer(normalAttribute, Globals.THREE_DIM_ATTRS, GLES20.GL_FLOAT, false, 0, 0);
					GLES20.glEnableVertexAttribArray(normalAttribute);
					GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
					
					GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, tbo[i]);
					GLES20.glVertexAttribPointer(textCoordinateAttribute, Globals.TWO_DIM_ATTRS, GLES20.GL_FLOAT, false, 0, 0);
					GLES20.glEnableVertexAttribArray(textCoordinateAttribute);
					GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
					
					GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[i]);
					GLES20.glDrawElements(GLES20.GL_TRIANGLES, part.getFaceBuffer().capacity(), GLES20.GL_UNSIGNED_SHORT, 0);
					GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
				}
			}
			
			GLES20.glDisableVertexAttribArray(normalAttribute);
			GLES20.glDisableVertexAttribArray(textCoordinateAttribute);
			GLES20.glDisableVertexAttribArray(colorAttribute);
			GLES20.glDisableVertexAttribArray(positionAttribute);
		}
	}
	
	public void drawVNT(int positionId, int normalId, int textureId) {
		if(vbo[0] > 0) {
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
			GLES20.glVertexAttribPointer(positionId, Globals.THREE_DIM_ATTRS, GLES20.GL_FLOAT, false, 0, 0);
			GLES20.glEnableVertexAttribArray(positionId);
			
			for(int i=0;i<parts.size();i++) {
				ModelPart part = parts.get(i);
				
				if(nbo[i] > 0 && tbo[i] > 0 && ibo[i] > 0) {
					GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, nbo[i]);
					GLES20.glVertexAttribPointer(normalId, Globals.THREE_DIM_ATTRS, GLES20.GL_FLOAT, false, 0, 0);
					GLES20.glEnableVertexAttribArray(normalId);
					GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
					
					GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, tbo[i]);
					GLES20.glVertexAttribPointer(textureId, Globals.TWO_DIM_ATTRS, GLES20.GL_FLOAT, false, 0, 0);
					GLES20.glEnableVertexAttribArray(textureId);
					GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
					
					GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[i]);
					GLES20.glDrawElements(GLES20.GL_TRIANGLES, part.getFaceBuffer().capacity(), GLES20.GL_UNSIGNED_SHORT, 0);
					GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
				}
			}
			
			GLES20.glDisableVertexAttribArray(normalId);
			GLES20.glDisableVertexAttribArray(textureId);
			GLES20.glDisableVertexAttribArray(positionId);
		}
	}
	
	public void drawVNC(int positionId, int normalId, int colorId) {
		if(vbo[0] > 0) {
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
			GLES20.glVertexAttribPointer(positionId, Globals.THREE_DIM_ATTRS, GLES20.GL_FLOAT, false, 0, 0);
			GLES20.glEnableVertexAttribArray(positionId);
			
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, cbo[0]);
			GLES20.glVertexAttribPointer(colorId, Globals.THREE_DIM_ATTRS, GLES20.GL_FLOAT, false, 0, 0);
			GLES20.glEnableVertexAttribArray(colorId);
			
			for(int i=0;i<parts.size();i++) {
				ModelPart part = parts.get(i);
				
				if(nbo[i] > 0 && tbo[i] > 0 && ibo[i] > 0) {
					GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, nbo[i]);
					GLES20.glVertexAttribPointer(normalId, Globals.THREE_DIM_ATTRS, GLES20.GL_FLOAT, false, 0, 0);
					GLES20.glEnableVertexAttribArray(normalId);
					GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
					
					GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[i]);
					GLES20.glDrawElements(GLES20.GL_TRIANGLES, part.getFaceBuffer().capacity(), GLES20.GL_UNSIGNED_SHORT, 0);
					GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
				}
			}
			
			GLES20.glDisableVertexAttribArray(normalId);
			GLES20.glDisableVertexAttribArray(colorId);
			GLES20.glDisableVertexAttribArray(positionId);
		}
	}
	
	public void buildVertexBuffer(Vector<Short> vertexPointers){
		ByteBuffer vBuf = ByteBuffer.allocateDirect(vertexPointers.size() * BYTES_PER_FLOAT * THREE_DIM_ATTRS);
		vBuf.order(ByteOrder.nativeOrder());
		vertexBuffer = vBuf.asFloatBuffer();
		for(int i=0;i<vertexPointers.size();i++){
			int index = vertexPointers.get(i);
			
			vertexBuffer.put(vertices.get(index * THREE_DIM_ATTRS));
			vertexBuffer.put(vertices.get(index * THREE_DIM_ATTRS + 1));
			vertexBuffer.put(vertices.get(index * THREE_DIM_ATTRS + 2));
		}
		vertexBuffer.position(0);
		
		// If we have colors alongside vertices
		// we use our vertex pointers sorting
		// to put them in a different buffer
		ByteBuffer cBuf = ByteBuffer.allocateDirect(vertexPointers.size() * BYTES_PER_FLOAT * THREE_DIM_ATTRS);
		cBuf.order(ByteOrder.nativeOrder());
		colorBuffer = cBuf.asFloatBuffer();
		if(colors.size() != 0) {
			for(int i=0;i<vertexPointers.size();i++){
				int index = vertexPointers.get(i);
				
				colorBuffer.put(colors.get(index * THREE_DIM_ATTRS));
				colorBuffer.put(colors.get(index * THREE_DIM_ATTRS + 1));
				colorBuffer.put(colors.get(index * THREE_DIM_ATTRS + 2));
			}
			colorBuffer.position(0);
		} else {
			for(int i=0;i<vertexPointers.size();i++){				
				colorBuffer.put(1.0f);
				colorBuffer.put(1.0f);
				colorBuffer.put(1.0f);
			}
			colorBuffer.position(0);
		}
	}
	
	public void setVertices(Vector<Float> vertices) {
		this.vertices = vertices;
	}
	
	public void setColors(Vector<Float> colors) {
		this.colors = colors;
	}
	
	public Vector<ModelPart> getParts() {
		return parts;
	}
	
	public void setParts(Vector<ModelPart> parts) {
		this.parts = parts;
	}
	
	public void applyBoundingSphere(BoundingSphere sphere){
		this.sphere = sphere;
	}
	
	public BoundingSphere getSphere() {
		return sphere;
	}
		
	public void setContext(Context context) {
		this.context = context;
	}
	
	public int getLightPosUniform() {
		return lightPosUniform;
	}
	
	public FloatBuffer getVertexBuffer() {
		vertexBuffer.position(0);
		return vertexBuffer;
	}
	
	public FloatBuffer getColorBuffer() {
		colorBuffer.position(0);
		return colorBuffer;
	}
	
	public static final Parcelable.Creator<Model> CREATOR = new Parcelable.Creator<Model>() {
        public Model createFromParcel(Parcel pc) {
            return new Model(pc);
        }
        public Model[] newArray(int size) {
            return new Model[size];
        }
	};
	
	public Model(Parcel parcel) {
		float[] vertexArray = parcel.createFloatArray();
		ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(vertexArray.length * BYTES_PER_FLOAT);
		vertexByteBuffer.order(ByteOrder.nativeOrder());
		vertexBuffer = vertexByteBuffer.asFloatBuffer();
		vertexBuffer.put(vertexArray);
		vertexBuffer.position(0);
		
		float[] colorArray = parcel.createFloatArray();
		ByteBuffer colorByteBuffer = ByteBuffer.allocateDirect(colorArray.length * BYTES_PER_FLOAT);
		colorByteBuffer.order(ByteOrder.nativeOrder());
		colorBuffer = colorByteBuffer.asFloatBuffer();
		colorBuffer.put(colorArray);
		colorBuffer.position(0);
		
		sphere = parcel.readParcelable(BoundingSphere.class.getClassLoader());
		
		ArrayList<ModelPart> partList = (ArrayList<ModelPart>) parcel.readArrayList(ModelPart.class.getClassLoader());
		parts = new Vector<ModelPart>();

		for(int i=0;i<partList.size();i++) {
			parts.add(partList.get(i));
		}
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		float[] vertexArray = Util.toFloatArray(vertexBuffer);
		float[] colorArray = Util.toFloatArray(colorBuffer);
		
		ArrayList<ModelPart> partList = new ArrayList<ModelPart>();
		for(int i=0;i<parts.size();i++) {
			partList.add(parts.get(i));
		}
		
		dest.writeFloatArray(vertexArray);
		dest.writeFloatArray(colorArray);
		dest.writeParcelable(sphere, flags);
		dest.writeList(partList);
	}
}
