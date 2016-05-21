package com.onaries.smarthome;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class CloudActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud);
        setTitle(R.string.title_activity_cloud);

        Toast.makeText(getApplicationContext(), "지원 예정입니다", Toast.LENGTH_SHORT).show();
    }
}
