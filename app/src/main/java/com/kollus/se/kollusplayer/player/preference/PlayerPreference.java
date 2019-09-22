package com.kollus.se.kollusplayer.player.preference;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;

import com.kollus.sdk.media.KollusStorage;
import com.kollus.sdk.media.util.Log;
import com.kollus.se.kollusplayer.KollusBaseActivity;
import com.kollus.se.kollusplayer.R;

public class PlayerPreference extends KollusBaseActivity {
	private final String TAG = PlayerPreference.class.getSimpleName();

	private CpuInfoPreference mCpuInfo;
	private PlayerInfoPreference mPlayerInfo;
	private GeneralPreference mGeneralInfo;
	private CaptionPreference mCaptionInfo;
	private SortPreference mSortInfo;
	private StoragePreference mStorageInfo;
	private GuidePreference mGuideInfo;

	//etlim 20170902 Activity Exit ==> Broadcast Event
	private String ACTION_ACTIVITY_FINISH_PLAYER_PREFERENCE = "kollus.test.media.action.activity.finish.player.preference";
	private PlayerPreferenceActivityBroadcastReceiver mPlayerPreferenceActivityBR;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.setting_layout);

		//etlim 20170902 Activity Exit ==> Broadcast Event
		PlayerPreferenceActivityBroadcastRegister();

        ImageView btn = (ImageView)findViewById(R.id.btn_back);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				setResult(RESULT_OK, getIntent());
				finish();
			}			
		});
		
		//PlayRatePreference rate = new PlayRatePreference(this);
		mGeneralInfo = new GeneralPreference(this);
		mCaptionInfo = new CaptionPreference(this);
		mSortInfo = new SortPreference(this);
		mStorageInfo = new StoragePreference(this);
		mCpuInfo = new CpuInfoPreference(this);
		mPlayerInfo = new PlayerInfoPreference(this);
		mGuideInfo = new GuidePreference(this);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
		mStorageInfo.invalidate();
	}

	//etlim 20170902 Activity Exit ==> Broadcast Event
	@Override
	protected void onDestroy() {
		unregisterReceiver(mPlayerPreferenceActivityBR);
		kollusStorage = KollusStorage.getInstance(this);
		super.onDestroy();
	}

	private void PlayerPreferenceActivityBroadcastRegister() {
		mPlayerPreferenceActivityBR = new PlayerPreferenceActivityBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_ACTIVITY_FINISH_PLAYER_PREFERENCE);
		registerReceiver(mPlayerPreferenceActivityBR, filter);
	}

	private class PlayerPreferenceActivityBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context ctx, Intent intent) {
			Log.d(TAG, "onReceive >>> " + intent.getAction());
			String action = intent.getAction();

			if (action.equals(ACTION_ACTIVITY_FINISH_PLAYER_PREFERENCE)) {
				try {
					finish();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (action.equals("")) {
				// do something
			}
		}
	}

}
