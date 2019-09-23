package kollus.test.media.player.preference;

import androidx.annotation.IntDef;

public class ValuePreference {
    @IntDef({
            DOUBLE_TAB_SCREEN_SIZE,
            DOUBLE_TAB_PLAY_PAUSE
    })
    public @interface DOUBLE_TAB_MODE {}

    public static final int DOUBLE_TAB_SCREEN_SIZE = 0;
    public static final int DOUBLE_TAB_PLAY_PAUSE = 1;
}
