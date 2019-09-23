package kollus.test.media.player;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import kollus.test.media.KollusBaseActivity;
import kollus.test.media.R;

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
