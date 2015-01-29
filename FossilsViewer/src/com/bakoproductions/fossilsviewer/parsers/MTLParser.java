package com.bakoproductions.fossilsviewer.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import android.content.Context;

import com.bakoproductions.fossilsviewer.objects.Material;

public class MTLParser implements MaterialParser{
	private Context context;
	private String mtlFile;
	private String parentPath;
	
	public MTLParser(Context context, String parentPath, String mtlFile){
		this.context = context;
		this.mtlFile = mtlFile;
		this.parentPath = parentPath;
	}

	@Override
	public int parse(Vector<Material> materials) {
		try {
			InputStream inputStream = context.getAssets().open(mtlFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			
			String line;
			Material currentMtl = null;
			while((line = br.readLine()) != null){
				line = line.replaceFirst("^\\s+", "");

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
					currentMtl.setTextureFileName(parentPath + "/" + textureFile[1]);					
				}
			}
		} catch (IOException e) {
			return IO_ERROR;
		} 
		return NO_ERROR;
	}

}
