package com.onaries.smarthome;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jjobes.slidedaytimepicker.SlideDayTimeListener;
import com.github.jjobes.slidedaytimepicker.SlideDayTimePicker;
import com.onaries.smarthome.fragment.TimeLogFragment2;
import com.onaries.smarthome.fragment.TimePickerFragment2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MultitapActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    // 서버 주소와 포트 번호
    private String host;
    private String strPort;
    private long time;
    private int port;
    private String recv = null;
    private Boolean preState = true;
    private String[] relName;
    private int[] relState;

    ImageButton multitap_btn1_on;
    ImageButton multitap_btn2_on;
    ImageButton multitap_btn3_on;
    ImageButton multitap_btn1_off;
    ImageButton multitap_btn2_off;
    ImageButton multitap_btn3_off;
    ImageButton multitap_btn_all_on;
    ImageButton multitap_btn_all_off;

    private TextView multitap1_textView;
    private TextView multitap2_textView;
    private TextView multitap3_textView;

    // 음성 인식
    private static Intent intent;
    private SpeechRecognizer mRecognizer;

    // 음성 인식 리소스
    private LinearLayout voiceLinearLayout;
    private TextView voiceText;


    private SlideDayTimeListener slideDayTimeListener, slideDayTimeListener2;
    private int weekday;
    private int hour1, hour2;
    private int minute1, minute2;

    // DialogFragment
    private Fragment fragment;
    private FragmentManager fragmentManager;

    final private String mysqlURL_sel_relay = "/sql/mysql_sel_relay.php";
    final private String mysqlURL_upd_relay_name = "/sql/mysql_upd_relay_name.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multitap);

        setTitle(R.string.title_activity_multitap);                                       // Title 설정
        intent = getIntent();                                        // Intent 객체
        // Host, Port 정보 불러오기
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        host = prefs.getString("server_ip", "127.0.0.1");
        strPort = prefs.getString("server_port2", "12346");
        port = Integer.parseInt(strPort);
        String sTime = prefs.getString("server_time", "5000");              // 시간 구하기
        time = Long.parseLong(sTime);                                       // 시간 파싱

        fragmentManager = getSupportFragmentManager();

        initSlideDayTimeListner();
        initSlideDayTimeListner2();

        voiceLinearLayout = (LinearLayout) findViewById(R.id.voiceLinearLayout);
        voiceText = (TextView) findViewById(R.id.textView5);

        multitap_btn1_on = (ImageButton) findViewById(R.id.button5);
        multitap_btn2_on = (ImageButton) findViewById(R.id.button7);
        multitap_btn3_on = (ImageButton) findViewById(R.id.button9);
        multitap_btn1_off = (ImageButton) findViewById(R.id.button6);
        multitap_btn2_off = (ImageButton) findViewById(R.id.button8);
        multitap_btn3_off = (ImageButton) findViewById(R.id.button10);
        multitap_btn_all_on = (ImageButton) findViewById(R.id.button11);
        multitap_btn_all_off = (ImageButton) findViewById(R.id.button12);

        // 멀티탭 이름 가져오기
        PhpDown phpDown = new PhpDown();
        String result = "";
        try {
            result = phpDown.execute("http://" + host + mysqlURL_sel_relay).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        try{
            JSONArray ja = new JSONArray(result);

            relName = new String[ja.length()];
            relState = new int[ja.length()];

            for(int i = 0; i < ja.length(); i++){
                JSONObject jo = ja.getJSONObject(i);
                relName[i] = jo.getString("RELAY_NAME");
                relState[i] = jo.getInt("STATE");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (relName != null){
            final SharedPreferences.Editor ed = prefs.edit();
            ed.putString("multitap1_name", relName[0]);
            ed.putString("multitap2_name", relName[1]);
            ed.putString("multitap3_name", relName[2]);
            ed.commit();
        }

        if (relState.length != 0){
            if(relState[0] == 0){
                multitap_btn1_off.setEnabled(false);
                Log.d("DEBUG", "1 off");
            }
            else {
                multitap_btn1_on.setEnabled(false);
                Log.d("DEBUG", "1 on");
            }
            if (relState[1] == 0) {                // 두번째 글자가 0일 경우
                multitap_btn2_off.setEnabled(false);    // 멀티탭 버튼 2 Off 를 비활성화
            }
            else {                                      // 두번째 글자가 1일 경우
                multitap_btn2_on.setEnabled(false);     // 멀티탭 버튼 2 On 을 비활성화
            }

            if (relState[2] == 0) {                // 세번째 글자가 0일 경우
                multitap_btn3_off.setEnabled(false);    // 멀티탭 버튼 3 Off 를 비활성화
            }
            else {                                      // 세번째 글자가 1일 경우
                multitap_btn3_on.setEnabled(false);     // 멀티탭 버튼 3 On 을 비활성화
            }

        }

        multitap1_textView = (TextView) findViewById(R.id.textView9);
        multitap2_textView = (TextView) findViewById(R.id.textView11);
        multitap3_textView = (TextView) findViewById(R.id.textView10);
        multitap1_textView.setText(prefs.getString("multitap1_name", "콘센트 1"));     // 멀티탭 1 텍스트에 설정된 값으로 설정
        multitap2_textView.setText(prefs.getString("multitap2_name", "콘센트 2"));     // 멀티탭 2 텍스트에 설정된 값으로 설정
        multitap3_textView.setText(prefs.getString("multitap3_name", "콘센트 3"));     // 멀티탭 3 텍스트에 설정된 값으로 설정

        multitap1_textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                   // 리스너 설정
                showInputDialog("multitap1_name");                      // Dialog 실행 및 저장 함수 호출
            }
        });
        multitap2_textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog("multitap2_name");
            }
        });
        multitap3_textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog("multitap3_name");
            }
        });

        // 음성 인식 기능이 설정되어 있으면 음성 인식 기능 사용
        if (prefs.getBoolean("multitap_voice_state", true)) {
            voiceLinearLayout.setVisibility(View.VISIBLE);

            intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-kR");

            mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            mRecognizer.setRecognitionListener(listener);
            mRecognizer.startListening(intent);
        }


    }



    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {
            String msg = null;

            //내가 만든 activity에서 넘어오는 오류 코드를 분류
            switch(error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    msg = "오디오 입력 중 오류가 발생했습니다.";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    msg = "단말에서 오류가 발생했습니다.";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    msg = "권한이 없습니다.";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    msg = "네트워크 오류가 발생했습니다.";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    msg = "일치하는 항목이 없습니다.";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    msg = "음성인식 서비스가 과부하 되었습니다.";
                    mRecognizer.stopListening();
                    mRecognizer.cancel();
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    msg = "서버에서 오류가 발생했습니다.";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    msg = "입력이 없습니다.";
                    voiceText.setText("다시 한번 말해주세요");

                    break;
            }
            Log.i("Speech", msg);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mRecognizer != null){
                        mRecognizer.startListening(intent);
                        voiceText.setText("음성인식 중");
                    }


                }
            }, 1000);
        }

        // 음성 인식 결과
        @Override
        public void onResults(Bundle results) {
//            String[] sKey = {"multitap1_name", "multitap2_name" , "multitap3_name", "all"};
//            String key = "";
//            boolean vState = false;      // 현재 음성인식 상태 확인
//            key = SpeechRecognizer.RESULTS_RECOGNITION;
//            final ArrayList<String> mResult = results.getStringArrayList(key);
//            String[] rs = new String[mResult.size()];
//            mResult.toArray(rs);
//            Log.i("Speech", rs[0]);
//
//            if (rs[0].equals(prefs.getString(sKey[0], "") + " 켜")) {
//                multitap_choice(sKey[0], true);
//                vState = true;
//            }
//            else if (rs[0].equals(prefs.getString(sKey[0], "") + " 꺼") || rs[0].equals(prefs.getString(sKey[0], "") + " 거")) {
//                multitap_choice(sKey[0], false);
//                vState = true;
//            }
//            if (rs[0].equals("전체 켜기") || rs[0].equals("전체 켜")) {
//                multitap_choice("all", true);
//            }
//            else if (rs[0].equals("전체 끄기") || rs[0].equals("전체 꺼")) {
//                multitap_choice("all", false);
//            }
//            else if (rs[0].equals("종료")) {
//                finish();
//            }
//            else if (vState == false){
//                Toast.makeText(getApplicationContext(), "정확하게 말해주십시오", Toast.LENGTH_SHORT).show();
//            }

            String voice = voice_choice(results);
            voiceText.setText(voice);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mRecognizer != null)
                        mRecognizer.startListening(intent);
                        voiceText.setText("음성인식 중");
                }
            }, 1000);

        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onDestroy() {
        if (mRecognizer != null)
            mRecognizer.destroy();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_multitab, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String voice_choice(Bundle results) {
        String[] sKey = {"multitap1_name", "multitap2_name" , "multitap3_name", "all"};
        String[] sNum = {"일번 켜", "일번 꺼", "일번 거", "이번 켜", "이번 꺼", "이번 거", "삼번 켜", "삼번 꺼", "삼번 거"};
        String key = "";
        boolean vState = false;      // 현재 음성인식 상태 확인
        key = SpeechRecognizer.RESULTS_RECOGNITION;
        final ArrayList<String> mResult = results.getStringArrayList(key);
        String[] rs = new String[mResult.size()];
        mResult.toArray(rs);
        Log.i("Speech", rs[0]);

        if (rs[0].equals(prefs.getString(sKey[0], "") + " 켜") || rs[0].equals(sNum[0])) {
            multitap_choice(sKey[0], true);
            vState = true;
        }
        else if (rs[0].equals(prefs.getString(sKey[0], "") + " 꺼") || rs[0].equals(prefs.getString(sKey[0], "") + " 거") || rs[0].equals(sNum[1]) || rs[0].equals(sNum[2])) {
            multitap_choice(sKey[0], false);
            vState = true;
        }

        if (rs[0].equals(prefs.getString(sKey[1], "") + " 켜") || rs[0].equals(sNum[3])) {
            multitap_choice(sKey[1], true);
            vState = true;
        }
        else if (rs[0].equals(prefs.getString(sKey[1], "") + " 꺼") || rs[0].equals(prefs.getString(sKey[1], "") + " 거")|| rs[0].equals(sNum[4]) || rs[0].equals(sNum[5])) {
            multitap_choice(sKey[1], false);
            vState = true;
        }

        if (rs[0].equals(prefs.getString(sKey[2], "") + " 켜") || rs[0].equals(sNum[6])) {
            multitap_choice(sKey[2], true);
            vState = true;
        }
        else if (rs[0].equals(prefs.getString(sKey[2], "") + " 꺼") || rs[0].equals(prefs.getString(sKey[2], "") + " 거")|| rs[0].equals(sNum[7]) || rs[0].equals(sNum[8])) {
            multitap_choice(sKey[2], false);
            vState = true;
        }



        if (rs[0].equals("전체 켜기") || rs[0].equals("전체 켜")) {
            multitap_choice("all", true);
        }
        else if (rs[0].equals("전체 끄기") || rs[0].equals("전체 꺼")) {
            multitap_choice("all", false);
        }
        else if (rs[0].equals("종료")) {
            finish();
        }
        else if (vState == false){
            Toast.makeText(getApplicationContext(), "정확하게 말해주십시오", Toast.LENGTH_SHORT).show();
        }

        return rs[0];
    }

    // 멀티탭 켜는 함수
    public void multitap_choice(String s, boolean state) {
        if (s.equals("multitap1_name")) {
            if (state == true)
                multitap1_Click();
            else
                multitap4_Click();

        }
        else if (s.equals("multitap2_name")) {
            if (state == true)
                multitap2_Click();
            else
                multitap5_Click();
        }
        else if (s.equals("multitap3_name")) {
            if (state == true)
                multitap3_Click();
            else
                multitab6_Click();
        }
        else if (s.equals("all")) {
            if (state == true)
                multitab7_Click();
            else
                multitap8_Click();

        }
    }


    public void multitap1_Click() {
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
                multitap_btn1_on.setEnabled(false);
                multitap_btn1_off.setEnabled(true);
            }
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.server_no_reply, Toast.LENGTH_SHORT).show();
        }
    }

    public void multitap2_Click() {
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
                multitap_btn2_on.setEnabled(false);
                multitap_btn2_off.setEnabled(true);
            }
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.server_no_reply, Toast.LENGTH_SHORT).show();
        }
    }

    public void multitap3_Click() {
        if (preState) {
            String recvData = null;
            TCPClient tc = new TCPClient(host, port, '3', getApplicationContext());
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
                multitap_btn3_on.setEnabled(false);
                multitap_btn3_off.setEnabled(true);
            }
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.server_no_reply, Toast.LENGTH_SHORT).show();
        }
    }

    public void multitap4_Click() {
        if (preState) {
            String recvData = null;
            TCPClient tc = new TCPClient(host, port, '4', getApplicationContext());
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
                multitap_btn1_on.setEnabled(true);
                multitap_btn1_off.setEnabled(false);
            }
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.server_no_reply, Toast.LENGTH_SHORT).show();
        }
    }

    public void multitap5_Click() {
        if (preState) {
            String recvData = null;
            TCPClient tc = new TCPClient(host, port, '5', getApplicationContext());
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
                multitap_btn2_on.setEnabled(true);
                multitap_btn2_off.setEnabled(false);
            }
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.server_no_reply, Toast.LENGTH_SHORT).show();
        }
    }

    public  void multitab6_Click() {
        if (preState) {
            String recvData = null;
            TCPClient tc = new TCPClient(host, port, '6', getApplicationContext());
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
            if (recvData != null) {
                Toast.makeText(getApplication(), R.string.multitap_send_complete, Toast.LENGTH_SHORT).show();
                multitap_btn3_on.setEnabled(true);
                multitap_btn3_off.setEnabled(false);
            }
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.server_no_reply, Toast.LENGTH_SHORT).show();
        }
    }

    public void multitab7_Click() {
        if (preState) {
            String recvData = null;
            TCPClient tc = new TCPClient(host, port, '7', getApplicationContext());
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
                multitap_btn1_on.setEnabled(false);
                multitap_btn1_off.setEnabled(true);
                multitap_btn2_on.setEnabled(false);
                multitap_btn2_off.setEnabled(true);
                multitap_btn3_on.setEnabled(false);
                multitap_btn3_off.setEnabled(true);
            }
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.server_no_reply, Toast.LENGTH_SHORT).show();
        }
    }

    public void multitap8_Click() {
        if (preState) {
            String recvData = null;
            TCPClient tc = new TCPClient(host, port, '8', getApplicationContext());
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
                multitap_btn1_on.setEnabled(true);
                multitap_btn1_off.setEnabled(false);
                multitap_btn2_on.setEnabled(true);
                multitap_btn2_off.setEnabled(false);
                multitap_btn3_on.setEnabled(true);
                multitap_btn3_off.setEnabled(false);
            }
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.server_no_reply, Toast.LENGTH_SHORT).show();
        }

    }
    // 멀티탭 1 On 버튼 클릭시 실행 함수
    public void multitap1_onClicked(View v) throws ExecutionException, InterruptedException {
        multitap1_Click();
    }

    // 멀티탭 1 Off 버튼 클릭시 실행 함수
    public void multitap2_onClicked(View v) throws ExecutionException, InterruptedException{
        multitap2_Click();
    }

    // 멀티탭 2 On 버튼 클릭시 실행 함수
    public void multitap3_onClicked(View v) throws ExecutionException, InterruptedException{
        multitap3_Click();
    }

    // 멀티탭 2 Off 버튼 클릭시 실행 함수
    public void multitap4_onClicked(View v) throws ExecutionException, InterruptedException{
        multitap4_Click();
    }

    // 멀티탭 3 On 버튼 클릭시 실행 함수
    public void multitap5_onClicked(View v) throws ExecutionException, InterruptedException{
        multitap5_Click();
    }

    // 멀티탭 3 Off 버튼 클릭시 실행 함수
    public void multitap6_onClicked(View v) throws ExecutionException, InterruptedException{
        multitab6_Click();
    }

    // 멀티탭 All On 버튼 클릭시 실행 함수
    public void multitap7_onClicked(View v) throws ExecutionException, InterruptedException{
        multitab7_Click();
    }

    // 멀티탭 All Off 버튼 클릭시 실행 함수
    public void multitap8_onClicked(View v) throws ExecutionException, InterruptedException{
        multitap8_Click();
    }

    // 새로고침 버튼 클릭시 실행 함수
    public void multitap_btn_status_onClicked(View v) throws InterruptedException {

        // TCP Client 시작
        String recvData = null;
        TCPClient tc = new TCPClient(host, port, '9', getApplicationContext());
        try {
            recvData = tc.execute(this).get(time, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            Toast.makeText(getApplicationContext(), R.string.server_delay, Toast.LENGTH_SHORT).show();
            e.printStackTrace();

            return;
        }

        if (!recvData.isEmpty()) {
            if (recv.charAt(0) == '0') {
                multitap_btn1_off.setEnabled(false);
                multitap_btn1_on.setEnabled(true);
            }
            else {
                multitap_btn1_on.setEnabled(false);
            }

            if (recv.charAt(1) == '0') {
                multitap_btn2_off.setEnabled(false);
                multitap_btn2_on.setEnabled(true);
            }
            else {
                multitap_btn2_on.setEnabled(false);
            }

            if (recv.charAt(2) == '0') {
                multitap_btn3_off.setEnabled(false);
                multitap_btn3_on.setEnabled(true);
            }
            else {
                multitap_btn3_on.setEnabled(false);
            }

            Toast.makeText(getApplicationContext(), "현재 상태를 불러왔습니다", Toast.LENGTH_SHORT).show();
        }

    }

    // Dialog 표시 함수
    protected void showInputDialog(String name) {
        LayoutInflater layoutInflater = LayoutInflater.from(MultitapActivity.this);
        View promptView = layoutInflater.inflate(R.layout.multiname_input, null);
        AlertDialog.Builder aBuilder = new AlertDialog.Builder(MultitapActivity.this);
        aBuilder.setView(promptView);
        aBuilder.setTitle(R.string.multitap_name_change);
        aBuilder.setIcon(R.drawable.multitab_01);
        aBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                }

                return false;
            }
        });

        final SharedPreferences.Editor ed = prefs.edit();
        final String mName = name;
        final EditText editText = (EditText) promptView.findViewById(R.id.multiname);


        aBuilder.setCancelable(false).setPositiveButton(getString(R.string.monitor_change), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int relay_no = 0;
                String result = editText.getText().toString();
                if (result.equals("")) {
                    Toast.makeText(getApplicationContext(), "빈칸으로 입력할 수 없습니다", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mName == "multitap1_name"){
                    multitap1_textView.setText(result);
                    relay_no = 1;
                }
                else if (mName == "multitap2_name") {
                    multitap2_textView.setText(result);
                    relay_no = 2;
                }
                else if (mName == "multitap3_name") {
                    multitap3_textView.setText(result);
                    relay_no = 3;
                }

                ed.putString(mName, result);
                ed.commit();

                String phpResult = "";
                PhpDown phpDown = new PhpDown();
                try {
                    phpResult = phpDown.execute("http://" + host + mysqlURL_upd_relay_name + "?relay_no=" + relay_no + "&relay_name=" + result).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                if (phpResult != ""){
                    Toast.makeText(getApplicationContext(), "에러", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "완료되었습니다", Toast.LENGTH_SHORT).show();
                }


            }
        }).setNegativeButton(getString(R.string.monitor_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = aBuilder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        alertDialog.show();
    }

    // 켜짐 시간 버튼
    public void startTimeButtonMulti(View v){

        // 현재 시간 정보 가져오기
        final Calendar c = Calendar.getInstance();
        final int hour = c.get(Calendar.HOUR_OF_DAY);
        final int minute = c.get(Calendar.MINUTE);
        final int day = c.get(Calendar.DAY_OF_WEEK);

        // 요일 시간 Picker 생성
        new SlideDayTimePicker.Builder(fragmentManager)
                .setListener(slideDayTimeListener)
                .setInitialDay(day)
                .setInitialHour(hour)
                .setInitialMinute(minute)
                .setIs24HourTime(true)
                .build()
                .show();
    }

    // 꺼짐 시간 버튼
    public void stopTimeButtonMulti(View v){

        // 현재 시간 정보 가져오기
        final Calendar c = Calendar.getInstance();
        final int hour = c.get(Calendar.HOUR_OF_DAY);
        final int minute = c.get(Calendar.MINUTE);
        final int day = c.get(Calendar.DAY_OF_WEEK);

        // 요일 시간 Picker 생성
        new SlideDayTimePicker.Builder(fragmentManager)
                .setListener(slideDayTimeListener2)
                .setInitialDay(day)
                .setInitialHour(hour)
                .setInitialMinute(minute)
                .setIs24HourTime(true)
                .build()
                .show();
    }

    public void initSlideDayTimeListner(){
        slideDayTimeListener = new SlideDayTimeListener() {
            @Override
            public void onDayTimeSet(int day, int hour, int minute) {
                // Do something with the day, hour and minute
                // the user has selected

                // 변수
                weekday = day;
                hour1 = hour;
                minute1 = minute;

                Toast.makeText(getApplicationContext(), "선택한 시간은 " + String.valueOf(day) + " " + String.valueOf(hour) + " " + String.valueOf(minute), Toast.LENGTH_SHORT).show();
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
        slideDayTimeListener2 = new SlideDayTimeListener() {
            @Override
            public void onDayTimeSet(int day, int hour, int minute) {
                // Do something with the day, hour and minute
                // the user has selected

                // 요일은 의미 없음
                hour2 = hour;
                minute2 = minute;
                Toast.makeText(getApplicationContext(), "선택한 시간은 " + String.valueOf(day) + " " + String.valueOf(hour) + " " + String.valueOf(minute), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDayTimeCancel() {
                // The user has canceled the dialog.
                // This override is optional

                super.onDayTimeCancel();
            }
        };
    }

    public void showTimePickerDialogMulti(View v) {
        Toast.makeText(getApplicationContext(), "시작 시간을 선택하신 후에 종료 시간을 선택해주세요. 시작 시간과 종료 시간은 같은 요일로 선택해주세요", Toast.LENGTH_SHORT).show();

        TimePickerFragment2 timePickerFragment2 = new TimePickerFragment2();
        timePickerFragment2.show(fragmentManager, "TimePicker");
    }

    public void showLogMulti(View v){
        TimeLogFragment2 timeLogFragment2 = new TimeLogFragment2();
        timeLogFragment2.show(fragmentManager, "TimeLog");
    }
}
