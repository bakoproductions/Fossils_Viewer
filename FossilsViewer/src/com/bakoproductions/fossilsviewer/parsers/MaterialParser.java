package com.bakoproductions.fossilsviewer.parsers;

import java.util.Vector;

import com.bakoproductions.fossilsviewer.objects.Material;

public interface MaterialParser {
	public static final int NO_ERROR = 0; 
	public static final int IO_ERROR = 1;
	public static final int RESOURCE_NOT_FOUND_ERROR = 2;
	
	public int parse(Vector<Material> materials);
}
