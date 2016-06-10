package com.onaries.smarthome;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jjobes.slidedaytimepicker.SlideDayTimeListener;
import com.github.jjobes.slidedaytimepicker.SlideDayTimePicker;
import com.onaries.smarthome.fragment.TimeLogFragment;
import com.onaries.smarthome.fragment.TimePickerFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LightActivity extends AppCompatActivity {

    private SlideDayTimeListener listener, listener2;
    private String node;
    private int weekday;
    private int hour1, hour2;
    private int minute1, minute2;
    private String host;
    private FragmentManager fragmentManager;
    private String[] bulName;
    private SharedPreferences prefs;
    private TextView light1_textView;

    private int port;
    private String strPort;
    private String recv = null;
    private Boolean preState = true;
    private long time;

    private ImageButton lightButton1On;
    private ImageButton lightButton1Off;

    final private String mysqlURL_sel_bulb = "/sql/mysql_sel_bulb.php";
    final private String mysqlURL_ins_time_bulb = "/sql/mysql_ins_time_bulb.php";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light);
        setTitle(R.string.title_activity_light);

        recv = getIntent().getExtras().getString("Status");

        fragmentManager = getSupportFragmentManager();

        initSlideDayTimeListner();
        initSlideDayTimeListner2();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // 서버, 포트 설정
        strPort = prefs.getString("server_port", "12345");
        host = prefs.getString("server_ip", "127.0.0.1");
        port = Integer.parseInt(strPort);

        String sTime = prefs.getString("server_time", "5000");              // 시간 구하기
        time = Long.parseLong(sTime);                                       // 시간 파싱

        // 전등 이름 가져오기
        PhpDown_noThread phpDownNoThread = new PhpDown_noThread("http://" + host + mysqlURL_sel_bulb);
        String result = phpDownNoThread.phpTask();

        try{
            JSONArray ja = new JSONArray(result);

            for(int i = 0; i < ja.length(); i++){
                JSONObject jo = ja.getJSONObject(i);
                bulName[i] = jo.getString("BULB_NAME");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (bulName != null){
            final SharedPreferences.Editor ed = prefs.edit();
            ed.putString("light1_name", bulName[0]);
        }

        light1_textView = (TextView) findViewById(R.id.txtLight1);
        light1_textView.setText(prefs.getString("light1_name", "전등 1"));

        lightButton1On = (ImageButton) findViewById(R.id.lightButton1On);
        lightButton1Off = (ImageButton) findViewById(R.id.lightButton1Off);

        if (recv == null) {     // 값이 null 일 경우 return (예외 처리)
            Toast.makeText(getApplicationContext(), R.string.server_no_reply, Toast.LENGTH_SHORT).show();
            preState = false;   // 버튼 작동 불가
            return;
        }
        if (!recv.isEmpty()){
            if(recv.charAt(0) == '0'){
                lightButton1Off.setEnabled(false);
            }
            else{
                lightButton1On.setEnabled(false);
            }
        }

        lightButton1On = (ImageButton) findViewById(R.id.lightButton1On);
        lightButton1Off = (ImageButton) findViewById(R.id.lightButton1Off);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.action_light_settings:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + host + "/webpage/main_cctv.php"));
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showTimePickerDialog(View v) {
        Toast.makeText(getApplicationContext(), "시작 시간을 선택하신 후에 종료 시간을 선택해주세요. 시작 시간과 종료 시간은 같은 요일로 선택해주세요", Toast.LENGTH_SHORT).show();


        TimePickerFragment timePickerFragment = new TimePickerFragment(0, host);
        timePickerFragment.show(fragmentManager, "TimePicker");


//        if (fragment == null) {
//            fragment = TimePickerFragment.newInstance();
//            fragmentManager.beginTransaction()
//                    .add(R.id.fragment_container, fragment)
//                    .commit();
//        }
//        //Dialog dialog = createDialog();


        //dialog.show();


    }

    // 예약 기록 보기 삭제 되게 만들어야 함
    public void showLog(View v) {
//        FragmentManager manager3 = getFragmentManager();
//        android.app.FragmentTransaction transaction = manager.beginTransaction();
//        LightHistoryFragment lightHistoryFragment = new LightHistoryFragment();

        TimeLogFragment timeLogFragment = new TimeLogFragment(host);
        timeLogFragment.show(fragmentManager, "TimeLog");

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // 시간 예약 Dialog 생성
    public AlertDialog createDialog(){
        final View layoutView = getLayoutInflater().inflate(R.layout.dialog_timeselect, null);  // Custom Layout 사용
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.time_settings);
        builder.setView(layoutView);

        // 확인 버튼
        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // 날짜 계산
                SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
                Time t1 = new Time(hour1, minute1, 0);
                Time t2 = new Time(hour2, minute2, 0);

                // 데이터베이스 반영
                String result = "";
                PhpDown phpDown = new PhpDown();
                try {
                    result = phpDown.execute("http://" + host + mysqlURL_ins_time_bulb +"?weekday=" + weekday + "?t1=" + t1.toString() + "?t2=" + t2.toString()).get();
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

        });

        // 취소 버튼
        builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        // AlertDialog를 반환함
        return builder.create();
    }

    // 켜짐 시간 버튼
    public void startTimeButton_light(View v){

        // 현재 시간 정보 가져오기
        final Calendar c = Calendar.getInstance();
        final int hour = c.get(Calendar.HOUR_OF_DAY);
        final int minute = c.get(Calendar.MINUTE);
        final int day = c.get(Calendar.DAY_OF_WEEK);

        // 요일 시간 Picker 생성
        new SlideDayTimePicker.Builder(getSupportFragmentManager())
                .setListener(listener)
                .setInitialDay(day)
                .setInitialHour(hour)
                .setInitialMinute(minute)
                .setIs24HourTime(true)
                .build()
                .show();
    }

    // 꺼짐 시간 버튼
    public void stopTimeButton_light(View v){

        // 현재 시간 정보 가져오기
        final Calendar c = Calendar.getInstance();
        final int hour = c.get(Calendar.HOUR_OF_DAY);
        final int minute = c.get(Calendar.MINUTE);
        final int day = c.get(Calendar.DAY_OF_WEEK);

        // 요일 시간 Picker 생성
        new SlideDayTimePicker.Builder(getSupportFragmentManager())
                .setListener(listener2)
                .setInitialDay(day)
                .setInitialHour(hour)
                .setInitialMinute(minute)
                .setIs24HourTime(true)
                .build()
                .show();
    }

    public void initSlideDayTimeListner(){
        listener = new SlideDayTimeListener() {
            @Override
            public void onDayTimeSet(int day, int hour, int minute) {
                // Do something with the day, hour and minute
                // the user has selected

                // 변수
                weekday = day;
                hour1 = hour;
                minute1 = minute;

                //Toast.makeText(getApplicationContext(), "선택한 시간은 " + String.valueOf(day) + " " + String.valueOf(hour) + " " + String.valueOf(minute), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDayTimeCancel() {
                // The user has canceled the dialog.
                // This override is optional

                super.onDayTimeCancel();
            }
        };
    }

    public void initSlideDayTimeListner2(){
        listener2 = new SlideDayTimeListener() {
            @Override
            public void onDayTimeSet(int day, int hour, int minute) {
                // Do something with the day, hour and minute
                // the user has selected

                // 요일은 의미 없음
                hour2 = hour;
                minute2 = minute;
                //Toast.makeText(getApplicationContext(), "선택한 시간은 " + String.valueOf(day) + " " + String.valueOf(hour) + " " + String.valueOf(minute), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDayTimeCancel() {
                // The user has canceled the dialog.
                // This override is optional

                super.onDayTimeCancel();
            }
        };
    }

    public void light1_Click() {
        if (preState) {             // 현재 상태가 True 일 경우, 이 값이 False인 경우는 서버가 연결되지 않은 경우
            String recvData = null;
            TCPClient tc = new TCPClient(host, port, '1', getApplicationContext());
            try {
                recvData = tc.execute(this).get(time, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                Toast.makeText(getApplicationContext(), R.string.server_delay, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            if(recvData != null){
                Toast.makeText(getApplication(), R.string.multitap_send_complete, Toast.LENGTH_SHORT).show();
                lightButton1On.setEnabled(false);
                lightButton1Off.setEnabled(true);
            }
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.server_no_reply, Toast.LENGTH_SHORT).show();
        }
    }

    public void light2_Click() {
        if (preState) {
            String recvData = null;
            TCPClient tc = new TCPClient(host, port, '2', getApplicationContext());
            try {
                recvData = tc.execute(this).get(time, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                Toast.makeText(getApplicationContext(), R.string.server_delay, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            if(recvData != null){
                Toast.makeText(getApplication(), R.string.multitap_send_complete, Toast.LENGTH_SHORT).show();
                lightButton1On.setEnabled(true);
                lightButton1Off.setEnabled(false);
            }
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.server_no_reply, Toast.LENGTH_SHORT).show();
        }
    }

    // 멀티탭 1 On 버튼 클릭시 실행 함수
    public void light1_onClicked(View v) throws ExecutionException, InterruptedException {
        light1_Click();
    }

    // 멀티탭 1 Off 버튼 클릭시 실행 함수
    public void light2_onClicked(View v) throws ExecutionException, InterruptedException{
        light2_Click();
    }
}
