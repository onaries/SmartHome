package com.onaries.smarthome.help;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.onaries.smarthome.R;


public class FragmentB extends Fragment {
    TextView textView5;
    TextView textView6;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       String title3="<h1>세 번째 아이콘(계량기 모양)</h1>";
        String text3= "&nbsp 이 화면에서는 현재 센서가 있는 곳의 " +
                "온도, 습도, 가스량, 밝기를 수치로 볼 수 있고 이전의 측정량을 그래프로 볼 수 있습니다. " +
                "<br> 먼저 설정 - 서버 설정에서 해당 기기가 설치된 IP주소를 입력해야 사용할 수 있습니다.<p>" +
                "&nbsp 수치에 대해 설명하자면 온도 습도의 경우 해상도가 0.1도(섭씨), 0.1퍼센트이며 " +
                "가스 센서와 조도 센서의 값은 1023이 최대입니다. <br>" +
                "&nbsp 평시 가스 센서의 값은 100정도이고 조도 센서의 값은 조명이 켜져있을 경우 200~300정도 조명이 꺼져있을 경우" +
                " 0~10정도 입니다. 가스 센서와 조도 센서의 값은 단위가 따로 정해져 있지 않으니 참고 " +
                "바랍니다.";


        View view = inflater.inflate(R.layout.fragment_b, container, false);


        textView5 = (TextView) view.findViewById(R.id.text3);
        textView6 = (TextView) view.findViewById(R.id.subtext3);
        textView5.setText(Html.fromHtml(title3));
        textView6.setText(Html.fromHtml(text3));
        return view;

    }
}

