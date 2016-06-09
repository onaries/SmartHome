package com.onaries.smarthome.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
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

import com.onaries.smarthome.PhpDown_noThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by SW on 2016-06-09.
 */
public class TimeLogFragment2 extends DialogFragment {

    private LinearLayout l1;
    private String host;
    private int num = 0;

    // Database의 자료 저장 리스트
    private int[] relay_no;
    private int[] weekday;
    private String[] start_time;
    private String[] stop_time;

    public TimeLogFragment2(String host) {
        this.host = host;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //
        PhpDown_noThread phpDownNoThread = new PhpDown_noThread("http://" + host + "/mysql_time_log_select_multi.php");
        String result = phpDownNoThread.phpTask();

        try {
            JSONArray ja = new JSONArray(result);

            for(int i = 0; i < ja.length(); i++){
                JSONObject jo = ja.getJSONObject(i);
                relay_no[i] = jo.getInt("REALY_NO");
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

            String strWeekday = getWeekday(weekday[i]);
            TextView t1 = new TextView(getActivity());
            t1.setText("노드 : " + relay_no[i] + " 요일 : " + strWeekday + " 시작 : " + start_time[i] + " 종료 : " + stop_time[i]);

            LinearLayout.LayoutParams t1Params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            t1.setLayoutParams(t1Params);

            Button xButton = new Button(getActivity());
            xButton.setId(i + 1);
            xButton.setText("삭제");
            xButton.setOnClickListener(onClickListener);

            LinearLayout.LayoutParams xButtonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            xButton.setLayoutParams(xButtonParams);

            // ChildView 추가
            l1.addView(t1, t1Params);
            l1.addView(xButton, xButtonParams);
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
                    PhpDown_noThread phpDownNoThread = new PhpDown_noThread("http://" + host + "/mysql_time_log_delete_multi.php?num=" + v.getId());
                    String result = phpDownNoThread.phpTask();

                    if(result != "True"){
                        Log.d("ERROR", "에러");
                    }

                    break;
            }
        }
    };

    public String getWeekday(int day){
        String weekday = "";

        switch (day) {
            case 0:
                weekday = "일";
                break;
            case 1:
                weekday = "월";
                break;
            case 2:
                weekday = "화";
                break;
            case 3:
                weekday = "수";
                break;
            case 4:
                weekday = "목";
                break;
            case 5:
                weekday = "금";
                break;
            case 6:
                weekday = "토";
                break;
        }

        return weekday;
    }
}
