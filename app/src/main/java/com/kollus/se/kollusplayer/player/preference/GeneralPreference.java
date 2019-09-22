package com.kollus.se.kollusplayer.player.preference;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.kollus.sdk.media.KollusPlayerDRMUpdateListener;
import com.kollus.sdk.media.KollusStorage;
import com.kollus.sdk.media.content.KollusContent;
import com.kollus.sdk.media.util.Log;
import com.kollus.sdk.media.util.Utils;
import com.kollus.se.kollusplayer.R;

import java.util.ArrayList;



public class GeneralPreference  extends View implements OnCheckedChangeListener, AdapterView.OnItemSelectedListener {
	private static final String TAG = GeneralPreference.class.getSimpleName();
	private Context mContext;
	private SharedPreferences mSharedPreferences;
	private Resources mResources;
	private View mLMSSendView;
	private View mDrmRefreshProcessView;
	private boolean mDrmRefreshProcessing;
	private CheckBox mCheckRefreshOnlyExpired;
	private Spinner mRenderMode;
	private Spinner mSeekInterval;
	private Spinner mDoubleTab;

	public GeneralPreference(PlayerPreference root) {
		super(root);
		
		mContext = root;
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		mResources = mContext.getResources();

		mDrmRefreshProcessView = root.findViewById(R.id.drm_refresh_processing);
		LinearLayout layout = (LinearLayout)root.findViewById(R.id.preference_root);
		View view = root.getLayoutInflater().inflate(R.layout.general_preference, null);
		layout.addView(view);		
		onBindView(view);
	}
	
	private void onBindView(View view) {
		boolean notifyNoWifi = mSharedPreferences.getBoolean(mResources.getString(R.string.preference_notify_no_wifi_key), true);
		SwitchCompat switchView = (SwitchCompat)view.findViewById(R.id.notify_no_wifi);
		switchView.setOnCheckedChangeListener(this);
		switchView.setChecked(notifyNoWifi);
		
		boolean resumPlay = mSharedPreferences.getBoolean(mResources.getString(R.string.preference_resume_play_key), 
				mResources.getBoolean(R.bool.default_force_nscreen));
		switchView = (SwitchCompat)view.findViewById(R.id.resume_playing);
		switchView.setOnCheckedChangeListener(this);
		switchView.setChecked(resumPlay);

//		if(Utils.getPlayerType() == Utils.PLAYER_TYPE_KOLLUS && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
			boolean isHwCodec = Utils.getDecoderType(mContext) == Utils.DecoderType.HW_CODEC;
			switchView = (SwitchCompat)view.findViewById(R.id.sw_codec_precedence);
			switchView.setChecked(!isHwCodec);
			switchView.setOnCheckedChangeListener(this);
//		}
//		else {
//			View codecLayer = view.findViewById(R.id.sw_codec_precedence_layer);		
//			codecLayer.setVisibility(View.GONE);
//		}

		mRenderMode = (Spinner)view.findViewById(R.id.render_mode);
		ArrayAdapter<String> aRenderMode = new ArrayAdapter<String>(
				mContext,
				android.R.layout.simple_spinner_dropdown_item,
				mResources.getStringArray(R.array.render_mode_array)
		);
		mRenderMode.setAdapter(aRenderMode);
//		if(isHwCodec) {
//			mRenderMode.setEnabled(false);
//			mRenderMode.setFocusable(false);
//		}
//		else {
		mRenderMode.setEnabled(true);
		mRenderMode.setFocusable(true);
		mRenderMode.setSelection(Utils.getRenderType(mContext));
		mRenderMode.setOnItemSelectedListener(this);
//		}

		mLMSSendView = view.findViewById(R.id.lms_precedence_layer);
		if(Log.isDebug())
			mLMSSendView.setVisibility(View.VISIBLE);
		boolean lmsSend = mSharedPreferences.getBoolean(mResources.getString(R.string.preference_lms_send_download_content_key), true);
		switchView = (SwitchCompat)view.findViewById(R.id.lms_precedence);
		switchView.setOnCheckedChangeListener(this);
		switchView.setChecked(lmsSend);

		mCheckRefreshOnlyExpired = (CheckBox)view.findViewById(R.id.refresh_only_expired);
		Button btn = (Button)view.findViewById(R.id.drm_all_refresh);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				mDrmRefreshProcessView.setVisibility(View.VISIBLE);
				mDrmRefreshProcessing = true;
				KollusStorage storage = KollusStorage.getInstance(mContext);
				ArrayList<KollusContent> list = storage.getDownloadContentList();

				Log.d(TAG, "---- DOWNLOAD Content Refresh DRM ----");
				for(KollusContent iter : list) {
					Log.d(TAG, String.format("Update DRM Info MCK '%s'", iter.getMediaContentKey()));
				}
				Log.d(TAG, "++++ DOWNLOAD Content Refresh DRM ++++");

				KollusPlayerDRMUpdateListener listener = new KollusPlayerDRMUpdateListener() {
					@Override
					public void onDRMUpdateStart() {
						Log.d(TAG, "onDRMUpdateStart");
					}

					@Override
					public void onDRMUpdateProcess(String request, String response) {
						Log.d(TAG, String.format("onDRMUpdateProcess request '%s'", request));
						Log.d(TAG, String.format("onDRMUpdateProcess response '%s'", response));
					}

					@Override
					public void onDRMUpdateComplete() {
						Log.d(TAG, "onDRMUpdateComplete");
						((Activity)mContext).runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mDrmRefreshProcessView.setVisibility(View.GONE);
							}
						});
						mDrmRefreshProcessing = false;
					}
				};

				storage.updateDownloadDRMInfo(listener, !mCheckRefreshOnlyExpired.isChecked());
			}
		});

		mSeekInterval = (Spinner)view.findViewById(R.id.spinner_seek_interval);
		ArrayAdapter<String> aSeekInterval = new ArrayAdapter<String>(
				mContext,
				android.R.layout.simple_spinner_dropdown_item,
				mResources.getStringArray(R.array.seek_interval_array)
		);
		mSeekInterval.setAdapter(aSeekInterval);
		int interval = mSharedPreferences.getInt(mResources.getString(R.string.preference_seek_interval_key), 10);
		int intervalIndex = 0;
		switch (interval) {
			case 10:
				intervalIndex = 0;
				break;
			case 20:
				intervalIndex = 1;
				break;
			case 30:
				intervalIndex = 2;
				break;
			case 60:
				intervalIndex = 3;
				break;
			case 300:
				intervalIndex = 4;
				break;
		}
		mSeekInterval.setSelection(intervalIndex);
		mSeekInterval.setOnItemSelectedListener(this);

		mDoubleTab = (Spinner)view.findViewById(R.id.spinner_double_tab);
		ArrayAdapter<String> aDoubleTab = new ArrayAdapter<String>(
				mContext,
				android.R.layout.simple_spinner_dropdown_item,
				mResources.getStringArray(R.array.double_tab_array)
		);
        mDoubleTab.setAdapter(aDoubleTab);
        mDoubleTab.setSelection(mSharedPreferences.getInt(mResources.getString(R.string.preference_double_tab_key), ValuePreference.DOUBLE_TAB_SCREEN_SIZE));
        mDoubleTab.setOnItemSelectedListener(this);
	}
	
	@Override
	public void onCheckedChanged(CompoundButton button, boolean checked) {
		// TODO Auto-generated method stub
		SharedPreferences.Editor editor = mSharedPreferences.edit();
		
		if(button.getId() == R.id.notify_no_wifi) {
			editor.putBoolean(mResources.getString(R.string.preference_notify_no_wifi_key), checked);
		}
		else if(button.getId() == R.id.resume_playing) {
			editor.putBoolean(mResources.getString(R.string.preference_resume_play_key), checked);
		}
		else if(button.getId() == R.id.sw_codec_precedence) {
			Utils.setDecoderType(mContext, !checked ? Utils.DecoderType.HW_CODEC : Utils.DecoderType.SW_CODEC);

//			if(checked) {
				mRenderMode.setEnabled(true);
				mRenderMode.setFocusable(true);
//			}
//			else {
//
//				mRenderMode.setEnabled(false);
//				mRenderMode.setFocusable(false);
//			}
		}
		else if(button.getId() == R.id.lms_precedence) {
			editor.putBoolean(mResources.getString(R.string.preference_lms_send_download_content_key), checked);
		}
		editor.commit();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if(parent == mRenderMode) {
			Utils.setRenderType(mContext, position);
		}
		else if (parent == mSeekInterval) {
			int interval = 10;
			switch (position) {
				case 0:
					interval = 10;
					break;
				case 1:
					interval = 20;
					break;
				case 2:
					interval = 30;
					break;
				case 3:
					interval = 60;
					break;
				case 4:
					interval = 300;
					break;
			}
			SharedPreferences.Editor editor = mSharedPreferences.edit();
			editor.putInt(mResources.getString(R.string.preference_seek_interval_key), interval);
			editor.commit();
		}
		else if(parent == mDoubleTab) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putInt(mResources.getString(R.string.preference_double_tab_key), position);
            editor.commit();
        }
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}
}
