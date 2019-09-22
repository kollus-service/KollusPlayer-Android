package com.kollus.se.kollusplayer;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import com.kollus.sdk.media.KollusStorage;
import com.kollus.sdk.media.util.ErrorCodes;
import com.kollus.sdk.media.util.Utils;
import com.kollus.se.kollusplayer.constant.KollusConstant;

public class KollusBaseActivity extends Activity {

    private final String TAG = KollusBaseActivity.class.getSimpleName();
    protected KollusStorage kollusStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        ConnectivityManager manager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        initStorage();
    }

    protected void initStorage() {
        kollusStorage = KollusStorage.getInstance(getApplicationContext());
        if(!kollusStorage.isReady()){
            int ret = kollusStorage.initialize(KollusConstant.KOLLUS_SDK_KEY, KollusConstant.KOLLUS_SDK_EXPIRE_DATE, KollusBaseActivity.this.getPackageName());
            if(ret != ErrorCodes.ERROR_OK){
                Log.e(TAG, "Kollus Storage Init Fail");
                if(ret != ErrorCodes.ERROR_EXPIRED_KEY){
                    Log.e(TAG, "Kollus SDK Key Expired");
                }
                return ;
            }

            kollusStorage.setDevice(
                    Utils.getStoragePath(KollusBaseActivity.this),
                    Utils.createUUIDSHA1(KollusBaseActivity.this),
                    Utils.createUUIDMD5(KollusBaseActivity.this),
                    Utils.isTablet(KollusBaseActivity.this));
        }
        kollusStorage.setNetworkTimeout(KollusConstant.NETWORK_TIMEOUT_SEC, KollusConstant.NETWORK_RETRY_COUNT);


    }
}
