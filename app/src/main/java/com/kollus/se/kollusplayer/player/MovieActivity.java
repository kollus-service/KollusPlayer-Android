/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kollus.se.kollusplayer.player;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings.SettingNotFoundException;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import com.kollus.sdk.media.content.KollusContent;
import com.kollus.sdk.media.util.ErrorCodes;
import com.kollus.sdk.media.util.KollusUri;
import com.kollus.sdk.media.util.Log;
import com.kollus.sdk.media.util.Utils;
import com.kollus.se.kollusplayer.KollusBaseActivity;
import com.kollus.se.kollusplayer.R;
import com.kollus.se.kollusplayer.player.preference.KollusConstants;
import com.kollus.se.kollusplayer.player.preference.ValuePreference;
import com.kollus.se.kollusplayer.player.util.DisplayUtil;
import com.kollus.se.kollusplayer.player.view.KollusAlertDialog;

import java.net.URLDecoder;

/**
 * This activity plays a video from a specified URI.
 *
 * The client of this activity can pass a logo bitmap in the intent (KEY_LOGO_BITMAP)
 * to set the action bar logo so the playback process looks more seamlessly integrated with
 * the original activity.
 */
public class MovieActivity extends KollusBaseActivity {
    @SuppressWarnings("unused")
    private static final String TAG = "MovieActivity";
    public static final String KEY_LOGO_BITMAP = "logo-bitmap";
    public static final String KEY_TREAT_UP_AS_BACK = "treat-up-as-back";
    private static final String SAVE_VOLOME_LEVEL = "save_volume_level";
	private static final String SAVE_BRIGHTNESS_LEVEL = "save_brightness_level";

	private int mScrollNoActionTop;
	private int mScrollNoActionBottom;
    private static final int SCROLL_MODE_N = 0;
    private static final int SCROLL_MODE_V = 1;
    private static final int SCROLL_MODE_H = 2;

    private static final int CONTROL_INC = 1;
    private static final int CONTROL_DEC = 2;
    private static final int CHECK_EXIT = 100;

    private static final int BRIGHTNESS_MIN = 15;
    private static final int BRIGHTNESS_MAX = 255;
    private static final int BRIGHTNESS_UNIT = 24;

    private static final int SCROLL_SEEK_MOUNT = 90;

	private static final int SCROLL_H_DELICACY = 45;
	private static final int SCROLL_V_DELICACY = 30;

//	private WindowManager mWindowManager;
//	private LayoutInflater mInflater;
//	private View mRootGroup;

    private MoviePlayer mPlayer;
    private Uri mUri;
    private Intent mIntent;
    private KollusAlertDialog mAlertDialog;

    private AudioManager mAudioManager;
	private int mSWVolumeLevel;
    private int mSystemVolumeLevel;
    private int mVolumeControlDistance;

    private int mScrollMode = SCROLL_MODE_N;
	private int mScrollAmountH = -1;
    private int mOriginSystemBrightnessMode;
    private int mOriginSystemBrightness;
    private int mSystemBrightness;
    private int mBrightnessControlDistance;

    private int mSeekControlDistance;

    private Toast mRepressForFinish;
    private boolean mExit = false;
	private final long EXIT_TIME = 1500;

	private boolean mTalkbackEnabled;
	private String mReturnUrl;

	//etlim 20170902 Activity Exit ==> Broadcast Event
    private String ACTION_ACTIVITY_FINISH_MOVIE = "kollus.test.media.action.activity.finish.movie";
	private MovieActivityFinishBroadcastReceiver mMovieActivityFinishBR;

	//etlim fixed. 20170808 navigation bar orientation issue, nexus P6.
	private final int HANDLER_ORIENTATION_LANDSCAPE = 1100;
	private final int HANDLER_ORIENTATION_REVERSED_LANDSCAPE = 1101;
	private SensorManager mSensorManager;
    private Sensor mRotationSensor;
	private int mOrientationType;
	private Resources mResources;

//	prevent screen mirroring
//	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			String action = intent.getAction();
//			if(action.equals(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)) {
//				ConnectivityManager connectManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//				NetworkInfo networkInfo = connectManager.getActiveNetworkInfo();
//
//				if(networkInfo != null) {
//					if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
//						for(NetworkInfo info : connectManager.getAllNetworkInfo()) {
//							if(info.getType() == ConnectivityManager.TYPE_MOBILE && info.isConnected())
//							{
//							}
//						}
//					}
//				}
//			}
//		}
//	};

	@Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.movie_view);

		//etlim 20170902 Activity Exit ==> Broadcast Event
        MovieActivityFinishBroadcastRegister();
		registerBluetoothBR();

		if(KollusConstants.SECURE_MODE && !Log.isDebug())
	        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			mScrollNoActionTop = getResources().getDimensionPixelSize(resourceId);
		}

		resourceId = getResources().getIdentifier("config_showNavigationBar", "bool", "android");
		if (resourceId > 0) {
			if(getResources().getBoolean(resourceId)) {
				resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
				if (resourceId > 0) {
					mScrollNoActionBottom = getResources().getDimensionPixelSize(resourceId);
				}
			}
		}

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		mIntent = getIntent();
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        mSystemVolumeLevel = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        try {
			mOriginSystemBrightnessMode = android.provider.Settings.System.getInt(getContentResolver(),
			        android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE);
			mOriginSystemBrightness = android.provider.Settings.System.getInt(getContentResolver(),
			        android.provider.Settings.System.SCREEN_BRIGHTNESS);
			mSystemBrightness = pref.getInt(SAVE_BRIGHTNESS_LEVEL, mOriginSystemBrightness);
		} catch (SettingNotFoundException e) {
		}

        mRepressForFinish = Toast.makeText(this, R.string.repress_backkey_for_play, Toast.LENGTH_SHORT);

        int volumeLevel = pref.getInt(SAVE_VOLOME_LEVEL, mSystemVolumeLevel);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeLevel, AudioManager.RINGER_MODE_SILENT);

		setSystemBrightness(mSystemBrightness);

        if (mIntent.hasExtra(MediaStore.EXTRA_SCREEN_ORIENTATION)) {
			int orientation = mIntent.getIntExtra(
                    MediaStore.EXTRA_SCREEN_ORIENTATION,
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            if (orientation != getRequestedOrientation()) {
                setRequestedOrientation(orientation);
            }
        }
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.buttonBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
        winParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        win.setAttributes(winParams);

        // We set the background in the theme to have the launching animation.
        // But for the performance (and battery), we remove the background here.
        win.setBackgroundDrawable(null);
        mGestureDetector = new GestureDetector(this, new SimpleGestureListener());
        mScaleGestureDetector = new ScaleGestureDetector(this, new SimpleScaleGestureListener());

        //etlim fixed. 20170808 navigation bar orientation issue, nexus P6.
		mOrientationType = DisplayUtil.getOrientation(this);
		mResources = getResources();
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

		handleIntent();
    }

    @Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
    	Log.d(TAG, "onNewIntent");
		super.onNewIntent(intent);
		setIntent(intent);
		mIntent = getIntent();
		handleIntent();
	}

//	@TargetApi(Build.VERSION_CODES.M)
	private void handleIntent() {
		if(KollusConstants.SECURE_MODE && Utils.isRooting()) {
			mAlertDialog = new KollusAlertDialog(MovieActivity.this)
					.setTitle(R.string.error_title)
					.setMessage(R.string.error_rooting)
					.setPositiveButton(R.string.confirm,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									finish();
								}
							})
					.show();
			return;
		}

//		DisplayMetrics displayMetrics = new DisplayMetrics();
//		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//		mWindowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
//		mInflater = LayoutInflater.from(this);
//		mRootGroup = mInflater.inflate(R.layout.movie_view, null);
//
//		WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams(displayMetrics.widthPixels / 2,
//				WindowManager.LayoutParams.WRAP_CONTENT,
//				WindowManager.LayoutParams.TYPE_PHONE,
//				WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
//				PixelFormat.TRANSLUCENT);
//		windowParams.gravity = Gravity.LEFT | Gravity.TOP;
//		mWindowManager.addView(mRootGroup, windowParams);

		final View rootView = findViewById(R.id.movie_view_root);
		Uri uri = mIntent.getData();
//		setSystemUiVisibility(rootView);
		if(mPlayer != null) {
			mPlayer.onDestroy();
			mPlayer = null;
		}

        if(uri != null) {
	        Log.d(TAG, "Uri >>> 1 "+uri);
			try {
				mReturnUrl = URLDecoder.decode(KollusUri.parse(uri.toString()).getQueryParameter("ret_url"), "UTF-8");
			} catch (Exception e) {
			}

			if("kollus".equalsIgnoreCase(uri.getScheme())) {
	        	String sUri = uri.toString();
	        	if(sUri.contains("url=")) {
//	        		uri = Uri.parse(Uri.decode(sUri.substring(sUri.indexOf("url=")+4)));
	        		uri = Uri.parse(sUri.substring(sUri.indexOf("url=")+4));
	        	}
	        	else {
//	        		uri = Uri.parse(Uri.decode(sUri.substring(9)));
	        		uri = Uri.parse(sUri.substring(9));
	        	}
	        }

	        Log.d(TAG, "Uri >>> 2 "+uri);
	        mPlayer = new MoviePlayer(rootView, this, uri, null, false) {
	            @Override
	            public void onCompletion() {
                    finish();
	            }
	        };
        }
        else {
//        	String downloadUrl = Uri.decode(mIntent.getStringExtra("download_play"));
			final String downloadUrl = mIntent.getStringExtra("download_play");
        	if(downloadUrl != null) {
				Log.d(TAG, "download_play : "+downloadUrl);
        		KollusContent content = kollusStorage.getDownloadKollusContent(downloadUrl);
//        		storage.releaseInstance();

        		if(content == null) {
        			String title = String.format("%s : %d", getResources().getString(R.string.ERROR_CODE),
        					ErrorCodes.ERROR_NOT_EXIST_DOWNLOADED_CONTENTS);
        			new KollusAlertDialog(MovieActivity.this).
        			setTitle(title).
        	        setMessage(ErrorCodes.getInstance(MovieActivity.this).getErrorString(ErrorCodes.ERROR_NOT_EXIST_DOWNLOADED_CONTENTS)).
        	        setPositiveButton(R.string.confirm,
        	                new DialogInterface.OnClickListener() {
        	                    public void onClick(DialogInterface dialog, int whichButton) {
									finish();
        	                    }
        	                }).
        	        show();
        		}
        		else {
    	        	mPlayer = new MoviePlayer(rootView, this, content.getMediaContentKey(), null, false) {
    		            @Override
    		            public void onCompletion() {
                            finish();
    		            }
    		        };
        		}
        	}
        	else {
				String mediaContentKey = mIntent.getStringExtra("media_content_key");
				Log.d(TAG, "Play in use mediaContentKey : "+mediaContentKey);
	        	mPlayer = new MoviePlayer(rootView, this, mediaContentKey, null, false) {
		            @Override
		            public void onCompletion() {
                        finish();
		            }
		        };
        	}
        }
    }

    private Intent createShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_STREAM, mUri);
        return intent;
    }

    @Override
    public void onStart() {
        ((AudioManager) getSystemService(AUDIO_SERVICE))
                .requestAudioFocus(null, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        super.onStart();
    }

    @Override
    protected void onStop() {
        ((AudioManager) getSystemService(AUDIO_SERVICE))
                .abandonAudioFocus(null);
        super.onStop();
    }

    @Override
    public void onPause() {
    	Log.d(TAG, "onPause");
    	if(mPlayer != null)
    		mPlayer.onPause();

    	if(mAlertDialog != null) {
    		mAlertDialog.dismiss();
    		mAlertDialog = null;
    	}

        android.provider.Settings.System.putInt(getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, mOriginSystemBrightnessMode);

        android.provider.Settings.System.putInt(getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS, mOriginSystemBrightness);

		mSensorManager.unregisterListener(mRotationSensorListener);

        super.onPause();
    }

    @Override
    public void onResume() {
    	Log.d(TAG, "onResume brightness "+mSystemBrightness);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
			mTalkbackEnabled = am.isEnabled() && am.isTouchExplorationEnabled();
		}

    	if(mPlayer != null) {
			if(KollusConstants.SECURE_MODE && Utils.isRooting()) {
				mPlayer.onPause();
				mAlertDialog = new KollusAlertDialog(MovieActivity.this)
		        .setTitle(R.string.error_title)
		        .setMessage(R.string.error_rooting)
		        .setPositiveButton(R.string.confirm,
		                new DialogInterface.OnClickListener() {
		                    public void onClick(DialogInterface dialog, int whichButton) {
		                    	finish();
		                    }
		                })
		        .show();
	        }
    		else
    			mPlayer.onResume();
    	}

        android.provider.Settings.System.putInt(getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
        android.provider.Settings.System.putInt(getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS, mSystemBrightness);

        mSensorManager.registerListener(mRotationSensorListener, mRotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
    	Log.d(TAG, "onDestroy");
		if(mPlayer != null)
			mPlayer.onDestroy();

		//etlim 20170902 Activity Exit ==> Broadcast Event
        unregisterReceiver(mMovieActivityFinishBR);
		unregisterReceiver(mBluetoothBR);

    	int volumeLevel = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
    	editor.putInt(SAVE_VOLOME_LEVEL, volumeLevel);
		editor.putInt(SAVE_BRIGHTNESS_LEVEL, mSystemBrightness);
    	editor.commit();

        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mSystemVolumeLevel, AudioManager.RINGER_MODE_SILENT);

//        Log.d(TAG, "Volume Save System "+mSystemVolumeLevel+", Preferences "+volumeLevel);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//		mWindowManager.removeView(mRootGroup);

        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d(TAG, String.format("onKeyDown action %d keyCode %d", event.getAction(), keyCode));
    	if( event.getAction() == KeyEvent.ACTION_DOWN ) {
			if( keyCode == KeyEvent.KEYCODE_BACK ){
				if(!mExit) {
					mRepressForFinish.show();
					mExit = true;
					mHandler.sendEmptyMessageDelayed(CHECK_EXIT, EXIT_TIME);
				}
				else {
					finish();
				}

				return true;
			}
			else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
				setVolumeControl(CONTROL_INC);
				return true;
        	}
			else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
				setVolumeControl(CONTROL_DEC);
				return true;
        	}
		}

    	if(mPlayer == null)
    		return super.onKeyDown(keyCode, event);
    	else {
			switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_UP: {
					setVolumeControl(CONTROL_INC);
					return true;
				}
				case KeyEvent.KEYCODE_DPAD_DOWN: {
					setVolumeControl(CONTROL_DEC);
					return true;
				}
			}
			return mPlayer.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
		}
    }

	private SensorEventListener mRotationSensorListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			if (mResources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			} else {
				if (mOrientationType != DisplayUtil.getOrientation(MovieActivity.this)) {
					mOrientationType = DisplayUtil.getOrientation(MovieActivity.this);
					if (mOrientationType == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
						mHandler.sendEmptyMessage(HANDLER_ORIENTATION_LANDSCAPE);
					} else if (mOrientationType == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
						mHandler.sendEmptyMessage(HANDLER_ORIENTATION_REVERSED_LANDSCAPE);
					} else {
						mHandler.sendEmptyMessage(HANDLER_ORIENTATION_LANDSCAPE);
					}
				}
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

    private void setVolumeControl(int direction) {
		if(mPlayer != null) {
			int curVolumeLevel = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			int maxVolumeLevel = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			if(direction == CONTROL_INC) {
				if(curVolumeLevel == maxVolumeLevel) {
					mSWVolumeLevel++;
					if(mSWVolumeLevel > KollusConstants.MAX_SW_VOLUME)
						mSWVolumeLevel = KollusConstants.MAX_SW_VOLUME;
				}
				else {
					mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
							AudioManager.ADJUST_RAISE, AudioManager.RINGER_MODE_SILENT);
				}

				//이어폰을 꽂았을 경우 max volume까지 못올리는 경우에 대한 방어코드
				int adjustVolumeLevel = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
				if(adjustVolumeLevel == curVolumeLevel && curVolumeLevel != maxVolumeLevel) {
					maxVolumeLevel = adjustVolumeLevel;
					mSWVolumeLevel++;
					if(mSWVolumeLevel > KollusConstants.MAX_SW_VOLUME)
						mSWVolumeLevel = KollusConstants.MAX_SW_VOLUME;
				}
			}
			else if(direction == CONTROL_DEC) {
				if(mSWVolumeLevel > 0) {
					mSWVolumeLevel--;
				}
				else {
					mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
							AudioManager.ADJUST_LOWER, AudioManager.RINGER_MODE_SILENT);
				}
			}

			mPlayer.setVolumeLabel(mSWVolumeLevel+curVolumeLevel, maxVolumeLevel);
		}
    }

    private void setBrightnessControl(int direction) {
    	int brightness = mSystemBrightness;

    	if(direction == CONTROL_INC) {
    		brightness += BRIGHTNESS_UNIT;
    	}
    	else if(direction == CONTROL_DEC) {
    		brightness -= BRIGHTNESS_UNIT;
    	}

    	if(brightness > BRIGHTNESS_MAX)
    		brightness = BRIGHTNESS_MAX;
		else if(brightness < BRIGHTNESS_MIN)
			brightness = BRIGHTNESS_MIN;

		int nBrightnessLevel = brightness/BRIGHTNESS_UNIT;
		mSystemBrightness = nBrightnessLevel*BRIGHTNESS_UNIT+BRIGHTNESS_MIN;
		setSystemBrightness(mSystemBrightness);

		mPlayer.setBrightnessLabel(nBrightnessLevel);
    }

	private void setSystemBrightness(int brightness) {
		android.provider.Settings.System.putInt(getContentResolver(),
				android.provider.Settings.System.SCREEN_BRIGHTNESS, brightness);

		Window win = getWindow();
		WindowManager.LayoutParams lp = win.getAttributes();
		lp.screenBrightness = (float)brightness/(float)BRIGHTNESS_MAX;
		win.setAttributes(lp);
	}

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	if(mPlayer == null)
    		return super.onKeyUp(keyCode, event);
    	else
			return mPlayer.onKeyUp(keyCode, event) || super.onKeyUp(keyCode, event);
    }

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);

        if(mPlayer != null) {
			mPlayer.setOrientation(newConfig.orientation);
		}
	}

    @Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if(mTalkbackEnabled)
			return super.onTouchEvent(event);

    	if(event.getPointerCount() > 1) {
    		return mScaleGestureDetector.onTouchEvent(event);
    	}
    	else {
    		if(event.getAction() == MotionEvent.ACTION_UP) {
    			if(mScrollMode == SCROLL_MODE_H) {
    				DisplayMetrics displayMetrics = new DisplayMetrics();
					getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
					mPlayer.setSeekLabel(displayMetrics.widthPixels, displayMetrics.heightPixels,
							(int)event.getX(), mScrollAmountH,
							mSeekControlDistance*1000, false);
					mSeekControlDistance = 0;
    			}
    			mScrollMode = SCROLL_MODE_N;
				mScrollAmountH = -1;
			}
    		return mGestureDetector.onTouchEvent(event);
    	}
	}

	private ScaleGestureDetector mScaleGestureDetector;
    private class SimpleScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onScale : "+detector);
			if(mPlayer != null && mPlayer.getPlayerType() != Utils.PLAYER_TYPE_NATIVE) {
				mPlayer.screenSizeScale(detector);
			}
			return super.onScale(detector);
		}

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			// TODO Auto-generated method stub
			if(mPlayer != null && mPlayer.getPlayerType() != Utils.PLAYER_TYPE_NATIVE) {
				mPlayer.screenSizeScaleBegin(detector);
			}
			return super.onScaleBegin(detector);
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			// TODO Auto-generated method stub
			if(mPlayer != null && mPlayer.getPlayerType() != Utils.PLAYER_TYPE_NATIVE) {
				mPlayer.screenSizeScaleEnd(detector);
			}
			super.onScaleEnd(detector);
		}

    }

	private GestureDetector mGestureDetector;
    private class SimpleGestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			// TODO Auto-generated method stub
			Log.e(TAG, "onSingleTapConfirmed");
			if(mPlayer != null)
	    		mPlayer.toggleMediaControlsVisibility();

			return super.onSingleTapConfirmed(e);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			// TODO Auto-generated method stub
			if(mPlayer != null) {
				if(mPlayer.isVRMode()) {
					mPlayer.moveVR(-distanceX, -distanceY);
				}
				else if(mPlayer.canMoveVideoScreen())
					mPlayer.moveVideoFrame(-distanceX, -distanceY);
				else {
//				else if(!mPlayer.isControlsShowing()){
					DisplayMetrics displayMetrics = new DisplayMetrics();
					getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
					int volumeChangeX  = displayMetrics.widthPixels/2;
					float moveX = Math.abs(e1.getX() - e2.getX());
					float moveY = Math.abs(e1.getY() - e2.getY());

					Log.d(TAG, String.format("onScroll e1 (%1.2f %1.2f) disp (%d %d)", e1.getX(), e1.getY(),
							displayMetrics.widthPixels, displayMetrics.heightPixels));

					if(mScrollMode == SCROLL_MODE_N) {
						if(e1.getY() < mScrollNoActionTop || e1.getY() > (displayMetrics.heightPixels-mScrollNoActionBottom))
							return super.onScroll(e1, e2, distanceX, distanceY);

						if(moveX > moveY)
							mScrollMode = SCROLL_MODE_H;
						else
							mScrollMode = SCROLL_MODE_V;
					}

					if(mScrollMode == SCROLL_MODE_V) {
						//Volume Control
						if(e1.getX() > volumeChangeX) {
							if((mVolumeControlDistance > 0 && distanceY < 0) ||
							   (mVolumeControlDistance < 0 && distanceY > 0))
								mVolumeControlDistance = 0;

							mVolumeControlDistance += distanceY;
							if(Math.abs(mVolumeControlDistance) > SCROLL_V_DELICACY) {
								if(distanceY < 0)
									setVolumeControl(CONTROL_DEC);
								else
									setVolumeControl(CONTROL_INC);
								mVolumeControlDistance = 0;
							}
						}
						//Brightness Control
						else {
							if((mBrightnessControlDistance > 0 && distanceY < 0) ||
							   (mBrightnessControlDistance < 0 && distanceY > 0))
								mBrightnessControlDistance = 0;

							mBrightnessControlDistance += distanceY;
							if(Math.abs(mBrightnessControlDistance) > SCROLL_H_DELICACY) {
								if(distanceY < 0)
									setBrightnessControl(CONTROL_DEC);
								else
									setBrightnessControl(CONTROL_INC);
								mBrightnessControlDistance = 0;
							}
						}
					}
					else {
						if(e2.getAction() == MotionEvent.ACTION_MOVE) {
							mSeekControlDistance = (int)((e2.getX() - e1.getX())*SCROLL_SEEK_MOUNT/displayMetrics.widthPixels);
							if(mScrollAmountH < 0)
								mScrollAmountH = (int)e2.getY();
							mPlayer.setSeekLabel(displayMetrics.widthPixels, displayMetrics.heightPixels,
									(int)e2.getX(), mScrollAmountH,
									mSeekControlDistance*1000, true);
						}
					}
				}
			}
			return super.onScroll(e1, e2, distanceX, distanceY);
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			// TODO Auto-generated method stub
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MovieActivity.this);
			@ValuePreference.DOUBLE_TAB_MODE int doubleTabMode = pref.getInt(mResources.getString(R.string.preference_double_tab_key), ValuePreference.DOUBLE_TAB_SCREEN_SIZE);
			if(doubleTabMode == ValuePreference.DOUBLE_TAB_SCREEN_SIZE) {
				if (mPlayer != null && mPlayer.getPlayerType() != Utils.PLAYER_TYPE_NATIVE) {
					mPlayer.toggleVideoSize();
				}
			}
			else if(doubleTabMode == ValuePreference.DOUBLE_TAB_PLAY_PAUSE) {
				if(mPlayer != null)
					mPlayer.onPlayPause();
			}
			return super.onDoubleTap(e);
		}
    }

    @Override
	public void finish() {
		// TODO Auto-generated method stub
    	if(mPlayer != null)
    		mPlayer.onDestroy();
    	mPlayer = null;

//		setResult(RESULT_OK, getIntent());
		if(mReturnUrl != null) {
			try {
				String retUrl = new String(Base64.decode(mReturnUrl, Base64.DEFAULT));
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(retUrl)));
			} catch (Exception e) {}
		}

		super.finish();
	}

    Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(msg.what == CHECK_EXIT) {
				mExit = false;
			}
			else if(msg.what == HANDLER_ORIENTATION_LANDSCAPE || msg.what == HANDLER_ORIENTATION_REVERSED_LANDSCAPE) {
				if(mPlayer != null) {
					mPlayer.setOrientation(Configuration.ORIENTATION_LANDSCAPE);
				}
			}
		}
    };

	//etlim 20170902 Activity Exit ==> Broadcast Event
    private void MovieActivityFinishBroadcastRegister() {
        mMovieActivityFinishBR = new MovieActivityFinishBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_ACTIVITY_FINISH_MOVIE);
        registerReceiver(mMovieActivityFinishBR, filter);
    }

    private class MovieActivityFinishBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context ctx, Intent intent) {
			Log.d(TAG, "onReceive >>> " + intent.getAction());
            String action = intent.getAction();

            if (action.equals(ACTION_ACTIVITY_FINISH_MOVIE)) {
                try {
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (action.equals("")) {
                // do something
            }
        }
    }

	BluetoothBroadcastReceiver mBluetoothBR;
    private void registerBluetoothBR() {
		mBluetoothBR = new BluetoothBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(mBluetoothBR, filter);
    }

	private class BluetoothBroadcastReceiver extends BroadcastReceiver {
		private final String TAG = BluetoothBroadcastReceiver.class.getSimpleName();
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			int state;
			int majorDeviceClass;
			BluetoothDevice bluetoothDevice;

			switch(action)
			{
				case BluetoothAdapter.ACTION_STATE_CHANGED:
					state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
					if (state == BluetoothAdapter.STATE_OFF)
					{
						Log.d(TAG, "Bluetooth is off");
					}
					else if (state == BluetoothAdapter.STATE_TURNING_OFF)
					{
						Log.d(TAG, "Bluetooth is turning off");
					}
					else if(state == BluetoothAdapter.STATE_ON)
					{
						Log.d(TAG, "Bluetooth Bluetooth is on");
					}
					break;

				case BluetoothDevice.ACTION_ACL_CONNECTED:
					bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					majorDeviceClass = bluetoothDevice.getBluetoothClass().getMajorDeviceClass();
					Log.d(TAG, "Bluetooth Connect to "+bluetoothDevice.getName());
					if(majorDeviceClass == BluetoothClass.Device.Major.AUDIO_VIDEO && mPlayer != null) {
						mPlayer.setBluetoothConnectChanged(true);
					}
					break;

				case BluetoothDevice.ACTION_ACL_DISCONNECTED:
					bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					majorDeviceClass = bluetoothDevice.getBluetoothClass().getMajorDeviceClass();
					Log.d(TAG, "Bluetooth DisConnect from "+bluetoothDevice.getName());
					if(majorDeviceClass == BluetoothClass.Device.Major.AUDIO_VIDEO && mPlayer != null) {
						mPlayer.setBluetoothConnectChanged(false);
					}
					break;
			}
		}
	}
}
