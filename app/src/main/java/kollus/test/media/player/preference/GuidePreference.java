package kollus.test.media.player.preference;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import kollus.test.media.R;
import kollus.test.media.player.GuideGestureActivity;
import kollus.test.media.player.GuideShortCutsActivity;


public class GuidePreference extends View {
	private Activity mActivity;
	public GuidePreference(PlayerPreference root) {
		super(root);

		LinearLayout layout = (LinearLayout)root.findViewById(R.id.preference_root);
		View view = root.getLayoutInflater().inflate(R.layout.guide_layout, null);
		layout.addView(view);
		mActivity = root;
		onBindView(view);
	}

	private void onBindView(View view) {
		Button btn = (Button)view.findViewById(R.id.btn_short_cuts);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mActivity.startActivity(new Intent(mActivity, GuideShortCutsActivity.class));
			}
		});

		btn = (Button)view.findViewById(R.id.btn_gesture);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mActivity.startActivity(new Intent(mActivity, GuideGestureActivity.class));
			}
		});
	}
}
