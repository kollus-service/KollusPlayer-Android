/*
 * Copyright (C) 2011 The Android Open Source Project
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
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Handler;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
//
//import kollus.test.media.util.DisplayUtil;
//import kollus.test.media.view.CaptionControlButton;
//import kollus.test.media.view.SegmentedControlButton;

import com.kollus.sdk.media.content.KollusBookmark;
import com.kollus.sdk.media.content.KollusContent.SubtitleInfo;
import com.kollus.sdk.media.util.Log;
import kollus.test.media.R;
import kollus.test.media.player.util.DisplayUtil;
import kollus.test.media.player.view.CaptionControlButton;
import kollus.test.media.player.view.SegmentedControlButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static android.content.Context.WINDOW_SERVICE;

/**
 * The playback controller for the Movie Player.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MovieControllerOverlay extends CommonControllerOverlay implements
        AnimationListener, OnKeyListener {
    private static final String TAG = "MovieControllerOverlay";
    private final int SHOW_CONTROL_DURATION = 10000;
    //화면 전환 예외처리 모델 정의
    public static final String BUILD_MODEL_NEXUS_6P = "Nexus 6P";
    public static final int BUILD_SDK_VERSION = 25;

    private View mRootView;
    private boolean hidden = false;

    private final Handler handler;
    private final Runnable startHidingRunnable;
    private final Animation hideAnimation;

    private final Runnable startVolumeHidingRunnable;
    private final Animation volumeHideAnimation;
    private final Runnable startBrightHidingRunnable;
    private final Animation brightHideAnimation;
    private List<String> mBookmarkLabels;
    private boolean mBookmarkable;

    //etlim fixed. 20170808 navigation bar orientation issue, nexus P6.
    private int mNavigationBarPortraitHeight;

    public MovieControllerOverlay(Context context, ViewGroup rootView) {
        super(context, rootView);
        Log.d(TAG, "MovieControllerOverlay Creator");
        mRootView = rootView;
//        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
//        	mRootView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
//        		@Override
//        		public void onSystemUiVisibilityChange(int visibility) {
//        			// TODO Auto-generated method stub
//        			if((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) {
//        				hideUI();
//        			}
//        			else {
//        				showUI();
//        			}
//        		}
//        	});
//        }
//
//        mMainLayout.setOnTouchListener(this);
        mMainLayout.setOnKeyListener(this);
        mTimeBar.setListener(this);
        handler = new Handler();
        startHidingRunnable = new Runnable() {
            @Override
            public void run() {
                startHiding();
            }
        };

        startVolumeHidingRunnable = new Runnable() {
            @Override
            public void run() {
                if (mVolumeLayout.getVisibility() == View.VISIBLE) {
                    mVolumeLayout.startAnimation(volumeHideAnimation);
                }
            }
        };

        startBrightHidingRunnable = new Runnable() {
            @Override
            public void run() {
                if (mBrightLayout.getVisibility() == View.VISIBLE) {
                    mBrightLayout.startAnimation(brightHideAnimation);
                }
            }
        };

        hideAnimation = AnimationUtils.loadAnimation(context, R.anim.player_out);
        hideAnimation.setAnimationListener(this);

        volumeHideAnimation = AnimationUtils.loadAnimation(context, R.anim.player_out);
        volumeHideAnimation.setAnimationListener(this);

        brightHideAnimation = AnimationUtils.loadAnimation(context, R.anim.player_out);
        brightHideAnimation.setAnimationListener(this);

        //etlim fixed. 20170808 navigation bar orientation issue, nexus P6.
        int navigation_portrait_id = mResources
                .getIdentifier("navigation_bar_height", "dimen", "android");
        if (navigation_portrait_id > 0) {
            mNavigationBarPortraitHeight = mResources.getDimensionPixelSize(navigation_portrait_id);
        }

        hide();
    }

    private void hideUI() {
        Log.d(TAG, "Control >>> hideUI");
        boolean wasHidden = hidden;
        hidden = true;
        super.hide();
        if (mListener != null && wasHidden != hidden) {
            mListener.onHidden();
        }
    }

    private void showUI() {
        Log.d(TAG, "Control >>> showUI");
        boolean wasHidden = hidden;
        hidden = false;
        mSelectBookmarkRemove = false;
        mBookmarkRemove.setSelected(mSelectBookmarkRemove);
        super.show();
        if (mListener != null && wasHidden != hidden) {
            mListener.onShown();
        }
        maybeStartHiding();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setSystemUiVisibility(boolean visible) {
        Log.d(TAG, "setSystemUiVisibility:" + visible);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mRootView.getLayoutParams();
        int newVis = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if (visible) {
            DisplayMetrics dm = new DisplayMetrics();
            WindowManager wm = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(dm);
            params.width = dm.widthPixels;
            params.height = dm.heightPixels;

            if (BUILD_MODEL_NEXUS_6P.equals(Build.MODEL)
                    && BUILD_SDK_VERSION < Build.VERSION.SDK_INT) {
                if (DisplayUtil.getOrientation(mContext) == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                    params.setMargins(mNavigationBarPortraitHeight, 0, 0, 0);
                } else {
                    params.setMargins(0, 0, 0, 0);
                }
            } else {
                params.setMargins(0, 0, 0, 0);
            }
        } else {
            newVis |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
            params.width = RelativeLayout.LayoutParams.FILL_PARENT;
            params.height = RelativeLayout.LayoutParams.FILL_PARENT;
        }

        mRootView.setLayoutParams(params);
        mRootView.setSystemUiVisibility(newVis);
    }

    @Override
    public void setOrientation(int orientation) {
        super.setOrientation(orientation);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setSystemUiVisibility((mRootView.getSystemUiVisibility() & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
                    != View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    @Override
    public void hide() {
        Log.d(TAG, "Control >>> hide");
        if (mTalkbackEnabled)
            return;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            hideUI();
        else {
            setSystemUiVisibility(false);
            hideUI();
        }
    }

    @Override
    public void hideBookmark() {
        mBookmarkView.setVisibility(View.GONE);
        mBookmarkHidden = true;
        mBookmark.setSelected(!mBookmarkHidden);

        if (mListener != null)
            mListener.onBookmarkHidden();
    }

    @Override
    public void hideCaption() {
        mCaptionListLayout.setVisibility(View.GONE);
        mCaptionHidden = true;
        mCaption.setSelected(!mCaptionHidden);
    }

    @Override
    public void show() {
        Log.d(TAG, "Control >>> show");
        if (mState == State.LOADING)
            return;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            showUI();
        } else {
            setSystemUiVisibility(true);
            showUI();
        }
    }

    @Override
    public void showBookmark() {
        cancelHiding();
        mPlayCenterLayer.setVisibility(View.INVISIBLE);
        mSubControlView.setVisibility(View.GONE);
        mPlayingRateView.setVisibility(View.GONE);
        mScreenShotView.setVisibility(View.GONE);
        mTimeBar.setVisibility(View.GONE);

        mBookmarkView.setVisibility(View.VISIBLE);
        mBookmarkHidden = false;
        mBookmark.setSelected(!mBookmarkHidden);

        mCaptionListLayout.setVisibility(View.GONE);
        mCaptionHidden = true;
        mCaption.setSelected(!mCaptionHidden);
    }

    @Override
    public void showCaption() {
        cancelHiding();
        mPlayCenterLayer.setVisibility(View.INVISIBLE);
        mSubControlView.setVisibility(View.GONE);
        mPlayingRateView.setVisibility(View.GONE);
        mTimeBar.setVisibility(View.GONE);
        mBookmarkView.setVisibility(View.GONE);

        mCaptionListLayout.setVisibility(View.VISIBLE);
        mCaptionHidden = false;
        mCaption.setSelected(!mCaptionHidden);

        mBookmarkView.setVisibility(View.GONE);
        mBookmarkHidden = true;
        mBookmark.setSelected(!mBookmarkHidden);
    }

    @Override
    public void setBookmarkable(boolean isBookable) {
        mBookmarkable = isBookable;
        if(mBookmarkable && !mTalkbackEnabled) {
            if(mSkinManager == null)
                mBookmark.setVisibility(View.VISIBLE);
            else if(mSkinManager.getControlbarEnable())
                mBookmark.setVisibility(View.VISIBLE);
        }
        else {
            mBookmark.setVisibility(View.GONE);
        }
    }

    @Override
    public void setBookmarkAdapter(ArrayAdapter adapter) {
    	mBookmark.setVisibility(View.VISIBLE);
        mBookmarkListView.setAdapter(adapter);
    }

    @Override
    public void setBookmarkSelected(int position) {

        mBookmarkListView.setSelected(position);
    }

    @Override
    public void setBookmarkCount(int type, int count) {
//    	String countText = String.format("%s <font color='#2e7eb4'>%d/%d</font>",
//    			mResources.getString(R.string.bookmark), count, BookmarkInfo.MAX_BOOKMARK);
        if (mBookmarkLabels.size() > 1) {
            int childCount = mBookmarkKind.getChildCount();
            for (int i = 0; i < childCount; i++) {
                SegmentedControlButton child = (SegmentedControlButton) mBookmarkKind.getChildAt(i);
                Log.d(TAG, String.format("setBookmarkCount type %d count %d childId %d", type, count, child.getId()));
                if (child.getId() == type) {
                    child.setCount(count);

                    if (mBookmarkable) {
                        if (mBookmarkKind.getVisibility() == View.VISIBLE && type > 1) {
                            mBookmarkAdd.setVisibility(View.GONE);
                            mBookmarkRemove.setVisibility(View.GONE);
                        } else {
                            mBookmarkAdd.setVisibility(View.VISIBLE);
                            mBookmarkRemove.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        } else {
            String countText = mBookmarkLabels.get(type);
            countText += String.format(" <font color='#2e7eb4'>%d</font>", count);
            mBookmarkCountView.setText(Html.fromHtml(countText));
        }
    }

    @Override
    public void setBookmarkWritable(boolean bWritable) {
        mBookmarkable = bWritable;
        if (bWritable) {
            mBookmarkAdd.setVisibility(View.VISIBLE);
            mBookmarkRemove.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setBookmarkList(ArrayList<KollusBookmark> list) {
        mTimeBar.setBookmarkList(list);
    }

    @Override
    public void setBookmarkLableList(List<String> labels) {
        mBookmarkLabels = labels;
        for (String label : mBookmarkLabels) {
            SegmentedControlButton btn = new SegmentedControlButton(mContext);
            btn.setId(mBookmarkKind.getChildCount());
            btn.setText(label);
            mBookmarkKind.addView(btn);
            if (mBookmarkKind.getChildCount() == 1)
                btn.setChecked(true);
        }

        if (mBookmarkKind.getChildCount() <= 1)
            mBookmarkKind.setVisibility(View.GONE);
        else {
            for (int i = 0; i < mBookmarkKind.getChildCount(); i++) {
                SegmentedControlButton btn = (SegmentedControlButton) mBookmarkKind.getChildAt(i);
                if (i == 0)
                    btn.setBackgroundResource(R.drawable.bookmark_tab_press_left, R.drawable.bookmark_tab_normal_left);
                else if ((i + 1) == mBookmarkKind.getChildCount())
                    btn.setBackgroundResource(R.drawable.bookmark_tab_press_right, R.drawable.bookmark_tab_normal_right);
                else
                    btn.setBackgroundResource(R.drawable.bookmark_tab_press_center, R.drawable.bookmark_tab_noraml_center);
            }
            mBookmarkCountView.setVisibility(View.GONE);
        }
    }

    @Override
    public void setCaptionList(Vector<SubtitleInfo> list) {
        mCaptionGroup.removeAllViews();
        if (list != null && list.size() > 0) {
            if (!mTalkbackEnabled)
                mCaption.setVisibility(View.VISIBLE);
            CaptionControlButton btn = new CaptionControlButton(mContext);
            btn.setId(mCaptionGroup.getChildCount());
            btn.setText(R.string.hide_caption);
            mCaptionGroup.addView(btn);
            for (SubtitleInfo info : list) {
                btn = new CaptionControlButton(mContext);
                btn.setId(mCaptionGroup.getChildCount());
                btn.setLanguageCode(info.languageCode);
                btn.setText(info.name);
                mCaptionGroup.addView(btn);
                if (mCaptionGroup.getChildCount() == 2) {
                    btn.setChecked(true);
                }
            }
        } else {
            mCaption.setVisibility(View.GONE);
        }
    }

    public void setState(State state) {
        mState = state;
        if (mControllerShown)
            show();
    }

    @Override
    public void setTalkbackEnabled(boolean bTalkback) {
        mTalkbackEnabled = bTalkback;
        if (mCaptionGroup.getChildCount() > 0 && !mTalkbackEnabled) {
            mCaption.setVisibility(View.VISIBLE);
        } else {
            mCaption.setVisibility(View.GONE);
        }

        if (mBookmarkable && !mTalkbackEnabled) {
            if (mSkinManager == null)
                mBookmark.setVisibility(View.VISIBLE);
            else if (mSkinManager.getControlbarEnable())
                mBookmark.setVisibility(View.VISIBLE);
        } else {
            mBookmark.setVisibility(View.GONE);
        }
    }

    private void maybeStartHiding() {
        cancelHiding();
        if (mState == State.PLAYING && !mTalkbackEnabled) {
            handler.postDelayed(startHidingRunnable, SHOW_CONTROL_DURATION);
        }
    }

    private void maybeStartVolumeHiding() {
        handler.removeCallbacks(startVolumeHidingRunnable);
        mVolumeLayout.setAnimation(null);

        handler.removeCallbacks(startBrightHidingRunnable);
        mBrightLayout.setAnimation(null);
        mBrightLayout.setVisibility(View.GONE);

        handler.postDelayed(startVolumeHidingRunnable, 1500);
    }

    private void maybeStartBrightHiding() {
        handler.removeCallbacks(startVolumeHidingRunnable);
        mVolumeLayout.setAnimation(null);
        mVolumeLayout.setVisibility(View.GONE);

        handler.removeCallbacks(startBrightHidingRunnable);
        mBrightLayout.setAnimation(null);

        handler.postDelayed(startBrightHidingRunnable, 1500);
    }

    @Override
    public void setDeleteButtonEnable(boolean enable) {
        mSelectBookmarkRemove = enable;
        mBookmarkRemove.setSelected(mSelectBookmarkRemove);
    }

    @Override
    public void setVolumeLabel(int level) {
        maybeStartVolumeHiding();

        if (mVolumeLayout.getVisibility() != View.VISIBLE)
            mVolumeLayout.setVisibility(View.VISIBLE);
        if (level == 0)
            mVolumeView.setEnabled(false);
        else
            mVolumeView.setEnabled(true);
        mVolumeString.setText("" + level);
    }

    @Override
    public void setBrightnessLabel(int level) {
        maybeStartBrightHiding();

        if (mBrightLayout.getVisibility() != View.VISIBLE)
            mBrightLayout.setVisibility(View.VISIBLE);
        mBrightString.setText("" + level);
    }

    @Override
    public int getProgressbarHeight() {
        return mTimeBar.getBarHeight();
    }

    private void startHiding() {
        if (mBookmarkHidden && mCaptionHidden) {
            startHideAnimation(mTitleView);
            if (mHasNoTitleCi)
                mNoTitleCiView.setVisibility(View.VISIBLE);
        }
        startHideAnimation(mAVSyncView);
        startHideAnimation(mPlayCenterLayer);
        startHideAnimation(mSubControlView);
        startHideAnimation(mPlayingRateView);
        startHideAnimation(mScreenShotView);
        startHideAnimation(mTimeBar);
    }

    private void startHideAnimation(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            view.startAnimation(hideAnimation);
        }
    }

    private void cancelHiding() {
        handler.removeCallbacks(startHidingRunnable);
        mTitleView.setAnimation(null);
        mTimeBar.setAnimation(null);
        mPlayCenterLayer.setAnimation(null);
        mSubControlView.setAnimation(null);
        mPlayingRateView.setAnimation(null);
        mScreenShotView.setAnimation(null);
    }

    @Override
    public void onAnimationStart(Animation animation) {
        // Do nothing.
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        // Do nothing.
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if (animation == hideAnimation)
            hide();
        else if (animation == volumeHideAnimation)
            mVolumeLayout.setVisibility(View.GONE);
        else if (animation == brightHideAnimation)
            mBrightLayout.setVisibility(View.GONE);
    }

    @Override
    protected void updateViews() {
        if (hidden) {
            return;
        }
        super.updateViews();
    }

    // TimeBar listener

    @Override
    public void onScrubbingStart() {
        cancelHiding();
        super.onScrubbingStart();
    }

    @Override
    public void onScrubbingMove(int time) {
        cancelHiding();
        super.onScrubbingMove(time);
    }

    @Override
    public void onScrubbingEnd(int time, int trimStartTime, int trimEndTime) {
        maybeStartHiding();
        super.onScrubbingEnd(time, trimStartTime, trimEndTime);
    }

//	@Override
//	public boolean onTouch(View v, MotionEvent event) {
//		// TODO Auto-generated method stub
//        if(event.getAction() == MotionEvent.ACTION_DOWN) {
//        	if(mBookmarkView.getVisibility() != View.VISIBLE) {
//		        if(hidden)
//		        	show();
//		        else
//		        	hide();
//        	}
//        }
//		return false;
//	}

    @Override
    public void onClick(View view) {
        super.onClick(view);

        if (mBookmarkHidden && mCaptionHidden) {
            show();
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (hidden) {
            show();
        }
        return true;
    }
}
