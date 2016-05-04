package com.onaries.smarthome.help;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.onaries.smarthome.R;


public class FragmentC extends Fragment {
    TextView textView7;
    TextView textView8;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String title4="<h1>네 번째 아이콘(톱니바퀴 모양)</h1>";
        String text4= "-서버 설정-<br>" +
                "&nbsp 서버IP, 포트, TIMEOUT을 입력할 수 있습니다. TIMEOUT의 시간(ms)안에 TCP연결이 성립되지 않는 경우에는 작동하지 않습니다. <br>" +
                "&nbsp 초기 TIMEOUT 값은 5000입니다. 너무 작게 입력하는 것은 추천하지 않습니다." +
                "<p>-멀티탭 설정-<br>&nbsp 콘센트 각각의 이름을 입력할 수 있습니다." +
                "<p>-모니터링 설정-<br>&nbsp 그래프에 입력한 숫자 개수 만큼의 값들을 불러옵니다. 또한 상한값, 하한값을 설정하여 일정값에 의해 알림이 오게 할 수 있습니다." +
                "<p>-알림 설정-<br>&nbsp 푸시 알림을 받을지 안 받을지 선택할 수 있으며 알림음, 진동 기능을 설정할 수 있습니다. 또한 어떤 센서나 멀태탭을 지정하여 알림을 끄거나 켤 수 있습니다." +
                "<p>-정보-<br>&nbsp 앱 정보 및 개발자 메일, 홈페이지 등을 볼 수 있습니다.";


        View view = inflater.inflate(R.layout.fragment_c, container, false);


        textView7 = (TextView) view.findViewById(R.id.text4);
        textView8 = (TextView) view.findViewById(R.id.subtext4);
        textView7.setText(Html.fromHtml(title4));
        textView8.setText(Html.fromHtml(text4));
        return view;

    }
}