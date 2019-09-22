package com.kollus.se.kollusplayer.player.preference;

import android.support.annotation.IntDef;

public class ValuePreference {
    @IntDef({
            DOUBLE_TAB_SCREEN_SIZE,
            DOUBLE_TAB_PLAY_PAUSE
    })
    public @interface DOUBLE_TAB_MODE {}

    public static final int DOUBLE_TAB_SCREEN_SIZE = 0;
    public static final int DOUBLE_TAB_PLAY_PAUSE = 1;
}
