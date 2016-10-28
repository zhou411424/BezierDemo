package com.xy.bezier;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.xy.bezier.widget.CruisePathView;

public class MainActivity extends AppCompatActivity {

    private Button mStartBtn;
    private CruisePathView mCruisePathView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStartBtn = (Button) findViewById(R.id.start_btn);
        mCruisePathView = (CruisePathView) findViewById(R.id.cruisePathView);

        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCruisePathView.playPathAnim();
            }
        });
    }

}
