package com.bakoproductions.fossilsviewer.annotations;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.bakoproductions.fossilsviewer.R;

public class Popup {
	private Context context;
	private DialogResult listener;
	private PopupWindow window;
	private Annotation annotation;
	
	private EditText annotationTitle;
	private EditText annotationText;
	
	private ImageButton closeButton;
	private ImageButton deleteButton;
	private ImageButton editButton;
	private ImageButton resizeButton;
	
	private boolean editingMode = false;
	private boolean maximizeMode = false;
	
	public Popup(Context context, Message message, DialogResult listener) {
		this.context = context;
		this.listener = listener;
		
		Activity parent = (Activity) context;
		
		LinearLayout parentLayout = (LinearLayout) parent.findViewById(R.id.popupHolder);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view =  inflater.inflate(R.layout.popup_layout, null);
		
		annotationTitle = (EditText) view.findViewById(R.id.annotationTitle);
		annotationText = (EditText) view.findViewById(R.id.annotationDescription);
		disableEditTexts();
		
		annotation = message.getData().getParcelable("annotation");
		annotationTitle.setText(annotation.getTitle());
		annotationText.setText(annotation.getText());
		
		closeButton = (ImageButton) view.findViewById(R.id.actionClose);
		closeButton.setOnClickListener(new OnCloseListener());
		
		deleteButton = (ImageButton) view.findViewById(R.id.actionDelete);
		deleteButton.setOnClickListener(new OnDeleteListener());
		
		editButton = (ImageButton) view.findViewById(R.id.actionEdit);
		editButton.setOnClickListener(new OnEditListener());
		
		resizeButton = (ImageButton) view.findViewById(R.id.actionResize);
		resizeButton.setOnClickListener(new OnResizeListener());
		
		window = new PopupWindow(view, 
	               LayoutParams.WRAP_CONTENT,  
	               LayoutParams.WRAP_CONTENT,
	               false);
		
		window.showAtLocation(parentLayout, Gravity.CENTER, 0, 0);
	}
	
	public void update(int x, int y) {
		window.update(x, y, -1, -1);
	}
	
	public void update(int x, int y, int width, int height) {
		window.update(x, y, width, height);
	}
	
	private void disableEditTexts() {
		annotationTitle.setEnabled(false);
		annotationTitle.setFocusable(false);
		
		annotationText.setEnabled(false);
		annotationTitle.setFocusable(false);
	}
	
	private void enableEditTexts() {
		annotationTitle.setEnabled(true);
		annotationTitle.setFocusable(true);
		annotationTitle.setFocusableInTouchMode(true);
		annotationTitle.requestFocus();
		
		annotationText.setEnabled(true);
		annotationText.setFocusable(true);
		annotationText.setFocusableInTouchMode(true);
	}	
	
	private class OnCloseListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if(editingMode) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle(R.string.close_annotation);
				builder.setMessage(R.string.discrad_changes);
				
				builder.setPositiveButton(R.string.discard, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						window.dismiss();
						listener.closeAnnotation();
					}
				});
				builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
				builder.create().show();
			} else {
				window.dismiss();
				listener.closeAnnotation();
			}
		}
	}
	
	private class OnDeleteListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(R.string.remove_annotation);
			builder.setMessage(R.string.are_you_sure);
			
			builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					listener.deleteAnnotation(annotation.getId());
					window.dismiss();
					listener.closeAnnotation();
				}
			});
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			builder.create().show();
		}
	}
	
	private class OnEditListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if(!editingMode) {
				editingMode = true;
				editButton.setImageResource(R.drawable.action_save_selector);
				
				window.setFocusable(true);
				window.update();
				enableEditTexts();
				
				annotationTitle.setClickable(true);
				annotationText.setClickable(true);
			} else {
				String title = annotationTitle.getText().toString();
				String description = annotationText.getText().toString();
				
				if(title == null || title.equals("") || title.matches("^\\s*$")) {
					Toast.makeText(context, R.string.toast_add_title, Toast.LENGTH_SHORT).show();
					return;
				}
				
				if(description == null || description.equals("") || description.matches("^\\s*$")) {
					Toast.makeText(context, R.string.toast_add_description, Toast.LENGTH_SHORT).show();
					return;
				}
				
				listener.editAnnotation(annotation.getId(), title, description);
				
				window.setFocusable(false);
				window.update();
				disableEditTexts();
				
				annotationTitle.setClickable(false);
				annotationText.setClickable(false);
				
				editingMode = false;
				editButton.setImageResource(R.drawable.action_edit_selector);
			}
		}
	}
	
	private class OnResizeListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub		
		}
	}
	
}
