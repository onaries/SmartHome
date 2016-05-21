package com.onaries.smarthome;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class CctvActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cctv);
        setTitle(R.string.title_activity_cctv);

        Toast.makeText(getApplicationContext(), "지원 예정입니다", Toast.LENGTH_SHORT).show();

    }
}
