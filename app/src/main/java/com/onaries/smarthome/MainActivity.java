package com.onaries.smarthome;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends Activity {

    private BackPressCloseHandler backPressCloseHandler;
    private Dialog dialog2;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    // SharedPreferences에 저장할 때 key 값으로 사용됨.
    public static final String PROPERTY_REG_ID = "REG_ID";

    // SharedPreferences에 저장할 때 key 값으로 사용됨.
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String TAG = "SmartHome";

    private static final String serverIP = "server_ip";
    private static final String serverPort = "server_port";
    private static final String serverTime = "server_time";
    private static final String regID = "reg_id";

    // 폴더 스트링
    private static final String dirPath = "/SmartHome";

    String SENDER_ID = "581389340254";  // GCM 서버 주소

    private GoogleCloudMessaging gcm;
    private Context context;
    private String regid;
    private String host;
    private String port;

    private NotificationManager mNotificationManager;
    public static final int NOTIFICATION_ID = 1;
    private SharedPreferences prefs;

    // Version name
    private String versionName;
    private TextView txtAppVer;

    // timeout
    private String sTimeout;
    long timeout;

    // Resource
    private ImageButton lightButton;
    private ImageButton multiButton;
    private ImageButton monitorButton;
    private ImageButton cctvButton;
    private ImageButton cloudButton;
    private ImageButton settingButton;

    // Animation 관련
    private int activityState = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);              // No ActionBar

        super.onCreate(savedInstanceState);                         // OnCreate
        setContentView(R.layout.activity_main);                     // SetContentView

        backPressCloseHandler = new BackPressCloseHandler(this);    // BackButton Handler
        context = getApplicationContext();                          // context

        prefs = PreferenceManager.getDefaultSharedPreferences(this);    // 환경설정 값 불러오기
        //sendNotification("시작되었습니다 ");

        host = prefs.getString(serverIP, "127.0.0.1");           // Server 주소 설정
        port = prefs.getString(serverPort, "5005");              // Server Port 설정 (TCP Server)

        // timeout 관련
        sTimeout = prefs.getString(serverTime, "5000");          // Timeout의 String 변수
        timeout = Long.parseLong(sTimeout);                         // long로 변환 (timeout)

        if (checkPlayServices()) {                                  // GCM기능을 위해서 PlayService Check
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);
            SharedPreferences.Editor ed = prefs.edit();
            ed.putString(PROPERTY_REG_ID, regid);                         // 환경설정에 regID라는 키로 저장
            ed.commit();
            if (regid.isEmpty()) {
                registerInBackground();
            }
        }
        else {
            Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, R.string.play_no_service, Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Version Name
        versionName = BuildConfig.VERSION_NAME;                     // Version Name 구하기

        // UI
        txtAppVer = (TextView) findViewById(R.id.appVer);           // TextView 초기화
        txtAppVer.setText("Version " + versionName);                // 텍스트 설정

        // 폴더 관련 설정 (그래프 저장)
        OutputStreamWriter out;                                                             // 파일 생성 관련 객체
        File path = new File(Environment.getExternalStorageDirectory() + dirPath);     // 새로운 파일 정의
        if (!path.exists()) {                                                               // 폴더가 없을 경우
            path.mkdirs();                                                                  // 폴더 생성
        }

        // 애니메이션 설정
        lightButton = (ImageButton) findViewById(R.id.imageButton);                         // 이미지 버튼 1 (조명 제어)
        multiButton = (ImageButton) findViewById(R.id.imageButton2);                        // 이미지 버튼 2 (멀티탭 제어)
        monitorButton = (ImageButton) findViewById(R.id.imageButton3);                      // 이미지 버튼 3 (모니터링)
        cctvButton = (ImageButton) findViewById(R.id.imageButton4);
        cloudButton = (ImageButton) findViewById(R.id.imageButton6);
        settingButton = (ImageButton) findViewById(R.id.imageButton7);                      // 이미지 버튼 6 (설정)

        if (activityState == 1) {                                                       // 애니메이션 및 설정값을 불러오는 작업을 한번만 하기위해 사용
            setSettingValue();                                                          // 설정값 불러오기 함수 호출
            activityState++;                                                            // 액티비티 상태 증가
        }

    }

    // 윈도우가 현재 상태로 바뀔 경우
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {                                // 애니메이션 효과 사용하기 위함
        if (activityState == 2) {
            animationButton();                                                          // 애니메이션 효과 함수 호출
            activityState++;
        }
        super.onWindowFocusChanged(hasFocus);
    }

    // Reg ID 값을 얻오는 함수
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");

        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }

        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    // Reg ID 값을 저장하기 위해 SharedPreferences를 얻는 함수
    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    // 앱버전 얻는 함수
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    // Reg ID 등록 함수
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    Log.i(TAG, "regid : " + regid);

                    // 서버에 발급받은 등록 아이디를 전송한다.
                    // 등록 아이디는 서버에서 앱에 푸쉬 메시지를 전송할 때 사용된다.
                    sendRegistrationIdToBackend();

                    // 등록 아이디를 저장해 등록 아이디를 매번 받지 않도록 한다.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                // mDisplay.append(msg + "\n");
            }

        }.execute(null, null, null);
    }

    // Reg ID 저장 함수
    private void storeRegistrationId(Context context, String regid) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regid);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private void sendRegistrationIdToBackend() {

    }

    /**
     * 기기가 Google Play Services APK 가지고 있는지를 확인합니다.
     * 만약 그렇지 않다면 다이얼로그를 띄워 사용자가 APK를 Google Play Store
     * 에서 다운받도록 하거나 기기의 시스템 설정에서 이를 사용할 수 있도록
     * 하게 합니다.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    // 아이폰 스타일의 투명한 로딩창 표시하기
    public void showTransLoadingDialog() {
        //스타일 리소스를 적용한 다이얼로그 객체
        dialog2 = new Dialog(this, R.style.trans_dialog);
        //프로그레스바 컴포넌트를 레이아웃 XML 없이 직접 생성
        //--> 파라미터는 Context객체를 요구하므로, Activity를 전달한다.
        ProgressBar pb = new ProgressBar(this);

        //다이얼로그에 프로그레스바 추가
        //--> 파라미터: 프로그레스바 컴포넌트, 가로/세로 사이즈 정보를 갖는 객체
        dialog2.addContentView(pb,
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        //창이 닫힐 경우의 이벤트 처리
        dialog2.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Toast.makeText(getApplicationContext(), R.string.loading_cancel, Toast.LENGTH_SHORT).show();
            }
        });
        dialog2.show();
    }

    // 설정값 불러오기 함수
    public void setSettingValue() {

        new AsyncTask<Object, Object, Boolean> () {                     // 네트워크 연결을 위해 비동기식 쓰레드 사용

            String[] data = new String[6];                              // 데이터 저장 스트링 배열 생성

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Boolean doInBackground(Object... params) {        // 백그라운드 실행
                try {
                    String jsonHtml;                                    // JSON 값을 얻어오기 위한 변수
                    PhpDown_noThread phpTask = new PhpDown_noThread("http://" + host + "/mysql_test10.php");    // 이 경로의 php의 JSON값을 얻어오기 위해 사용
                    jsonHtml = phpTask.phpTask();                       // 실제 수행 부분
                    JSONArray jo = new JSONArray(jsonHtml);             // JSON Array로 변환

                    for(int i = 0; i < jo.length(); i++) {              // 반복 실행
                        JSONObject object = jo.getJSONObject(i);        // 한 행마다 JSON 객체를 가져옴
                        data[i] = object.getString("value");            // Value값을 data 배열에 삽입
                    }
                } catch (JSONException e) {                             // JSON 예외
                    e.printStackTrace();
                    return false;
                }

                SharedPreferences.Editor ed = prefs.edit();             // 환경설정 변경 기능 활성화
                ed.putString("gas_high", data[0]);                      // 가스값 저장
                ed.putString("humi_high", data[1]);                     // 습도 상한값 저장
                ed.putString("humi_low", data[2]);                      // 습도 하한값 저장
                ed.putString("temp_high", data[3]);                     // 온도 상한값 저장
                ed.putString("temp_low", data[4]);                      // 온도 하한값 저장
                ed.putString("update_time", data[5]);                   // 업데이트 주기 저장
                ed.apply();                                             // 적용

                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (result) {   // 만약 결과가 true 이면
                    Toast.makeText(getApplicationContext(), "설정값을 불러왔습니다", Toast.LENGTH_SHORT).show();  // 알맞는 토스트 메시지 출력
                }
                else {
                    Toast.makeText(getApplicationContext(), "서버 설정이 필요합니다", Toast.LENGTH_SHORT).show(); // 알맞는 토스트 메시지 출력
                }
            }
        }.execute();
    }

    private void animationButton() {
        Animation animation, animation2, animation3, animation4, animation5, animation6;
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha);    // anim의 alpha 불러옴
        animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha2);  // anim의 alpha2 불러옴
        animation3 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha3);  // anim의 alpha2 불러옴
        animation4 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha4);  // anim의 alpha2 불러옴
        animation5 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha5);  // anim의 alpha2 불러옴
        animation6 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha6);  // anim의 alpha2 불러옴
        //animation = new AlphaAnimation(0.0f, 1.0f);
        //animation.setDuration(500);

        lightButton.startAnimation(animation);                                              // 이미지 버튼 1의 애니메이션 시작
        multiButton.startAnimation(animation2);                                             // 이미지 버튼 2의 애니메이션 시작
        monitorButton.startAnimation(animation3);                                            // 이미지 버튼 3의 애니메이션 시작
        cctvButton.startAnimation(animation4);                                            // 이미지 버튼 4의 애니메이션 시작
        cloudButton.startAnimation(animation5);
        settingButton.startAnimation(animation6);
    }

    // 뒤로가기 버튼 함수
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        backPressCloseHandler.onBackPressed();          // 뒤로가기 버튼 설정
    }

    // OnPause
    @Override
    protected void onPause() {
        if (dialog2 != null) {
            dialog2.dismiss();
        }
        super.onPause();
    }

    // OnStart
    @Override
    protected void onStart() {
        super.onStart();
    }

    // OnResume 액티비티가 다시 불러졌을 경우
    @Override
    protected void onResume() {

        super.onResume();
        checkPlayServices();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        host = prefs.getString(serverIP, "127.0.0.1");      // 서버 IP 불러옴
        port = prefs.getString(serverPort, "5005");         // 서버 포트 불러옴

        // timeout
        sTimeout = prefs.getString(serverTime, "5000");     // 서버 Timeout 불러옴
        timeout = Long.parseLong(sTimeout);                 // Long타입으로 변환
    }

    // OnCreateOptionsMenu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return false;
    }

    // OnOptionsItemSelected
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

    // 이미지 버튼 1 (조명 제어) 클릭시 실행 함수
    public void setImageButton1_onClick(View v){
        // LightActivity 연결
        Intent intent = new Intent(MainActivity.this, LightActivity.class);                         // 인텐트 생성
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);             // 하나의 액티비티만을 실행
        Toast.makeText(getApplicationContext(), "추후 지원 예정입니다", Toast.LENGTH_SHORT).show();  // 토스트 메시지 출력
        startActivity(intent);                                                                      // 액티비티 실행
    }

    // 이미지 버튼 2 (멀티탭 제어) 클릭시 실행 함수
    public void setImageButton2_onClick(View v){
        // MultitapActivity 연결

        // 새로운 스레드 생성 및 실행
        new AsyncTask<Object, Object, Boolean>() {      // 네트워크 작업을 위한 비동기식 쓰레드 생성

            ProgressDialog progressDialog;              // ProgressDialog 객체 선언

            // 스레드 실행 전 UI 처리
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showTransLoadingDialog();               // 로딩 함수
            }

            // 실제 Background에서 작동하는 부분
            @Override
            protected Boolean doInBackground(Object[] params) {

                // 인터넷 연결 확인
                ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnected()) {     // 인터넷 연결이 정상적이라면

                    //tcp 연결
                    TCPClient_noThread tcpTask = new TCPClient_noThread(host, Integer.parseInt(port), '9', getApplicationContext());    // TCP를 통해 서버에 9라는 메시지를 가진 패킷을 보냄
                    String recvData = tcpTask.tcpTask();        // 서버로부터 값을 얻어옴


                    Intent intent = new Intent(MainActivity.this, MultitapActivity.class);          // 인텐트 생성
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP); // 하나의 액티비티만을 실행
                    intent.putExtra("Status", recvData);                                            // 인텐트에 보낼 변수 추가
                    startActivity(intent);                                                          // 액티비티 실행

                    return true;
                }
                else {
                    publishProgress();                                                              // 인터넷 연결이 안되어 있을 경우 실행
                }
                // Host, Port 정보 불러오기


                return false;
            }

            @Override
            protected void onPostExecute(Boolean o) {
                super.onPostExecute(o);
                dialog2.dismiss();          // 로딩창 제거
                //progressDialog.dismiss();
            }

            @Override
            protected void onProgressUpdate(Object[] values) {
                Toast.makeText(getApplicationContext(), R.string.main_internet, Toast.LENGTH_SHORT).show(); // 알맞는 토스트 메시지 출력
                super.onProgressUpdate(values);
            }
        }.execute();


    }

    // 이미지 버튼 3 (모니터링) 클릭시 실행 함수
    public void setImageButton3_onClick(View v){
        // MonitorActivity 연결

        new AsyncTask<Object, Object, Object>() {       // 네트워크 작업을 위한 비동기식 쓰레드 생성

            ProgressDialog progressDialog;
            String[] tag = {"time", "temp", "humi", "gas", "photo"};
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
/*
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setTitle(getString(R.string.loading_title));
                progressDialog.setMessage(getString(R.string.loading_message));
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCanceledOnTouchOutside(false);

                progressDialog.show(); */
                showTransLoadingDialog();       // 로딩창 생성
            }

            // 백그라운드 동작 함수
            @Override
            protected Object doInBackground(Object[] params) {

                ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnected()) {     // 인터넷 연결이 정상적이라면
                    
                    PhpDown_noThread phpTask = new PhpDown_noThread("http://" + host + "/mysql_test3.php");     // JSON 형태의 값을 얻어오기 위함
                    String result = phpTask.phpTask();
                    String time = null;
                    String temp = null;
                    String humi = null;
                    String gas = null;
                    String photo = null;
                    try {

                        JSONArray jo = new JSONArray(result);

                        for(int i = 0; i < jo.length(); i++) {
                            JSONObject object = jo.getJSONObject(i);
                            time = object.getString(tag[0]);
                            temp = object.getString(tag[1]);
                            humi = object.getString(tag[2]);
                            gas = object.getString(tag[3]);
                            photo = object.getString(tag[4]);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(MainActivity.this, MonitorActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra(tag[0], time).putExtra(tag[1], temp).putExtra(tag[2], humi).putExtra(tag[3], gas).putExtra(tag[4], photo);
                    startActivity(intent);
                }
                else {
                    publishProgress();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                dialog2.dismiss();
                //progressDialog.dismiss();
            }

            @Override
            protected void onProgressUpdate(Object[] values) {

                Toast.makeText(getApplicationContext(), R.string.main_internet, Toast.LENGTH_SHORT).show();
                super.onProgressUpdate(values);
            }

        }.execute();
    }

    public void setImageButton4_onClick(View v){
        // CctvActivity 연결

    }

    public void setImageButton5_onClick(View v){
        // CloudActivity 연결

    }

    public void setImageButton6_onClick(View v){
        // SettingActivity 연결
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    public void HelpButton_onClick(View v){
        Intent intent = new Intent(MainActivity.this, HelpActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}
