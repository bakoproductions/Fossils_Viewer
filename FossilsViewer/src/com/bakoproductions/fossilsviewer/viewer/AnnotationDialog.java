package com.bakoproductions.fossilsviewer.viewer;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bakoproductions.fossilsviewer.R;


/*
 * Everything here runs under UI thread
 * Any calls to System services are acceptable
 */

public class AnnotationDialog {
	private Context context;
	private AlertDialog alert;
	
	private EditText annotationTitleEditText;
	private EditText annotationDescriptionEditText;
	
	public AnnotationDialog(Context context) {
		this.context = context;
		
		Builder dialog = new Builder(context);
		dialog.setTitle(R.string.add_annotation);
		
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		
		annotationTitleEditText = new EditText(context);
		annotationTitleEditText.setHint(R.string.annotation_title);
		annotationTitleEditText.setFocusableInTouchMode(true);		
		annotationTitleEditText.requestFocus();
		layout.addView(annotationTitleEditText);
		
		annotationDescriptionEditText = new EditText(context);
		annotationDescriptionEditText.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		annotationDescriptionEditText.setHint(R.string.annotation_description);
		annotationDescriptionEditText.setSingleLine(false);
		annotationDescriptionEditText.setMinLines(2);
		layout.addView(annotationDescriptionEditText);
		
		dialog.setView(layout);
		
		dialog.setPositiveButton(R.string.save, null);
		dialog.setNegativeButton(R.string.cancel, null);
		alert = dialog.create();
		
		alert.setOnShowListener(new ShowListener());
		alert.show();
	}
	
	private class SaveListener implements OnClickListener {
		
		@Override
		public void onClick(View v) {
			String title = annotationTitleEditText.getText().toString();
			String description = annotationDescriptionEditText.getText().toString();
			
			if(title == null || title.equals("") || title.matches("^\\s*$")) {
				Toast.makeText(context, R.string.toast_add_title, Toast.LENGTH_SHORT).show();
				return;
			}
			
			if(description == null || description.equals("") || description.matches("^\\s*$")) {
				Toast.makeText(context, R.string.toast_add_description, Toast.LENGTH_SHORT).show();
				return;
			}
			
			Log.i("Bako", title + " " + description);
			alert.dismiss();
		}
	}
	
	private class CancelListener implements OnClickListener {
		
		@Override
		public void onClick(View v) {
			alert.dismiss();
		}
	}
	
	private class ShowListener implements OnShowListener {
		@Override
		public void onShow(DialogInterface dialog) {
			Button positive = alert.getButton(AlertDialog.BUTTON_POSITIVE);
			positive.setOnClickListener(new SaveListener());
			
			Button negative = alert.getButton(AlertDialog.BUTTON_NEGATIVE);
			negative.setOnClickListener(new CancelListener());
		}
	}
}
