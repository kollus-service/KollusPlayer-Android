/*
 * Copyright (C) 2012 The Android Open Source Project
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
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.kollus.sdk.media.KollusPlayerContentMode;
import com.kollus.sdk.media.util.Log;
import com.kollus.sdk.media.util.SkinManager;
import com.kollus.sdk.media.util.Utils;
import kollus.test.media.R;
import kollus.test.media.player.preference.KollusConstants;
import kollus.test.media.player.view.BookmarkScrollView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import static android.content.Context.WINDOW_SERVICE;

/**
 * The common playback controller for the Movie Player or Video Trimming.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
public abstract class CommonControllerOverlay implements
        ControllerOverlay,
        OnClickListener,
        TimeBar.Listener,
        OnItemClickListener,
        RadioGroup.OnCheckedChangeListener {

    protected enum State {
        PLAYING,
        PAUSED,
        BUFFERING,
        ENDED,
        ERROR,
        LOADING
    }
    private static final String TAG = CommonControllerOverlay.class.getSimpleName();
    private static final String SAVE_VOLOME_LEVEL = "save_volume_level";

    private final int VW_MARGIN_H_MAX = 200;
    private final int VW_MARGIN_V_MAX = 200;
    private final int MSG_SKIP_SHOW = 100;
    private final int MSG_SKIP_CHECK = 101;
    private final int SKIP_INTERVAL = 1000;
    private final int SKIP_NEED_SHOW = 0;
    private int SKIP_SHOW_SEC = 5000;
    private int mRemainSkipSec;

    protected final Context mContext;
    protected Listener mListener;
    protected SkinManager mSkinManager;

    private boolean mControlHidden;
    private boolean mScreenShotHidden;
    private ViewGroup mRootView;
    protected final LinearLayout mTitleView;
    protected TextView mLiveView;
    protected TextView mTitle;
    protected ImageView mVR;
    protected ImageView mVR360;
    protected ImageView mCapture;
    protected ImageView mBookmark;
    protected TimeBar mTimeBar;
    protected int mTimeBarTotalTime;
    protected int mTimeBarCurrentTime;

    protected View mAVSyncView;
    protected View mBtnAVSyncMinus;
    protected View mBtnAVSyncPlus;
    protected TextView mAVSyncText;
    protected int mAVSyncAmount;
    protected boolean mAVSyncVisible;

    protected ImageView mMuteView;

    protected View mMainLayout;
    protected final LinearLayout mLoadingView;
    protected final TextView mLoadingText;
    private RelativeLayout mVideoWaterMarkLayer;
    private TextView mVideoWaterMark;

    protected final View mPlayCenterLayer;
    protected final ImageView mPlayPauseReplayView;
    protected final View mRewView;
    protected final View mFfView;

    protected final View mSeekTimeLayer;
    protected final TextView mSeekTimeText;
    protected final TextView mSeekTimeAmountText;

    protected final View mSubControlView;
    protected final ImageView mScreenRotateLockView;
    protected final ImageView mScreenSizeModeView;
    protected final ImageView mABRepeatView;
    protected final ImageView mRepeatView;

    protected boolean mScreenRotateLock;
    protected int mScreenSizeMode = KollusPlayerContentMode.ScaleAspectFit;
    protected int mABRepeat;
    protected boolean mRepeat;

    protected final LinearLayout mPlayingRateView;
    protected final ImageView mPlayingRateUp;
    protected final TextView  mPlayingRateText;
    protected final ImageView mPlayingRateDown;

    protected final LinearLayout mSkipLayer;
    protected final TextView mSkipView;

    protected final LinearLayout mBookmarkView;
    protected final TextView mBookmarkCountView;
    protected final RadioGroup mBookmarkKind;
    protected final BookmarkScrollView mBookmarkListView;
    private LinearLayout mBookmarkBtnLayout;
    protected final ImageView mBookmarkAdd;
    protected final ImageView mBookmarkRemove;

    protected final TextView  mWaterMarkView;

    protected final ImageView mCaption;
    protected final LinearLayout mCaptionListLayout;
    protected final RadioGroup mCaptionGroup;

    protected boolean mScreenShotExist;
    protected final RelativeLayout mScreenShotView;
    protected final ImageView mScreenShot;
    protected final TextView mScreenShotTime;
    private int SCREEN_SHOT_WIDTH = -1;
    private int SCREEN_SHOT_HEIGHT = 120;//135;
    protected Resources mResources;

    protected State mState;

    protected boolean mControllerShown = true;
    protected boolean mCanReplay = true;
    protected boolean mCaptionHidden = true;
    protected boolean mBookmarkHidden = true;
    protected boolean mSelectBookmarkRemove = false;

    private static final float DEFAULT_DENSITY = 1.5f;

    protected View mVolumeLayout;
    protected ImageView mVolumeView;
    protected TextView mVolumeString;

    protected View mBrightLayout;
    protected TextView mBrightString;

    private ImageView mCiView;
    protected ImageView mNoTitleCiView;
    protected boolean   mHasNoTitleCi;

    private Animation mVolumeHideAnim;

    protected final RelativeLayout mSeekScreenShotView;
    protected final ImageView mSeekScreenShot;
    protected final TextView mSeekScreenShotTime;
    private int mSeekScreenShotGap = 50;

    private MoviePlayer mMoviePlayer;
    private View mControlView;
    private TextView mPlayerText;
    private TextView mCodecText;
    private TextView mResolutionText;
    private TextView mFrameRateText;

    private float mDensity;
    private int mSmallestScreenWidthDp;
    private boolean mLive;
    private boolean mTimeShift;
    protected boolean mTalkbackEnabled;
    private Random mRandom = new Random();

    private final int mVideoWaterMarkAlign[] = {
            RelativeLayout.ALIGN_PARENT_LEFT,
            RelativeLayout.CENTER_HORIZONTAL,
            RelativeLayout.ALIGN_PARENT_RIGHT
    };
    private int mVideoWaterMarkVAlign[] = {
            RelativeLayout.ALIGN_PARENT_TOP,
            RelativeLayout.CENTER_VERTICAL,
            RelativeLayout.ALIGN_PARENT_BOTTOM
    };

    public CommonControllerOverlay(Context context, ViewGroup rootView) {
        Log.d(TAG, "CommonControllerOverlay Creator");
        mContext = context;
        mScreenShotExist = false;
        mRootView = rootView;

        LayoutInflater inflater = LayoutInflater.from(context);
        mControlView = inflater.inflate(R.layout.movie_control, rootView, false);
        mControlView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mListener.onLayoutChange(v, left, top, right, bottom);
            }
        });
        mRootView.addView(mControlView);

        mResources = context.getResources();
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        mDensity =  dm.density;

        try {
            mSmallestScreenWidthDp = mResources.getConfiguration().smallestScreenWidthDp;
        } catch (java.lang.Error e) {}

        mState = State.LOADING;
        // TODO: Move the following layout code into xml file.

        float scale = dm.density/DEFAULT_DENSITY;
        SCREEN_SHOT_HEIGHT = (int) (SCREEN_SHOT_HEIGHT*scale);

        mMainLayout = mControlView.findViewById(R.id.main_layout);
        mTitleView = (LinearLayout)mControlView.findViewById(R.id.title_layout);
        mLiveView = (TextView)mControlView.findViewById(R.id.live);
        mTitle = (TextView)mControlView.findViewById(R.id.title);

        mAVSyncView = mControlView.findViewById(R.id.av_sync_layout);
        mBtnAVSyncMinus = mControlView.findViewById(R.id.btn_av_sync_minus);
        mBtnAVSyncMinus.setOnClickListener(this);
        mBtnAVSyncPlus = mControlView.findViewById(R.id.btn_av_sync_plus);
        mBtnAVSyncPlus.setOnClickListener(this);

        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(mContext);
        mAVSyncAmount = preference.getInt(mResources.getString(R.string.preference_audio_delay_key), 0);
        mAVSyncText = (TextView) mControlView.findViewById(R.id.av_sync_text);
        String label = String.format("%1.1f%s", mAVSyncAmount/1000., mResources.getString(R.string.sec));
        mAVSyncText.setText(label);

        mSkipLayer = (LinearLayout)mControlView.findViewById(R.id.skip_layer);
        mSkipView = (TextView)mControlView.findViewById(R.id.skip_view);
        mSkipView.setOnClickListener(this);

        mVR = (ImageView)mControlView.findViewById(R.id.btn_vr);
        mVR.setOnClickListener(this);
        mVR.setSelected(false);

        mVR360 = (ImageView)mControlView.findViewById(R.id.btn_vr360);
        mVR360.setOnClickListener(this);
        mVR360.setSelected(false);
//        if (KollusConstants.SUPPORT_VR && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
//            mVR360.setVisibility(View.VISIBLE);
//        else
//            mVR360.setVisibility(View.GONE);

        mCapture = (ImageView)mControlView.findViewById(R.id.btn_capture);
        mCapture.setOnClickListener(this);
        if(KollusConstants.SUPPORT_SCREEN_CAPTURE)
            mCapture.setVisibility(View.VISIBLE);
        else
            mCapture.setVisibility(View.GONE);

        mBookmark = (ImageView)mControlView.findViewById(R.id.btn_bookmark);
        mBookmark.setOnClickListener(this);
        mBookmark.setVisibility(View.GONE);

        mMuteView = (ImageView)mControlView.findViewById(R.id.btn_vol_mute);
        mMuteView.setOnClickListener(this);

        // Depending on the usage, the timeBar can show a single scrubber, or
        // multiple ones for trimming.
        mTimeBar = (TimeBar)mControlView.findViewById(R.id.timebar);

        mLoadingView = (LinearLayout)mControlView.findViewById(R.id.loading_layout);
        mLoadingText = (TextView)mControlView.findViewById(R.id.loading_text);

        mVideoWaterMarkLayer = (RelativeLayout) mControlView.findViewById(R.id.video_water_mark_layout);

        mPlayCenterLayer = mControlView.findViewById(R.id.player_center_layer);

        mPlayPauseReplayView = (ImageView)mControlView.findViewById(R.id.btn_play_pause);
        mPlayPauseReplayView.setOnClickListener(this);

        mRewView = mControlView.findViewById(R.id.btn_rew);
        mRewView.setOnClickListener(this);

        mFfView = mControlView.findViewById(R.id.btn_ff);
        mFfView.setOnClickListener(this);

        int interval = preference.getInt(mResources.getString(R.string.preference_seek_interval_key), 10);
        TextView text = (TextView)mControlView.findViewById(R.id.btn_rew_str);
        text.setText(String.valueOf(interval));
        text = (TextView)mControlView.findViewById(R.id.btn_ff_str);
        text.setText(String.valueOf(interval));

        mSeekTimeLayer = mControlView.findViewById(R.id.seek_time_layer);
        mSeekTimeText = (TextView)mControlView.findViewById(R.id.seek_time_text);
        mSeekTimeAmountText = (TextView)mControlView.findViewById(R.id.seek_time_amount);

        mPlayingRateView = (LinearLayout)mControlView.findViewById(R.id.rate_layout);

        mSubControlView = mControlView.findViewById(R.id.subcontrol_layout);
        mScreenRotateLockView = (ImageView)mControlView.findViewById(R.id.btn_rotate_lock);
        mScreenRotateLockView.setOnClickListener(this);
        mScreenRotateLock = false;

        mScreenSizeModeView = (ImageView)mControlView.findViewById(R.id.btn_screen_size);
        mScreenSizeModeView.setOnClickListener(this);
        mScreenSizeMode = KollusPlayerContentMode.ScaleAspectFit;
        if(Utils.getPlayerType() == Utils.PLAYER_TYPE_NATIVE)
            mScreenSizeModeView.setVisibility(View.GONE);

        mABRepeatView = (ImageView)mControlView.findViewById(R.id.btn_ab_repeat);
        mABRepeatView.setOnClickListener(this);
        mABRepeat = REPEAT_MODE_DISABLE;

        mRepeatView = (ImageView)mControlView.findViewById(R.id.btn_repeat);
        mRepeatView.setOnClickListener(this);
        mRepeat = false;

        mPlayingRateUp = (ImageView)mControlView.findViewById(R.id.btn_rate_up);
        mPlayingRateUp.setOnClickListener(this);

        mPlayingRateText = (TextView)mControlView.findViewById(R.id.current_rate);
        mPlayingRateText.setOnClickListener(this);

        mPlayingRateDown = (ImageView)mControlView.findViewById(R.id.btn_rate_down);
        mPlayingRateDown.setOnClickListener(this);

        //BookMark [[
        mBookmarkView = (LinearLayout)mControlView.findViewById(R.id.bookmark_list_layout);
        mBookmarkView.setVisibility(View.GONE);

        mBookmarkKind = (RadioGroup)mBookmarkView.findViewById(R.id.bookmark_kind);
        mBookmarkKind.setOnCheckedChangeListener(this);

        mBookmarkCountView = (TextView) mBookmarkView.findViewById(R.id.bookmark_count);
//        if(mSmallestScreenWidthDp < 600) {
//        	mBookmarkCountView.setTextSize(mDensity*12);
//        }

        mBookmarkListView = (BookmarkScrollView) mBookmarkView.findViewById(R.id.bookmark_list);
        mBookmarkListView.setOnItemClickListener(this);
        LinearLayout layout = (LinearLayout) mBookmarkView.findViewById(R.id.bookmark_list_layer);
        mBookmarkListView.setLayout(layout);

        mBookmarkBtnLayout = (LinearLayout)mBookmarkView.findViewById(R.id.bookmark_btn_layout);

        mBookmarkAdd = (ImageView) mBookmarkView.findViewById(R.id.bookmark_add);
        if(mSmallestScreenWidthDp < 600) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mBookmarkAdd.getLayoutParams();
            params.width = (int)(mDensity*24);
            params.height = (int)(mDensity*24);
            mBookmarkAdd.setLayoutParams(params);
        }
        mBookmarkAdd.setOnClickListener(this);
        mBookmarkAdd.setVisibility(View.GONE);

        mBookmarkRemove = (ImageView) mBookmarkView.findViewById(R.id.bookmark_delete);
        if(mSmallestScreenWidthDp < 600) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mBookmarkRemove.getLayoutParams();
            params.width = (int)(mDensity*24);
            params.height = (int)(mDensity*24);
            mBookmarkRemove.setLayoutParams(params);
        }
        mBookmarkRemove.setOnClickListener(this);
        mBookmarkRemove.setVisibility(View.GONE);
        //BookMark ]]

        mCaptionListLayout = (LinearLayout)mControlView.findViewById(R.id.caption_list_layout);
        mCaptionListLayout.setVisibility(View.GONE);
        mCaption = (ImageView)mControlView.findViewById(R.id.btn_caption);
        mCaption.setOnClickListener(this);
        mCaption.setVisibility(View.GONE);
        mCaptionGroup = (RadioGroup)mCaptionListLayout.findViewById(R.id.caption_list);
        mCaptionGroup.setOnCheckedChangeListener(this);

        mScreenShotView = (RelativeLayout) mControlView.findViewById(R.id.screenshot_layout);
        mScreenShot = (ImageView) mScreenShotView.findViewById(R.id.screenshot_thumbnail);
        mScreenShotTime = (TextView) mScreenShotView.findViewById(R.id.screenshot_time);

        mVolumeLayout = mControlView.findViewById(R.id.volume_layout);
        mVolumeView = (ImageView)mControlView.findViewById(R.id.volume_view);
        mVolumeString = (TextView)mControlView.findViewById(R.id.volume_string);

        mBrightLayout = mControlView.findViewById(R.id.bright_layout);
        mBrightString = (TextView)mControlView.findViewById(R.id.bright_string);

        mSeekScreenShotView = (RelativeLayout) mControlView.findViewById(R.id.seek_thumbnail_layout);
        mSeekScreenShot = (ImageView) mSeekScreenShotView.findViewById(R.id.seek_thumbnail);
        mSeekScreenShotTime = (TextView) mSeekScreenShotView.findViewById(R.id.seek_thumbnail_time);
        mSeekScreenShotGap = (int) (mSeekScreenShotGap*scale);

        mCiView =(ImageView)mRootView.findViewById(R.id.ci);
        mNoTitleCiView = (ImageView)mRootView.findViewById(R.id.no_title_ci);

        mWaterMarkView = (TextView)mRootView.findViewById(R.id.water_mark);

        View debugLayer = mRootView.findViewById(R.id.debug_layout);
        if(Log.isDebug())
            debugLayer.setVisibility(View.VISIBLE);
        mPlayerText = (TextView)mRootView.findViewById(R.id.player_text);
        mCodecText = (TextView)mRootView.findViewById(R.id.codec_text);
        mResolutionText = (TextView)mRootView.findViewById(R.id.resolution_text);
        mFrameRateText = (TextView)mRootView.findViewById(R.id.frame_rate_text);
    }

    @Override
    public void dettachController() {
        mRootView.removeView(mControlView);
    }

    @Override
    public void setSkinManager(SkinManager skin) {
        mSkinManager = skin;
        if(skin.hasSkin()) {
            String ciUri = skin.getCiUri();
            if(ciUri != null && ciUri.length() > 0) {
                Log.d(TAG, String.format("Logo '%s'", ciUri));
                if(ciUri.startsWith("http://")) {
                    try {
                        URL connectURL = new URL(ciUri);
                        InputStream is;
                        is = connectURL.openStream();
                        int length = is.available();
                        byte buffer[] = new byte[length];
                        is.read(buffer);
                        mCiView.setImageBitmap(BitmapFactory.decodeByteArray(buffer, 0, length));
                        mCiView.setVisibility(View.VISIBLE);

                        mNoTitleCiView.setImageBitmap(BitmapFactory.decodeByteArray(buffer, 0, length));
                        if(((BitmapDrawable)mNoTitleCiView.getDrawable()).getBitmap() != null )
                            mHasNoTitleCi = true;
                    } catch (MalformedURLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                else {
                    mCiView.setImageBitmap(BitmapFactory.decodeFile(ciUri));
                    mCiView.setVisibility(View.VISIBLE);

                    mNoTitleCiView.setImageBitmap(BitmapFactory.decodeFile(ciUri));
                    mHasNoTitleCi = true;
                }
            }

            mTitleView.setBackgroundColor(skin.getTitleBarBgColor());
            mTimeBar.setPlayedColor(skin.getProgressBarColor());
            mTimeBar.setProgressColor(skin.getProgressBarBgColor());
            mTimeBar.setIndexColor(skin.getIndexBarColor());
            mScreenShotHidden = !skin.getScreenShotEnable();
            mScreenShotView.setBackgroundColor(skin.getScreenShotBgColor());
            if(!skin.getControlbarEnable()) {
                mControlHidden = true;
                mCaption.setVisibility(View.GONE);
                mBookmark.setVisibility(View.GONE);
                mMuteView.setVisibility(View.GONE);
                mScreenSizeModeView.setVisibility(View.GONE);
                mSubControlView.setVisibility(View.GONE);
                mPlayingRateView.setVisibility(View.GONE);
                mBookmarkView.setVisibility(View.GONE);
                mTimeBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    @Override
    public void setCanReplay(boolean canReplay) {
        this.mCanReplay = canReplay;
    }

    @Override
    public void showPlaying() {
        Log.d(TAG, "showPlaying");
        mState = State.PLAYING;
        showMainView(mPlayPauseReplayView);
    }

    @Override
    public void showPaused() {
        mState = State.PAUSED;
        showMainView(mPlayPauseReplayView);
    }

    @Override
    public void showEnded() {
        mState = State.ENDED;
        showMainView(mPlayPauseReplayView);
    }

    @Override
    public void showLoading() {
        Log.d(TAG, "showLoading");
        mState = State.LOADING;
        mLoadingText.setText(R.string.initializing_str);
        mLoadingView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showBuffering() {
        Log.d(TAG, "showBuffering");
        mState = State.BUFFERING;
        mLoadingText.setText(R.string.buffering_str);
        showMainView(mLoadingView);
    }

    @Override
    public void showWaterMark() {
        Log.d(TAG, "showWaterMark");
        mWaterMarkView.setVisibility(View.VISIBLE);
        Animation blinkAnimation = AnimationUtils.loadAnimation(mContext, R.anim.blink);
        mWaterMarkView.startAnimation(blinkAnimation);
    }

    @Override
    public void showSkip(int sec) {
        SKIP_SHOW_SEC = sec*1000;
        mHandler.sendEmptyMessageDelayed(MSG_SKIP_SHOW, SKIP_NEED_SHOW);
    }

    @Override
    public void hideSkip() {
        mSkipLayer.setVisibility(View.GONE);
    }

    @Override
    public void showSeekingTime(int seekTo, int seekAmount) {
        mPlayCenterLayer.setVisibility(View.GONE);
        mVolumeLayout.setVisibility(View.GONE);
        mBrightLayout.setVisibility(View.GONE);
        mSeekTimeLayer.setVisibility(View.VISIBLE);
        String signText;
        if(seekAmount < 0) {
            seekAmount *= -1;
            signText = "-";
        }
        else {
            signText = "+";
        }
        mSeekTimeText.setText(Utils.stringForTime(seekTo));
        mSeekTimeAmountText.setText(String.format("(%s%s)", signText, Utils.stringForTime(seekAmount)));
    }

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            if(msg.what == MSG_SKIP_SHOW) {
                String text = mResources.getString(R.string.skip);
                mRemainSkipSec = SKIP_SHOW_SEC;
                text += " >>> " + (mRemainSkipSec/1000);
                mSkipView.setText(text);
                mSkipLayer.setVisibility(View.VISIBLE);
                mHandler.sendEmptyMessageDelayed(MSG_SKIP_CHECK, SKIP_INTERVAL);
            }
            else if(msg.what == MSG_SKIP_CHECK) {
                mRemainSkipSec -= SKIP_INTERVAL;
                String text = mResources.getString(R.string.skip);
                if(mRemainSkipSec > 0) {
                    text += " >> " + (mRemainSkipSec/1000);
                    mHandler.sendEmptyMessageDelayed(MSG_SKIP_CHECK, SKIP_INTERVAL);
                }
                mSkipView.setText(text);
            }
        }

    };

    @Override
    public void setOrientation(int orientation) {
//    	Log.d(TAG, String.format("setOrientation %d dimens (%d %d) count [%s] (%d %d) btn x %d",
//    			orientation,
//    			mRootView.getMeasuredWidth(), mRootView.getMeasuredHeight(),
//    			mBookmarkCountView.getText(),
//    			mBookmarkCountView.getLeft(), mBookmarkCountView.getMeasuredWidth(),
//    			mBookmarkBtnLayout.getLeft()));
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mBookmarkCountView.getLayoutParams();
        int rootCenter = mRootView.getMeasuredHeight()/2;
        int counterRight = rootCenter+mBookmarkCountView.getMeasuredWidth()/2;

        if(orientation == Configuration.ORIENTATION_LANDSCAPE)
//    	if(counterRight > mBookmarkBtnLayout.getLeft())
        {
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            params.addRule(RelativeLayout.CENTER_VERTICAL, 0);
            params.addRule(RelativeLayout.CENTER_IN_PARENT, 1);
        }
        else if(orientation == Configuration.ORIENTATION_PORTRAIT)
//    	else
        {
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
            params.addRule(RelativeLayout.CENTER_VERTICAL, 1);
            params.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
        }

        mBookmarkCountView.setLayoutParams(params);
    }

    @Override
    public void setTimes(int currentTime, int seekableTime, int totalTime,
                         int trimStartTime, int trimEndTime) {
        mTimeBar.setTime(currentTime, seekableTime, totalTime, trimStartTime, trimEndTime);

        if(mTimeShift) {
            if (mTimeBarCurrentTime != currentTime || mTimeBarTotalTime != totalTime) {
                if (currentTime == totalTime) {
                    mLiveView.setBackgroundResource(R.drawable.round_red);
                    mLiveView.setOnClickListener(null);
                } else {
                    mLiveView.setBackgroundResource(R.drawable.round_gray);
                    mLiveView.setOnClickListener(this);
                }
                mTimeBarCurrentTime = currentTime;
                mTimeBarTotalTime = totalTime;
            }
        }
    }

    @Override
    public void setTitleText(String title) {
        mTitle.setText(title);
    }

    @Override
    public void setPlayingRateText(double playing_rate) {
        mPlayingRateText.setText(String.format("%1.1fx", playing_rate));
    }

    @Override
    public void setPlayerTypeText(String type) {
        mPlayerText.setText(type);
    }

    @Override
    public void setCodecText(String codec) {
        mCodecText.setText(codec);
    }

    @Override
    public void setResolutionText(int width, int height) {
        mResolutionText.setText(String.format("%dx%d", width, height));
    }

    @Override
    public void setFrameRateText(int frameRate, int rejectRate) {
        mFrameRateText.setText(String.format("%d/%d", frameRate, rejectRate));
    }

    private void showMainView(View view) {
        show();
    }

    @Override
    public void show() {
        updateViews();
        mControllerShown = true;
//        mMainLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hide() {
        if(mTalkbackEnabled)
            return;

        mAVSyncView.setVisibility(View.GONE);
        mPlayCenterLayer.setVisibility(View.INVISIBLE);
        mLoadingView.setVisibility(View.INVISIBLE);
        mSubControlView.setVisibility(View.INVISIBLE);
        mPlayingRateView.setVisibility(View.INVISIBLE);
        mSeekTimeLayer.setVisibility(View.GONE);
        mTimeBar.setVisibility(View.INVISIBLE);
        if(mBookmarkHidden) {
            mTitleView.setVisibility(View.INVISIBLE);
            mBookmarkView.setVisibility(View.INVISIBLE);
            if(mHasNoTitleCi)
                mNoTitleCiView.setVisibility(View.VISIBLE);
            mControllerShown = false;
        }
    }

    @Override
    public boolean isShowing() {
//    	return (mMainLayout.getVisibility() == View.VISIBLE) && mBookmarkHidden;
        return mControllerShown && mBookmarkHidden && mCaptionHidden;
    }

    @Override
    public void setMoviePlayer(MoviePlayer parent) {
        mMoviePlayer = parent;
    }

    @Override
    public void setBluetoothConnectChanged(boolean connect) {
        mAVSyncVisible = connect;
        if(mAVSyncVisible) {
            if(isShowing())
                mAVSyncView.setVisibility(View.VISIBLE);
        }
        else {
            mAVSyncView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        if (mListener != null) {
            if(view == mLiveView) {
                mListener.onTimeShiftOff();
            }
            else if(view == mBtnAVSyncMinus || view == mBtnAVSyncPlus) {
                if(view == mBtnAVSyncMinus)
                    mAVSyncAmount -= 100;
                else
                    mAVSyncAmount += 100;
                String label = String.format("%1.1f%s", mAVSyncAmount/1000., mResources.getString(R.string.sec));
                mAVSyncText.setText(label);
                mListener.onAudioDelay(mAVSyncAmount);
            }
            else if (view == mPlayPauseReplayView) {
                if (mState == State.ENDED) {
                    if (mCanReplay) {
                        mListener.onReplay();
                    }
                } else if (mState == State.PAUSED || mState == State.PLAYING) {
                    if (mState == State.PAUSED)
                        showPlaying();
                    else
                        showPaused();
                    mListener.onPlayPause();
                }
            }
            else if (view == mRewView) {
                if (mState == State.PAUSED || mState == State.PLAYING) {
                    mListener.onRew();
                }
            }
            else if (view == mFfView) {
                if (mState == State.PAUSED || mState == State.PLAYING) {
                    mListener.onFf();
                }
            }
            else if(view == mScreenRotateLockView) {
                toggleScreenLock();
            }
            else if(view == mScreenSizeModeView) {
                toggleScreenSizeMode();
            }
            else if(view == mMuteView) {
                mListener.onToggleMute();
            }
            else if(view == mABRepeatView) {
                toggleRepeatAB();
            }
            else if(view == mRepeatView) {
                toggleRepeat();
            }
            else if(view == mPlayingRateUp) {
                mListener.onPlayingRate(1);
            }
            else if(view == mPlayingRateText) {
                mListener.onPlayingRate(0);
            }
            else if(view == mPlayingRateDown) {
                mListener.onPlayingRate(-1);
            }
            else if(view == mCaption) {
                if(mCaptionHidden) {
                    showCaption();
                }
                else {
                    hideCaption();
                }
            }
            else if(view == mVR) {
                mVR.setSelected(!mVR.isSelected());
                mListener.onVR(mVR.isSelected());
            }
            else if(view == mVR360) {
                mVR360.setSelected(!mVR360.isSelected());
                mListener.onVR360(mVR360.isSelected());
                if(mVR360.isSelected()) {
                    mVR.setVisibility(View.VISIBLE);
                }
                else {
                    mVR.setSelected(false);
                    mVR.setVisibility(View.GONE);
                }
            }
            else if(view == mCapture) {
                mListener.onScreenCapture();
            }
            else if(view == mBookmark) {
                if(mBookmarkHidden) {
                    showBookmark();
                }
                else {
                    hideBookmark();
                }
            }
            else if(view == mBookmarkAdd) {
                mListener.onBookmarkAdd();
            }
            else if(view == mBookmarkRemove) {
                mSelectBookmarkRemove = !mSelectBookmarkRemove;
                mBookmarkRemove.setSelected(mSelectBookmarkRemove);
                mListener.onBookmarkRemoveView();
            }
            else if(view == mSkipView) {
                if(mRemainSkipSec <= 0) {
                    mSkipLayer.setVisibility(View.GONE);
                    mListener.onSkip();
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        mListener.onBookmarkSeek(position);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        // TODO Auto-generated method stub
        if(mBookmarkKind == group)
            mListener.onBookmarkKind(checkedId);
        else if(mCaptionGroup == group) {
            if(checkedId == 0)
                mListener.onCaptionHide();
            else
                mListener.onCaptionSelected(checkedId-1);
        }
    }

    protected void updateViews() {
        if(mBookmarkHidden && mCaptionHidden) {
            mTitleView.setVisibility(View.VISIBLE);
            if(mAVSyncVisible)
                mAVSyncView.setVisibility(View.VISIBLE);
            mNoTitleCiView.setVisibility(View.INVISIBLE);
            if(!mControlHidden) {
                mSubControlView.setVisibility(View.VISIBLE);
                if(mLive) {
                    if(mTimeShift) {
                        mRewView.setVisibility(View.VISIBLE);
                        mFfView.setVisibility(View.VISIBLE);
                        mABRepeatView.setVisibility(View.VISIBLE);
                        mRepeatView.setVisibility(View.VISIBLE);
                        mTimeBar.setVisibility(View.VISIBLE);
                    }
                    else {
                        mRewView.setVisibility(View.GONE);
                        mFfView.setVisibility(View.GONE);
                        mABRepeatView.setVisibility(View.INVISIBLE);
                        mRepeatView.setVisibility(View.INVISIBLE);
                        mTimeBar.setVisibility(View.GONE);
                    }
                }
                else {
                    mABRepeatView.setVisibility(View.VISIBLE);
                    mRepeatView.setVisibility(View.VISIBLE);
                    mTimeBar.setVisibility(View.VISIBLE);
                    if(mMoviePlayer.supportPlaybackrateControl())
                        mPlayingRateView.setVisibility(View.VISIBLE);
                    else
                        mPlayingRateView.setVisibility(View.INVISIBLE);
                }
            }

            if(mState == State.LOADING || mState == State.BUFFERING) {
                mLoadingView.setVisibility(View.VISIBLE);
            }
            else {
                mLoadingView.setVisibility(View.INVISIBLE);
                mPlayPauseReplayView.setImageResource(
                        mState == State.PAUSED ? R.drawable.btn_play :
                                R.drawable.btn_pause);
                mPlayPauseReplayView.setContentDescription(mResources.getString(mState == State.PAUSED ? R.string.talkback_play:R.string.talkback_pause));
                mPlayCenterLayer.setVisibility(
                        (mState != State.ERROR &&
                                !(mState == State.ENDED && !mCanReplay))
                                ? View.VISIBLE : View.GONE);
                mSeekTimeLayer.setVisibility(View.GONE);
            }
        }
    }

    // TimeBar listener

    @Override
    public void onScrubbingStart() {
//        if(mBookmarkView.getVisibility() != View.VISIBLE)
        mListener.onSeekStart();
    }

    @Override
    public void onScrubbingMove(int time) {
        mListener.onSeekMove(time);
    }

    @Override
    public void onScrubbingEnd(int time, int trimStartTime, int trimEndTime) {
        mListener.onSeekEnd(time, trimStartTime, trimEndTime);
        mScreenShotView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void setScreenShotEnabled(boolean exist) {
        if(!mScreenShotHidden)
            mScreenShotExist = exist;
    }

    @Override
    public void setScreenShot(Bitmap bm, int time) {
        // TODO Auto-generated method stub
        mScreenShot.setImageBitmap(bm);
        if(time < 60*60*1000)
            mScreenShotTime.setText(Utils.stringForTimeMMSS(time));
        else
            mScreenShotTime.setText(Utils.stringForTimeHMMSS(time));

        if(SCREEN_SHOT_WIDTH < 0)
            SCREEN_SHOT_WIDTH = bm.getWidth()*SCREEN_SHOT_HEIGHT/bm.getHeight();

        int l = mTimeBar.getScrubberX()-SCREEN_SHOT_WIDTH/2;
        int r = l + SCREEN_SHOT_WIDTH;

        if(l<0) {
            l = 0;
        }
        else if(r > mTimeBar.getWidth()) {
            l = mTimeBar.getWidth()-SCREEN_SHOT_WIDTH;
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mScreenShotView.getLayoutParams();
        params.width = SCREEN_SHOT_WIDTH;
        params.height = SCREEN_SHOT_HEIGHT;
        params.leftMargin = l;
        mScreenShotView.setLayoutParams(params);
        mScreenShotView.setVisibility(View.VISIBLE);
    }

    @Override
    public void setSeekLabel(int maxX, int maxY, int x, int y, Bitmap bm, int time, boolean bShow) {
        // TODO Auto-generated method stub
        if(bShow) {
            if(bm == null) {
                mSeekScreenShotView.setVisibility(View.GONE);
                return;
            }
            else {
                mSeekScreenShot.setImageBitmap(bm);
            }

            if(time < 60*60*1000)
                mSeekScreenShotTime.setText(Utils.stringForTimeMMSS(time));
            else
                mSeekScreenShotTime.setText(Utils.stringForTimeHMMSS(time));

            if(SCREEN_SHOT_WIDTH < 0) {
                if(bm == null)
                    SCREEN_SHOT_WIDTH = SCREEN_SHOT_HEIGHT*4/3;
                else
                    SCREEN_SHOT_WIDTH = bm.getWidth()*SCREEN_SHOT_HEIGHT/bm.getHeight();
            }

            int l = x-SCREEN_SHOT_WIDTH/2;
            int t = y-SCREEN_SHOT_HEIGHT-mSeekScreenShotGap;

            if(l<0) {
                l = 0;
            }
            else if((l + SCREEN_SHOT_WIDTH) > maxX) {
                l = maxX-SCREEN_SHOT_WIDTH;
            }

            if(t<0) {
                t = y+mSeekScreenShotGap;
            }

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mSeekScreenShotView.getLayoutParams();
            params.width = SCREEN_SHOT_WIDTH;
            params.height = SCREEN_SHOT_HEIGHT;
            params.leftMargin = l;
            params.topMargin  = t;
            mSeekScreenShotView.setLayoutParams(params);

            if(mSeekScreenShotView.getVisibility() != View.VISIBLE)
                mSeekScreenShotView.setVisibility(View.VISIBLE);
        }
        else {
            mSeekScreenShotView.setVisibility(View.GONE);
            mSeekTimeLayer.setVisibility(View.GONE);
        }
    }

    @Override
    public void setSeekable(boolean enable) {
        mABRepeatView.setEnabled(enable);
        mRepeatView.setEnabled(enable);
        mTimeBar.setSeekable(enable);
        if(enable) {
            mRewView.setVisibility(View.VISIBLE);
            mFfView.setVisibility(View.VISIBLE);
        }
        else {
            mRewView.setVisibility(View.GONE);
            mFfView.setVisibility(View.GONE);
        }
    }

    @Override
    public void setLive(boolean bLive, boolean bTimeShift) {
        mLive = bLive;
        mTimeShift = bTimeShift;
        if(mLive) {
            mLiveView.setVisibility(View.VISIBLE);
            if(!mTimeShift) {
                mPlayingRateView.setVisibility(View.GONE);
                mABRepeatView.setVisibility(View.INVISIBLE);
                mRepeatView.setVisibility(View.INVISIBLE);
                mTimeBar.setVisibility(View.GONE);
            }
            mTimeBar.setTimeShift(bTimeShift);
        }
        else {
            mLiveView.setVisibility(View.GONE);
        }
    }

    @Override
    public void toggleScreenLock() {
        mScreenRotateLock = !mScreenRotateLock;
        if(mScreenRotateLock)
            mScreenRotateLockView.setBackgroundResource(R.drawable.rotation_lock_on);
        else
            mScreenRotateLockView.setBackgroundResource(R.drawable.rotation_lock);

        if(mListener != null)
            mListener.onScreenRotateLock(mScreenRotateLock);
    }

    @Override
    public void toggleScreenSizeMode() {
        if(mScreenSizeMode == KollusPlayerContentMode.ScaleAspectFit) {
            mScreenSizeMode = KollusPlayerContentMode.ScaleAspectFill;
            mScreenSizeModeView.setBackgroundResource(R.drawable.ic_screen_fullscreen_stretch);
            mScreenSizeModeView.setContentDescription(mResources.getString(R.string.talkback_screen_full_stretch));
        }
        else if(mScreenSizeMode == KollusPlayerContentMode.ScaleAspectFill) {
            mScreenSizeMode = KollusPlayerContentMode.ScaleAspectFillStretch;
            mScreenSizeModeView.setBackgroundResource(R.drawable.ic_screen_realsize);
            mScreenSizeModeView.setContentDescription(mResources.getString(R.string.talkback_screen_original));
        }
        else if(mScreenSizeMode == KollusPlayerContentMode.ScaleAspectFillStretch) {
            mScreenSizeMode = KollusPlayerContentMode.ScaleCenter;
            mScreenSizeModeView.setBackgroundResource(R.drawable.ic_screen_fit);
            mScreenSizeModeView.setContentDescription(mResources.getString(R.string.talkback_screen_fit));
        }
        else if(mScreenSizeMode == KollusPlayerContentMode.ScaleCenter) {
            mScreenSizeMode = KollusPlayerContentMode.ScaleAspectFit;
            mScreenSizeModeView.setBackgroundResource(R.drawable.ic_screen_fullscreen);
            mScreenSizeModeView.setContentDescription(mResources.getString(R.string.talkback_screen_full));
        }
        else  if(mScreenSizeMode == KollusPlayerContentMode.ScaleZoom) {
            mScreenSizeMode = KollusPlayerContentMode.ScaleAspectFit;
            mScreenSizeModeView.setBackgroundResource(R.drawable.ic_screen_fullscreen);
            mScreenSizeModeView.setContentDescription(mResources.getString(R.string.talkback_screen_full));
        }

        if(mListener != null)
            mListener.onScreenSizeMode(mScreenSizeMode);
    }

    @Override
    public void screenSizeScaleBegin() {
        mScreenSizeMode = KollusPlayerContentMode.ScaleZoom;
        mScreenSizeModeView.setBackgroundResource(R.drawable.ic_screen_fit);
    }

    @Override
    public void toggleRepeatAB() {
        if(mABRepeat == REPEAT_MODE_DISABLE) {
            setRepeatABImage(REPEAT_MODE_A);
        }
        else if(mABRepeat == REPEAT_MODE_A) {
            setRepeatABImage(REPEAT_MODE_B);
        }
        else if(mABRepeat == REPEAT_MODE_B) {
            setRepeatABImage(REPEAT_MODE_DISABLE);
        }

        if(mListener != null)
            mListener.onRepeatAB(mABRepeat);
    }

    public void setRepeatABImage(int mode) {
        if(mode == REPEAT_MODE_A) {
            mABRepeatView.setBackgroundResource(R.drawable.repeat_ab_a);
            mABRepeatView.setContentDescription(mResources.getString(R.string.talkback_ab_repeat_start));
            mABRepeat = REPEAT_MODE_A;
        }
        else if(mode == REPEAT_MODE_B) {
            mABRepeatView.setBackgroundResource(R.drawable.repeat_ab_b);
            mABRepeatView.setContentDescription(mResources.getString(R.string.talkback_ab_repeat_end));
            mABRepeat = REPEAT_MODE_B;
        }
        else if(mode == REPEAT_MODE_DISABLE) {
            mABRepeatView.setBackgroundResource(R.drawable.repeat_ab);
            mABRepeatView.setContentDescription(mResources.getString(R.string.talkback_ab_repeat));
            mABRepeat = REPEAT_MODE_DISABLE;
        }
    }

    public void resetRepeatABImage() {
        mABRepeatView.setBackgroundResource(R.drawable.repeat_ab);
        mABRepeatView.setContentDescription(mResources.getString(R.string.talkback_ab_repeat));
        mABRepeat = REPEAT_MODE_DISABLE;
        if(mListener != null)
            mListener.onRepeatAB(mABRepeat);
    }

    public void setRepeatAB(int repeatAMs, int repeatBMs) {
        mTimeBar.setRepeatAB(repeatAMs, repeatBMs);
    }

    @Override
    public void toggleRepeat() {
        mRepeat = !mRepeat;
        if(mRepeat) {
            mRepeatView.setBackgroundResource(R.drawable.repeat_on);
            mRepeatView.setContentDescription(mResources.getString(R.string.talkback_repeat_on));
        }
        else {
            mRepeatView.setBackgroundResource(R.drawable.repeat);
            mRepeatView.setContentDescription(mResources.getString(R.string.talkback_repeat_off));
        }

        if(mListener != null)
            mListener.onRepeat(mRepeat);
    }

    @Override
    public void setMute(boolean mute) {
        // TODO Auto-generated method stub
        if(mute) {
            mMuteView.setBackgroundResource(R.drawable.vol_mute_am);
            mMuteView.setContentDescription(mResources.getString(R.string.talkback_mute));
        }
        else {
            mMuteView.setBackgroundResource(R.drawable.vol_am);
            mMuteView.setContentDescription(mResources.getString(R.string.talkback_unmute));
        }
    }

    @Override
    public void supportVR360(boolean support) {
        if (support)
            mVR360.setVisibility(View.VISIBLE);
        else
            mVR360.setVisibility(View.GONE);
    }
}
