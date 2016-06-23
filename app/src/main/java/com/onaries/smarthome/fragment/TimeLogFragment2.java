package com.onaries.smarthome.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.onaries.smarthome.PhpDown;
import com.onaries.smarthome.PhpDown_noThread;
import com.onaries.smarthome.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

/**
 * Created by SW on 2016-06-09.
 */
public class TimeLogFragment2 extends DialogFragment {

    private LinearLayout l1;
    private String host;
    private int num = 0;

    // Database의 자료 저장 리스트
    private int[] no;
    private int[] relay_no;
    private int[] weekday;
    private String[] start_time;
    private String[] stop_time;

    private SharedPreferences prefs;

    final private String mysqlURL = "/sql/mysql_sel_relay_conf.php";
    final private String mysqlURL_del_relay_conf = "/sql/mysql_del_relay_conf.php";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        host = prefs.getString("server_ip", "127.0.0.1");

        PhpDown phpDown = new PhpDown();
        String result = "";
        try {
            result = phpDown.execute("http://" + host + mysqlURL).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        try {
            JSONArray ja = new JSONArray(result);

            no = new int[ja.length()];
            relay_no = new int[ja.length()];
            weekday = new int[ja.length()];
            start_time = new String[ja.length()];
            stop_time = new String[ja.length()];

            for(int i = 0; i < ja.length(); i++){
                JSONObject jo = ja.getJSONObject(i);
                no[i] = jo.getInt("NO");
                relay_no[i] = jo.getInt("RELAY_NO");
                weekday[i] = jo.getInt("WEEKDAY");
                start_time[i] = jo.getString("START_TIME");
                stop_time[i] = jo.getString("STOP_TIME");
            }

        } catch (JSONException e){
            e.printStackTrace();
        }


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        l1 = new LinearLayout(getActivity());
        l1.setOrientation(LinearLayout.VERTICAL);
        l1.computeScroll();

        if(relay_no == null){
            Toast.makeText(getActivity(), "서버 연결이 필요합니다.", Toast.LENGTH_SHORT).show();
            return new AlertDialog.Builder(getActivity())
                    .setTitle("예약 취소")
                    .setView(l1)
                    .setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .create();
        }

        for(int i = 0; i < relay_no.length; i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            l1.setLayoutParams(params);

            LinearLayout l2 = new LinearLayout(getActivity());
            l2.setOrientation(LinearLayout.HORIZONTAL);

            Button oButton = new Button(getActivity());
            oButton.setBackground(getResources().getDrawable(R.drawable.ic_action_tick));

            LinearLayout.LayoutParams o1Params = new LinearLayout.LayoutParams(150, 150);
            oButton.setLayoutParams(o1Params);

            String strWeekday = getWeekday(weekday[i]);
            TextView t1 = new TextView(getActivity());
            t1.setText("노드 : " + relay_no[i] + " 요일 : " + strWeekday + " 시작 : " + start_time[i] + " 종료 : " + stop_time[i]);

            LinearLayout.LayoutParams t1Params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            t1.setLayoutParams(t1Params);

            Button xButton = new Button(getActivity());
            xButton.setId(no[i]);
            xButton.setBackground(getResources().getDrawable(R.drawable.ic_action_cancel));
            xButton.setOnClickListener(onClickListener);

            LinearLayout.LayoutParams xButtonParams = new LinearLayout.LayoutParams(150, 150);
            xButton.setLayoutParams(xButtonParams);

            // ChildView 추가
            l2.addView(oButton, o1Params);
            l2.addView(t1, t1Params);
            l2.addView(xButton, xButtonParams);
            l1.addView(l2);
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle("예약 취소")
                .setView(l1)
                .setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();
    }

    public View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                default:

                    // Mysql에서 Delete문 실행
                    // 여기서 Num은 데이터베이스 열의 번호
                    PhpDown phpDown = new PhpDown();
                    String result = "";
                    try {
                        result = phpDown.execute("http://" + host + mysqlURL_del_relay_conf + "?no=" + v.getId()).get();
                        Toast.makeText(getActivity(), "삭제되었습니다", Toast.LENGTH_SHORT).show();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    break;
            }
        }
    };

    public String getWeekday(int day){
        String weekday = "";

        switch (day) {
            case 0:
                weekday = "월";
                break;
            case 1:
                weekday = "화";
                break;
            case 2:
                weekday = "수";
                break;
            case 3:
                weekday = "목";
                break;
            case 4:
                weekday = "금";
                break;
            case 5:
                weekday = "토";
                break;
            case 6:
                weekday = "일";
                break;
        }

        return weekday;
    }
}
