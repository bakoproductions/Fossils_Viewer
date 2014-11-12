package com.bakoproductions.fossilsviewer.viewer;

import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.bakoproductions.fossilsviewer.ar.ARActivity;
import com.bakoproductions.fossilsviewer.gestures.ClickDetector;
import com.bakoproductions.fossilsviewer.gestures.LongPressDetector;
import com.bakoproductions.fossilsviewer.gestures.RotationDetector;
import com.bakoproductions.fossilsviewer.gestures.TranslationDetector;
import com.bakoproductions.fossilsviewer.viewer.ViewerActivity.DialogHandler;


public class ViewerGLSurfaceView extends GLSurfaceView {
	private Context context;
	private ViewerRenderer renderer;
	private DialogHandler dialogHanlder;
	
	private ClickDetector clickDetector;
	private LongPressDetector longPressDetector;
	private ScaleGestureDetector scaleDetector;
	private TranslationDetector translationDetector;
    private RotationDetector rotationDetector;
	
	public ViewerGLSurfaceView(Context context) {
		super(context);
		this.context = context;
		
		clickDetector = new ClickDetector(new ClickListener());
		longPressDetector = new LongPressDetector(new LongPressListener());
		scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		translationDetector = new TranslationDetector(new TranslationListener());
		rotationDetector = new RotationDetector(new RotationListener());
		
	}
	
	public ViewerGLSurfaceView(Context context, AttributeSet attribs) {
	    super(context, attribs);
	    this.context = context;
	    
	    clickDetector = new ClickDetector(new ClickListener());
		longPressDetector = new LongPressDetector(new LongPressListener());
		scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		translationDetector = new TranslationDetector(new TranslationListener());
		rotationDetector = new RotationDetector(new RotationListener());
	}

	@Override
	public void onPause(){
	    super.onPause();
	}

	@Override
	public void onResume(){
	    super.onResume();
	}

	public void start(ViewerRenderer renderer) {
		this.renderer = renderer;
		setRenderer(renderer);
	}
	
	public void setHandler(DialogHandler dialogHandler) {
		this.dialogHanlder = dialogHandler;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		clickDetector.onTouchEvent(event);
		longPressDetector.onTouchEvent(event);
		rotationDetector.onTouchEvent(event);
		scaleDetector.onTouchEvent(event);
		translationDetector.onTouchEvent(event);
		return true;
	}
	
	private class ClickListener implements ClickDetector.OnClickListener {
		@Override
		public void onClick(ClickDetector clickDetector, int x, int y) {
			if(!longPressDetector.isInProgress()) { 
				Intent intent = new Intent(context, ARActivity.class);
				intent.putExtra("model", renderer.getModel());
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
		}
	}
	
	private class LongPressListener implements LongPressDetector.OnLongClickListener {
		@Override
		public void onLongClick(LongPressDetector longPressDetector, float x, float y) {
			Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(100);
			
			renderer.setUserClicked(true);
			renderer.setClickX(x);
			renderer.setClickY(y);
			
			dialogHanlder.sendEmptyMessage(0);
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
				
				posX += dx/100;
				posY -= dy/100;
				
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
}
