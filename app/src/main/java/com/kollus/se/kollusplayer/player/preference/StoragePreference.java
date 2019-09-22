package com.kollus.se.kollusplayer.player.preference;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.kollus.sdk.media.KollusStorage;
import com.kollus.sdk.media.util.Log;
import com.kollus.sdk.media.util.Utils;
import com.kollus.se.kollusplayer.R;
import com.kollus.se.kollusplayer.player.util.DiskUtil;

import java.util.Vector;



public class StoragePreference extends View implements 
	OnClickListener, OnCheckedChangeListener {
	private static final String TAG = StoragePreference.class.getSimpleName();
	
	private static final int RESOURCE_STORAGE_BASE_ID = 100;
	private Context mContext;
	private KollusStorage mStorage;
	private String mGUIDSha1;
	private String mGUIDMd5;
	private Vector<String> mExternalStorages;
	private TextView mDownloadSizeTextView;
	private TextView mEtcSizeTextView;
	private TextView mEmptySizeTextView;
	private TextView mCacheSizeTextView;
	
	public StoragePreference(PlayerPreference root) {
		super(root);
		// TODO Auto-generated constructor stub
		mContext = root;
		mStorage = KollusStorage.getInstance(mContext);
		mGUIDSha1 = KollusConstants.getPlayerId(mContext);
		mGUIDMd5 = KollusConstants.getPlayerIdWithMD5(mContext);
		
		LinearLayout layout = (LinearLayout)root.findViewById(R.id.preference_root);
		View view = root.getLayoutInflater().inflate(R.layout.storage_preference, null);
		layout.addView(view);		
		onBindView(view);
	}
	
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
//		mStorage.releaseInstance();
		super.finalize();
	}

	private void onBindView(View view) {
		Button button = (Button)((ViewGroup)view).findViewById(R.id.current_storage_empty);
		button.setOnClickListener(this);

		LinearLayout storageLayout = (LinearLayout)((ViewGroup)view).findViewById(R.id.storage_info);
		mDownloadSizeTextView = (TextView)((ViewGroup)view).findViewById(R.id.storage_download_size);
		mEtcSizeTextView = (TextView)((ViewGroup)view).findViewById(R.id.storage_etc_size);
		mEmptySizeTextView = (TextView)((ViewGroup)view).findViewById(R.id.storage_empty_size);
		mCacheSizeTextView = (TextView)((ViewGroup)view).findViewById(R.id.storage_cache_size);
		mExternalStorages = DiskUtil.getExternalMounts(mContext);
		
		if(mExternalStorages.size() > 1) {
			String extDir = android.os.Environment.getExternalStorageDirectory().toString();
			String storageLocation = Utils.getStoragePath(mContext);
			int storagIndex = 0;
			RadioGroup btnGroup = new RadioGroup(mContext);
			btnGroup.setOnCheckedChangeListener(this);
			storageLayout.addView(btnGroup);			
			for(String path : mExternalStorages) {
				RadioButton btn = new RadioButton(mContext);
				if(path.startsWith(extDir))
					btn.setText(R.string.inner_storage);
				else
					btn.setText(R.string.outer_storage);
				btn.setId(RESOURCE_STORAGE_BASE_ID+storagIndex);
				btnGroup.addView(btn);
				Log.i(TAG, String.format("path %s storageLocation %s", path, storageLocation));
				if(storageLocation.startsWith(path))
					btnGroup.check(RESOURCE_STORAGE_BASE_ID+storagIndex);
				
				storagIndex++;
			}
		}
		else {
			LinearLayout storageLocationLayer = (LinearLayout)((ViewGroup)view).findViewById(R.id.storage_location_layer);
			storageLocationLayer.setVisibility(View.GONE);
			String sizeText = DiskUtil.getStringSize(mStorage.getUsedSize(KollusStorage.TYPE_CACHE));
			mCacheSizeTextView.setText(sizeText);

			String storagePath = Utils.getStoragePath(mContext);
			long usedSize = mStorage.getUsedSize(KollusStorage.TYPE_DOWNLOAD);
			long freeSize = Utils.getAvailableMemorySize(storagePath);
			long totalSize = Utils.getTotalMemorySize(storagePath);
			long etcSize = totalSize - usedSize - freeSize;

			mDownloadSizeTextView.setText(DiskUtil.getStringSize(usedSize));
			mEtcSizeTextView.setText(DiskUtil.getStringSize(etcSize));
			mEmptySizeTextView.setText(DiskUtil.getStringSize(freeSize));
		}
	}
	
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		int id = view.getId();
		if(id == R.id.current_storage_empty) {
			mStorage.clearCache();
			String sizeText = DiskUtil.getStringSize(mStorage.getUsedSize(KollusStorage.TYPE_CACHE));
			mCacheSizeTextView.setText(sizeText);

			String storagePath = Utils.getStoragePath(mContext);
			long usedSize = mStorage.getUsedSize(KollusStorage.TYPE_DOWNLOAD);
			long freeSize = Utils.getAvailableMemorySize(storagePath);
			long totalSize = Utils.getTotalMemorySize(storagePath);
			long etcSize = totalSize - usedSize - freeSize;

			mDownloadSizeTextView.setText(DiskUtil.getStringSize(usedSize));
			mEtcSizeTextView.setText(DiskUtil.getStringSize(etcSize));
			mEmptySizeTextView.setText(DiskUtil.getStringSize(freeSize));
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkId) {
		// TODO Auto-generated method stub
//		Log.i(TAG, "onCheckedChanged id:"+checkId);
		if(checkId >= RESOURCE_STORAGE_BASE_ID && checkId < (RESOURCE_STORAGE_BASE_ID+mExternalStorages.size())) {
			int storageIndex = checkId - RESOURCE_STORAGE_BASE_ID;
			String savedPath = Utils.getStoragePath(mContext);
			String savingPath = mExternalStorages.elementAt(storageIndex);

			Utils.setStoragePath(mContext, savingPath);
			
			if(!savedPath.equals(savingPath)) {
				Log.d(TAG, String.format("setStoragePath %s --> %s(%s)", savedPath, savingPath, Utils.getStoragePath(mContext)));
				mStorage.finish();
				mStorage = KollusStorage.getInstance(mContext);
				mStorage.initialize(KollusConstants.KEY, KollusConstants.EXPIRE_DATE, mContext.getPackageName());
				mStorage.setDevice(savingPath, mGUIDSha1, mGUIDMd5, Utils.isTablet(mContext));
			}
			
			String sizeText = DiskUtil.getStringSize(mStorage.getUsedSize(KollusStorage.TYPE_CACHE));
			mCacheSizeTextView.setText(sizeText);

			long usedSize = mStorage.getUsedSize(KollusStorage.TYPE_DOWNLOAD);
			long freeSize = Utils.getAvailableMemorySize(savingPath);
			long totalSize = Utils.getTotalMemorySize(savingPath);
			long etcSize = totalSize - usedSize - freeSize;

			mDownloadSizeTextView.setText(DiskUtil.getStringSize(usedSize));
			mEtcSizeTextView.setText(DiskUtil.getStringSize(etcSize));
			mEmptySizeTextView.setText(DiskUtil.getStringSize(freeSize));
//			notifyChanged();
		}
	}
}
