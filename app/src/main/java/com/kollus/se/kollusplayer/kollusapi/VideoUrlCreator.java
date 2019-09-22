package com.kollus.se.kollusplayer.kollusapi;

import android.net.Uri;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kollus.se.kollusplayer.constant.KollusConstant;
import com.kollus.se.kollusplayer.kollusapi.jwt.JwtGenerator;
import com.kollus.se.kollusplayer.kollusapi.jwt.McGenerator;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class VideoUrlCreator {
    public static Uri createUrl(String cuid, String mck) throws NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
        String uri = "";
        String strToken = "";
        McGenerator mcGenerator = new McGenerator();
        mcGenerator.mckey(mck);
        JwtGenerator jwtGenerator = new JwtGenerator();
        String token = jwtGenerator.cuid(cuid)
                .expt(System.currentTimeMillis() + KollusConstant.KOLLUS_JWT_EXPT)
                .mc(mcGenerator.build())
                .secret_key(KollusConstant.KOLLUS_SECRET_KEY)
                .generate();
        uri = String.format("https://v.kr.kollus.com/si?jwt=%s&custom_key=%s", strToken, KollusConstant.KOLLUS_CUSTOM_KEY);
        return Uri.parse(uri);
    }
}
