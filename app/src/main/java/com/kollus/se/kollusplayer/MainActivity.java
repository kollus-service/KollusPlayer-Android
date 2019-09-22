package com.kollus.se.kollusplayer;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button btnNative;
    private Button btnWebview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.initView();
        this.setLisener();
    }
    public void initView(){
        this.btnNative = findViewById(R.id.btnNative);
        this.btnWebview = findViewById(R.id.btnWebview);
    }

    public void setLisener(){
        btnNative.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NativeActivity.class);
                startActivity(intent);

            }
        });
        btnWebview.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), WebviewActivity.class);
                startActivity(intent);
            }
        });
    }

}
