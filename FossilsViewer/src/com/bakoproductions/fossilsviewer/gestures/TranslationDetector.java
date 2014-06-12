package com.bakoproductions.fossilsviewer.gestures;

import android.util.Log;
import android.view.MotionEvent;

import com.bakoproductions.fossilsviewer.util.Globals;

public class TranslationDetector {    
    private float lastTouchX;
    private float lastTouchY;
    
    private float dx;
    private float dy;
    
    private int activePointerId;
    
    private OnTranslationListener listener;
    
    public TranslationDetector(OnTranslationListener listener){
    	this.listener = listener;
    	activePointerId = Globals.INVALID_POINTER_ID;    	
    }
    
    public float getDx() {
		return dx;
	}
    
    public float getDy() {
		return dy;
	}
    
    public float getLastTouchX() {
		return lastTouchX;
	}
    
    public float getLastTouchY() {
		return lastTouchY;
	}
    
    public boolean onTouchEvent(MotionEvent event){
    	final int action = event.getAction();
    	switch (action & MotionEvent.ACTION_MASK) {
    		case MotionEvent.ACTION_DOWN: {
    			final float x = event.getX();
		        final float y = event.getY();
		        
		        lastTouchX = x;
		        lastTouchY = y;
		     
		        activePointerId = event.getPointerId(0);
    			break;
    		} case MotionEvent.ACTION_MOVE: {
    			final int pointerIndex = event.findPointerIndex(activePointerId);
    			final float x = event.getX(pointerIndex);
		        final float y = event.getY(pointerIndex);		      
	            
	            if(listener != null)
	            	listener.onTranslation(this, x, y);
    			
	            lastTouchX = x;
		        lastTouchY = y;
	            break;
    		} case MotionEvent.ACTION_UP: {
		        activePointerId = Globals.INVALID_POINTER_ID;
		        break;
		    } case MotionEvent.ACTION_CANCEL: {
		        activePointerId = Globals.INVALID_POINTER_ID;
		        break;
		    } case MotionEvent.ACTION_POINTER_UP: {
		        final int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		        final int pointerId = event.getPointerId(pointerIndex);
		        if (pointerId == activePointerId) {
		            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
		            lastTouchX = event.getX(newPointerIndex);
		            lastTouchY = event.getY(newPointerIndex);
		            activePointerId = event.getPointerId(newPointerIndex);
		        }
		        break;
		    }
    	}
    	return true;
    }
    
    public static interface OnTranslationListener{
    	public void onTranslation(TranslationDetector translationDetector, float x, float y);
    }
}
