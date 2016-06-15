package com.onaries.smarthome;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class CloudActivity extends AppCompatActivity {

    private WebView cloudView;

    private String host;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud);
        setTitle(R.string.title_activity_cloud);

        host = getIntent().getStringExtra("host");

        cloudView = (WebView) findViewById(R.id.cloudView);
        cloudView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = cloudView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        cloudView.loadUrl("http://" + host + "/cloud/index.php");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_cloud, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_webbrowser:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + host + "/cloud/index.php"));
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
