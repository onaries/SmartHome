package com.onaries.smarthome.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.onaries.smarthome.LightActivity;
import com.onaries.smarthome.PhpDown;
import com.onaries.smarthome.R;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import butterknife.ButterKnife;

/**
 * Created by SW on 2016-06-05.
 */
public class TimePickerFragment extends DialogFragment implements View.OnClickListener{

    private int weekday;
    private int hour1, hour2;
    private int minute1, minute2;
    private String host;
    private int pos = 0;
    private Spinner spinner, spinner2;
    private View dialogView;
    private ArrayAdapter<String> adapter;
    private SharedPreferences prefs;

    final private String mysqlUrl = "/sql/mysql_ins_time_bulb.php";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_timeselect, null);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        host = prefs.getString("server_ip", "127.0.0.1");

        List<String> list = new ArrayList<String>();

        list.add(prefs.getString("light1_name", "전등 1"));

        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button startTimeButton = (Button) dialogView.findViewById(R.id.startTimeButton);
        Button stopTimeButton = (Button) dialogView.findViewById(R.id.stopTimeButton);

        startTimeButton.setOnClickListener(this);
        stopTimeButton.setOnClickListener(this);

        spinner = (Spinner) dialogView.findViewById(R.id.timeNode);
        spinner.setAdapter(adapter);
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
        spinner2 = (Spinner) dialogView.findViewById(R.id.weekdayLight);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                weekday = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setTitle("시간 설정")
                .setView(dialogView)
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String strPos = String.valueOf(pos);

                        // 시간 스트링
                        String t1 = String.valueOf(hour1) + String.valueOf(minute1) + "00";
                        String t2 = String.valueOf(hour2) + String.valueOf(minute2) + "00";

                        // 데이터베이스 반영
                        String result = "";
                        PhpDown phpDown = new PhpDown();
                        try {
                            result = phpDown.execute("http://" + host + mysqlUrl +"?weekday=" + weekday + "&t1=" + t1.toString() + "&t2=" + t2.toString() + "&node=" + strPos).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }

                        Log.d("DEBUG", strPos + " " + t1 + " " +t2 + " " +weekday);
                        // 결과값이 1이 아닐경우
                        if(result != ""){
                            Log.d("Error", "시간 예약 기능 오류");
                        }
                        else{
                            Toast.makeText(getActivity(), "정상적으로 처리되었습니다", Toast.LENGTH_SHORT).show();
                        }

                    }

                })
                .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();

    }

    @Override
    public void onClick(View v) {
        final Calendar c = Calendar.getInstance();
        final int hour = c.get(Calendar.HOUR_OF_DAY);
        final int minute = c.get(Calendar.MINUTE);
        final int day = c.get(Calendar.DAY_OF_WEEK);

        switch (v.getId()){

            case R.id.startTimeButton:
                new TimePickerDialog(getActivity(), timeSetListener, hour, minute, true).show();
                break;
            case R.id.stopTimeButton:
                new TimePickerDialog(getActivity(), timeSetListener2, hour, minute, true).show();
                break;
        }
    }

    private TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            hour1 = hourOfDay;
            minute1 = minute;
        }
    };

    private TimePickerDialog.OnTimeSetListener timeSetListener2 = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            hour2 = hourOfDay;
            minute2 = minute;
        }
    };
}


