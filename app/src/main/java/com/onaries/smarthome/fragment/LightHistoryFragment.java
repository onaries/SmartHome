package com.onaries.smarthome.fragment;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.onaries.smarthome.LightActivity;
import com.onaries.smarthome.PhpDown_noThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by SW on 2016-06-05.
 */
public class LightHistoryFragment extends Fragment {

    private SharedPreferences prefs;
    private String host;

    private int[] relay_no;
    private int[] weekday;
    private String[] start_time;
    private String[] stop_time;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        host = prefs.getString("server_ip", "127.0.0.1");

        // 데이터베이스로부터 예약 설정 값 불러오기
        getReservedData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // View 만들기





        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void getReservedData(){
        new AsyncTask<Object, Object, Object>() {

            @Override
            protected Object doInBackground(Object... params) {

                try {
                    String jsonHtml;
                    PhpDown_noThread phpTask = new PhpDown_noThread("http://" + host + "/getLightReservedData.php");
                    jsonHtml = phpTask.phpTask();

                    JSONArray jo = new JSONArray(jsonHtml);
                    relay_no = new int[jo.length()];
                    weekday = new int[jo.length()];
                    start_time = new String[jo.length()];
                    stop_time = new String[jo.length()];

                    for(int i = 0; i < jo.length(); i++) {
                        JSONObject object = jo.getJSONObject(i);
                        relay_no[i] = object.getInt("RELAY_NO");
                        weekday[i] = object.getInt("WEEKDAY");
                        start_time[i] = object.getString("START_TIME");
                        stop_time[i] = object.getString("STOP_TIME");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }
}
