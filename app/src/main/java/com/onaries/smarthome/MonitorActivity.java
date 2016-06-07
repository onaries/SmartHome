package com.onaries.smarthome;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import butterknife.Bind;

public class MonitorActivity extends AppCompatActivity implements OnChartValueSelectedListener, SwipeRefreshLayout.OnRefreshListener{

    private SharedPreferences prefs;
    private LineChart mChart;
    private int preState;
    private String jsonHtml;
    private String host;
    private final float photoHighVal = 1023f;

    @Bind(R.id.preTemp) TextView texPreTemp;
    @Bind(R.id.preHumi) TextView texPreHumi;
    @Bind(R.id.preGas) TextView texPreGas;
    @Bind(R.id.prePhoto) TextView texPrePhoto;
    @Bind(R.id.updateTime) TextView texUpdateTime;

    private String temp;
    private String humi;
    private String gas;
    private String photo;
    private String time;

    private SwipeRefreshLayout mSwipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_monitor);

        Intent intent = getIntent();
        setTitle(R.string.title_activity_monitor);
        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);

        mChart.setDescription("");
        mChart.setNoDataTextDescription(getString(R.string.chart_under_button_click));
        mChart.setTouchEnabled(true);
        mChart.setDragDecelerationFrictionCoef(0.9f);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);

        mChart.setDrawGridBackground(false);
        mChart.setHighlightPerDragEnabled(true);
        mChart.setPinchZoom(false);
        mChart.setBackgroundColor(Color.WHITE);

        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);

        mChart.setMarkerView(mv);
        preState = 1;   // 현재상태는 1

        Legend l = mChart.getLegend();

        l.setForm(Legend.LegendForm.LINE);
        l.setTextSize(11f);
        l.setTextColor(Color.BLACK);
        l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTextSize(12f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setSpaceBetweenLabels(1);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setAxisMaxValue(40f);
        leftAxis.setDrawGridLines(true);


        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setTextColor(Color.RED);
        rightAxis.setAxisMaxValue(100);
        rightAxis.setStartAtZero(false);
        rightAxis.setAxisMinValue(0);
        rightAxis.setDrawGridLines(false);

        // 서버값 가져오기
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        host = prefs.getString("server_ip", "127.0.0.1");


        // 실시간 값 가져오기
        /*
        PhpDown phpDown;
        JSONObject jsonHtm;
        phpDown = new PhpDown();

        try {
             jsonHtml = phpDown.execute("http://" + host + "/mysql_test3.php").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    */
        String sTime;
        temp = intent.getExtras().getString("temp");
        humi = intent.getExtras().getString("humi");
        gas = intent.getExtras().getString("gas");
        photo = intent.getExtras().getString("photo");
        time = intent.getExtras().getString("time");

        if (temp == null) {
            Toast.makeText(getApplicationContext(), "서버가 연결되지 않았습니다", Toast.LENGTH_SHORT).show();
            return;
        }
/*
        try {

            JSONArray jo = new JSONArray(jsonHtml);

            for(int i = 0; i < jo.length(); i++) {
                JSONObject object = jo.getJSONObject(i);
                time = object.getString("time");
                temp = object.getString("temp");
                humi = object.getString("humi");
                gas = object.getString("gas");
                photo = object.getString("photo");
            }

            texPreTemp.setText(temp + "℃");
            texPreHumi.setText(humi + "%");
            texPreGas.setText(gas);
            texPrePhoto.setText(photo);
            texUpdateTime.setText(" " + time);

        } catch (JSONException e) {
            e.printStackTrace();
        }
*/
        // 그래프 그리기

        texPreTemp.setText(temp + "℃");
        texPreHumi.setText(humi + "%");
        texPreGas.setText(gas);
        texPrePhoto.setText(photo);
        texUpdateTime.setText(" " + time);

        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeRefresh.setColorSchemeResources(R.color.refresh_color1, R.color.refresh_color2, R.color.refresh_color3, R.color.refresh_color4);
        mSwipeRefresh.setOnRefreshListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_monitor, menu);
        return true;
    }

    // 새로고침 관련
    @Override
    public void onRefresh() {
        new AsyncTask<Object, Object, Object>() {

            String time = null;
            String temp = null;
            String humi = null;
            String gas = null;
            String photo = null;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Object doInBackground(Object... params) {
                PhpDown_noThread phpTask = new PhpDown_noThread("http://" + host + "/mysql_test3.php");
                String result = phpTask.phpTask();
                try {

                    JSONArray jo = new JSONArray(result);

                    for(int i = 0; i < jo.length(); i++) {
                        JSONObject object = jo.getJSONObject(i);
                        time = object.getString("time");
                        temp = object.getString("temp");
                        humi = object.getString("humi");
                        gas = object.getString("gas");
                        photo = object.getString("photo");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                texPreTemp.setText(temp + "℃");
                texPreHumi.setText(humi + "%");
                texPreGas.setText(gas);
                texPrePhoto.setText(photo);
                texUpdateTime.setText(" " + time);
                Toast.makeText(getApplicationContext(), R.string.monitor_complete , Toast.LENGTH_SHORT).show();
                mSwipeRefresh.setRefreshing(false);

            }
        }.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        LayoutInflater layoutInflater = LayoutInflater.from(MonitorActivity.this);
        View promptView = layoutInflater.inflate(R.layout.multiname_input, null);
        AlertDialog.Builder aBuilder = new AlertDialog.Builder(MonitorActivity.this);
        final EditText editText = (EditText) promptView.findViewById(R.id.multiname);
        final SharedPreferences.Editor ed = prefs.edit();

        //noinspection SimplifiableIfStatement
        switch(item.getItemId()) {
            case R.id.action_refresh:
                // 실시간 값 가져오기
                new AsyncTask<Object, Object, Object>() {

                    String time = null;
                    String temp = null;
                    String humi = null;
                    String gas = null;
                    String photo = null;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                    }

                    @Override
                    protected Object doInBackground(Object... params) {
                        PhpDown_noThread phpTask = new PhpDown_noThread("http://" + host + "/mysql_test3.php");
                        String result = phpTask.phpTask();
                        try {

                            JSONArray jo = new JSONArray(result);

                            for(int i = 0; i < jo.length(); i++) {
                                JSONObject object = jo.getJSONObject(i);
                                time = object.getString("time");
                                temp = object.getString("temp");
                                humi = object.getString("humi");
                                gas = object.getString("gas");
                                photo = object.getString("photo");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        texPreTemp.setText(temp + "℃");
                        texPreHumi.setText(humi + "%");
                        texPreGas.setText(gas);
                        texPrePhoto.setText(photo);
                        texUpdateTime.setText(" " + time);
                        Toast.makeText(getApplicationContext(), R.string.monitor_complete , Toast.LENGTH_SHORT).show();
                    }
                }.execute();
                break;
            case R.id.action_toggle_value:
                if (mChart.getData() == null){
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), R.string.monitor_graph_no, Toast.LENGTH_SHORT).show();
                        }
                    });
                    return false;
                }
                for (DataSet<?> set : mChart.getData().getDataSets())
                    set.setDrawValues(!set.isDrawValuesEnabled());
                mChart.invalidate();
                break;
            case R.id.action_toggle_pinchzoom:
                if (mChart.isPinchZoomEnabled())
                    mChart.setPinchZoom(false);
                else
                    mChart.setPinchZoom(true);

                mChart.invalidate();
                break;
            case R.id.action_toggle_autoscale:
                mChart.setAutoScaleMinMaxEnabled(!mChart.isAutoScaleMinMaxEnabled());
                mChart.notifyDataSetChanged();
                break;
            case R.id.action_save:      // 그래프 저장하기

                if (!(mChart.getData() == null)) {
                    long currentTimeMillis = System.currentTimeMillis();
                    if (mChart.saveToPath("title" + currentTimeMillis, "/SmartHome")) {
                        Toast.makeText(getApplicationContext(), Environment.getExternalStorageDirectory().getPath() + "/SmartHome/title" + currentTimeMillis + ".png 로 저장 완료!",
                                Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(getApplicationContext(), R.string.monitor_save_fail, Toast.LENGTH_SHORT)
                                .show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "그래프가 없습니다", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_toggle_dnum:   // 가져올 데이터 수 변경
                aBuilder.setView(promptView);
                aBuilder.setTitle(R.string.monitor_dnum_change);
                aBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            dialog.dismiss();
                        }

                        return false;
                    }
                });
                String pDnum = prefs.getString("dnum", "100");
                editText.setText(pDnum);
                editText.setSelectAllOnFocus(true);

                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                aBuilder.setCancelable(false).setPositiveButton(R.string.monitor_change, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ed.putString("dnum", editText.getText().toString());
                        ed.commit();
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.monitor_changed_text, Toast.LENGTH_SHORT).show();

                            }
                        });

                    }
                }).setNegativeButton(R.string.monitor_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = aBuilder.create();
                alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                alertDialog.show();
                break;
            case R.id.action_gas_high:      // 가스 상한값 변경
                aBuilder.setView(promptView);
                aBuilder.setTitle(R.string.monitor_gas_high);
                aBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            dialog.dismiss();
                        }

                        return false;
                    }
                });

                final String pGas = prefs.getString("gas_high", "300");
                editText.setText(pGas);
                editText.setSelectAllOnFocus(true);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                aBuilder.setCancelable(false).setPositiveButton(R.string.monitor_change, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setValueChange("gas_high", editText.getText().toString());
                        /*
                        ed.putString("gas_high", editText.getText().toString());
                        ed.commit();
                        String htmlResult = "";
                        PhpDown phpDown2 = new PhpDown();

                        try {
                            htmlResult = phpDown2.execute("http://" + host + "/mysql_test6.php?gas=" + editText.getText().toString()).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        */

                    }
                }).setNegativeButton(R.string.monitor_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog2 = aBuilder.create();
                alertDialog2.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                alertDialog2.show();

                break;
            case R.id.action_humi_high:     // 습도 상한값 변경
                aBuilder.setView(promptView);
                aBuilder.setTitle(R.string.monitor_humi_high);
                aBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            dialog.dismiss();
                        }

                        return false;
                    }
                });

                final String pHumiHigh = prefs.getString("humi_high", "80");
                editText.setText(pHumiHigh);
                editText.setSelectAllOnFocus(true);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                aBuilder.setCancelable(false).setPositiveButton(R.string.monitor_change, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setValueChange("humi_high", editText.getText().toString());

                    }
                }).setNegativeButton(R.string.monitor_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog3 = aBuilder.create();
                alertDialog3.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                alertDialog3.show();
                break;
            case R.id.action_humi_low:      // 습도 하한값 변경
                aBuilder.setView(promptView);
                aBuilder.setTitle(R.string.monitor_humi_low);
                aBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            dialog.dismiss();
                        }

                        return false;
                    }
                });

                final String pHumiLow = prefs.getString("humi_low", "30");
                editText.setText(pHumiLow);
                editText.setSelectAllOnFocus(true);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                aBuilder.setCancelable(false).setPositiveButton(R.string.monitor_change, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setValueChange("humi_low", editText.getText().toString());
                    }
                }).setNegativeButton(R.string.monitor_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog4 = aBuilder.create();
                alertDialog4.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                alertDialog4.show();
                break;
            case R.id.action_temp_high:     // 온도 상한값 변경
                aBuilder.setView(promptView);
                aBuilder.setTitle(R.string.monitor_temp_high);
                aBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            dialog.dismiss();
                        }

                        return false;
                    }
                });

                final String pTempHigh = prefs.getString("temp_high", "30");
                editText.setText(pTempHigh);
                editText.setSelectAllOnFocus(true);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                aBuilder.setCancelable(false).setPositiveButton(R.string.monitor_change, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setValueChange("temp_high", editText.getText().toString());
                    }
                }).setNegativeButton(R.string.monitor_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog5 = aBuilder.create();
                alertDialog5.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                alertDialog5.show();

                break;
            case R.id.action_temp_low:  // 온도 하한값 변경
                aBuilder.setView(promptView);
                aBuilder.setTitle(R.string.monitor_temp_low);
                aBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            dialog.dismiss();
                        }

                        return false;
                    }
                });

                final String pTempLow = prefs.getString("temp_low", "10");
                editText.setText(pTempLow);
                editText.setSelectAllOnFocus(true);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                aBuilder.setCancelable(false).setPositiveButton(R.string.monitor_change, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setValueChange("temp_low", editText.getText().toString());
                    }
                }).setNegativeButton(R.string.monitor_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog6 = aBuilder.create();
                alertDialog6.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                alertDialog6.show();
                break;
            case R.id.action_update_time:   // 업데이트 주기 변경
                aBuilder.setView(promptView);
                aBuilder.setTitle(R.string.monitor_update_time);
                aBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            dialog.dismiss();
                        }

                        return false;
                    }
                });

                final String pUpdateTime = prefs.getString("update_time", "10");
                editText.setText(pUpdateTime);
                editText.setSelectAllOnFocus(true);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                aBuilder.setCancelable(false).setPositiveButton(R.string.monitor_change, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setValueChange("update_time", editText.getText().toString());
                    }
                }).setNegativeButton(R.string.monitor_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog7 = aBuilder.create();
                alertDialog7.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                alertDialog7.show();
                break;
            case R.id.action_share:     // 공유
                Intent shartIntent = new Intent();
                String sendText = getString(R.string.monitor_share_temp) + temp + "℃\n"
                        + getString(R.string.monitor_share_humi) + humi + "%\n"
                        + getString(R.string.monitor_share_gas) + gas + "\n"
                        + getString(R.string.monitor_share_photo) + photo + "\n"
                        + getString(R.string.monitor_share_time) + time;
                shartIntent.setAction(Intent.ACTION_SEND);
                shartIntent.putExtra(Intent.EXTRA_TEXT, sendText);
                shartIntent.setType("text/plain");
                startActivity(shartIntent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setThData(int count, float range) {

        PhpDown phpDown = new PhpDown();
        JSONObject json;
        String jsonStr = null;
        String time = null;
        String temp = null;
        String humi = null;
        preState = 1;
        String sDnum = prefs.getString("dnum", "100");

        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<Entry> yVals1 = new ArrayList<Entry>();
        ArrayList<Entry> yVals2 = new ArrayList<Entry>();

        try {
            jsonStr = phpDown.execute("http://" + host + "/mysql_test5.php?dnum=" + sDnum).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        try {

            JSONArray jo = new JSONArray(jsonStr);

            for(int i = 0; i < jo.length(); i++) {
                JSONObject object = jo.getJSONObject(i);
                time = object.getString("time");
                temp = object.getString("temp");
                humi = object.getString("humi");
                xVals.add(time);
                yVals1.add(new Entry(Float.parseFloat(temp), i));
                yVals2.add(new Entry(Float.parseFloat(humi), i));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals1, getString(R.string.monitor_temp));
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(Color.rgb(0xD9,0x55,0x76));
        set1.setCircleColor(Color.rgb(0xD9, 0x55, 0x76));
        set1.setLineWidth(2f);
        set1.setCircleSize(3f);
        set1.setFillAlpha(65);
        set1.setFillColor(ColorTemplate.getHoloBlue());
        set1.setHighLightColor(Color.rgb(253, 199, 86));
        set1.setDrawCircleHole(false);
        //set1.setFillFormatter(new MyFillFormatter(0f));
//        set1.setDrawHorizontalHighlightIndicator(false);
//        set1.setVisible(false);
//        set1.setCircleHoleColor(Color.WHITE);

        // create a dataset and give it a type
        LineDataSet set2 = new LineDataSet(yVals2, getString(R.string.monitor_humi));
        set2.setAxisDependency(YAxis.AxisDependency.RIGHT);
        set2.setColor(Color.rgb(0x00,0x62,0x8C));
        set2.setCircleColor(Color.rgb(0x00,0x62,0x8C));
        set2.setLineWidth(2f);
        set2.setCircleSize(3f);
        set2.setFillAlpha(65);
        set2.setFillColor(Color.RED);
        set2.setDrawCircleHole(false);
        set2.setHighLightColor(Color.rgb(253, 199, 86));
        //set2.setFillFormatter(new MyFillFormatter(900f));

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set2);
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);
        data.setValueTextColor(Color.BLACK);
        data.setValueTextSize(9f);

        // set data
        mChart.setData(data);
    }

    private void setGasData(int count, float range) {

        PhpDown phpDown = new PhpDown();
        JSONObject json;
        String jsonStr = null;
        String time = null;
        String gas = null;
        preState = 0;
        String sDnum = prefs.getString("dnum", "100");

        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<Entry> yVals = new ArrayList<Entry>();

        try {
            jsonStr = phpDown.execute("http://" + host + "/mysql_test5.php?dnum=" + sDnum).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        try {

            JSONArray jo = new JSONArray(jsonStr);

            for(int i = 0; i < jo.length(); i++) {
                JSONObject object = jo.getJSONObject(i);
                time = object.getString("time");
                gas = object.getString("gas");
                xVals.add(time);
                yVals.add(new Entry(Float.parseFloat(gas), i));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals, getString(R.string.monitor_gas));
        // set1.setFillAlpha(110);
        // set1.setFillColor(Color.RED);

        // set the line to be drawn like this "- - - - - -"
        set1.enableDashedLine(10f, 5f, 0f);
        set1.enableDashedHighlightLine(10f, 5f, 0f);
        set1.setColor(Color.rgb(0x1E,0xA3,0x99));
        set1.setCircleColor(Color.rgb(0x1E,0xA3,0x99));
        set1.setLineWidth(1f);
        set1.setCircleSize(3f);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(9f);
        set1.setFillAlpha(65);
        set1.setFillColor(Color.BLACK);
//        set1.setDrawFilled(true);
        // set1.setShader(new LinearGradient(0, 0, 0, mChart.getHeight(),
        // Color.BLACK, Color.WHITE, Shader.TileMode.MIRROR));

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        // set data
        mChart.setData(data);
    }

    private LineData setPhotoData(int count, float range) {

        PhpDown phpDown = new PhpDown();
        JSONObject json;
        String jsonStr = null;
        String time = null;
        String photo = null;
        preState = 0;
        String sDnum = prefs.getString("dnum", "100");

        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<Entry> yVals = new ArrayList<Entry>();

        try {
            jsonStr = phpDown.execute("http://" + host + "/mysql_test5.php?dnum=" + sDnum).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        try {

            JSONArray jo = new JSONArray(jsonStr);

            for(int i = 0; i < jo.length(); i++) {
                JSONObject object = jo.getJSONObject(i);
                time = object.getString("time");
                photo = object.getString("photo");
                xVals.add(time);
                yVals.add(new Entry(Float.parseFloat(photo), i));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals, getString(R.string.monitor_photo));
        // set1.setFillAlpha(110);
        // set1.setFillColor(Color.RED);

        // set the line to be drawn like this "- - - - - -"
        set1.enableDashedLine(10f, 5f, 0f);
        set1.enableDashedHighlightLine(10f, 5f, 0f);
        set1.setColor(Color.rgb(0xF3, 0x9A, 0xA5));
        set1.setCircleColor(Color.rgb(0xF3, 0x9A, 0xA5));
        set1.setLineWidth(1f);
        set1.setCircleSize(3f);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(9f);
        set1.setFillAlpha(65);
        set1.setFillColor(Color.BLACK);
//        set1.setDrawFilled(true);
        // set1.setShader(new LinearGradient(0, 0, 0, mChart.getHeight(),
        // Color.BLACK, Color.WHITE, Shader.TileMode.MIRROR));

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        // set data
        //mChart.setData(data);
        return data;
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    // 온습도 버튼 클릭시
    public void btn_th_onClicked(View v){
        new AsyncTask<Object, Object, LineData>() {

            ProgressDialog progressDialog;


            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                progressDialog = new ProgressDialog(MonitorActivity.this);
                progressDialog.setTitle(getString(R.string.loading_title));
                progressDialog.setMessage(getString(R.string.loading_message));
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCanceledOnTouchOutside(false);

                progressDialog.show();
            }

            @Override
            protected LineData doInBackground(Object[] params) {
                String sDnum = prefs.getString("dnum", "100");
                StringBuilder jsonHtml = new StringBuilder();
                try {
                    URL url = new URL("http://" + host + "/mysql_test5.php?dnum=" + sDnum + "&n=1");
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();

                    if (conn != null) {
                        conn.setConnectTimeout(10000);
                        conn.setUseCaches(false);

                        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                            while(true) {
                                String line = br.readLine();
                                if (line == null) break;
                                jsonHtml.append(line + "\n");
                            }

                            br.close();
                        }
                        conn.disconnect();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                JSONObject json;
                String jsonStr = jsonHtml.toString();
                String time = null;
                String temp = null;
                String humi = null;

                ArrayList<String> xVals = new ArrayList<String>();
                ArrayList<Entry> yVals1 = new ArrayList<Entry>();
                ArrayList<Entry> yVals2 = new ArrayList<Entry>();

                try {

                    JSONArray jo = new JSONArray(jsonStr);

                    for(int i = 0; i < jo.length(); i++) {
                        JSONObject object = jo.getJSONObject(i);
                        time = object.getString("time");
                        temp = object.getString("temp");
                        humi = object.getString("humi");
                        time = time.substring(0, time.length() - 3);
                        xVals.add(time);
                        yVals1.add(new Entry(Float.parseFloat(temp), i));
                        yVals2.add(new Entry(Float.parseFloat(humi), i));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // create a dataset and give it a type
                LineDataSet set1 = new LineDataSet(yVals1, getString(R.string.monitor_temp));
                set1.setAxisDependency(YAxis.AxisDependency.LEFT);
                set1.setColor(Color.rgb(0xD9,0x55,0x76));
                set1.setCircleColor(Color.rgb(0xD9, 0x55, 0x76));
                set1.setLineWidth(2f);
                set1.setCircleSize(3f);
                set1.setFillAlpha(65);
                set1.setFillColor(ColorTemplate.getHoloBlue());
                set1.setHighLightColor(Color.rgb(253, 199, 86));
                set1.setDrawCircleHole(false);
                //set1.setFillFormatter(new MyFillFormatter(0f));
                //        set1.setDrawHorizontalHighlightIndicator(false);
                //        set1.setVisible(false);
                //        set1.setCircleHoleColor(Color.WHITE);

                // create a dataset and give it a type
                LineDataSet set2 = new LineDataSet(yVals2, getString(R.string.monitor_humi));
                set2.setAxisDependency(YAxis.AxisDependency.RIGHT);
                set2.setColor(Color.rgb(0x00,0x62,0x8C));
                set2.setCircleColor(Color.rgb(0x00,0x62,0x8C));
                set2.setLineWidth(2f);
                set2.setCircleSize(3f);
                set2.setFillAlpha(65);
                set2.setFillColor(Color.RED);
                set2.setDrawCircleHole(false);
                set2.setHighLightColor(Color.rgb(253, 199, 86));
                //set2.setFillFormatter(new MyFillFormatter(900f));

                ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
                dataSets.add(set2);
                dataSets.add(set1); // add the datasets

                // create a data object with the datasets
                LineData data = new LineData(xVals, dataSets);
                data.setValueTextColor(Color.BLACK);
                data.setValueTextSize(9f);
                return data;
            }

            @Override
            protected void onProgressUpdate(Object[] values) {
                super.onProgressUpdate(values);


            }

            @Override
            protected void onPostExecute(LineData data) {
                super.onPostExecute(data);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                MyMarkerView mv = new MyMarkerView(getApplicationContext(), R.layout.custom_marker_view);
                mChart.setMarkerView(mv);
                thGraph();
                mChart.setData(data);           // 그래프 데이터 세팅
                mChart.animateX(2500);
            }

        }.execute();
    }

    // 가스 버튼 클릭시
    public void btn_gas_onClicked(View v){
        new AsyncTask<Object, Object, LineData>() {

            ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                progressDialog = new ProgressDialog(MonitorActivity.this);
                progressDialog.setTitle(getString(R.string.loading_title));
                progressDialog.setMessage(getString(R.string.loading_message));
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCanceledOnTouchOutside(false);

                progressDialog.show();
            }

            @Override
            protected LineData doInBackground(Object[] params) {

                String sDnum = prefs.getString("dnum", "100");
                StringBuilder jsonHtml = new StringBuilder();
                try {
                    URL url = new URL("http://" + host + "/mysql_test5.php?dnum=" + sDnum + "&n=2");
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();

                    if (conn != null) {
                        conn.setConnectTimeout(10000);
                        conn.setUseCaches(false);

                        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                            while(true) {
                                String line = br.readLine();
                                if (line == null) break;
                                jsonHtml.append(line + "\n");
                            }

                            br.close();
                        }
                        conn.disconnect();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                JSONObject json;
                String jsonStr = jsonHtml.toString();
                String time = null;
                String gas = null;

                ArrayList<String> xVals = new ArrayList<String>();
                ArrayList<Entry> yVals = new ArrayList<Entry>();

                try {

                    JSONArray jo = new JSONArray(jsonStr);

                    for(int i = 0; i < jo.length(); i++) {
                        JSONObject object = jo.getJSONObject(i);
                        time = object.getString("time");
                        gas = object.getString("gas");
                        time = time.substring(0, time.length()-3);
                        xVals.add(time);
                        yVals.add(new Entry(Float.parseFloat(gas), i));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // create a dataset and give it a type
                LineDataSet set1 = new LineDataSet(yVals, getString(R.string.monitor_gas));
                // set1.setFillAlpha(110);
                // set1.setFillColor(Color.RED);

                // set the line to be drawn like this "- - - - - -"
                set1.enableDashedLine(10f, 5f, 0f);
                set1.enableDashedHighlightLine(10f, 5f, 0f);
                set1.setColor(Color.rgb(0x1E,0xA3,0x99));
                set1.setCircleColor(Color.rgb(0x1E,0xA3,0x99));
                set1.setLineWidth(1f);
                set1.setCircleSize(3f);
                set1.setDrawCircleHole(false);
                set1.setValueTextSize(9f);
                set1.setFillAlpha(65);
                set1.setFillColor(Color.BLACK);
//        set1.setDrawFilled(true);
                // set1.setShader(new LinearGradient(0, 0, 0, mChart.getHeight(),
                // Color.BLACK, Color.WHITE, Shader.TileMode.MIRROR));

                ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
                dataSets.add(set1); // add the datasets

                // create a data object with the datasets
                LineData data = new LineData(xVals, dataSets);

                // set data
                //mChart.setData(data);
                return data;
            }

            @Override
            protected void onProgressUpdate(Object[] values) {
                super.onProgressUpdate(values);

            }

            @Override
            protected void onPostExecute(LineData data) {
                super.onPostExecute(data);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                MyMarkerView2 mv = new MyMarkerView2(getApplicationContext(), R.layout.custom_marker_view);
                mChart.setMarkerView(mv);
                gasGraph();
                mChart.setData(data);           // 그래프 데이터 세팅
                mChart.animateX(2500, Easing.EasingOption.EaseInOutQuart);
            }

        }.execute();
    }

    // 밝기 버튼 클릭시
    public void btn_photo_onClicked(View v){

        new AsyncTask<Object, Object, LineData>() {

            ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = new ProgressDialog(MonitorActivity.this);
                progressDialog.setTitle(getString(R.string.loading_title));
                progressDialog.setMessage(getString(R.string.loading_message));
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
            }

            @Override
            protected LineData doInBackground(Object[] params) {

                String sDnum = prefs.getString("dnum", "100");
                StringBuilder jsonHtml = new StringBuilder();
                try {
                    URL url = new URL("http://" + host + "/mysql_test5.php?dnum=" + sDnum + "&n=3");
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();

                    if (conn != null) {
                        conn.setConnectTimeout(10000);
                        conn.setUseCaches(false);

                        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                            while(true) {
                                String line = br.readLine();
                                if (line == null) break;
                                jsonHtml.append(line + "\n");
                            }

                            br.close();
                        }
                        conn.disconnect();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                JSONObject json;
                String jsonStr = jsonHtml.toString();
                String time = null;
                String photo = null;

                ArrayList<String> xVals = new ArrayList<String>();
                ArrayList<Entry> yVals = new ArrayList<Entry>();

                try {

                    JSONArray jo = new JSONArray(jsonStr);

                    for(int i = 0; i < jo.length(); i++) {
                        JSONObject object = jo.getJSONObject(i);
                        time = object.getString("time");
                        time = time.substring(0, time.length() - 3);
                        photo = object.getString("photo");
                        xVals.add(time);
                        yVals.add(new Entry(Float.parseFloat(photo), i));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // create a dataset and give it a type
                LineDataSet set1 = new LineDataSet(yVals, getString(R.string.monitor_photo));
                // set1.setFillAlpha(110);
                // set1.setFillColor(Color.RED);

                // set the line to be drawn like this "- - - - - -"
                set1.enableDashedLine(10f, 5f, 0f);
                set1.enableDashedHighlightLine(10f, 5f, 0f);
                set1.setColor(Color.rgb(0xF3, 0x9A, 0xA5));
                set1.setCircleColor(Color.rgb(0xF3, 0x9A, 0xA5));
                set1.setLineWidth(1f);
                set1.setCircleSize(3f);
                set1.setDrawCircleHole(false);
                set1.setValueTextSize(9f);
                set1.setFillAlpha(65);
                set1.setFillColor(Color.BLACK);
//        set1.setDrawFilled(true);
                // set1.setShader(new LinearGradient(0, 0, 0, mChart.getHeight(),
                // Color.BLACK, Color.WHITE, Shader.TileMode.MIRROR));

                ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
                dataSets.add(set1); // add the datasets

                // create a data object with the datasets
                LineData data = new LineData(xVals, dataSets);

                // set data
                //mChart.setData(data);
                return data;
            }

            @Override
            protected void onProgressUpdate(Object[] values) {
                super.onProgressUpdate(values);

            }

            @Override
            protected void onPostExecute(LineData data) {
                super.onPostExecute(data);
                progressDialog.dismiss();       // 로딩 창이 사라짐
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                MyMarkerView2 mv = new MyMarkerView2(getApplicationContext(), R.layout.custom_marker_view);
                mChart.setMarkerView(mv);
                photoGraph();                   // 그래프 관련 초기화
                mChart.setData(data);           // 그래프 데이터 세팅
                //setPhotoData(100, 20);
                mChart.animateX(2500, Easing.EasingOption.EaseInOutQuart);
            }
        }.execute();


    }

    public void thGraph(){
        mChart.invalidate();
        mChart.removeAllViews();
        mChart.getAxisRight().setEnabled(true);
        mChart.getAxisLeft().removeAllLimitLines();
        Legend l = mChart.getLegend();

        l.setForm(Legend.LegendForm.LINE);
        l.setTextSize(11f);
        l.setTextColor(Color.BLACK);
        l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTextSize(12f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setSpaceBetweenLabels(1);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.rgb(0xD9,0x55,0x76));
        leftAxis.setAxisMaxValue(40f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setTextColor(Color.rgb(0x00,0x62,0x8C));
        rightAxis.setAxisMaxValue(100);
        rightAxis.setStartAtZero(false);
        rightAxis.setAxisMinValue(0);
        rightAxis.setDrawGridLines(false);
    }

    public void gasGraph(){
        mChart.invalidate();
        mChart.removeAllViews();
        String sGasVal = prefs.getString("gas_high", "300");

        LimitLine llXAxis = new LimitLine(10f, "Index 10");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);

        XAxis xAxis = mChart.getXAxis();

        LimitLine ll1 = new LimitLine(Float.parseFloat(sGasVal), getString(R.string.monitor_upper_limit));
        ll1.setEnabled(true);
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setTextSize(10f);

        LimitLine ll2 = new LimitLine(-30f, getString(R.string.monitor_lower_limit));
        ll2.setEnabled(false);
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(10f);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.addLimitLine(ll1);
        leftAxis.addLimitLine(ll2);
        leftAxis.setTextColor(Color.rgb(0x1E, 0xA3, 0x99));
        leftAxis.setAxisMaxValue(photoHighVal);
        leftAxis.setAxisMinValue(0f);
        leftAxis.setStartAtZero(false);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);

        leftAxis.setDrawLimitLinesBehindData(true);
        mChart.getAxisRight().setEnabled(false);
    }

    public void photoGraph(){
        mChart.invalidate();
        mChart.removeAllViews();

        XAxis xAxis = mChart.getXAxis();

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.setTextColor(Color.rgb(0xF3, 0x9A, 0xA5));
        leftAxis.setAxisMaxValue(photoHighVal);
        leftAxis.setAxisMinValue(0f);
        leftAxis.setStartAtZero(false);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);

        leftAxis.setDrawLimitLinesBehindData(true);
        mChart.getAxisRight().setEnabled(false);

    }

    public void refreshBtn_onClick(View v){ // 새로고침 버튼

        new AsyncTask<Object, Object, Object>() {

            String time = null;
            String temp = null;
            String humi = null;
            String gas = null;
            String photo = null;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Object doInBackground(Object... params) {
                PhpDown_noThread phpTask = new PhpDown_noThread("http://" + host + "/mysql_test3.php");
                String result = phpTask.phpTask();
                try {

                    JSONArray jo = new JSONArray(result);

                    for(int i = 0; i < jo.length(); i++) {
                        JSONObject object = jo.getJSONObject(i);
                        time = object.getString("time");
                        temp = object.getString("temp");
                        humi = object.getString("humi");
                        gas = object.getString("gas");
                        photo = object.getString("photo");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                texPreTemp.setText(temp + "℃");
                texPreHumi.setText(humi + "%");
                texPreGas.setText(gas);
                texPrePhoto.setText(photo);
                texUpdateTime.setText(" " + time);
                Toast.makeText(getApplicationContext(), R.string.monitor_complete , Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    void setValueChange(final String preferenceName, final String value) {


        String htmlResult = "";
        PhpDown phpDown2 = new PhpDown();

        new AsyncTask<Object, Object, Object>() {

            String result;
            String name;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (preferenceName.equals("gas_high")){
                    name = "gas";
                }
                else {
                    name = preferenceName;
                }
            }

            @Override
            protected Object doInBackground(Object... params) {

                PhpDown_noThread phpDown = new PhpDown_noThread("http://" + host + "/mysql_test6.php?" + name + "=" + value);
                result = phpDown.phpTask();

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if (result.equals("Success\n")) {
                    Toast.makeText(getApplicationContext(), R.string.monitor_complete, Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor ed = prefs.edit();
                    ed.putString(preferenceName, value);
                    ed.commit();
                }
                else {
                    Toast.makeText(getApplicationContext(), "실패하였습니다", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

}
