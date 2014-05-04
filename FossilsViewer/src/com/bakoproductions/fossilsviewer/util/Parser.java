package com.bakoproductions.fossilsviewer.util;

import com.bakoproductions.fossilsviewer.objects.Model;

public interface Parser {
	public static final int NO_ERROR = 0; 
	public static final int IO_ERROR = 1;
	public static final int RESOURCE_NOT_FOUND_ERROR = 2;
	
	public int parse(Model model);
}
