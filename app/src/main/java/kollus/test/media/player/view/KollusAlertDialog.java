package kollus.test.media.player.view;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class KollusAlertDialog {
	private static final String TAG = KollusAlertDialog.class.getSimpleName();
	private AlertDialog.Builder mAlertDialogBuilder;
	private AlertDialog mAlertDialog;
	private Context mContext;
	private boolean mCustomView;
	
	private DialogInterface.OnClickListener mPositiveClickListener;
	private DialogInterface.OnClickListener mNegativeClickListener;
	private DialogInterface.OnCancelListener mCancelListener;
	private DialogInterface.OnKeyListener mKeyListener;
	
	public KollusAlertDialog(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mAlertDialogBuilder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
		} else {
			mAlertDialogBuilder = new AlertDialog.Builder(context);
		}

		mAlertDialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
								 KeyEvent event) {
				if (mKeyListener != null) {
					boolean bRet = mKeyListener.onKey(dialog, keyCode, event);
					return bRet;
				}
				else if( event.getAction() == KeyEvent.ACTION_DOWN ) {
					if( keyCode == KeyEvent.KEYCODE_BACK ) {
						return true;
					}
				}
				return false;
			}
		});

		mContext = context;
	}
	
	public KollusAlertDialog setTitle(String title) {
		mAlertDialogBuilder.setTitle(title);
		return this;
	}
	
	public KollusAlertDialog setTitle(int resId) {
		mAlertDialogBuilder.setTitle(resId);
		return this;
	}
	
	public KollusAlertDialog setMessage(String message) {
		mAlertDialogBuilder.setMessage(message);
		return this;
	}
	
	public KollusAlertDialog setMessage(int resId) {
		mAlertDialogBuilder.setMessage(resId);
		return this;
	}
	
	public KollusAlertDialog setView(View v) {
		mAlertDialogBuilder.setView(v);
		mCustomView = true;
		return this;
	}
	
	public KollusAlertDialog setPositiveButton(int textId, DialogInterface.OnClickListener listener) {
		mPositiveClickListener = listener;
		mAlertDialogBuilder.setPositiveButton(textId, new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int whichButton) {
			                if (mPositiveClickListener != null) {
			                	mPositiveClickListener.onClick(dialog, whichButton);
			                }
			            }
			        });
		return this;
	}
	
	public KollusAlertDialog setNegativeButton(int textId, DialogInterface.OnClickListener listener) {
		mNegativeClickListener = listener;
		mAlertDialogBuilder.setNegativeButton(textId, new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int whichButton) {
			                if (mNegativeClickListener != null) {
			                	mNegativeClickListener.onClick(dialog, whichButton);
			                }
			            }
			        });
		return this;
	}
	
	public KollusAlertDialog setCancelable(boolean cancelable) {
		mAlertDialogBuilder.setCancelable(cancelable);
		return this;
	}
	
	public KollusAlertDialog setOnKeyListener(DialogInterface.OnKeyListener listener) {
		mKeyListener = listener;
		return this;
	}
	
	public KollusAlertDialog setOnCancelListener(DialogInterface.OnCancelListener listener) {
		mCancelListener = listener;
		mAlertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
			                if (mCancelListener != null) {
			                	mCancelListener.onCancel(dialog);  	
			                }
			            }
			        });
		return this;
	}
	
	public KollusAlertDialog create() {
		mAlertDialogBuilder.create();
		return this;
	}
	
	public KollusAlertDialog show() {
		if(mAlertDialog == null) {
			mAlertDialog = mAlertDialogBuilder.show();
			mAlertDialog.setCanceledOnTouchOutside(false);
		}
		else {
			mAlertDialog.show();
		}
		
		if(mCustomView) {
			WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
			DisplayMetrics metrics = new DisplayMetrics();
	    	wm.getDefaultDisplay().getMetrics(metrics);

	    	WindowManager.LayoutParams params = mAlertDialog.getWindow().getAttributes();
	    	params.height = metrics.heightPixels*8/10;
	    	mAlertDialog.getWindow().setAttributes(params);
		}
		
		return this;
	}
	
	public void cancel() {
		if(mAlertDialog != null)
			mAlertDialog.cancel();
	}
	
	public KollusAlertDialog dismiss() {
		mAlertDialog.dismiss();
		return this;
	}
}
