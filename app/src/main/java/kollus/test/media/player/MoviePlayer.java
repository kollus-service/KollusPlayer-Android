/*
 * Copyright (C) 2009 The Android Open Source Project
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

package kollus.test.media.player;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.kollus.sdk.media.KollusPlayerBookmarkListener;
import com.kollus.sdk.media.KollusPlayerCallbackListener;
import com.kollus.sdk.media.KollusPlayerContentMode;
import com.kollus.sdk.media.KollusPlayerDRMListener;
import com.kollus.sdk.media.KollusPlayerLMSListener;
import com.kollus.sdk.media.KollusPlayerThumbnailListener;
import com.kollus.sdk.media.KollusStorage;
import com.kollus.sdk.media.MediaPlayer;
import com.kollus.sdk.media.MediaPlayer.OnCompletionListener;
import com.kollus.sdk.media.MediaPlayer.OnErrorListener;
import com.kollus.sdk.media.MediaPlayer.OnExternalDisplayDetectListener;
import com.kollus.sdk.media.MediaPlayer.OnPreparedListener;
import com.kollus.sdk.media.MediaPlayer.OnTimedTextDetectListener;
import com.kollus.sdk.media.MediaPlayer.OnTimedTextListener;
import com.kollus.sdk.media.MediaPlayerBase;
import com.kollus.sdk.media.MediaPlayerBase.TrackInfo;
import com.kollus.sdk.media.content.BandwidthItem;
import com.kollus.sdk.media.content.KollusBookmark;
import com.kollus.sdk.media.content.KollusContent;
import com.kollus.sdk.media.content.KollusContent.SubtitleInfo;
import com.kollus.sdk.media.util.emulatordetector.EmulatorDetector;
import com.kollus.sdk.media.util.ErrorCodes;
import com.kollus.sdk.media.util.Log;
import com.kollus.sdk.media.util.SkinManager;
import com.kollus.sdk.media.util.Utils;
import kollus.test.media.R;
import kollus.test.media.player.contents.BookmarkAdapter;
import kollus.test.media.player.preference.KollusConstants;
import kollus.test.media.player.util.DisplayUtil;
import kollus.test.media.player.view.KollusAlertDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import static android.content.Context.ACCESSIBILITY_SERVICE;

@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
public class MoviePlayer implements
        OnErrorListener, OnCompletionListener,
        ControllerOverlay.Listener, OnPreparedListener,
        OnTimedTextListener, OnTimedTextDetectListener, BookmarkAdapter.onBookmarkRemoveListener,
        OnExternalDisplayDetectListener {
    @SuppressWarnings("unused")
    private static final String TAG = "MoviePlayer";

    // These are constants in KeyEvent, appearing on API level 11.
    private static final int KEYCODE_MEDIA_PLAY = 126;
    private static final int KEYCODE_MEDIA_PAUSE = 127;

    // Copied from MediaPlaybackService in the Music Player app.
    private static final String SERVICECMD = "com.android.music.musicservicecommand";
    private static final String CMDNAME = "command";
    private static final String CMDPAUSE = "pause";

    private static final long BLACK_TIMEOUT = 500;

    private final float MIN_PLAY_RATE = 0.5f;
    private final float MAX_PLAY_RATE = 2.0f;

    private Activity mContext;
    private ViewGroup mRootView;
    private ViewGroup mControlView;
    private VideoView mVideoView;
    private ImageView mCaptionImageView;
    private ImageView mSoundOnlyImageView;
    private Bitmap    mSoundBitmap;
    private Bitmap    mCurrentThumbnail;
    private Bitmap    mDefaultThumbnail;
    private boolean   mSoundOnly;
    //	private WebView mCaptionStringView;
    private TextView mCaptionStringView;

    private int mCaptionColor = 0xffffffff;
    private int mCaptionSize = 12;
    private int mStrokeColor = 0xff000000;
    private float mStrokeWidth = 5;//0.5f;

    private Uri mUri;
    private String mMediaContentKey;
    private Uri mCaptionUri;
    private final Handler mHandler = new Handler();
    private MovieControllerOverlay mController;

    // If the time bar is being dragged.
    private boolean mDragging;

    // If the time bar is visible.
    private boolean mShowing;

    private float mPlayingRate = 1.0f;

    private KollusContent mKollusContent;
    private BookmarkAdapter mBookmarkAdapter;
    private ArrayList<KollusBookmark> mBookmarkList;
    private BitmapRegionDecoder mScreenShot = null;
    private BitmapFactory.Options mScreenShotOption;
    private int mScreenShotWidth;
    private int mScreenShotHeight;
    private int mScreenShotCount;
    private float mScreenShotInterval;
    private Toast mToast;
    private int mRepeatAMs = -1;
    private int mRepeatBMs = -1;
    private boolean mVolumeMute;
    private Vector<SubtitleInfo> mSubtitles;
    private View mSurfaceView;
    private boolean mVideoWaterMarkShow;

    private boolean mExternalDisplayPlugged;
    private int mExternalDisplayType;

    private int mSeekScreenShotTimeMs = -1;
    private boolean mSeekLocked = false;

    private boolean mScreenLocked;

    private KollusAlertDialog mAlertDialog;
    private boolean mStarted;
    private int mSeekableEnd = -1;

    private boolean mVRMode;

    private boolean mTalkbackEnabled;
    private int mSeekInterval;

    private boolean mBluetoothConnect;
    private int mAudioDelayMs;
    private boolean mReceivedThumbnail;

    private final Runnable mPlayingChecker = new Runnable() {
        @Override
        public void run() {
            if (mVideoView.isPlaying()) {
                mController.showPlaying();
            } else {
                mHandler.postDelayed(mPlayingChecker, 250);
            }
        }
    };

    private final Runnable mProgressChecker = new Runnable() {
        @Override
        public void run() {
            if(mSeekLocked && mKollusContent.getSkipSec()*1000 < mVideoView.getCurrentPosition())
                mSeekLocked = false;

            if(mRepeatAMs >= 0 && mRepeatAMs < mRepeatBMs && mRepeatBMs <= mVideoView.getCurrentPosition()) {
                mVideoView.seekToExact(mRepeatAMs);
            }
            int pos = setProgress();
            mHandler.postDelayed(mProgressChecker, 1000 - (pos % 1000));
        }
    };

    private final Runnable mVideoWaterMarkRunnable = new Runnable() {
        @Override
        public void run() {
            mVideoWaterMarkShow = !mVideoWaterMarkShow;
            if(mVideoWaterMarkShow) {
                mVideoView.showVideoWaterMark();
                mHandler.postDelayed(mVideoWaterMarkRunnable, mKollusContent.getVideoWaterMarkShowTime()*1000);
            }
            else {
                mVideoView.hideVideoWaterMark();
                mHandler.postDelayed(mVideoWaterMarkRunnable, mKollusContent.getVideoWaterMarkHideTime()*1000);
            }
        }
    };

    public MoviePlayer(View rootView, MovieActivity movieActivity,
                       Uri videoUri, Bundle savedInstance, boolean canReplay) {
        Log.d(TAG, "MoviePlayer Creator");
        mUri = videoUri;

        init(rootView, movieActivity, savedInstance, canReplay);
    }

    public MoviePlayer(View rootView, MovieActivity movieActivity,
                       String mediaContentKey, Bundle savedInstance, boolean canReplay) {
        Log.d(TAG, "MoviePlayer Creator");
        mMediaContentKey = mediaContentKey;

        init(rootView, movieActivity, savedInstance, canReplay);
    }

    private void init(View rootView, MovieActivity movieActivity,
                      Bundle savedInstance, boolean canReplay) {
        mContext = movieActivity;
        mRootView = (ViewGroup)rootView;

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(mContext);
        Resources resources = mContext.getResources();

        mControlView = (ViewGroup)mRootView.findViewById(R.id.control_view_layer);
        mController = new MovieControllerOverlay(mContext, mControlView);
        mController.setBookmarkable(false);
        mController.setListener(this);
        mController.setCanReplay(canReplay);

        //mVideoView = (VideoView) rootView.findViewById(R.id.surface_view);
        RelativeLayout layout = (RelativeLayout)mRootView.findViewById(R.id.surface_view_layer);
        mVideoView = new VideoView(movieActivity);
        mVideoView.setMediaController(mController);
        Intent intent = movieActivity.getIntent();
//        StorageManager manager = (StorageManager)intent.getParcelableExtra("manager");
        mVideoView.setOnExternalDisplayDetectListener(this);

        mBookmarkAdapter = new BookmarkAdapter(mContext);
        mBookmarkAdapter.setOnBookmarkRemoveListener(this);
        mController.setBookmarkAdapter(mBookmarkAdapter);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        mSurfaceView = new SurfaceView(mContext);
        mVideoView.addView(mSurfaceView, params);
        mVideoView.initVideoView((SurfaceView)mSurfaceView);

        layout.addView(mVideoView, params);

        mVideoView.setOnErrorListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnInfoListener(mInfoListener);
        mVideoView.setKollusPlayerBookmarkListener(mKollusPlayerBookmarkListener);
        mVideoView.setKollusPlayerLMSListener(mKollusPlayerLMSListener);
        mVideoView.setKollusPlayerDRMListener(mKollusPlayerDRMListener);
        mVideoView.setKollusPlayerCallbackListener(mKollusPlayerCallbackListener);
        mVideoView.setKollusPlayerThumbnailListener(mKollusPlayerThumbnailListener);
        mVideoView.setOnTimedTextDetectListener(this);
        mVideoView.setOnTimedTextListener(this);
        if(mUri != null)
            mVideoView.setVideoURI(mUri);
        else
            mVideoView.setVideoMCK(mMediaContentKey);
//        mVideoView.setOnTouchListener(mTouchListener);

        mCaptionColor = preference.getInt(
                resources.getString(R.string.preference_caption_color_key),
                resources.getColor(R.color.default_caption_color));
        mCaptionSize = preference.getInt(
                resources.getString(R.string.preference_caption_size_key),
                resources.getInteger(R.integer.default_caption_size));

        mStrokeColor = preference.getInt(resources.getString(R.string.preference_stroke_color_key),
                resources.getColor(R.color.default_stroke_color));
        boolean stroke = preference.getBoolean(resources.getString(R.string.preference_stroke_key),
                resources.getBoolean(R.bool.default_stroke));
        if(!stroke)
            mStrokeWidth = 0;

        boolean caption_bg = preference.getBoolean(resources.getString(R.string.preference_caption_bg_color_key),
                false);
        mCaptionImageView = (ImageView)mRootView.findViewById(R.id.captionImage);
        mCaptionImageView.setBackgroundColor(Color.TRANSPARENT);
        mCaptionImageView.setImageBitmap(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888));
//		mCaptionImageView.setOnTouchListener(mTouchListener);

//		mCaptionStringView = (WebView)rootView.findViewById(R.id.captionString);
        mCaptionStringView = (TextView)mRootView.findViewById(R.id.captionString);
//		mCaptionSize = (int) TypedValue.applyDimension(
//							TypedValue.COMPLEX_UNIT_PX, mCaptionSize, resources.getDisplayMetrics());
        mCaptionStringView.setTextColor(mCaptionColor);
        mCaptionStringView.setTextSize(mCaptionSize);
        //mCaptionStringView.setShadowLayer(mStrokeWidth, 1, 1, mStrokeColor);
        mCaptionStringView.setShadowLayer(mStrokeWidth, 0, 0, mStrokeColor);
        if(caption_bg)
            mCaptionStringView.setBackgroundColor(resources.getColor(R.color.default_caption_bg_color));
        else
            mCaptionStringView.setBackgroundColor(Color.TRANSPARENT);
        mCaptionStringView.setText("");
//		mCaptionStringView.setOnTouchListener(mTouchListener);

        mSoundOnlyImageView = (ImageView)mRootView.findViewById(R.id.sound_only);

//        mRootView.setOnTouchListener(mTouchListener);

        // The SurfaceView is transparent before drawing the first frame.
        // This makes the UI flashing when open a video. (black -> old screen
        // -> video) However, we have no way to know the timing of the first
        // frame. So, we hide the VideoView for a while to make sure the
        // video has been drawn on it.
        mVideoView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mVideoView.setVisibility(View.VISIBLE);
            }
        }, BLACK_TIMEOUT);

        Intent i = new Intent(SERVICECMD);
        i.putExtra(CMDNAME, CMDPAUSE);
        movieActivity.sendBroadcast(i);

        mController.setMoviePlayer(this);
        mController.showLoading();
        mSeekInterval = preference.getInt(resources.getString(R.string.preference_seek_interval_key),
                10)*1000;

        if (savedInstance != null) { // this is a resumed activity
            mVideoView.start();
            mVideoView.suspend();
        }

        mAudioDelayMs = preference.getInt(resources.getString(R.string.preference_audio_delay_key), 0);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null && bluetoothAdapter.isEnabled()
                && bluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED) {
            mController.setBluetoothConnectChanged(true);
            mBluetoothConnect = true;
        }
        unlockScreenOrientation();
    }

    private String formatDuration(final Context context, int duration) {
        int h = duration / 3600;
        int m = (duration - h * 3600) / 60;
        int s = duration - (h * 3600 + m * 60);
        String durationValue;
        if (h == 0) {
            durationValue = String.format(context.getString(R.string.details_ms), m, s);
        } else {
            durationValue = String.format(context.getString(R.string.details_hms), h, m, s);
        }
        return durationValue;
    }

    private void showResumeDialog(Context context, final int playAt) {
        mAlertDialog = new KollusAlertDialog(context).
                setTitle(R.string.resume_playing_title).
                setMessage(String.format(
                        context.getString(R.string.resume_playing_message),
                        formatDuration(context, playAt / 1000))).
                setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        onCompletion();
                    }
                }).
                setPositiveButton(
                        R.string.resume_playing_resume, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //                mVideoView.seekTo(playAt);
                                mVideoView.seekToExact(playAt);
                                startVideo();
                            }
                        }).
                setNegativeButton(
                        R.string.resume_playing_restart, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startVideo();
                            }
                        }).
                show();
    }

    public void addTimedTextSource(Uri uri) {
        mCaptionUri = uri;
    }

    public void onPause() {
//        mHandler.removeCallbacksAndMessages(null);
//        mVideoView.suspend();
        pauseVideo();
    }

    public void onResume() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            AccessibilityManager am = (AccessibilityManager) mContext.getSystemService(ACCESSIBILITY_SERVICE);
            mTalkbackEnabled = am.isEnabled() && am.isTouchExplorationEnabled();
        }
        mController.setTalkbackEnabled(mTalkbackEnabled);

        playVideo();
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mVideoView.stopPlayback();

        if(mScreenShot != null) {
            mScreenShot.recycle();
        }

        if(mAlertDialog != null) {
            mAlertDialog.dismiss();
        }

        if(mBookmarkList != null) {
            for (KollusBookmark bookmark : mBookmarkList) {
                Bitmap bitmap = bookmark.getThumbnail();
                if (bitmap != null && bitmap != mDefaultThumbnail)
                    bitmap.recycle();
            }
        }

        if(mDefaultThumbnail != null && mDefaultThumbnail != mSoundBitmap)
            mDefaultThumbnail.recycle();

        try {
            mHandler.removeCallbacks(mProgressChecker);
        } catch (Exception e) {}

        try {
            mHandler.removeCallbacks(mVideoWaterMarkRunnable);
        } catch (Exception e) {}
    }

    // This updates the time bar display (if necessary). It is called every
    // second by mProgressChecker and also from places where the time bar needs
    // to be updated immediately.
    private int setProgress() {
        if (mDragging || !mShowing) {
            return 0;
        }
        int position = mVideoView.getCurrentPosition();
        int duration = mVideoView.getDuration();
        if(mSeekableEnd >= 0 && mSeekableEnd < position)
            mSeekableEnd = position;
        mController.setTimes(position, mSeekableEnd, duration, 0, 0);
        return position;
    }

    private void startVideo() {
        // For streams that we expect to be slow to start up, show a
        // progress spinner until playback starts.
        if(finishByDetectExternalDisplay()) {
            return;
        }

        if(!checkAutoTime()) {
            return;
        }

        if(mUri != null) {
            String scheme = mUri.getScheme();
            if ("http".equalsIgnoreCase(scheme) || "rtsp".equalsIgnoreCase(scheme)) {
                mController.showLoading();
                mHandler.removeCallbacks(mPlayingChecker);
                mHandler.postDelayed(mPlayingChecker, 250);
            } else {
                mController.showPlaying();
            }
        } else {
            mController.showPlaying();
        }

        mRootView.setKeepScreenOn(true);
//		float playing_rate = 2.0f;
//        if(mVideoView.setPlayingRate(playing_rate)) {
//            mController.setPlayingRateText(playing_rate);
//            mPlayingRate = playing_rate;
//        }
        mVideoView.start();
        mStarted = true;

        setProgress();
    }

    private void playVideo() {
        if(!mStarted) {
            Log.w(TAG, "cannot playVideo, why not started");
            return;
        }

        if(finishByDetectExternalDisplay()) {
            return;
        }

        if(!checkAutoTime()) {
            return;
        }

        mRootView.setKeepScreenOn(true);
        mVideoView.start();
        mController.setState(CommonControllerOverlay.State.PLAYING);
        setProgress();
    }

    private void pauseVideo() {
        if(!mStarted) {
            Log.w(TAG, "cannot pauseVideo, why not started");
            return;
        }

        mRootView.setKeepScreenOn(false);
        mVideoView.pause();
        mController.setState(CommonControllerOverlay.State.PAUSED);
    }

    private void seekVideo(int time) {
        if(mSeekLocked) {
            return;
        }

        if(mSeekableEnd >= 0 && time > mSeekableEnd)
            time = mSeekableEnd;

        resetRepeatAB(time);
        mVideoView.seekTo(time);
        mRootView.setKeepScreenOn(true);  //etlim fixed. 20170829 Screen On.
        mVideoView.start();
        mController.setState(CommonControllerOverlay.State.PLAYING);
        setProgress();
    }

    private boolean finishByDetectExternalDisplay() {
        if(mKollusContent == null)
            return false;

        boolean detected = false;
        if(mExternalDisplayPlugged && mKollusContent.getDisableTvOut() && !mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEVISION)) {
            int message = R.string.restart_after_remove_hdmi_cable;
            if(mExternalDisplayType == MediaPlayerBase.EXTERNAL_HDMI_DISPLAY)
                message = R.string.restart_after_remove_hdmi_cable;
            else if(mExternalDisplayType == MediaPlayerBase.EXTERNAL_WIFI_DISPLAY)
                message = R.string.restart_after_off_wifi_display;

            mVideoView.pause();

            mAlertDialog = new KollusAlertDialog(mContext).
                    setTitle(R.string.error_title).
                    setMessage(message).
                    setPositiveButton(R.string.VideoView_error_button,
                            new OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    /* If we get here, there is no onError listener, so
                                     * at least inform them that the video is over.
                                     */
                                    onCompletion();
                                }
                            }).
                    setCancelable(false).
                    show();

            detected = true;
        }

        return detected;
    }

    private boolean checkAutoTime() {
//    	boolean auto = false;
//    	try {
//			auto = (Settings.System.getInt(mContext.getContentResolver(), Settings.System.AUTO_TIME) == 1);
//			if(!auto) {
//				new KollusAlertDialog(mContext)
//	            .setTitle(R.string.error_title)
//	            .setMessage(R.string.notify_auto_time)
//	            .setPositiveButton(R.string.VideoView_error_button,
//	                    new DialogInterface.OnClickListener() {
//	                        public void onClick(DialogInterface dialog, int whichButton) {
//	                            /* If we get here, there is no onError listener, so
//	                             * at least inform them that the video is over.
//	                             */
//	                        	onCompletion(null);
//	                        }
//	                    })
//	            .setCancelable(false)
//	            .show();
//			}
//		} catch (SettingNotFoundException e) {
//			// TODO Auto-generated catch block
//		}
//
//    	return auto;
        return true;
    }

    @Override
    public void onExternalDisplayDetect(int type, boolean plugged) {
        // TODO Auto-generated method stub
        Log.d(TAG, String.format("onExternalDisplayDetect type %d plugged %b", type, plugged));
        mExternalDisplayType = type;
        mExternalDisplayPlugged = plugged;

        finishByDetectExternalDisplay();
    }

    // Below are notifications from VideoView
    @Override
    public boolean onError(MediaPlayer player, int framework_err, int impl_err) {
        mHandler.removeCallbacksAndMessages(null);
        // VideoView will show an error dialog if we return false, so no need
        // to show more message.
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mController.showEnded();
        onCompletion();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // TODO Auto-generated method stub
//		try {
//			TrackInfo[] tracks = mVideoView.getTrackInfo();
//			mSoundOnly = true;
//			for(TrackInfo info : tracks) {
//				if(info.getTrackType() == TrackInfo.MEDIA_TRACK_TYPE_VIDEO) {
//					mSoundOnly = false;
//					break;
//				}
//			}
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
        if(mVideoView.getVideoWidth() > 0 && mVideoView.getVideoHeight() > 0) {
            mSoundOnly = false;
        }
        else {
            mSoundOnly = true;
        }

        initUI();

        if(mKollusContent.isVmCheck()) {
            EmulatorDetector emulatorDetector = new EmulatorDetector();
            if(emulatorDetector.isEmulator(this.mContext)) {
                mAlertDialog = new KollusAlertDialog(mContext)
                        .setTitle(R.string.error_title)
                        .setMessage(R.string.error_vm)
                        .setPositiveButton(R.string.confirm,
                                new OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        onCompletion();
                                    }
                                })
                        .show();
                return;
            }

//            AccelerometerDetector accelerometerDetector = AccelerometerDetector.builder()
//                    .setDelay(500)
//                    .setEventCount(5)
//                    //check continues 500*5 = 2500ms
//                    .build();
//
//            GyroscopeDetector gyroscopeDetector = GyroscopeDetector.builder()
//                    .setDelay(500)
//                    .setEventCount(5)
//                    .build();
//            EmulatorDetectorViaSensor emulatorDetector = new EmulatorDetectorViaSensor(accelerometerDetector, gyroscopeDetector);
//            emulatorDetector.detect(mContext, new Callback() {
//                @Override
//                public void onDetect(boolean isEmulator) {
//                    if(isEmulator) {
//                        mVideoView.pause();
//                        mAlertDialog = new KollusAlertDialog(mContext)
//                                .setTitle(R.string.error_title)
//                                .setMessage(R.string.error_vm)
//                                .setPositiveButton(R.string.confirm,
//                                        new DialogInterface.OnClickListener() {
//                                            public void onClick(DialogInterface dialog, int whichButton) {
//                                                onCompletion();
//                                            }
//                                        })
//                                .show();
//                    }
//                }
//
//                @Override
//                public void onError(Exception exception) {
//                    Log.e(TAG, exception.getMessage());
//                }
//            });
        }

        if(mBluetoothConnect)
            mVideoView.setAudioDelay(mAudioDelayMs);

        int playAt = mVideoView.getPlayAt();
        if (playAt > 0) {
            SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(mContext);
            Resources resources = mContext.getResources();
            boolean resumePlaying = preference.getBoolean(resources.getString(R.string.preference_resume_play_key),
                    resources.getBoolean(R.bool.default_force_nscreen));

            if(!mKollusContent.getForceNScreen() && !resumePlaying) {
                showResumeDialog(mContext, playAt);
            }
            else {
//	        		mVideoView.seekTo(playAt);
                mVideoView.seekToExact(playAt);
                startVideo();
            }
        } else {
            startVideo();
        }

        this.setOrientation(mContext.getResources().getConfiguration().orientation);
        mHandler.post(mProgressChecker);
    }

    @Override
    public void onTimedTextDetect(MediaPlayer mp, int trackIndex) {
        // TODO Auto-generated method stub
        mVideoView.selectTrack(trackIndex);
    }

    @Override
    public void onTimedText(MediaPlayer mp, String text) {
        // TODO Auto-generated method stub
        if(text != null) {
//			final String mimeType = "text/html";
//	        final String encoding = "UTF-8";
//
//        	try {
//		        String formattedCaption = String.format(text,
//		        		mCaptionSize,
//		        		(mCaptionColor&0x00ffffff),
//		        		(mStrokeColor&0x00ffffff),
//		        		mStrokeWidth);
//		        //Log.d(TAG, "onTimedText:"+formattedCaption);
//		        mCaptionStringView.loadDataWithBaseURL("x-data://base", formattedCaption, mimeType, encoding, null);
//	    	}catch(Exception e) {
//        		e.printStackTrace();
//        	}
//			Log.d(TAG, "onTimedText:"+text);
            mCaptionStringView.setText(Html.fromHtml(text));
        }
        else {
            mCaptionStringView.setText("");
        }
    }

    @Override
    public void onTimedImage(MediaPlayer mp, byte[] image, int width, int height) {
        // TODO Auto-generated method stub
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int idx=0, color;
        for(int j=0; j<height; j++) {
            for(int i=0; i<width; i++) {
                color = ((image[idx++] << 24)&0xff000000);
                color |= ((image[idx++] << 16)&0x00ff0000);
                color |= ((image[idx++] << 8)&0x0000ff00);
                color |= ((image[idx++] << 0)&0x000000ff);
                bitmap.setPixel(i, j, color);
            }
        }
//		Log.d(TAG, String.format("onDrawCaption width %d height %d data len %d", width, height, idx));
        mCaptionImageView.setImageBitmap(bitmap);
    }

    private MediaPlayer.OnInfoListener mInfoListener =
            new MediaPlayer.OnInfoListener() {

                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    // TODO Auto-generated method stub
                    return false;
                }

                @Override
                public void onBufferingStart(MediaPlayer mp) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onBufferingEnd(MediaPlayer mp) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onFrameDrop(MediaPlayer mp) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onDownloadRate(MediaPlayer mp, int downloadRate) {

                }

                @Override
                public void onDetectBandwidth(MediaPlayer mediaPlayer, List<BandwidthItem> list) {

                }

                @Override
                public void onBandwidth(MediaPlayer mediaPlayer, BandwidthItem bandwidthItem) {

                }
            };

    private KollusPlayerBookmarkListener mKollusPlayerBookmarkListener =
            new KollusPlayerBookmarkListener() {

                @Override
                public void onBookmark(List<KollusBookmark> bookmark, boolean bWritable) {
                    // TODO Auto-generated method stub
                    mBookmarkList = new ArrayList<KollusBookmark>();
                    mBookmarkList.addAll(bookmark);
                    mBookmarkAdapter.setArrayList(mBookmarkList, bWritable);
                    mController.setBookmarkLableList(mBookmarkAdapter.getBookmarkLableList());
                    mController.setBookmarkList(mBookmarkList);
                    if (!mKollusContent.isIntro())
                        mController.setBookmarkable(true);
                    mController.setBookmarkWritable(bWritable);
                    mController.setBookmarkCount(0, mBookmarkList.size());
                    mBookmarkAdapter.notifyDataSetChanged();
                    Log.d(TAG, "onBookmarkInfoDetected " + mBookmarkList.size());
                }

                @Override
                public void onGetBookmarkError(int nErrorCode) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onBookmarkDeleted(int position, boolean bDeleted) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onBookmarkUpdated(int position, boolean bUpdated) {
                    // TODO Auto-generated method stub

                }

            };

    private KollusPlayerLMSListener mKollusPlayerLMSListener =
            new KollusPlayerLMSListener() {

                @Override
                public void onLMS(String request, String response) {
                    // TODO Auto-generated method stub
                    Log.i(TAG, String.format("onLMS request '%s' response '%s'", request, response));
                }

            };

    private KollusPlayerDRMListener mKollusPlayerDRMListener = new KollusPlayerDRMListener() {

        @Override
        public void onDRM(String request, String response) {
            // TODO Auto-generated method stub
            Log.i(TAG, String.format("onDRM request '%s' response '%s'", request, response));
        }

        @Override
        public void onDRMInfo(KollusContent content, int nInfoCode) {
            Log.i(TAG, String.format("onDRMInfo index %d nInfoCode %d message %s", content.getUriIndex(), nInfoCode, content.getServiceProviderMessage()));
        }

    };

    private KollusPlayerCallbackListener mKollusPlayerCallbackListener = new KollusPlayerCallbackListener() {

        @Override
        public void onCallbackMessage(String request, String response) {
            // TODO Auto-generated method stub
            Log.i(TAG, String.format("onPlaycallback request '%s' response '%s'", request, response));
        }

    };

    private KollusPlayerThumbnailListener mKollusPlayerThumbnailListener =
            new KollusPlayerThumbnailListener() {
                @Override
                public void onCached(int index, int nErrorCode, String thumbPath) {
                    Log.d(TAG, String.format("onCachedThumbnail index %d nErrorCode %d path '%s'", index, nErrorCode, thumbPath));
                    synchronized (mBookmarkAdapter) {
                        if(nErrorCode == ErrorCodes.ERROR_OK) {
                            mReceivedThumbnail = true;
                            if (mKollusContent != null && thumbPath.equals(mKollusContent.getScreenShotPath())) {
                                initScreenShot();
                                mBookmarkAdapter.setThumbnailInfo(mScreenShot, mDefaultThumbnail,
                                        mScreenShotWidth, mScreenShotHeight, mScreenShotCount, mScreenShotInterval);
                            }
                        }
                    }
                }
            };

    public TrackInfo[] getTrackInfo() {
        return mVideoView.getTrackInfo();
    }

    public void onCompletion() {
    }

    // Below are notifications from ControllerOverlay
    @Override
    public void onPlayPause() {
        if (mVideoView.isPlaying()) {
            pauseVideo();
        } else {
            playVideo();
        }
    }

    @Override
    public void onRew() {
        int time = mVideoView.getCurrentPosition()-mSeekInterval;
        if(time < 0)
            time = 0;
        seekVideo(time);
    }

    @Override
    public void onFf() {
        int time = mVideoView.getCurrentPosition()+mSeekInterval;
        if(time > mVideoView.getDuration())
            time = mVideoView.getDuration();
        seekVideo(time);
    }

    private Bitmap getScreenShotBitmap(int time) {
        if(mKollusContent == null)
            return null;

        Bitmap bm = null;
        int playSectionStart = 0;
        if(mKollusContent.getPlaySectionEnd() > 0) {
            playSectionStart = mKollusContent.getPlaySectionStart();
        }

        try {
            int x=0, y=0, row=0, column=0;
            if(mScreenShot != null) {
                int index = Math.round((time+playSectionStart)/1000/mScreenShotInterval)+3;
                if(index >= mScreenShotCount)
                    index = mScreenShotCount-1;
//	        Log.d(TAG, String.format("getScreenShotBitmap index %d time %d", index, time));
                column = index%10;
                row = index/10;
                x = mScreenShotWidth*column;
                y = mScreenShotHeight*row;
                Rect rect = new Rect(x, y, x+mScreenShotWidth, y+mScreenShotHeight);
                bm = mScreenShot.decodeRegion(rect, mScreenShotOption);
            }
            else if(mSoundOnly)
                bm = mSoundBitmap;
        } catch(Exception e) {
            e.printStackTrace();
        }

        if(mCurrentThumbnail != null && mCurrentThumbnail != mSoundBitmap && mCurrentThumbnail != mDefaultThumbnail) {
            mCurrentThumbnail.recycle();
        }

        mCurrentThumbnail = bm;
        return bm;
    }

    @Override
    public void onSeekStart() {
        if(mSeekLocked)
            return;

//        mDragging = true;
        mVideoView.pause();
    }

    @Override
    public void onSeekMove(int time) {
        if(mSeekLocked)
            return;

        if(mSeekableEnd >= 0 && time > mSeekableEnd)
            time = mSeekableEnd;

        if(mScreenShot != null) {
            mController.setScreenShot(getScreenShotBitmap(time), time);
            mController.showSeekingTime(time, time - mVideoView.getCurrentPosition());
        }

        mController.setTimes(time, mSeekableEnd, mVideoView.getDuration(), 0, 0);
    }

    @Override
    public void onSeekEnd(int time, int start, int end) {
        if(mKollusContent.isLive() && mKollusContent.getSeekable())
            time += mVideoView.getDuration();

        seekVideo(time);
    }

    @Override
    public void onVR(boolean enable) {

    }

    @Override
    public void onVR360(boolean enable) {
    }

    @Override
    public void onScreenCapture() {
        Window window = ((Activity)mContext).getWindow();
//        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        mController.hide();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String extDir = android.os.Environment.getExternalStorageDirectory()+"/Pictures";
        String saveFileName = String.format("%s/ScreenShot_%s.png", extDir, df.format(new Date()));
        DisplayUtil.saveScreenShot(mSurfaceView, 1280, 720, saveFileName);
        mController.show();
        Toast.makeText(mContext,saveFileName+" Captured.", Toast.LENGTH_LONG).show();
//        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }

    @Override
    public void onBookmarkAdd() {
        int time = mVideoView.getCurrentPosition();
        int myMarking = 0;

//    	if(mScreenShot == null)
//    		return;

        for(KollusBookmark iter : mBookmarkList) {
            if(iter.getLevel() == KollusBookmark.USER_LEVEL)
                myMarking++;
        }

        if(myMarking >= KollusBookmark.MAX_BOOKMARK) {
            Resources resources = mContext.getResources();
            String msg = String.format(resources.getString(R.string.ERROR_MAX_BOOKMARK), KollusBookmark.MAX_BOOKMARK);
            showMessage(msg);
            return;
        }

        try {
            int index = mBookmarkAdapter.add(time);
            mController.setBookmarkSelected(index);
            mController.setBookmarkCount(mBookmarkAdapter.getBookmarkKind(), mBookmarkAdapter.getBookmarkCount());

            mVideoView.updateKollusBookmark(time);
        } catch(IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBookmarkRemoveView() {
        mBookmarkAdapter.updateBookmarkRemoveView();
    }

    @Override
    public void onBookmarkRemove(int index) {
        // TODO Auto-generated method stub
        try {
            int time = mBookmarkAdapter.getBookmarkTime(index);
            mBookmarkAdapter.remove(index);
            mController.setBookmarkCount(mBookmarkAdapter.getBookmarkKind(), mBookmarkAdapter.getBookmarkCount());
            if(mBookmarkAdapter.getBookmarkCount() == 0) {
                mController.setDeleteButtonEnable(false);
            }

            mVideoView.deleteKollusBookmark(time);
        } catch(IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBookmarkSeek(int index) {
        int time = mBookmarkAdapter.getBookmarkTime(index);
        if(time >= 0) {
            resetRepeatAB(time);
//    		mVideoView.seekTo(time);
            mVideoView.seekToExact(time);
        }
        Log.d(TAG, String.format("onBookmarkSeek index %d time %d", index, time));
    }

    @Override
    public void onBookmarkKind(int kind) {
        mBookmarkAdapter.setBookmarkKind(kind);
        mController.setBookmarkCount(kind, mBookmarkAdapter.getBookmarkCount());
    }

    @Override
    public void onCaptionSelected(int position) {
        try {
            mCaptionImageView.setVisibility(View.VISIBLE);
            mCaptionStringView.setVisibility(View.VISIBLE);

            SubtitleInfo subtitle = mSubtitles.get(position);
            mCaptionUri = Uri.parse(subtitle.url);
            if(subtitle.url != null/*&& subtitle.url.startsWith("http://")*/) {
                mVideoView.addTimedTextSource(mCaptionUri);
            }
        }
        catch(Exception e) {

        }
    }

    @Override
    public void onCaptionHide() {
        mCaptionImageView.setVisibility(View.GONE);
        mCaptionStringView.setVisibility(View.GONE);
    }

    @Override
    public void onShown() {
        mShowing = true;
        setProgress();

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mCaptionStringView.getLayoutParams();
        params.bottomMargin = mController.getProgressbarHeight()+5;
        mCaptionStringView.setLayoutParams(params);
//        showSystemUi(true);
    }

    @Override
    public void onHidden() {
        mShowing = false;
//        showSystemUi(false);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mCaptionStringView.getLayoutParams();
        params.bottomMargin = 5;
        mCaptionStringView.setLayoutParams(params);
    }

    @Override
    public void onReplay() {
        startVideo();
    }

    @Override
    public void onPlayingRate(int mode) {
        // TODO Auto-generated method stub
        float playing_rate = mPlayingRate;
        float rate_unit = 0.1f;
        if(mode == 1 && mPlayingRate >= 2)
            rate_unit = 0.5f;
        else if(mode == -1 && mPlayingRate > 2)
            rate_unit = 0.5f;
        switch(mode) {
            case -1:
                playing_rate -= rate_unit;
                break;
            case 1:
                playing_rate += rate_unit;
                break;
            default:
                playing_rate = 1.0f;
                break;
        }

        playing_rate = Math.round(playing_rate*10f)/10f;
        if(playing_rate < MIN_PLAY_RATE)
            playing_rate = MIN_PLAY_RATE;
        else if(playing_rate > MAX_PLAY_RATE)
            playing_rate = MAX_PLAY_RATE;

        if(mVideoView.setPlayingRate(playing_rate)) {
            mController.setPlayingRateText(playing_rate);
            mPlayingRate = playing_rate;
        }
    }

    @Override
    public void onBookmarkHidden() {
        mBookmarkAdapter.hideBookmarkRemoveView();

        mController.show();
    }

    @Override
    public void onSkip() {
        if(mVideoView != null)
            mVideoView.skip();
    }

    public void toggleMediaControlsVisibility() {
        if(mVideoView != null)
            mVideoView.toggleMediaControlsVisibility();
    }

    public boolean isControlsShowing() {
        if(mVideoView != null)
            return mVideoView.isControlsShowing();

        return false;
    }

    public void screenSizeScaleBegin(ScaleGestureDetector detector) {
        if(mVideoView != null && !mVRMode)
            mVideoView.screenSizeScaleBegin(detector);
    }

    public void screenSizeScale(ScaleGestureDetector detector) {
        if(mVideoView != null && !mVRMode)
            mVideoView.screenSizeScale(detector);
    }

    public void screenSizeScaleEnd(ScaleGestureDetector detector) {
        if(mVideoView != null && !mVRMode) {
            mVideoView.screenSizeScaleEnd(detector);
        }
    }

    public void toggleVideoSize() {
        if(mVRMode)
            return;

        mController.toggleScreenSizeMode();
    }

    public boolean canMoveVideoScreen() {
        if(mVideoView != null && !mVRMode)
            return mVideoView.canMoveVideoScreen();

        return false;
    }

    public void moveVideoFrame(float x, float y) {
        if(mVideoView != null && !mVRMode)
            mVideoView.moveVideoFrame(x, y);
    }

    public void setOrientation(int orientation) {
        mController.setOrientation(orientation);
    }

    public void setVolumeLabel(int level, int maxLevel) {
        mVolumeMute = false;
        mController.setMute(mVolumeMute);

        mController.setVolumeLabel(level);

        if(mVideoView != null) {
            if(level > maxLevel)
                mVideoView.setVolumeLevel(level-maxLevel);
            else
                mVideoView.setVolumeLevel(0);
            mVideoView.setMute(mVolumeMute);
        }
    }

    public void setBrightnessLabel(int level) {
        mController.setBrightnessLabel(level);
    }

    public void setSeekLabel(int maxX, int maxY, int x, int y, int mountMs, boolean bShow) {
        if(mKollusContent == null || (!mKollusContent.getSeekable() && mSeekableEnd < 0) || mKollusContent.isLive())
            return;

        if(mSeekLocked && mKollusContent.getSkipSec()*1000 < mVideoView.getCurrentPosition())
            mSeekLocked = false;

        if(mSeekLocked)
            return;

        if(mVideoView != null) {
            int seekTimeMs = 0;
            if(bShow)
                mVideoView.pause();
            if(mSeekScreenShotTimeMs < 0)
                mSeekScreenShotTimeMs = mVideoView.getCurrentPosition();
            seekTimeMs = mSeekScreenShotTimeMs+mountMs;

            if(mSeekableEnd >= 0 && seekTimeMs > mSeekableEnd)
                seekTimeMs = mSeekableEnd;
            else if(seekTimeMs < 0)
                seekTimeMs = 0;
            else if(seekTimeMs > mVideoView.getDuration())
                seekTimeMs = mVideoView.getDuration();

            if(!bShow) {
                resetRepeatAB(seekTimeMs);
                mVideoView.seekToExact(seekTimeMs);
                mSeekScreenShotTimeMs = -1;
            }

            mController.showSeekingTime(seekTimeMs, seekTimeMs - mVideoView.getCurrentPosition());
            mController.setSeekLabel(maxX, maxY, x, y, getScreenShotBitmap(seekTimeMs), seekTimeMs, bShow);

            if(!bShow) {
                mRootView.setKeepScreenOn(true);  //etlim fixed. 20170829 Screen On.
                mVideoView.start();
                if(mShowing)
                    mController.showPlaying();
                else
                    mController.hide();
            }
        }
    }

    public boolean isVRMode() {
        return mVRMode;
    }

    public void moveVR(float distanceX, float distanceY) {
        mVideoView.moveVR(distanceX, distanceY);
    }

    public int getPlayerType() {
        if(mVideoView != null)
            return mVideoView.getPlayerType();

        return Utils.PLAYER_TYPE_NONE;
    }

    protected boolean supportPlaybackrateControl() {
        if(mVideoView != null && mKollusContent != null)
            return mVideoView.supportPlaybackrateControl() && !mKollusContent.getDisablePlayRate();
        return false;
    }

    private void resetRepeatAB(int seekTimeMs) {
        if(mRepeatAMs < 0 || mRepeatBMs < 0)
            return;

        if(mRepeatAMs <= seekTimeMs && seekTimeMs <= mRepeatBMs)
            return;

        mController.resetRepeatABImage();
    }

    // Below are key events passed from MovieActivity.
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Some headsets will fire off 7-10 events on a single click
        Log.d(TAG, String.format("onKeyDown keyCode %d repeat count %d", keyCode, event.getRepeatCount()));
        if (event.getRepeatCount() > 0) {
            return isMediaKey(keyCode);
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_HEADSETHOOK:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_SPACE:
                if (mVideoView.isPlaying()) {
                    pauseVideo();
                } else {
                    playVideo();
                }
                return true;
            case KEYCODE_MEDIA_PAUSE:
                if (mVideoView.isPlaying()) {
                    pauseVideo();
                }
                return true;
            case KEYCODE_MEDIA_PLAY:
                if (!mVideoView.isPlaying()) {
                    playVideo();
                }
                return true;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                onRew();
                return true;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                onFf();
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                onRew();
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                onFf();
                return true;
            case KeyEvent.KEYCODE_M:
                onToggleMute();
                return true;
            case KeyEvent.KEYCODE_Z:
                onPlayingRate(0);
                return true;
            case KeyEvent.KEYCODE_X:
                onPlayingRate(-1);
                return true;
            case KeyEvent.KEYCODE_C:
                onPlayingRate(1);
                return true;
            case KeyEvent.KEYCODE_LEFT_BRACKET:
                onRepeatAB(ControllerOverlay.REPEAT_MODE_A);
                mController.setRepeatABImage(ControllerOverlay.REPEAT_MODE_A);
                return true;
            case KeyEvent.KEYCODE_RIGHT_BRACKET:
                onRepeatAB(ControllerOverlay.REPEAT_MODE_B);
                mController.setRepeatABImage(ControllerOverlay.REPEAT_MODE_B);
                return true;
            case KeyEvent.KEYCODE_BACKSLASH:
            case KeyEvent.KEYCODE_P:
                onRepeatAB(ControllerOverlay.REPEAT_MODE_DISABLE);
                mController.setRepeatABImage(ControllerOverlay.REPEAT_MODE_DISABLE);
                return true;
        }
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return isMediaKey(keyCode);
    }

    public void setBluetoothConnectChanged(boolean connect) {
        mBluetoothConnect = connect;
        mController.setBluetoothConnectChanged(mBluetoothConnect);
        if(mVideoView != null) {
            if(mBluetoothConnect)
                mVideoView.setAudioDelay(mAudioDelayMs);
            else
                mVideoView.setAudioDelay(0);
        }
    }

    private static boolean isMediaKey(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS
                || keyCode == KeyEvent.KEYCODE_MEDIA_NEXT
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE;
    }

    private void initUI() {
        mKollusContent = new KollusContent();
        if(mVideoView.getKollusContent(mKollusContent)) {
            Log.i(TAG, "ContentInfo ==> " + mKollusContent);
            KollusContent content = new KollusContent();
            KollusStorage storage = KollusStorage.getInstance(mContext);
            storage.getKollusContent(content, mKollusContent.getMediaContentKey());
            Log.i(TAG, "Storage ContentInfo ==> " + content);

            String cource = mKollusContent.getCourse();
            String subcource = mKollusContent.getSubCourse();
            String title;
            if(cource != null && cource.length() > 0) {
                if(subcource != null && subcource.length() > 0)
                    title = cource+"("+subcource+")";
                else
                    title  = cource;
            }
            else
                title = subcource;
            mController.setTitleText(title);

            mVolumeMute = mKollusContent.getMute();
            if(!mKollusContent.getSeekable())
                mSeekableEnd = mKollusContent.getSeekableEnd();

            mController.setMute(mVolumeMute);
            mController.setSkinManager(new SkinManager(mKollusContent.getSkinString()));
            if(mKollusContent.getHasWaterMark()) {
                mController.showWaterMark();
            }

            if(mKollusContent.isIntro() && mKollusContent.getSkipSec() >= 0) {
                mController.showSkip(mKollusContent.getSkipSec());
                mSeekLocked = false;
            }
            else {
                mController.hideSkip();
            }

            try {
                mSubtitles = mKollusContent.getSubtitleInfo();
                if(mSubtitles.size() > 0) {
                    SubtitleInfo subtitle = mSubtitles.get(0);
                    mCaptionUri = Uri.parse(subtitle.url);
                    if(subtitle.url != null && subtitle.url.startsWith("http://"))
                        mVideoView.addTimedTextSource(mCaptionUri);
                }

                mController.setCaptionList(mSubtitles);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                mController.setCaptionList(null);
                e.printStackTrace();
            }

            mController.setBookmarkable(false);
            mController.setSeekable(mKollusContent.getSeekable() || mSeekableEnd >= 0);
            mController.setScreenShotEnabled(mScreenShot != null || mSoundOnly);
            mController.setBookmarkable(false);
            mPlayingRate = 1.0f;
            mController.setPlayingRateText(mPlayingRate);
            mController.resetRepeatABImage();
            mController.setLive(mKollusContent.isLive(), mKollusContent.isLive() && mKollusContent.getSeekable());
            mController.hide();
            mCaptionStringView.setText("");

            if(mKollusContent.isVr() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                mController.supportVR360(true);

            if(mSoundOnly) {
                String thumb = mKollusContent.getThumbnailPath();
                if(thumb != null && !thumb.startsWith("http")) {
                    mSoundBitmap = BitmapFactory.decodeFile(thumb);
                    mSoundOnlyImageView.setImageBitmap(mSoundBitmap);//setImageURI(Uri.parse(thumb));
                }
                else {
                    mSoundBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.sound_only);
                }

                mSoundOnlyImageView.setVisibility(View.VISIBLE);
            }
            else {
                mSoundOnlyImageView.setVisibility(View.GONE);
            }

            if(mKollusContent.isThumbnailEnable()) {
                synchronized (mBookmarkAdapter) {
                    boolean hasThumbnail = mKollusContent.isThumbnailDownloadSync() || (mReceivedThumbnail && mScreenShot == null);
                    if (hasThumbnail) {
                        initScreenShot();
                        mBookmarkAdapter.setThumbnailInfo(mScreenShot, mSoundOnly ? mSoundBitmap : mDefaultThumbnail,
                                mScreenShotWidth, mScreenShotHeight, mScreenShotCount, mScreenShotInterval);
                    }
                }
            }
            else {
                makeDefaultThumbnail(192, 108);
                mBookmarkAdapter.setThumbnailInfo(null, mSoundOnly ? mSoundBitmap : mDefaultThumbnail,
                        0, 0, 0, 0);
            }

            if(!mKollusContent.getVideoWaterMarkCode().isEmpty() && KollusConstants.SUPPORT_VIDEO_WATER_MARK) {
                Log.d(TAG, String.format("VideoWaterMark code '%s' size %d color #%X alpha %d",
                        mKollusContent.getVideoWaterMarkCode(),
                        mKollusContent.getVideoWaterMarkFontSize(),
                        mKollusContent.getVideoWaterMarkFontColor(),
                        mKollusContent.getVideoWaterMarkAlpha()));
                TextView watermark = new TextView(mContext);
                watermark.setText(mKollusContent.getVideoWaterMarkCode());
                watermark.setTextSize(TypedValue.COMPLEX_UNIT_PT, mKollusContent.getVideoWaterMarkFontSize());
                watermark.setTextColor(mKollusContent.getVideoWaterMarkFontColor()|0xFF000000);
                watermark.setAlpha(mKollusContent.getVideoWaterMarkAlpha()/255.0f);
                mVideoView.setVideoWaterMark(watermark);
                //mControlView.addView
                mRootView.addView(watermark, new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT));

                mHandler.post(mVideoWaterMarkRunnable);
            }
        }
    }

    private void makeDefaultThumbnail(int width, int height) {
        mDefaultThumbnail = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mDefaultThumbnail.eraseColor(0xff999999);
    }

    private void initScreenShot() {
        String screenShotPath = mKollusContent.getScreenShotPath();
        int end = screenShotPath.lastIndexOf('.');
        int start = screenShotPath.lastIndexOf('.', end-1);

        if(start < end) {
            String info = screenShotPath.substring(start+1, end);
            Scanner sc = new Scanner(info);
            sc.useDelimiter("x");
            mScreenShotWidth = sc.nextInt();
            mScreenShotHeight = sc.nextInt();
            mScreenShotCount = sc.nextInt();
            mScreenShotInterval = (float) (mKollusContent.getDuration()/mScreenShotCount/1000.);
            sc.close();

            try {
                mScreenShot = BitmapRegionDecoder.newInstance(screenShotPath, true);
                if(mScreenShot != null) {
                    Log.d(TAG, String.format("ScreenShot width %d height %d",
                            mScreenShot.getWidth(), mScreenShot.getHeight()));
                    mScreenShotOption = new BitmapFactory.Options();
                }
                else
                    Log.e(TAG, "ScreenShot null");
            } catch (Exception e) {
                Log.e(TAG, "ScreenShot Exception");
                e.printStackTrace();
            } catch (OutOfMemoryError e) {
                Log.e(TAG, "ScreenShot OutOfMemoryError");
                e.printStackTrace();
            }

            makeDefaultThumbnail(mScreenShotWidth, mScreenShotHeight);
            Log.d(TAG, String.format("ScreenShot w %d h %d count %d duration %d",
                    mScreenShotWidth, mScreenShotHeight, mScreenShotCount, mKollusContent.getDuration()));
        }
        else {
            mScreenShotWidth = 192;
            mScreenShotHeight = 108;
        }
        makeDefaultThumbnail(mScreenShotWidth, mScreenShotHeight);
    }

    private void lockScreenOrientation() {
        final int orientation = mContext.getResources().getConfiguration().orientation;
        final int rotation = mContext.getWindowManager().getDefaultDisplay().getOrientation();

        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
        else if (rotation == Surface.ROTATION_180 || rotation == Surface.ROTATION_270) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
            }
            else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            }
        }
    }

    private void unlockScreenOrientation() {
        mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    @Override
    public void onScreenRotateLock(boolean lock) {
        // TODO Auto-generated method stub
        if(lock)
            lockScreenOrientation();
        else
            unlockScreenOrientation();
        mScreenLocked = lock;
    }

    @Override
    public void onScreenSizeMode(int mode) {
        // TODO Auto-generated method stub
        if(mVideoView != null)
            mVideoView.toggleVideoSize(mode);

        Resources res = mContext.getResources();
        if(mode == KollusPlayerContentMode.ScaleAspectFill)
            showMessage(res.getString(R.string.FULL_SCREEN));
        else if(mode == KollusPlayerContentMode.ScaleAspectFillStretch)
            showMessage(res.getString(R.string.FULL_SCREEN_STRETCH));
        else if(mode == KollusPlayerContentMode.ScaleAspectFit)
            showMessage(res.getString(R.string.FIT_SCREEN));
        else
            showMessage(res.getString(R.string.REAL_SIZE_SCREEN));
    }

    @Override
    public void onRepeatAB(int direction) {
        // TODO Auto-generated method stub
        if(direction == ControllerOverlay.REPEAT_MODE_A) {
            mRepeatAMs = mVideoView.getCurrentPosition();
        }
        else if(direction == ControllerOverlay.REPEAT_MODE_B) {
            int timeMs = mVideoView.getCurrentPosition();
            if(mRepeatAMs < timeMs)
                mRepeatBMs = timeMs;
            else {
                int tempMs = mRepeatAMs;
                mRepeatAMs = timeMs;
                mRepeatBMs = tempMs;
            }
        }
        else if(direction == ControllerOverlay.REPEAT_MODE_DISABLE) {
            mRepeatAMs = mRepeatBMs = -1;
        }

        if(mRepeatAMs >= 0 && mRepeatAMs < mRepeatBMs) {
            mVideoView.seekToExact(mRepeatAMs);
        }

        mController.setRepeatAB(mRepeatAMs, mRepeatBMs);
    }

    @Override
    public void onRepeat(boolean enable) {
        // TODO Auto-generated method stub
        if(mVideoView != null)
            mVideoView.setLooping(enable);
    }

    @Override
    public void onAudioDelay(int timeMs) {
        mAudioDelayMs = timeMs;
        if(mVideoView != null)
            mVideoView.setAudioDelay(mAudioDelayMs);

        Resources resources = mContext.getResources();
        SharedPreferences.Editor preference = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
        preference.putInt(resources.getString(R.string.preference_audio_delay_key), mAudioDelayMs);
        preference.commit();
    }

    @Override
    public void onTimeShiftOff() {
        if(mVideoView != null)
            mVideoView.seekTo(mVideoView.getDuration());
    }

    @Override
    public void onToggleMute() {
        // TODO Auto-generated method stub
        mVolumeMute = !mVolumeMute;
        if(mVideoView != null)
            mVideoView.setMute(mVolumeMute);
        mController.setMute(mVolumeMute);
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom) {
        if(mVideoView != null)
            mVideoView.adjustVideoWaterMarkPosition(left, top, right, bottom);
    }

    private void showMessage(String msg) {
        if(mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }
}

