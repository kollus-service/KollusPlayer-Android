package kollus.test.media.kollusapi.jwt;

import android.util.Base64;
import android.util.Log;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class JwtUtil {
    private final String TAG = JwtUtil.class.getSimpleName();
    public String createJwt(final String headerJson, final String payloadJson, String secretKey)
            throws NoSuchAlgorithmException, InvalidKeyException {
        String header = Base64.encodeToString(headerJson.getBytes(StandardCharsets.UTF_8), Base64.URL_SAFE|Base64.NO_PADDING|Base64.NO_WRAP);
        String payload = Base64.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8), Base64.URL_SAFE|Base64.NO_PADDING|Base64.NO_WRAP);
        String content = String.format("%s.%s", header, payload);
        final Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signatureBytes = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.encodeToString(signatureBytes, Base64.URL_SAFE|Base64.NO_PADDING|Base64.NO_WRAP);
        Log.e(TAG, String.format("%s.%s", content, signature));
        return String.format("%s.%s", content, signature);
    }
    public String createJwt(final String payloadJson, String secretKey)
            throws InvalidKeyException, NoSuchAlgorithmException {
        String headerJson = "{\"alg\": \"HS256\",\"typ\": \"JWT\"}";
        return createJwt(headerJson, payloadJson, secretKey);
    }



    public String[] splitJwt(String jwt) throws Exception{
        String[] parts = jwt.split("\\.");
        if (parts.length == 2 && jwt.endsWith(".")) {
            parts = new String[] { parts[0], parts[1], "" };
        }
        if (parts.length != 3) {
            throw new Exception(String.format("The token was expected to have 3 parts, but got %s.", parts.length));
        }
        return parts;
    }
    public String[] decodeJwt(String jwt) throws Exception {

        String[] parts = splitJwt(jwt);
        String headerJson = new String(Base64.decode(parts[0], Base64.URL_SAFE));
        String payloadJson =new String(Base64.decode(parts[0], Base64.URL_SAFE));
        String signature = parts[2];
        return new String[]{headerJson, payloadJson, signature};
    }
    public boolean verify(String secretKey, String jwt) throws Exception{
        String[] parts = splitJwt(jwt);
        byte[] contentBytes = String.format("%s.%s", parts[0], parts[1]).getBytes(StandardCharsets.UTF_8);
        byte[] signatureBytes = Base64.decode(parts[2], Base64.URL_SAFE);

        final Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] newSignatureBytes = mac.doFinal(contentBytes);
        return MessageDigest.isEqual(newSignatureBytes, signatureBytes);

    }
}
