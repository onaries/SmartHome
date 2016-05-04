package com.onaries.smarthome;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.ImageView;

// 처음 실행되는 Activity로 3초동안 지속되고 finish 처리 되는 Activity
public class IntroActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        ImageView imageView = (ImageView) findViewById(R.id.intro_motion);          // ImageView 객체 설정
        imageView.setBackgroundResource(R.drawable.anim_device_motion);             // Resource 설정
        AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();    // 애니메이션 Drawable 객체에 지정
        animationDrawable.start();                                                  // 애니메이션 Drawable Start
        Handler handler = new Handler();                                            // 지연 실행을 위해서 Handler 정의
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(IntroActivity.this, MainActivity.class);
                startActivity(intent);

                finish();       // 액티비티가 실행된 후 finish 처리로 이 액티비티 종료
            }
        }, 3000);       // 3초 이후에 실행되게 설정
    }

    @Override
    public void onBackPressed() {

    }
}
