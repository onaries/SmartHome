package com.onaries.smarthome;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by SW on 2015-12-06.
 */
public class SuperAwesomeCardFragment extends Fragment {

    private static final String ARG_POSITION = "position";

    private int position;

    TextView textView1;
    TextView textView2;
    TextView textView3;
    TextView textView4;
    TextView textView5;
    TextView textView6;
    TextView textView7;
    TextView textView8;
    View view;

    public static SuperAwesomeCardFragment newInstance(int position) {
        SuperAwesomeCardFragment f = new SuperAwesomeCardFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        position = getArguments().getInt(ARG_POSITION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (position == 0) {

            String title1="<p><h1>첫 번째 아이콘(전구 모양)</h1></p>";
            String text1="&nbsp 아직 미구현 항목으로 추후 구현 예정입니다.";
            String title2="<p><h1>두 번째 아이콘(콘센트 모양)</h1></p>";
            String text2="&nbsp 이 항목은 멀티탭의 콘센트 3개를 제어 할 수 있는 곳입니다. " +
                    "제어를 하기 위해선 먼저 4번째 아이콘의 서버 설정 메뉴에서 서버 IP를 입력해야 합니다. " +
                    " 그 후에 각각의 콘센트를 켜고 끌 수 있고 전체 켜기, 전체 끄기 버튼을 이용하여" +
                    " 콘센트 전체를 키고 끌 수 있습니다. <p>" +
                    "<p>&nbsp 음성인식 기능을 추가하였습니다. 설정에 음성인식 기능이 설정되어 있을경우에만 사용이 가능하며 " +
                    "사용 설정이 되어있으면 멀티탭의 설정된 이름에 \"켜\", \"꺼\", 라는 명령어를 통해 제어가 가능합니다. " +
                    "이름은 현재는 \"한글\" 만 가능합니다." +
                    "<br> &nbsp 예를 들어 \"충전기 켜\" 라고 하면 \"충전기\"라고 설정된 콘센트 부분이 켜지게 됩니다. " +
                    "&nbsp 그 외에도 \"종료\", \"전체 켜기\", \"전체 끄기\", \"전체 켜\", \"전체 꺼\" 명령어가 사용 가능합니다." +
                    "<br> &nbsp 이름은 직관적인 이름이 인식이 잘되며 어려운 발음은 인식이 잘 안될 수도 있습니다." +
                    "<p> &nbsp 추후 예약 시간 기능을 구현할 계획입니다.";

            view = inflater.inflate(R.layout.fragment_a,container,false);

            textView1=(TextView)view.findViewById(R.id.text1);
            textView2=(TextView)view.findViewById(R.id.subtext1);
            textView3=(TextView)view.findViewById(R.id.text2);
            textView4=(TextView)view.findViewById(R.id.subtext2);
            textView1.setText(Html.fromHtml(title1));
            textView2.setText(Html.fromHtml(text1));
            textView3.setText(Html.fromHtml(title2));
            textView4.setText(Html.fromHtml(text2));
        }
        else if (position == 1) {
            String title3="<h1>세 번째 아이콘(계량기 모양)</h1>";
            String text3= "&nbsp 이 화면에서는 현재 센서가 있는 곳의 " +
                    "온도, 습도, 가스량, 밝기를 수치로 볼 수 있고 이전의 측정량을 그래프로 볼 수 있습니다. " +
                    "<br> 먼저 설정 - 서버 설정에서 해당 기기가 설치된 IP주소를 입력해야 사용할 수 있습니다.<p>" +
                    "&nbsp 수치에 대해 설명하자면 온도 습도의 경우 해상도가 0.1도(섭씨), 0.1퍼센트이며 " +
                    "가스 센서와 조도 센서의 값은 1023이 최대입니다. <br>" +
                    "&nbsp 평시 가스 센서의 값은 100정도이고 조도 센서의 값은 조명이 켜져있을 경우 200~300정도 조명이 꺼져있을 경우" +
                    " 0~10정도 입니다. 가스 센서와 조도 센서의 값은 단위가 따로 정해져 있지 않으니 참고 " +
                    "바랍니다.";

            view = inflater.inflate(R.layout.fragment_b, container, false);


            textView5 = (TextView) view.findViewById(R.id.text3);
            textView6 = (TextView) view.findViewById(R.id.subtext3);
            textView5.setText(Html.fromHtml(title3));
            textView6.setText(Html.fromHtml(text3));
        }
        else {
            String title4="<h1>네 번째 아이콘(톱니바퀴 모양)</h1>";
            String text4= "-서버 설정-<br>" +
                    "&nbsp 서버IP, 포트, TIMEOUT을 입력할 수 있습니다. TIMEOUT의 시간(ms)안에 TCP연결이 성립되지 않는 경우에는 작동하지 않습니다. <br>" +
                    "&nbsp 초기 TIMEOUT 값은 5000입니다. 너무 작게 입력하는 것은 추천하지 않습니다." +
                    "<p>-멀티탭 설정-<br>&nbsp 콘센트 각각의 이름을 입력할 수 있습니다. 또한 음성인식 기능을 사용할 지 여부를 설정할 수 있습니다." +
                    "<p>-모니터링 설정-<br>&nbsp 그래프에 입력한 숫자 개수 만큼의 값들을 불러옵니다. 또한 상한값, 하한값을 설정하여 일정값에 의해 알림이 오게 할 수 있습니다." +
                    "<p>-알림 설정-<br>&nbsp 푸시 알림을 받을지 안 받을지 선택할 수 있으며 알림음, 진동 기능을 설정할 수 있습니다. 또한 어떤 센서나 멀태탭을 지정하여 " +
                    "알림을 끄거나 켤 수 있습니다. <br>&nbsp 알림 기능을 사용하기 위해서는 설정 - 정보에 서버에 REG ID 등록을 하여야 합니다." +
                    "<p>-정보-<br>&nbsp 서버에 REG ID 등록 및 앱 정보 및 개발자 메일, 홈페이지 등을 볼 수 있습니다.";

            view = inflater.inflate(R.layout.fragment_c, container, false);


            textView7 = (TextView) view.findViewById(R.id.text4);
            textView8 = (TextView) view.findViewById(R.id.subtext4);
            textView7.setText(Html.fromHtml(title4));
            textView8.setText(Html.fromHtml(text4));
        }
        //v.setText("CARD " + (position + 1));

        return view;
    }
}
