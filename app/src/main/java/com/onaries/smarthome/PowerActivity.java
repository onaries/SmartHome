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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
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

import butterknife.Bind;

public class PowerActivity extends AppCompatActivity implements OnChartValueSelectedListener, SwipeRefreshLayout.OnRefreshListener{

    private SharedPreferences prefs;
    private LineChart mChart;
    private int preState;
    private String jsonHtml;
    private String host;
    private final float photoHighVal = 1023f;

    private TextView txtPower1;
    private TextView txtPower2;
    private TextView txtPower3;
    private TextView txtUpdateTime;

    private String power1, power2, power3;
    private String time;

    private SwipeRefreshLayout mSwipeRefresh;

    final private String mysqlURL_limit = "/sql/mysql_sel_power_limit.php";
    final private String mysqlURL_dnum = "/sql/mysql_sel_power_dnum.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_power);

        Intent intent = getIntent();
        setTitle(R.string.title_activity_power);
        mChart = (LineChart) findViewById(R.id.chart2);
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

        String sTime;
        power1 = intent.getExtras().getString("POWER1");
        power2 = intent.getExtras().getString("POWER2");
        power3 = intent.getExtras().getString("POWER3");
        time = intent.getExtras().getString("TIME");

        if (power1 == null) {
            Toast.makeText(getApplicationContext(), "서버가 연결되지 않았습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        // 그래프 그리기

        txtPower1 = (TextView) findViewById(R.id.prePower1);
        txtPower2 = (TextView) findViewById(R.id.prePower2);
        txtPower3 = (TextView) findViewById(R.id.prePower3);
        txtUpdateTime = (TextView) findViewById(R.id.updateTimePower);

        // 전력 표시 (단위 KW)
        txtPower1.setText(power1 + " KW");
        txtPower2.setText(power2 + " KW");
        txtPower3.setText(power3 + " KW");
        txtUpdateTime.setText(" " + time);

        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_layout2);
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
            String power1 = null;
            String power2 = null;
            String power3 = null;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Object doInBackground(Object... params) {
                PhpDown_noThread phpTask = new PhpDown_noThread("http://" + host + mysqlURL_limit);
                String result = phpTask.phpTask();
                try {

                    JSONArray jo = new JSONArray(result);


                    for(int i = 0; i < jo.length(); i++) {
                        JSONObject object = jo.getJSONObject(i);
                        time = object.getString("TIME");
                        power1 = object.getString("POWER1");
                        power2 = object.getString("POWER2");
                        power3 = object.getString("POWER3");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                txtPower1.setText(power1 + " KW");
                txtPower2.setText(power2 + " KW");
                txtPower3.setText(power3 + " KW");
                txtUpdateTime.setText(" " + time);
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

        LayoutInflater layoutInflater = LayoutInflater.from(PowerActivity.this);
        View promptView = layoutInflater.inflate(R.layout.multiname_input, null);
        AlertDialog.Builder aBuilder = new AlertDialog.Builder(PowerActivity.this);
        final EditText editText = (EditText) promptView.findViewById(R.id.multiname);
        final SharedPreferences.Editor ed = prefs.edit();

        //noinspection SimplifiableIfStatement
        switch(item.getItemId()) {
            case R.id.action_refresh:
                // 실시간 값 가져오기
                new AsyncTask<Object, Object, Object>() {

                    String time = null;
                    String power1 = null;
                    String power2 = null;
                    String power3 = null;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                    }

                    @Override
                    protected Object doInBackground(Object... params) {
                        PhpDown_noThread phpTask = new PhpDown_noThread("http://" + host + mysqlURL_limit);
                        String result = phpTask.phpTask();
                        try {

                            JSONArray jo = new JSONArray(result);

                            for(int i = 0; i < jo.length(); i++) {
                                JSONObject object = jo.getJSONObject(i);
                                time = object.getString("time");
                                power1 = object.getString("power1");
                                power2 = object.getString("power2");
                                power3 = object.getString("power3");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        txtPower1.setText(power1 + " KW");
                        txtPower2.setText(power2 + " KW");
                        txtPower3.setText(power3 + " KW");
                        txtUpdateTime.setText(" " + time);
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
                String sendText = getString(R.string.monitor_share_temp) + power1 + "℃\n"
                        + getString(R.string.monitor_share_humi) + power2 + "%\n"
                        + getString(R.string.monitor_share_gas) + power3 + "\n"
                        + getString(R.string.monitor_share_time) + time;
                shartIntent.setAction(Intent.ACTION_SEND);
                shartIntent.putExtra(Intent.EXTRA_TEXT, sendText);
                shartIntent.setType("text/plain");
                startActivity(shartIntent);
                break;
        }

        return super.onOptionsItemSelected(item);
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
    public void btn_power_onClicked(View v){
        new AsyncTask<Object, Object, LineData>() {

            ProgressDialog progressDialog;


            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                progressDialog = new ProgressDialog(PowerActivity.this);
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
                    URL url = new URL("http://" + host + mysqlURL_dnum +"?dnum=" + sDnum);
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
                String time, power1, power2, power3;    // 초기화

                ArrayList<String> xVals = new ArrayList<String>();
                ArrayList<Entry> yVals1 = new ArrayList<Entry>();   // Power1
                ArrayList<Entry> yVals2 = new ArrayList<Entry>();   // Power2
                ArrayList<Entry> yVals3 = new ArrayList<Entry>();   // Power3

                try {
                    JSONArray jo = new JSONArray(jsonStr);

                    for(int i = 0; i < jo.length(); i++) {
                        JSONObject object = jo.getJSONObject(i);
                        time = object.getString("TIME");
                        power1 = object.getString("POWER1");
                        power2 = object.getString("POWER2");
                        power3 = object.getString("POWER3");
                        time = time.substring(2);
                        time = time.substring(0, time.length() - 3);
                        xVals.add(time);
                        yVals1.add(new Entry(Float.parseFloat(power1), i));
                        yVals2.add(new Entry(Float.parseFloat(power2), i));
                        yVals3.add(new Entry(Float.parseFloat(power3), i));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // create a dataset and give it a type
                LineDataSet set1 = new LineDataSet(yVals1, getString(R.string.monitor_power1));
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
                LineDataSet set2 = new LineDataSet(yVals2, getString(R.string.monitor_power2));
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

                LineDataSet set3 = new LineDataSet(yVals3, getString(R.string.monitor_power3));
                set3.setAxisDependency(YAxis.AxisDependency.RIGHT);
                set3.setColor(Color.rgb(0x60,0x8C,0x00));
                set3.setCircleColor(Color.rgb(0x37,0x46,0x15));
                set3.setLineWidth(2f);
                set3.setCircleSize(3f);
                set3.setFillAlpha(65);
                set3.setFillColor(Color.RED);
                set3.setDrawCircleHole(false);
                set3.setHighLightColor(Color.rgb(253, 199, 86));

                ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
                dataSets.add(set1); // add the datasets
                dataSets.add(set2);
                dataSets.add(set3);

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
                powerGraph();
                mChart.setData(data);           // 그래프 데이터 세팅
                mChart.animateX(2500);
            }

        }.execute();
    }

    public void powerGraph(){
        mChart.invalidate();
        mChart.removeAllViews();
//        mChart.getAxisRight().setEnabled(true);
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

        YAxis power1Axis = mChart.getAxisLeft();
        power1Axis.setTextColor(Color.rgb(0xD9,0x55,0x76));
        power1Axis.setAxisMaxValue(1200f);
        power1Axis.setDrawGridLines(true);

        YAxis power2Axis = mChart.getAxisRight();
        power2Axis.setTextColor(Color.rgb(0x00,0x62,0x8C));
        power2Axis.setAxisMaxValue(1200f);
        power2Axis.setStartAtZero(false);
        power2Axis.setAxisMinValue(0);
        power2Axis.setDrawGridLines(false);

        YAxis power3Axis = mChart.getAxisRight();
        power3Axis.setTextColor(Color.rgb(0x60,0x8C,0x00));
        power3Axis.setAxisMaxValue(1200f);
        power3Axis.setStartAtZero(false);
        power3Axis.setAxisMinValue(0);
        power3Axis.setDrawGridLines(false);
    }

    public void refreshBtn_onClick(View v){ // 새로고침 버튼

        new AsyncTask<Object, Object, Object>() {

            String time = null;
            String power1 = null;
            String power2 = null;
            String power3 = null;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Object doInBackground(Object... params) {
                PhpDown_noThread phpTask = new PhpDown_noThread("http://" + host + mysqlURL_limit);
                String result = phpTask.phpTask();
                try {

                    JSONArray jo = new JSONArray(result);

                    for(int i = 0; i < jo.length(); i++) {
                        JSONObject object = jo.getJSONObject(i);
                        time = object.getString("time");
                        power1 = object.getString("power1");
                        power2 = object.getString("power2");
                        power3 = object.getString("power3");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                txtPower1.setText(power1 + " KW");
                txtPower2.setText(power2 + " KW");
                txtPower3.setText(power3 + " KW");
                txtUpdateTime.setText(" " + time);
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
