package com.bakoproductions.fossilsviewer.annotations;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bakoproductions.fossilsviewer.R;

public class AddAnnotationDialog {
	private DialogResult listener;
	private AlertDialog dialog;
	
	private float[] intersection;
	private float[] normal;
	private EditText annotationTitleEditText;
	private EditText annotationDescriptionEditText;
	
	public AddAnnotationDialog(Context context, Message message, DialogResult listener) {
		this.listener = listener;
		
		Builder builder = new Builder(context);
		builder.setTitle(R.string.add_annotation);
		
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.add_annotation_layout, layout);			
		builder.setView(layout);
		
		annotationTitleEditText = (EditText) layout.findViewById(R.id.annotationTitle);
		annotationDescriptionEditText = (EditText) layout.findViewById(R.id.annotationText);
		
		builder.setPositiveButton(R.string.save, null);
		builder.setNegativeButton(R.string.cancel, null);
		dialog = builder.create();
		
		intersection = message.getData().getFloatArray("intersection");
		normal = message.getData().getFloatArray("normal");
		
		dialog.setOnShowListener(new ShowAddListener());
		dialog.show();
	}
	
	private class ShowAddListener implements OnShowListener {
		@Override
		public void onShow(DialogInterface dialogInterface) {
			Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
			positive.setOnClickListener(new SaveListener());
			
			Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
			negative.setOnClickListener(new CancelListener());
		}
	}
	
	private class SaveListener implements OnClickListener {
		
		@Override
		public void onClick(View view) {			
			String title = annotationTitleEditText.getText().toString();
			String description = annotationDescriptionEditText.getText().toString();
			
			if(title == null || title.equals("") || title.matches("^\\s*$")) {
				Toast.makeText(view.getContext(), R.string.toast_add_title, Toast.LENGTH_SHORT).show();
				return;
			}
			
			if(description == null || description.equals("") || description.matches("^\\s*$")) {
				Toast.makeText(view.getContext(), R.string.toast_add_description, Toast.LENGTH_SHORT).show();
				return;
			}
			
			Annotation annotation = new Annotation();
			annotation.setX(intersection[0]);
			annotation.setY(intersection[1]);
			annotation.setZ(intersection[2]);
			annotation.setNx(normal[0]);
			annotation.setNy(normal[1]);
			annotation.setNz(normal[2]);
			annotation.setTitle(title);
			annotation.setText(description);
			
			listener.saveAnnotation(annotation);
			dialog.dismiss();
		}
	}
	
	private class CancelListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			dialog.dismiss();
		}
	}
}
