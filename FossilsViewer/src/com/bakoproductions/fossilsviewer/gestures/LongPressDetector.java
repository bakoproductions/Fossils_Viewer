package com.bakoproductions.fossilsviewer.gestures;

import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Point;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class LongPressDetector {
	private static final int MIN_DIST_MOVE_SQUARED = 100;
	
	private Timer timer;
	private Point previousPoint;
	private OnLongClickListener listener;
	private boolean inProgress;
	
	public LongPressDetector(OnLongClickListener listener) {
		this.listener = listener;
		
		inProgress = false;
	}
	
	public boolean isInProgress() {
		return inProgress;
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		
		switch (action & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				timer = new Timer();
				final float x = event.getX();
				final float y = event.getY();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						inProgress = true;
						listener.onLongClick(LongPressDetector.this, x, y);
					}
				}, ViewConfiguration.getLongPressTimeout());
				return true;
			case MotionEvent.ACTION_MOVE:
				Point currentPoint = new Point((int)event.getX(), (int)event.getY());
				
				if(previousPoint == null)
					previousPoint = currentPoint;
				
				int dx = Math.abs(currentPoint.x - previousPoint.x);
				int dy = Math.abs(currentPoint.y - previousPoint.y);
				
				int dist = dx*dx + dy*dy;
				
				boolean isMoving = dist >= MIN_DIST_MOVE_SQUARED;
				
				if(isMoving) {
					inProgress = false;
					timer.cancel();
					return false;
				} else {
					return true;
				}
			default:
				inProgress = false;
				timer.cancel();
				return false;
		}
	}
	
	public static interface OnLongClickListener{
		public void onLongClick(LongPressDetector longPressDetector, float x, float y); 
	}
}
