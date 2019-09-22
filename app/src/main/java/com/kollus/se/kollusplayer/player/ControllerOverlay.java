package com.kollus.se.kollusplayer.player;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ArrayAdapter;

import com.kollus.sdk.media.content.KollusBookmark;
import com.kollus.sdk.media.content.KollusContent;
import com.kollus.sdk.media.util.SkinManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

interface ControllerOverlay {


    interface Listener {
        void onPlayPause();
        void onRew();
        void onFf();
        void onSeekStart();
        void onSeekMove(int time);
        void onSeekEnd(int time, int trimStartTime, int trimEndTime);
        void onVR(boolean enable);
        void onVR360(boolean enable);
        void onScreenCapture();
        void onBookmarkKind(int type);
        void onBookmarkAdd();
        void onBookmarkRemoveView();
        void onBookmarkSeek(int position);
        void onCaptionSelected(int position);
        void onCaptionHide();
        void onShown();
        void onHidden();
        void onReplay();
        void onPlayingRate(int mode);
        void onBookmarkHidden();
        void onToggleMute();
        void onSkip();

        void onScreenRotateLock(boolean lock);
        void onScreenSizeMode(int mode);
        void onRepeatAB(int direction);
        void onRepeat(boolean enable);
        void onAudioDelay(int timeMs);
        void onTimeShiftOff();

        void onLayoutChange(View v, int left, int top, int right, int bottom);
    }

    public static final int REPEAT_MODE_DISABLE = 0;
    public static final int REPEAT_MODE_A = 1;
    public static final int REPEAT_MODE_B = 2;

    void dettachController();

    void setSkinManager(SkinManager skin);

    void setListener(Listener listener);

    void setCanReplay(boolean canReplay);

    void show();

    void showBookmark();

    void showCaption();

    void hide();

    void hideBookmark();

    void hideCaption();

    void setBookmarkList(ArrayList<KollusBookmark> list);

    void setBookmarkLableList(List<String> labels);

    void setBookmarkable(boolean isBookable);

    void setBookmarkAdapter(ArrayAdapter adapter);

    void setBookmarkSelected(int position);

    void setBookmarkCount(int type, int count);

    void setBookmarkWritable(boolean bWritable);

    void setDeleteButtonEnable(boolean enable);

    void setCaptionList(Vector<KollusContent.SubtitleInfo> list);

    boolean isShowing();

    void showPlaying();

    void showPaused();

    void showEnded();

    void showLoading();

    void showBuffering();

    void showWaterMark();

    void showSkip(int sec);

    void hideSkip();

    void showSeekingTime(int seekTo, int seekAmount);

    void setSeekable(boolean enable);

    void setLive(boolean bLive, boolean bTimeShift);

    void setOrientation(int orientation);

    void setTitleText(String title);

    void setMute(boolean mute);

    void setScreenShotEnabled(boolean exist);

    void setScreenShot(Bitmap bm, int time);

    void setTimes(int currentTime, int cachedTime, int totalTime,
                  int trimStartTime, int trimEndTime);

    void setPlayingRateText(double playing_rate);

    void setMoviePlayer(MoviePlayer parent);

    void setVolumeLabel(int level);

    void setSeekLabel(int maxX, int maxY, int x, int y, Bitmap bm, int time, boolean bShow);

    void setBrightnessLabel(int level);

    void setPlayerTypeText(String type);

    void setCodecText(String codec);

    void setResolutionText(int width, int height);

    void setFrameRateText(int frameRate, int rejectRate);

    void toggleScreenLock();

    void toggleScreenSizeMode();

    void screenSizeScaleBegin();

    void toggleRepeatAB();

    void toggleRepeat();

    int getProgressbarHeight();

    void supportVR360(boolean support);

    void setTalkbackEnabled(boolean bTalkback);

    void setBluetoothConnectChanged(boolean connect);
}
