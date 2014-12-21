package com.bakoproductions.fossilsviewer.shaders;

import com.bakoproductions.fossilsviewer.util.TextResourceReader;

import android.content.Context;
import android.opengl.GLES20;

public class ShadersUtil {
	public static final String MVP_MATRIX_UNIFORM = "u_MVPMatrix";
	public static final String MV_MATRIX_UNIFORM = "u_MVMatrix";
	public static final String TEXTURE_UNIFORM = "u_Texture";
	public static final String LIGHT_POS_UNIFORM = "u_LightPos";

	public static final String POSITION_ATTRIBUTE = "a_Position";
	public static final String NORMAL_ATTRIBUTE = "a_Normal";
	public static final String COLOR_ATTRIBUTE = "a_Color";
	public static final String TEXTURE_COORDINATE_ATTRIBUTE = "a_TexCoordinate";
	public static final String LIGHT_COLOR_ATTRIBUTE = "a_LightColor";
	public static final String TEXTURE_ENABLED_ATTRIBUTE = "a_TextureEnabled";
	
	private String vertexShader;
	private String fragmentShader; 
	
	private int vertexShaderID;
	private int fragmentShaderID;
	
	public ShadersUtil(Context context, int vertexShaderResource, int fragmentShaderResource) {
		vertexShader = TextResourceReader.readTextFileFromResource(context, vertexShaderResource);
		fragmentShader = TextResourceReader.readTextFileFromResource(context, fragmentShaderResource);
	}
	
	public void loadVertexShader() {
		vertexShaderID = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
		
		if(vertexShaderID != 0) {
			GLES20.glShaderSource(vertexShaderID, vertexShader);
			GLES20.glCompileShader(vertexShaderID);
			
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(vertexShaderID, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
			
			if(compileStatus[0] == 0) {
				GLES20.glDeleteShader(vertexShaderID);
				vertexShaderID = 0;
			}
		} else {
			throw new RuntimeException("Error creating vertex shader");
		}
		
	}
	
	public void loadFragmentShader() {
		fragmentShaderID = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
		
		if(fragmentShaderID != 0) {
			GLES20.glShaderSource(fragmentShaderID, fragmentShader);
			GLES20.glCompileShader(fragmentShaderID);
			
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(fragmentShaderID, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
			
			if(compileStatus[0] == 0) {
				GLES20.glDeleteShader(fragmentShaderID);
				fragmentShaderID = 0;
			}
		} else {
			throw new RuntimeException("Error creating fragment shader");
		}
	}
	
	public int linkShaders() {
		int programID = GLES20.glCreateProgram();
		
		if(programID != 0) {
			GLES20.glAttachShader(programID, vertexShaderID);
			GLES20.glAttachShader(programID, fragmentShaderID);
			
			GLES20.glLinkProgram(programID);
			
			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(programID, GLES20.GL_LINK_STATUS, linkStatus, 0);
			if(linkStatus[0] == 0) {
				GLES20.glDeleteProgram(programID);
				programID = 0;
			}
			
			return programID;
		} else {
			throw new RuntimeException("Error creating program");
		}
	}
}
