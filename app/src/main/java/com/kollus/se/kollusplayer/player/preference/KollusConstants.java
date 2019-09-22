package com.kollus.se.kollusplayer.player.preference;

import android.content.Context;

import com.kollus.sdk.media.util.Utils;

public class KollusConstants {
    public static final String KEY = "11060daaf8cc998ae10fc07c1614cf5a78b8c348";
    public static final String EXPIRE_DATE = "2019/07/31";

    public static final int SERVER_PORT = 8388;
//    public static final boolean SUPPORT_VR      = false;
    public static final boolean SUPPORT_SCREEN_CAPTURE  = false;

    public static final boolean SUPPORT_VIDEO_WATER_MARK = true;

    public static final int BASE_NETWORK_TIMEOUT_SEC     = 10;
    public static final int BASE_NETWORK_RETRY_COUNT     = 3;

    public static int NETWORK_TIMEOUT_SEC     = BASE_NETWORK_TIMEOUT_SEC;
    public static int NETWORK_RETRY_COUNT     = BASE_NETWORK_RETRY_COUNT;
    public static final boolean SECURE_MODE          = true;

    public static final int MAX_SW_VOLUME = 15;

    public static String getPlayerId(Context context) {
        String playerId;
//        if(Log.isDebug())
//            playerId = "febb933cbcf041c9c996a9d6e38b24adc0c2adb6";
//        else
            playerId = Utils.createUUIDSHA1(context);

        return playerId;
    }

    public static String getPlayerIdWithMD5(Context context) {
        String playerIdWidthMd5;
//        if(Log.isDebug())
//            playerIdWidthMd5 = "57589c1e8f046812e4a4f518d57ff929";
//        else
            playerIdWidthMd5 = Utils.createUUIDMD5(context);
        return playerIdWidthMd5;
    }
}
