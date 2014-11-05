package com.bakoproductions.fossilsviewer.gestures;

import android.graphics.Point;
import android.view.MotionEvent;

public class ClickDetector {
	private static final int MIN_DIST_MOVE_SQUARED = 100;
	
	private OnClickListener listener;
	private Point previousPoint;
	
	public ClickDetector(OnClickListener listener) {
		this.listener = listener;
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			if(previousPoint == null)
				previousPoint = new Point();
			
			previousPoint.x = (int) event.getX();
			previousPoint.y = (int) event.getY();

			return true;
		case MotionEvent.ACTION_UP:
			int dx = (int) Math.abs(event.getX() - previousPoint.x);
			int dy = (int) Math.abs(event.getY() - previousPoint.y);
			
			int dist = dx*dx + dy*dy;
			
			boolean hasMoved = dist >= MIN_DIST_MOVE_SQUARED;
			
			if(hasMoved)
				return false;
			else {
				listener.onClick(this, (int) event.getX(), (int) event.getY());
				return true;
			}
		default:
			return false;
		}
	}
	
	public static interface OnClickListener {
		public void onClick(ClickDetector clickDetector, int x, int y);
	}
}
