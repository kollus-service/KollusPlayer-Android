package com.kollus.se.kollusplayer.player;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.kollus.se.kollusplayer.KollusBaseActivity;
import com.kollus.se.kollusplayer.R;

public class GuideShortCutsActivity extends KollusBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide_short_cuts);

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
}
