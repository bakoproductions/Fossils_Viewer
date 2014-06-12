package com.bakoproductions.fossilsviewer.gestures;

import java.util.HashMap;

import com.bakoproductions.fossilsviewer.util.Globals;

import android.util.Log;
import android.view.MotionEvent;

public class RotationDetector {
	public static final float ROTATION_SCALE = 0.4f;
	private float xrot;
	private float yrot;
	
	private float lastTouchX;
    private float lastTouchY;
    
    private HashMap<Integer, float[]> activePointers;
	
	private OnRotationListener listener;
	private int activePointerId;
	
	public RotationDetector(OnRotationListener listener){
		this.listener = listener;
		activePointers = new HashMap<Integer, float[]>();
	}
	
	public void setRotationValues(float xrot, float yrot){
		this.xrot = xrot;
		this.yrot = yrot;
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
				float[] position = new float[2];
				position[0] = event.getX();
				position[1] = event.getY();
				
				lastTouchX = position[0];
				lastTouchY = position[1];
				
				activePointers.put(event.getPointerId(0), position);
				break;
			} case MotionEvent.ACTION_POINTER_DOWN: {
				int pointerIndex = event.getActionIndex();
				int pointerId = event.getPointerId(pointerIndex);
				
				float[] position = new float[2];
				position[0] = event.getX(pointerIndex);
				position[1] = event.getY(pointerIndex);
				
				lastTouchX = (lastTouchX + position[0]) / 2.0f;
				lastTouchY = (lastTouchY + position[1]) / 2.0f;
				
				activePointers.put(pointerId, position);
				Log.d("Bako", "p down " + position[0] + " " + position[1]);
				break;
			} case MotionEvent.ACTION_MOVE: {
				if(activePointers.size() != 3)
					return false;
				
				float sumX = 0;
				float sumY = 0;
				for(int size = event.getPointerCount(), i = 0; i < size; i++){
					float[] position = activePointers.get(event.getPointerId(i));
					if(position != null){
						position[0] = event.getX(i);
						position[1] = event.getY(i);
						
						sumX += position[0];
						sumY += position[1];
					}
				}
		        final float x = sumX / activePointers.size();
		        final float y = sumY / activePointers.size();
		        
		        if(listener != null)
		        	listener.onRotation(this, x, y);
		        
		        lastTouchX = x;
		        lastTouchY = y;
		        break;
			} case MotionEvent.ACTION_UP: {
				int pointerIndex = event.getActionIndex();
				int pointerId = event.getPointerId(pointerIndex);
				
				float[] pos = activePointers.get(pointerId);
				activePointers.remove(pointerId);
				break;
			} case MotionEvent.ACTION_POINTER_UP: {
				int pointerIndex = event.getActionIndex();
				int pointerId = event.getPointerId(pointerIndex);
				
				float[] pos = activePointers.get(pointerId);
				activePointers.remove(pointerId);
		        break;
			}
		}
    	Log.d("Bako", "Last " + lastTouchX + " " + lastTouchY);
		return true;
	}
	
	public static interface OnRotationListener{
		public void onRotation(RotationDetector rotationDetector, float x, float y); 
	}
}
