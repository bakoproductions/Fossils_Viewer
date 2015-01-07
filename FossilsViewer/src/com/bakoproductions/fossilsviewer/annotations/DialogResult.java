package com.bakoproductions.fossilsviewer.annotations;

public interface DialogResult {
	public void saveAnnotation(Annotation annotation);
	public void editAnnotation(int id, String title, String text);
	public void deleteAnnotation(int id);
	public void closeAnnotation();
}
