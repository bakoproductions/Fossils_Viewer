package com.bakoproductions.fossilsviewer.gestures;

import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Point;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class ClickDetector extends GestureDetector.SimpleOnGestureListener {
	private static final int MIN_DIST_MOVE_SQUARED = 100;
	
	private OnClickListener onClickListener;
	private OnLongClickListener onLongClickListener;
	
	private Timer clickTimer;
	private Timer longClickTimer;
	private Point previousPoint;
	
	private boolean clickConsumed;
	private boolean longClickConsumed;
	
	public ClickDetector(OnClickListener onClickListener, OnLongClickListener onLongClickListener) {
		this.onClickListener = onClickListener;
		this.onLongClickListener = onLongClickListener;
	}
	
	public void onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		
		switch (action & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN: {			
				final float x = event.getX();
				final float y = event.getY();
				
				clickTimer = new Timer();
				clickConsumed = false;
				clickTimer.schedule(new TimerTask() {
					@Override
					public void run() {
						clickConsumed = true;
					}
				}, ViewConfiguration.getTapTimeout());
				
				longClickTimer = new Timer();
				longClickConsumed = false;
				longClickTimer.schedule(new TimerTask() {
					@Override
					public void run() {
						onLongClickListener.onLongClick(ClickDetector.this, x, y);
						longClickConsumed = true;
					}
				}, ViewConfiguration.getLongPressTimeout());
				break;
			} case MotionEvent.ACTION_MOVE: {
				Point currentPoint = new Point((int)event.getX(), (int)event.getY());
				
				if(previousPoint == null)
					previousPoint = currentPoint;
				
				int dx = Math.abs(currentPoint.x - previousPoint.x);
				int dy = Math.abs(currentPoint.y - previousPoint.y);
				
				int dist = dx*dx + dy*dy;
				
				boolean isMoving = dist >= MIN_DIST_MOVE_SQUARED;
				
				if(isMoving) 
					longClickTimer.cancel();
				break;
			} case MotionEvent.ACTION_UP: {
				longClickTimer.cancel();
				if(!longClickConsumed) {
					if(!clickConsumed)	
						onClickListener.onClick(this, (int) event.getX(), (int) event.getY());
				}
				break;
			} default:
				longClickTimer.cancel();
		}
	}
	
	public static interface OnClickListener {
		public void onClick(ClickDetector clickDetector, int x, int y);
	}
	
	public static interface OnLongClickListener{
		public void onLongClick(ClickDetector clickDetector, float x, float y); 
	}
}
