package com.hzy.adjustlayout.demo;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ViewGroup viewGroup = findViewById(R.id.adjustLayout);
        findViewById(R.id.button2).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = new TextView(v.getContext());
                textView.setText(UUID.randomUUID().toString().substring(0,(int)(Math.random()*10+2)));
                textView.setBackgroundColor(Color.argb(255,(int)(Math.random()*255),(int)(Math.random()*255),(int)(Math.random()*255)));
                textView.setSingleLine();
                viewGroup.addView(textView, (int)(Math.random()*viewGroup.getChildCount()));
            }
        });

        findViewById(R.id.button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                viewGroup.removeViewAt((int)(Math.random()*viewGroup.getChildCount()));
            }
        });
    }



}
