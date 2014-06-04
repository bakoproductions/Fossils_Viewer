package com.bakoproductions.fossilsviewer;

import static com.bakoproductions.fossilsviewer.util.Globals.BYTES_PER_FLOAT;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.Log;

import com.bakoproductions.fossilsviewer.objects.Model;
import com.bakoproductions.fossilsviewer.parsers.ModelParser;
import com.bakoproductions.fossilsviewer.parsers.OBJParser;

import edu.dhbw.andar.ARObject;
import edu.dhbw.andar.pub.SimpleBox;
import edu.dhbw.andar.util.GraphicsUtil;

public class CustomObject extends ARObject {
	private SimpleBox box;
	private Model model;
	private OBJParser objParser;
	private Context context;
	private float z = 30.0f;
	private float[] lightAmbient = {1.0f, 1.0f, 1.0f, 1.0f};
	private float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
	private float[] lightPosition = {10.0f, 10.0f, 10.0f, 1.0f};
	private FloatBuffer lightAmbientBuffer;
	private FloatBuffer lightDiffuseBuffer;
	private FloatBuffer lightPositionBuffer;
	
	private FloatBuffer mat_flash;
	private FloatBuffer mat_ambient;
	private FloatBuffer mat_flash_shiny;
	private FloatBuffer mat_diffuse;
	
	public CustomObject(Context context, String name, String patternName, double markerWidth, double[] markerCenter, Model model) {
		super(name, patternName, markerWidth, markerCenter);
		this.context = context;
		
		float   mat_ambientf[]     = {0f, 1.0f, 0f, 1.0f};
		float   mat_flashf[]       = {0f, 1.0f, 0f, 1.0f};
		float   mat_diffusef[]     = {0f, 1.0f, 0f, 1.0f};
		float   mat_flash_shinyf[] = {50.0f};

		mat_ambient = GraphicsUtil.makeFloatBuffer(mat_ambientf);
		mat_flash = GraphicsUtil.makeFloatBuffer(mat_flashf);
		mat_flash_shiny = GraphicsUtil.makeFloatBuffer(mat_flash_shinyf);
		mat_diffuse = GraphicsUtil.makeFloatBuffer(mat_diffusef);
		
		this.model = model;
	}
	
	@Override
	public void init(GL10 gl) {
		gl.glEnable(GL10.GL_TEXTURE_2D);
		model.bindTextures(gl);
	}
	
	@Override
	public final void draw(GL10 gl) {
		super.draw(gl);
					
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR,mat_flash);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, mat_flash_shiny);	
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, mat_diffuse);	
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, mat_ambient);
		
		gl.glTranslatef(0.0f, 0.0f, 0.0f);
		gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);	//X
		gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);	//Y
		gl.glScalef(10.0f, 10.0f, 10.0f);
		model.draw(gl);						
	}
}
