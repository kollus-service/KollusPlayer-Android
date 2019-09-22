package com.kollus.se.kollusplayer.player.view;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AlertMessage {
	private static final String TAG = AlertMessage.class.getSimpleName();
	private AlertDialog.Builder mAlertDialog;
	private boolean mMessageShowing;
	private DialogInterface.OnClickListener mPositiveClickListener;
	private DialogInterface.OnClickListener mNegativeClickListener;
	
	public AlertMessage(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mAlertDialog = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
		} else {
			mAlertDialog = new AlertDialog.Builder(context);
		}
	}
	
	public AlertMessage setTitle(String title) {
		mAlertDialog.setTitle(title);
		return this;
	}
	
	public AlertMessage setMessage(String message) {
		mAlertDialog.setMessage(message);
		return this;
	}
	
	public AlertMessage setPositiveButton(int textId, DialogInterface.OnClickListener listener) {
		mPositiveClickListener = listener;
		mAlertDialog.setPositiveButton(textId, new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int whichButton) {
			                if (mPositiveClickListener != null) {
			                	mPositiveClickListener.onClick(dialog, whichButton);
			                	mMessageShowing = false;
			                	clear();
			                }
			            }
			        });
		return this;
	}
	
	public AlertMessage setNegativeButton(int textId, DialogInterface.OnClickListener listener) {
		mNegativeClickListener = listener;
		mAlertDialog.setNegativeButton(textId, new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int whichButton) {
			                if (mNegativeClickListener != null) {
			                	mNegativeClickListener.onClick(dialog, whichButton);
			                	mMessageShowing = false;
			                	clear();
			                }
			            }
			        });
		return this;
	}
	
	public AlertMessage setCancelable(boolean cancelable) {
		mAlertDialog.setCancelable(cancelable);
		return this;
	}
	
	public AlertMessage show() {
		mMessageShowing = true;
		mAlertDialog.show();
		return this;
	}
	
	public boolean isShowing() {
		return mMessageShowing;
	}
	
	private void clear() {
		mAlertDialog.setTitle(null);
		mAlertDialog.setMessage(null);
		mPositiveClickListener = null;
		mNegativeClickListener = null;
	}
}
