package com.bakoproductions.fossilsviewer.annotations;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.bakoproductions.fossilsviewer.R;

public class DeleteAnnotationDialog {
	private AlertDialog alert;
	
	public DeleteAnnotationDialog(Context context, Message message, DialogResult listener) {		
		Builder builder = new Builder(context);
		builder.setTitle(R.string.remove_annotation);
		builder.setMessage(R.string.are_you_sure);
		
		builder.setPositiveButton(R.string.delete, null);
		builder.setNegativeButton(R.string.cancel, null);
		alert = builder.create();
		
		alert.setOnShowListener(new ShowDeleteListener());
		alert.show();
	}
	
	private class ShowDeleteListener implements OnShowListener {
		@Override
		public void onShow(DialogInterface dialog) {
			Button positive = alert.getButton(AlertDialog.BUTTON_POSITIVE);
			positive.setOnClickListener(new DeleteListener());
			
			Button negative = alert.getButton(AlertDialog.BUTTON_NEGATIVE);
			negative.setOnClickListener(new CancelListener());
		}
	}
	
	private class DeleteListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			// TODO: add delete after user interaction
			// listener.deleteAnnotation(x, y, z);
			alert.dismiss();
		}
	}
	
	private class CancelListener implements OnClickListener {
		
		@Override
		public void onClick(View v) {
			alert.dismiss();
		}
	}
}
