package com.onaries.smarthome;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by SW on 2015-11-20.
 */
public class GcmIntentService extends IntentService {
    private NotificationManager mNotificationManager;
    private SharedPreferences prefs;        // 설정에서 쓰기 위해 사용되는 변수
    private int notification_id = 1;
    public GcmIntentService() {
//        Used to name the worker thread, important only for debugging.
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);    // 메시지 타입
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); // 설정값 불러오기

        if (!extras.isEmpty()) {    // Bundle에 값이 있을 경우
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                // 메시지가 에러일경우
                sendNotification("Send error:" + extras.toString(), notification_id);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                // 메시지가 삭제되었을 경우
                sendNotification("Deleted messages on server: " + extras.toString(), notification_id);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // 메시지가 제대로 잘 전송된 경우
                String msg = intent.getStringExtra("message");  // GCM으로 받은 메시지중 message라는 태그 부분의 메시지 추출
                String kMsg = "";   // 실제 알림 메시지를 이용하기 위해 사용하는 문자열 변수
                int noti_id;    // noti_id값을 달리하여 하나의 알림에 겹쳐보이지 않게 하기 위함

                // 받은 메시지가 "gas"일 경우 - 가스값이 일정값을 넘었을 경우
                if (msg.equals("gas")) {
                    kMsg = getString(R.string.gcm_message_gas_high);
                    noti_id = 2;
                }
                // 받은 메시지가 "humi_high"일 경우 - 습도값이 일정값을 넘었을 경우
                else if (msg.equals("humi_high")) {
                    String humi_high = prefs.getString("humi_high", "80");
                    kMsg = getString(R.string.gcm_message_humi_high_1) + humi_high + getString(R.string.gcm_message_humi_high_2);
                    noti_id = 3;
                }
                else if (msg.equals("humi_low")) {
                    String humi_low = prefs.getString("humi_low", "30");
                    kMsg = getString(R.string.gcm_message_humi_high_1) + humi_low + getString(R.string.gcm_message_humi_low_2);
                    noti_id = 3;
                }
                else if (msg.equals("temp_high")) {
                    String temp_high = prefs.getString("temp_high", "30");
                    kMsg = getString(R.string.gcm_message_temp_1) + temp_high + getString(R.string.gcm_message_temp_high1);
                    noti_id = 4;
                }
                else if (msg.equals("temp_low")) {
                    String temp_low = prefs.getString("temp_low", "30");
                    kMsg = getString(R.string.gcm_message_temp_1) + temp_low + getString(R.string.gcm_message_temp_low_1);
                    noti_id = 4;
                }
                else if (msg.equals("multi1_on")) {
                    kMsg = getString(R.string.gcm_message_multi1_on);
                    noti_id = 5;
                }
                else if (msg.equals("multi1_off")) {
                    kMsg = getString(R.string.gcm_message_multi1_off);
                    noti_id = 5;
                }
                else if (msg.equals("multi2_on")) {
                    kMsg = getString(R.string.gcm_message_multi2_on);
                    noti_id = 5;
                }
                else if (msg.equals("multi2_off")) {
                    kMsg = getString(R.string.gcm_message_multi2_off);
                    noti_id = 5;
                }
                else if (msg.equals("multi3_on")) {
                    kMsg = getString(R.string.gcm_message_multi3_on);
                    noti_id = 5;
                }
                else if (msg.equals("multi3_off")) {
                    kMsg = getString(R.string.gcm_message_multi3_off);
                    noti_id = 5;
                }
                else if (msg.equals("multi_all_on")) {
                    kMsg = getString(R.string.gcm_message_multi_all_on);
                    noti_id = 5;
                }
                else if (msg.equals("multi_all_off")) {
                    kMsg = getString(R.string.gcm_message_multi_all_off);
                    noti_id = 5;
                }
                else {
                    kMsg = getString(R.string.gcm_mssage_error);
                    noti_id = 1;
                }

                if (noti_id == 2 && prefs.getBoolean("notification_gas_sensor", true)) {
                    sendNotification(kMsg, noti_id);
                }
                else if (noti_id == 3 && prefs.getBoolean("notification_humi_sensor", true)) {
                    sendNotification(kMsg, noti_id);
                }
                else if (noti_id == 4 && prefs.getBoolean("notification_temp_sensor", true)) {
                    sendNotification(kMsg, noti_id);
                }
                else if (noti_id == 5 && prefs.getBoolean("notification_multitab", true)) {
                    sendNotification(kMsg, noti_id);
                }
                else {
                    return;
                }
                Log.i("GcmIntentService", "Received: " + extras.toString());
                Log.i("GcmIntentService", msg );
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg, int notification_id) {    // 문자열 변수인 메시지와 숫자 변수인 notification_id
        // 메시지 Notification에 띄우기 위해 사용되는 함수

       /* mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                       // .setSmallIcon(R.drawable.gcm)
                        .setContentTitle("GCM Notification")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        */
        boolean vibrate = prefs.getBoolean("notifications_new_message_vibrate", true);                  // 설정에서 진동 여부를 불러옴
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("msg", msg);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);    // Notification에서 클릭할 경우 실행되는 intent

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher).setContentTitle(getString(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg).setAutoCancel(true)
                .setSound(Uri.parse(prefs.getString("notifications_new_message_ringtone", "DEFAULT_SOUND")));

        if (vibrate) {  // 진동 기능이 설정되어 있을 경우
            mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
        }
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(notification_id, mBuilder.build());
    }
}