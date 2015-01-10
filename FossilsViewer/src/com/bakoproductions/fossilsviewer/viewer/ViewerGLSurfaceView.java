package com.bakoproductions.fossilsviewer.viewer;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.Toast;

import com.bakoproductions.fossilsviewer.R;
import com.bakoproductions.fossilsviewer.annotations.AddAnnotationDialog;
import com.bakoproductions.fossilsviewer.annotations.DeleteAnnotationDialog;
import com.bakoproductions.fossilsviewer.annotations.Popup;
import com.bakoproductions.fossilsviewer.gestures.ClickDetector;
import com.bakoproductions.fossilsviewer.gestures.RotationDetector;
import com.bakoproductions.fossilsviewer.gestures.TranslationDetector;

public class ViewerGLSurfaceView extends GLSurfaceView {
	private Context context;
	private ViewerRenderer renderer;
	
	private ClickDetector clickDetector;
	private ScaleGestureDetector scaleDetector;
	private TranslationDetector translationDetector;
    private RotationDetector rotationDetector;
	
	public ViewerGLSurfaceView(Context context) {
		super(context);
		this.context = context;
		
		clickDetector = new ClickDetector(new ClickListener(), new LongPressListener());
		scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		translationDetector = new TranslationDetector(new TranslationListener());
		rotationDetector = new RotationDetector(new RotationListener());
		
	}
	
	public ViewerGLSurfaceView(Context context, AttributeSet attribs) {
	    super(context, attribs);
	    this.context = context;
	    
	    clickDetector = new ClickDetector(new ClickListener(), new LongPressListener());
		scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		translationDetector = new TranslationDetector(new TranslationListener());
		rotationDetector = new RotationDetector(new RotationListener());
	}

	public void start(ViewerRenderer renderer) {
		this.renderer = renderer;
		
		setEGLContextClientVersion(2);
		setRenderer(renderer);
	}
	
	public DialogHandler getDialogHandler() {
		return new DialogHandler();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {		
		clickDetector.onTouchEvent(event);
		
		scaleDetector.onTouchEvent(event);
		if(scaleDetector.isInProgress()) 
			return true;
	    
		boolean rotated = rotationDetector.onTouchEvent(event);
		boolean translated = translationDetector.onTouchEvent(event);
		
		if(rotated || translated)
			return true;
		
		return true;
	}
	
	private class ClickListener implements ClickDetector.OnClickListener {
		@Override
		public void onClick(ClickDetector clickDetector, int x, int y) {
			renderer.setUserRequestedPopup(true);
			renderer.setClickX(x);
			renderer.setClickY(y);
		}
	}
	
	private class LongPressListener implements ClickDetector.OnLongClickListener {
		@Override
		public void onLongClick(ClickDetector longPressDetector, float x, float y) {
			Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(100);
			
			renderer.setUserRequestedAnnotation(true);
			renderer.setClickX(x);
			renderer.setClickY(y);
		}
	}
	
	private class TranslationListener implements TranslationDetector.OnTranslationListener {
		@Override
		public void onTranslation(TranslationDetector translationDetector, float x, float y) {
			if(x < 0 || y < 0)
				return;
			
			if(renderer.isLockedTranslation()) {
				return;
			}
			
			if(!scaleDetector.isInProgress() && !rotationDetector.isInProgress()){
				final float dx = x - translationDetector.getLastTouchX();
				final float dy = y - translationDetector.getLastTouchY();
			
				float posX = renderer.getPosX();
				float posY = renderer.getPosY();
				
				posX += dx/10;
				posY -= dy/10;
				
				renderer.setPosX(posX);
				renderer.setPosY(posY);
				
				invalidate();
			}
		}
	}
	
	private class RotationListener implements RotationDetector.OnRotationListener{
		@Override
		public void onRotation(RotationDetector rotationDetector, float x, float y) {
			if(!scaleDetector.isInProgress()){
				final float dx = x - rotationDetector.getLastTouchX();
				final float dy = y - rotationDetector.getLastTouchY();
				
				float rotX = renderer.getRotX();
				float rotY = renderer.getRotY();
				
				rotX += dy * RotationDetector.ROTATION_SCALE;
				rotY += dx * RotationDetector.ROTATION_SCALE;
				
				if(rotX >= 360.0f || rotX <= -360.0f) {
					rotX = 0.0f;
				}
				
				if(rotY >= 360.0f || rotY <= -360.0f) {
					rotY = 0.0f;
				}
				
				renderer.setRotX(rotX);
				renderer.setRotY(rotY);
				
				invalidate();
			}
		}
	}
	
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
	    @Override
	    public boolean onScale(ScaleGestureDetector detector) {
	    	if(!rotationDetector.isInProgress()) {
	    		float scaleFactor = renderer.getScaleFactor();
	    		
		        scaleFactor *= detector.getScaleFactor();
		        
		        renderer.setScaleFactor(Math.max(0.1f, Math.min(scaleFactor, 5.0f)));
		        
		        invalidate();
		        return true;
	    	}
	    	return false;
	    }
	}
	
	public class DialogHandler extends Handler {
		public static final int ADD_ANNOTATION = 0;
		public static final int REMOVE_ANNOTATION = 1;
		public static final int OPEN_ANNOTATION = 2;
		public static final int MOVE_ANNOTATION = 3;
		public static final int CLOSE_ANNOTATION_FROM_RENDERER = 4;		// In case that the user clicked back button
		public static final int PAUSE_ANNOTATION = 5;
		public static final int NO_HIT = 6;
		
		private Popup popup;
		
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == ADD_ANNOTATION) {
				new AddAnnotationDialog(context, msg, renderer);
			} else if (msg.what == REMOVE_ANNOTATION) {
				new DeleteAnnotationDialog(context, msg, renderer);
			} else if (msg.what == OPEN_ANNOTATION) {
				popup = new Popup(context, msg, renderer);	
			} else if (msg.what == MOVE_ANNOTATION) {
				Bundle data = msg.getData();
				String title = data.getString("title");
				String description = data.getString("description");
				
				int[] location = msg.getData().getIntArray("location");
				popup.update(title, description, location[0], location[1]);
			} else if(msg.what == CLOSE_ANNOTATION_FROM_RENDERER) {
				popup.closePopup();
			} else if(msg.what == PAUSE_ANNOTATION) {
				popup.pausePopup();
			} else if (msg.what == NO_HIT) {
				Toast.makeText(context, R.string.toast_try_long_click_on_object, Toast.LENGTH_SHORT).show();
			}
		}
	}
}
