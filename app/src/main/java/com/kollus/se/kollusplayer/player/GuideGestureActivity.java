package com.kollus.se.kollusplayer.player;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kollus.se.kollusplayer.KollusBaseActivity;
import com.kollus.se.kollusplayer.R;

public class GuideGestureActivity extends KollusBaseActivity {
    private ViewPager mViewPager;

    final private int GUIDE_TOTAL_COUNT = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide_gesture_layout);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(1);    // TODO: Check
        mViewPager.setAdapter(new PagerAdapter(this));

        ImageView btn = (ImageView)findViewById(R.id.btn_back);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                setResult(RESULT_OK, getIntent());
                finish();
            }
        });
    }

    private class PagerAdapter extends android.support.v4.view.PagerAdapter {
        private LayoutInflater mInflater;

        private PagerAdapter(Context context) {
            super();
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return GUIDE_TOTAL_COUNT;
        }

        @Override
        public Object instantiateItem(ViewGroup pager, int position) {
            View view = getItem(position);
            pager.addView(view, 0);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup pager, int position, Object view) {
            pager.removeView((View) view);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        private View getItem(int position) {
            View view = mInflater.inflate(R.layout.guide_gesture_view, null);

            ImageView imageView = (ImageView) view.findViewById(R.id.guide_image);
            imageView.setImageResource(rArrayGuideImage[position]);

            TextView guideText = (TextView) view.findViewById(R.id.guide_text);
            guideText.setText(rArrayGuideText[position]);

            int curPage = position + 1;
            TextView textPageNum = (TextView) view.findViewById(R.id.guide_page_num);
            textPageNum.setText(curPage + "/" + GUIDE_TOTAL_COUNT);

            return view;
        }
    }

    final private int[] rArrayGuideImage = {
        R.drawable.gesture_zoom,
        R.drawable.gesture_brightness,
        R.drawable.gesture_sound,
        R.drawable.gesture_seek
    };

    final private int[] rArrayGuideText = {
        R.string.help_size_control,
        R.string.help_bright_control,
        R.string.help_volume_control,
        R.string.help_seek
    };
}
