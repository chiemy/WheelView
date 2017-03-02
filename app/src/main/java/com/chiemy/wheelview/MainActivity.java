package com.chiemy.wheelview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private Integer [] mInt = {1, 2, 3, 4, 5, 6};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WheelView wheelView = (WheelView) findViewById(R.id.wheelView);
        // wheelView.setSelectedPosition(1);

        WheelAdapter<Integer> wheelAdapter = new WheelAdapter<Integer>() {
            @Override
            public View onCreateItemView(ViewGroup parent) {
                return LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_wheel_item, parent, false);
            }

            @Override
            public void onBindView(View view, int position) {
                TextView tv = (TextView) view.findViewById(R.id.tv);
                tv.setText(String.valueOf(getItem(position)));
            }
        };
        wheelAdapter.setData(Arrays.asList(mInt));

        wheelView.setAdapter(wheelAdapter);

        wheelView.setOnWheelChangedListener(new WheelView.OnWheelChangedListener() {
            @Override
            public void onSelected(WheelView wheelView, int position) {
                Toast.makeText(MainActivity.this, "" + position, Toast.LENGTH_SHORT).show();
            }
        });

        wheelView.postDelayed(new Runnable() {
            @Override
            public void run() {
                wheelView.setSelectedPosition(3);
            }
        }, 2000);
    }
}
