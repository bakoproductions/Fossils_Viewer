package com.bakoproductions.fossilsviewer.ar;

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
	private Model model;
	private float z = 30.0f;
	
	public CustomObject(String name, String patternName, double markerWidth, double[] markerCenter, Model model) {
		super(name, patternName, markerWidth, markerCenter);
		
		this.model = model;
	}
	
	@Override
	public void init(GL10 gl) {
		gl.glEnable(GL10.GL_TEXTURE_2D);
		model.prepareTextures(gl);
	}
	
	@Override
	public final void draw(GL10 gl) {
		super.draw(gl);

		gl.glTranslatef(0.0f, 0.0f, 0.0f);
		gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);	//X
		gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);	//Y
		gl.glScalef(10.0f, 10.0f, 10.0f);
		model.draw(gl);						
	}
}
