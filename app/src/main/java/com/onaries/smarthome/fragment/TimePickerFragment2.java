package com.onaries.smarthome.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.onaries.smarthome.PhpDown;
import com.onaries.smarthome.R;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutionException;

/**
 * Created by SW on 2016-06-05.
 */
public class TimePickerFragment2 extends DialogFragment {

    private int weekday;
    private int hour1, hour2;
    private int minute1, minute2;
    private String host;
    private int pos = 0;
    private int type;
    private String mysqlUrl;
    private Spinner spinner;

    public TimePickerFragment2(int type, String host) {
        // type은 0일 경우 전구, 1일 경우 멀티탭
        this.type = type;
        this.host = host;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mysqlUrl = "/mysql_time_update_multi.php";
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_timeselect_multi, null);

        spinner = (Spinner) dialogView.findViewById(R.id.timeNode2);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pos = position;
                pos += 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return new AlertDialog.Builder(getActivity())
                .setTitle("시간 설정")
                .setView(dialogView)
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String strPos = String.valueOf(pos);

                        // 날짜 계산
                        SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
                        Time t1 = new Time(hour1, minute1, 0);
                        Time t2 = new Time(hour2, minute2, 0);

                        // 데이터베이스 반영
                        String result = "";
                        PhpDown phpDown = new PhpDown();
                        try {
                            result = phpDown.execute("http://" + host + mysqlUrl +"?weekday=" + weekday + "?t1=" + t1.toString() + "?t2=" + t2.toString() + "?node=" + strPos).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }

                        // 결과값이 1이 아닐경우
                        if(result != "1"){
                            Log.d("Error", "시간 예약 기능 오류");
                        }

                    }

                })
                .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();

    }
}
