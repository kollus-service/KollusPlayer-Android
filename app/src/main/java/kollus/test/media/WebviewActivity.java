package kollus.test.media;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import androidx.annotation.Nullable;
import kollus.test.media.kollusapi.VideoUrlCreator;
import kollus.test.media.player.MovieActivity;

public class WebviewActivity extends KollusBaseActivity {
    private static final String TAG = WebviewActivity.class.getSimpleName();
    private WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        webView = (WebView) findViewById(R.id.wvList);

        loadWebViewDatafinal(webView, "file:///android_asset/KollusWebview/index.html");
    }

    private void loadWebViewDatafinal(WebView wv, String url) {
        WebView.setWebContentsDebuggingEnabled(true);
        WebSettings ws = wv.getSettings();

        ws.setJavaScriptEnabled(true);


        ws.setAllowFileAccess(true);
        ws.setAllowFileAccessFromFileURLs(true);
        ws.setAllowUniversalAccessFromFileURLs(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            ws.setDomStorageEnabled(true);
        }

        wv.setWebChromeClient(new WebChromeClient());
        wv.addJavascriptInterface(new WebBridge(), "kollusHandler");
        wv.loadUrl(url);
    }
    class WebBridge {
        @JavascriptInterface
        public void play(String mck){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), MovieActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP| Intent.FLAG_ACTIVITY_NEW_TASK);
                    Uri play_uri = null;

                    try {
                        play_uri = VideoUrlCreator.createUrl("catenoid", mck);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    intent.setData(play_uri);
                    startActivity(intent);
                }
            });
        }
    }
}
