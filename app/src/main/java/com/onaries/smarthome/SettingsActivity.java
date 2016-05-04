package com.onaries.smarthome;


import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    private static SharedPreferences prefs;
    public static String host;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        getListView().setBackgroundColor(Color.WHITE);
        setTitle(R.string.title_activity_settings);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        host = prefs.getString("server_ip", "127.0.0.1");
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            }
            else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };


    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName)
                || MultitabPreferenceFragment.class.getName().equals(fragmentName)
                || MonitorPreferenceFragment.class.getName().equals(fragmentName)
                || GcmPreferenceFragment.class.getName().equals(fragmentName)
                || InfoPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */

    // 서버 설정 관련
    public static class GeneralPreferenceFragment extends PreferenceFragment {

        String[] serverID = {"server_ip", "server_port", "server_time"};
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_server);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            for (String s : serverID)
                bindPreferenceSummaryToValue(findPreference(s));

            EditTextPreference serverIP = (EditTextPreference) findPreference(serverID[0]);
            serverIP.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    setSettingValue(newValue.toString());
                    preference.setSummary(newValue.toString());
                    return true;
                }
            });
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            view.setBackgroundColor(Color.WHITE);
            return view;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));

                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        // 설정값 불러오기 함수
        public void setSettingValue(final String newHost) {

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
                        PhpDown_noThread phpTask = new PhpDown_noThread("http://" + newHost + "/mysql_test10.php");    // 이 경로의 php의 JSON값을 얻어오기 위해 사용
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
                        Toast.makeText(getActivity(), "설정값을 불러왔습니다", Toast.LENGTH_SHORT).show();  // 알맞는 토스트 메시지 출력
                    }
                    else {
                        Toast.makeText(getActivity(), "서버 주소가 잘못되었습니다", Toast.LENGTH_SHORT).show(); // 알맞는 토스트 메시지 출력
                    }
                }
            }.execute();
        }

    }


    public static class MultitabPreferenceFragment extends PreferenceFragment {

        String[] mulName = {"multitap1_name", "multitap2_name", "multitap3_name"};

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_multitab);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.

            // multitab 관련
            for (String s : mulName)
                bindPreferenceSummaryToValue(findPreference(s));

            SwitchPreference multitap_voice_state = (SwitchPreference) findPreference("multitap_voice_state");
            multitap_voice_state.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    if (newValue.equals(true)) {
                        preference.setSummary("음성인식 기능을 사용하고 있습니다");
                    }
                    else {
                        preference.setSummary("음성인식 기능을 사용하고 있지 않습니다");
                    }

                    return true;
                }
            });
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            view.setBackgroundColor(Color.WHITE);
            return view;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }


    public static class MonitorPreferenceFragment extends PreferenceFragment {

        String[] msg_id = {"dnum", "gas_high", "humi_high", "humi_low", "temp_high", "temp_low", "update_time"};


        void setValueChange(final String preferenceName, final String value, final Preference preference) {

            String htmlResult = "";
            PhpDown phpDown2 = new PhpDown();

            new AsyncTask<Object, Object, Boolean>() {

                String result;
                String name;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    if (preferenceName.equals(msg_id[1])){
                        name = "gas";
                    }
                    else {
                        name = preferenceName;
                    }
                }

                @Override
                protected Boolean doInBackground(Object... params) {

                    PhpDown_noThread phpDown = new PhpDown_noThread("http://" + host + "/mysql_test6.php?" + name + "=" + value);
                    result = phpDown.phpTask();

                    // result 값이 Success 일 경우
                    if (result.equals("Success\n")) {
                        return true;
                    }
                    return false;
                }

                @Override
                protected void onPostExecute(Boolean outcome) {
                    super.onPostExecute(outcome);
                    if (outcome) {
                        Toast.makeText(getActivity(), R.string.monitor_complete, Toast.LENGTH_SHORT).show();
                        SharedPreferences.Editor ed = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                        ed.putString(preferenceName, value);
                        ed.apply();
                        preference.setSummary(value);
                    }
                    else {
                        Toast.makeText(getActivity(), R.string.notification_failed, Toast.LENGTH_SHORT).show();
                    }
                }
            }.execute();

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            assert view != null;
            view.setBackgroundColor(Color.WHITE);
            return view;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_monitor);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.

            // monitor 관련
            for (String s : msg_id)
                bindPreferenceSummaryToValue(findPreference(s));

            EditTextPreference gas_high = (EditTextPreference) findPreference(msg_id[1]);
            EditTextPreference humi_high = (EditTextPreference) findPreference(msg_id[2]);
            EditTextPreference humi_low = (EditTextPreference) findPreference(msg_id[3]);
            EditTextPreference temp_high = (EditTextPreference) findPreference(msg_id[4]);
            EditTextPreference temp_low = (EditTextPreference) findPreference(msg_id[5]);
            EditTextPreference update_time = (EditTextPreference) findPreference(msg_id[6]);
            gas_high.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    setValueChange(msg_id[1], newValue.toString(), preference);
                    return true;
                }
            });
            humi_high.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    setValueChange(msg_id[2], newValue.toString(), preference);
                    return true;
                }
            });
            humi_low.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    setValueChange(msg_id[3], newValue.toString(), preference);
                    return true;
                }
            });
            temp_high.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    setValueChange(msg_id[4], newValue.toString(), preference);
                    return true;
                }
            });
            temp_low.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    setValueChange(msg_id[5], newValue.toString(), preference);
                    return true;
                }
            });
            update_time.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    setValueChange(msg_id[6], newValue.toString(), preference);
                    return true;
                }
            });

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }


    public static class GcmPreferenceFragment extends PreferenceFragment {

        String regID = "REG_ID";

        public class EditTextLongClickListener extends EditTextPreference implements View.OnLongClickListener {

            public EditTextLongClickListener(Context context) {
                super(context);
            }

            @Override
            public boolean onLongClick(View v) {

                
                return true;
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_gcm);

            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.

            final ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

            // multitab 관련
            bindPreferenceSummaryToValue(findPreference(regID));

            final EditTextPreference regIdPreference = (EditTextPreference) findPreference(regID);
            regIdPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    ClipData clip = ClipData.newPlainText(regID, preference.getSummary());
                    clipboardManager.setPrimaryClip(clip);
                    Toast.makeText(getActivity(), R.string.Settings_copy, Toast.LENGTH_SHORT).show();
                    regIdPreference.getDialog().dismiss();
                    return true;
                }
            });



            /*
            getPreferenceScreen().setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (preference.getKey() == "BTN_REG") {
                        String htmlResult = "";
                        PhpDown phpDown = new PhpDown();
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        String reg_id = prefs.getString("REG_ID", "");
                        try {
                            htmlResult = phpDown.execute("http://" + host + "/mysql_test7.php?reg_id=" + reg_id).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }

                        if (htmlResult.equals("Success")) {
                            Toast.makeText(getActivity(), "등록하였습니다", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "실패하였습니다. 다시 시도하여 주시기 바랍니다", Toast.LENGTH_SHORT).show();
                        }


                        if (!reg_id.equals(null)) {  // null 이 아닐경우

                        } else {
                            Toast.makeText(getActivity(), "REG ID가 널값입니다", Toast.LENGTH_SHORT).show();
                        }
                    }

                    return true;
                }
            });

            */

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            view.setBackgroundColor(Color.WHITE);

            return view;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */

    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);


            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
            SwitchPreference switchPreference = (SwitchPreference) findPreference("notification_onoff");
            switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    String reg_id = prefs.getString("REG_ID", "");
                    if (newValue.equals(true)) {
                        String htmlResult = "";
                        PhpDown phpDown2 = new PhpDown();

                        try {
                            htmlResult = phpDown2.execute("http://" + host + "/mysql_test8.php?gcm=1&reg_id=" + reg_id).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }

                        if (htmlResult.equals("Success1\n")) {
                            Toast.makeText(getActivity(), R.string.notification_on, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(getActivity(), R.string.gcm_no_registration, Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        String htmlResult = "";
                        PhpDown phpDown2 = new PhpDown();

                        try {
                            htmlResult = phpDown2.execute("http://" + host + "/mysql_test8.php?gcm=0&reg_id=" + reg_id).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        if (htmlResult.equals("Success1\n")) {
                            Toast.makeText(getActivity(), R.string.notification_off, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(getActivity(), R.string.gcm_no_registration, Toast.LENGTH_SHORT).show();
                        }
                    }
                    return true;
                }
            });

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            view.setBackgroundColor(Color.WHITE);
            return view;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */


    public static class InfoPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_info);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            final ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

            // multitab 관련

            Preference preference = (Preference) findPreference("REG_ID");
            bindPreferenceSummaryToValue(findPreference("REG_ID"));
            // final EditTextPreference regIdPreference = (EditTextPreference) findPreference("REG_ID");
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    ClipData clip = ClipData.newPlainText("REG_ID", preference.getSummary());
                    clipboardManager.setPrimaryClip(clip);
                    Toast.makeText(getActivity(), R.string.Settings_copy, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            Preference gcmRegistration = (Preference) findPreference("gcm_registration");
            gcmRegistration.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    new AsyncTask<Object, Object, String> () {

                        @Override
                        protected String doInBackground(Object... params) {

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            String reg_id = prefs.getString("REG_ID", "");
                            if (reg_id.equals("")) {
                                return null;
                            }
                            boolean state = prefs.getBoolean("notification_onoff", true);
                            int iState = state ? 1 : 0;
                            PhpDown_noThread phpTask = new PhpDown_noThread("http://" + host + "/mysql_test7.php?reg_id=\"" + reg_id + "\"" + "&state=" + iState);
                            String result = phpTask.phpTask();
                            return result;
                        }

                        @Override
                        protected void onPostExecute(String result) {
                            super.onPostExecute(result);
                            if (result == null) {
                                Toast.makeText(getActivity(), R.string.notification_failed, Toast.LENGTH_SHORT).show();
                            }
                            else if (result.equals("Success\n")) {
                                Toast.makeText(getActivity(), R.string.notification_success, Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getActivity(), R.string.notification_already_registration, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }.execute();
                    return true;
                }
            });
//            regIdPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//                @Override
//                public boolean onPreferenceClick(Preference preference) {
//
//                    ClipData clip = ClipData.newPlainText("REG_ID", preference.getSummary());
//                    clipboardManager.setPrimaryClip(clip);
//                    Toast.makeText(getActivity(), R.string.Settings_copy, Toast.LENGTH_SHORT).show();
//                    regIdPreference.getDialog().dismiss();
//                    return true;
//                }
//            });



            bindPreferenceSummaryToValue(findPreference("app_ver"));
            Preference appVer = (Preference) findPreference("app_ver");
            appVer.setSummary(BuildConfig.VERSION_NAME);
            bindPreferenceSummaryToValue(findPreference("app_developer"));
            /*
            getPreferenceScreen().setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (preference.getKey() == "BTN_REG") {
                        String htmlResult = "";
                        PhpDown phpDown = new PhpDown();
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        String reg_id = prefs.getString("REG_ID", "");
                        try {
                            htmlResult = phpDown.execute("http://" + host + "/mysql_test7.php?reg_id=" + reg_id).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }

                        if (htmlResult.equals("Success")) {
                            Toast.makeText(getActivity(), "등록하였습니다", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "실패하였습니다. 다시 시도하여 주시기 바랍니다", Toast.LENGTH_SHORT).show();
                        }


                        if (!reg_id.equals(null)) {  // null 이 아닐경우

                        } else {
                            Toast.makeText(getActivity(), "REG ID가 널값입니다", Toast.LENGTH_SHORT).show();
                        }
                    }

                    return true;
                }
            });

            */

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            view.setBackgroundColor(Color.WHITE);
            return view;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
