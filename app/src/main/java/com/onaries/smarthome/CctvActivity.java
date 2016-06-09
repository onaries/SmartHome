package com.onaries.smarthome;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;

public class CctvActivity extends AppCompatActivity {

    private WebView cctvView;

    private String host;
    private Timer timer;

    private int delay = 5000;
    private int period = 1000;

    private int year, month, date;
    private int hour, minute, second;

    private long millisec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cctv);
        setTitle(R.string.title_activity_cctv);

        host = getIntent().getStringExtra("host");

        cctvView = (WebView) findViewById(R.id.cctvView);
        cctvView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = cctvView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        cctvView.loadUrl("http://" + host + "/webpage/main_cctv.php");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_cctv, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_webbrowser2:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + host + "/webpage/main_cctv.php"));
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
