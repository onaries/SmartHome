package com.onaries.smarthome;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.widget.Button;

import com.astuetz.PagerSlidingTabStrip;

public class HelpActivity extends AppCompatActivity {

    private ViewPager viewPager = null;
    private PagerSlidingTabStrip tabs;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        setTitle(R.string.title_activity_help);
        //viewPager

        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        viewPager = (ViewPager)findViewById(R.id.viewPager);
        MyViewPagerAdapter adapter = new MyViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        viewPager.setPageMargin(pageMargin);
        tabs.setViewPager(viewPager);
        tabs.setUnderlineHeight(2);
        tabs.setIndicatorHeight(10);
        int color = getResources().getColor(R.color.main_color);
        tabs.setIndicatorColor(color);
//        btn[0] = (Button)findViewById(R.id.btn_a);
//        btn[1] = (Button)findViewById(R.id.btn_b);
//        btn[2] = (Button)findViewById(R.id.btn_c);
//
//        for(int i = 0; i < btn.length; i++){
//            btn[i].setOnClickListener(this);
//        }


    }

//    @Override
//    public void onClick(View v) {
//
//        switch(v.getId()){
//            case R.id.btn_a:
//                viewPager.setCurrentItem(0);
//                Toast.makeText(this,"page 1 of 3", Toast.LENGTH_SHORT).show();
//                break;
//            case R.id.btn_b:
//                viewPager.setCurrentItem(1);
//                Toast.makeText(this,"page 2 of 3", Toast.LENGTH_SHORT).show();
//                break;
//            case R.id.btn_c:
//                viewPager.setCurrentItem(2);
//                Toast.makeText(this,"page 3 of 3", Toast.LENGTH_SHORT).show();
//                break;
//            default:
//                break;
//        }
//    }
}
