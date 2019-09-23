package kollus.test.media.player.preference;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kollus.sdk.media.MediaPlayer;
import com.kollus.sdk.media.util.CpuInfo;
import com.kollus.sdk.media.util.Log;
import com.kollus.sdk.media.util.Utils;
import kollus.test.media.R;
import kollus.test.media.player.util.PropertyUtil;

public class PlayerInfoPreference  extends View {
	private final int DEVOLP = 3;
	private Context mContext;
	private String mVersion;
	private int mDebugCount;
	public PlayerInfoPreference(PlayerPreference root) {
		super(root);

		LinearLayout layout = (LinearLayout)root.findViewById(R.id.preference_root);
		ViewGroup view = (ViewGroup)root.getLayoutInflater().inflate(R.layout.player_info_preference, null);
		layout.addView(view);		
		try {
			mContext = root.getApplicationContext();
			PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
			mVersion = packageInfo.versionName+"_r"+DEVOLP;
			if(Log.isDebug())
				mVersion += " (d)";
			mVersion += "\n"+ MediaPlayer.VERSION;
			onBindView(view);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void onBindView(ViewGroup view) {
		CpuInfo cpu = CpuInfo.getInstance();

		final View deviceInfo = view.findViewById(R.id.device_info);
		if(Log.isDebug())
			deviceInfo.setVisibility(VISIBLE);
		else
			deviceInfo.setVisibility(GONE);

		TextView text = (TextView)view.findViewById(R.id.device_model);
		text.setText(String.format("%s(%s/%d)", Build.MODEL, Build.VERSION.RELEASE,Build.VERSION.SDK_INT));

		text = (TextView)view.findViewById(R.id.device_board);
		text.setText(Build.BOARD);

		text = (TextView)view.findViewById(R.id.device_platform);
		text.setText(PropertyUtil.getSystemPropertyCached("ro.board.platform"));

		text = (TextView)view.findViewById(R.id.player_version);
		text.setText(mVersion);
		if(!Log.isDebug()) {
			text.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					mDebugCount++;
					if(mDebugCount >= 3)
						deviceInfo.setVisibility(VISIBLE);
				}
			});
		}

		text = (TextView)view.findViewById(R.id.player_id);
		String deviceGUIDSHA1 = KollusConstants.getPlayerId(mContext).toUpperCase();
		String storagePath = Utils.getStoragePath(mContext);
		
		StringBuffer id = new StringBuffer();
		for(int i=0; i<deviceGUIDSHA1.length();){
			id.append(deviceGUIDSHA1.subSequence(i, i+8));
			i += 8;
			if(i<deviceGUIDSHA1.length())
				id.append(" - ");
		}
		text.setText(id);
	}
}
