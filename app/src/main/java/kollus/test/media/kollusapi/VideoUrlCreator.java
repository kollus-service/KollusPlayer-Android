package kollus.test.media.kollusapi;

import android.net.Uri;
import android.util.Base64;

import com.fasterxml.jackson.core.JsonProcessingException;
import kollus.test.media.constant.KollusConstant;
import kollus.test.media.kollusapi.jwt.JwtGenerator;
import kollus.test.media.kollusapi.jwt.McGenerator;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class VideoUrlCreator {
    public static Uri createUrl(String cuid, String mck) throws NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException, UnsupportedEncodingException {
        String play_uri = "";
        String strToken = "";
        McGenerator mcGenerator = new McGenerator();
        mcGenerator.mckey(mck);
        JwtGenerator jwtGenerator = new JwtGenerator();
        String token = jwtGenerator.cuid(cuid)
                .expt(System.currentTimeMillis() + KollusConstant.KOLLUS_JWT_EXPT)
                .mc(mcGenerator.build())
                .secret_key(KollusConstant.KOLLUS_SECRET_KEY)
                .generate();
        play_uri = String.format("https://v.kr.kollus.com/si?jwt=%s&custom_key=%s",token, KollusConstant.KOLLUS_CUSTOM_KEY);

        return Uri.parse(String.format("kollus://path?url=%s", play_uri));
//        return Uri.parse(String.format("kollus://path?url=%s", URLEncoder.encode(play_uri, "utf-8")));
//        return Uri.parse(String.format("kollus://path?url=%s", Base64.encodeToString(play_uri.getBytes(Charset.forName("UTF-8")),Base64.URL_SAFE)));
//        return Uri.parse(Base64.encodeToString(play_uri.getBytes(Charset.forName("UTF-8")), Base64.URL_SAFE | Base64.NO_PADDING));
//        return Uri.parse(play_uri);
    }
}
