package com.bakoproductions.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Vector;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.bakoproductions.fossilsviewer.objects.Material;
import com.bakoproductions.fossilsviewer.objects.Model;

public class MTLParser implements MaterialParser{
	private Context context;
	private String mtlFile;
	
	public MTLParser(Context context, String mtlFile){
		this.context = context;
		this.mtlFile = mtlFile;
	}

	@Override
	public int parse(Vector<Material> materials) {
		try {
			InputStream inputStream = context.getAssets().open(mtlFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			
			String line;
			Material currentMtl = null;
			while((line = br.readLine()) != null){
				if(line.startsWith("newmtl")){
					String materialName = line.split("[ ]+",2)[1];
					currentMtl = new Material(materialName);
					materials.add(currentMtl);					
				}else if(line.startsWith("Ka")){
					String[] amCol = line.split("[ ]+");
					currentMtl.setAmbientColor(
							Float.parseFloat(amCol[1]), 
							Float.parseFloat(amCol[2]), 
							Float.parseFloat(amCol[3]));
				}else if(line.startsWith("Kd")){
					String[] difCol = line.split("[ ]+");
					currentMtl.setDiffuseColor(
							Float.parseFloat(difCol[1]), 
							Float.parseFloat(difCol[2]), 
							Float.parseFloat(difCol[3]));
				}else if(line.startsWith("Ks")){
					String[] specCol = line.split("[ ]+");
					currentMtl.setSpecularColor(
							Float.parseFloat(specCol[1]), 
							Float.parseFloat(specCol[2]), 
							Float.parseFloat(specCol[3]));
				}else if(line.startsWith("Tr") || line.startsWith("d")){
					String[] alCol = line.split("[ ]+");
					currentMtl.setAlpha(Float.parseFloat(alCol[1]));
				}else if(line.startsWith("Ns")){
					String[] shine = line.split("[ ]+");
					currentMtl.setShine(Float.parseFloat(shine[1]));
				}else if(line.startsWith("illum")){
					String[] illumination = line.split("[ ]+");
					currentMtl.setIllumination(Integer.parseInt(illumination[1]));
				}else if(line.startsWith("map_Kd")){
					String[] textureFile = line.split("[ ]+");
					currentMtl.setTextureFileName(textureFile[1]);
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
