package com.onaries.smarthome.help;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.onaries.smarthome.R;

public class FragmentA extends Fragment {
    TextView textView1;
    TextView textView2;
    TextView textView3;
    TextView textView4;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String title1="<p><h1>첫 번째 아이콘(전구 모양)</h1></p>";
        String text1="&nbsp 아직 미구현 항목으로 추후 구현 예정입니다.";
        String title2="<p><h1>두 번째 아이콘(콘센트 모양)</h1></p>";
        String text2="&nbsp 이 항목은 멀티탭의 콘센트 3개를 제어 할 수 있는 곳입니다. " +
                "제어를 하기 위해선 먼저 4번째 아이콘의 서버 설정 메뉴에서 서버 IP를 입력해야 합니다. " +
                " 그 후에 각각의 콘센트를 켜고 끌 수 있고 전체 켜기, 전체 끄기 버튼을 이용하여" +
                " 콘센트 전체를 키고 끌 수 있습니다. <p>" +
                "&nbsp 추후 예약 시간 기능을 구현할 계획입니다.";


        View view = inflater.inflate(R.layout.fragment_a,container,false);


        textView1=(TextView)view.findViewById(R.id.text1);
        textView2=(TextView)view.findViewById(R.id.subtext1);
        textView3=(TextView)view.findViewById(R.id.text2);
        textView4=(TextView)view.findViewById(R.id.subtext2);
        textView1.setText(Html.fromHtml(title1));
        textView2.setText(Html.fromHtml(text1));
        textView3.setText(Html.fromHtml(title2));
        textView4.setText(Html.fromHtml(text2));

        return view;

    }

}
