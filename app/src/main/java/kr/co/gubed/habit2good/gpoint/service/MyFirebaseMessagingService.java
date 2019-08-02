package kr.co.gubed.habit2good.gpoint.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

import kr.co.gubed.habit2good.DashboardActivity;
import kr.co.gubed.habit2good.PointmallActivity;
import kr.co.gubed.habit2good.R;
import kr.co.gubed.habit2good.gpoint.util.Applications;
import kr.co.gubed.habit2good.gpoint.util.CommonUtil;
import kr.co.gubed.habit2good.gpoint.util.Preference;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    private final String NOTIFICATION_TYPE_REWARD_GPOINT = "reward";
    private final String NOTIFICATION_TYPE_REWARD_TROPHY = "trophy";
    private final String NOTIFICATION_TYPE_PURCHASE = "purchase";
    private final String NOTIFICATION_TYPE_GIFT = "gift";
    private final String NOTIFICATION_TYPE_GOOD_SAYING = "goodsaying";
    private final String NOTIFICATION_TYPE_NOTICE = "notice";

    //int numberOfMessage = 0;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.i(TAG, "From: " + remoteMessage.getFrom());
        Log.i(TAG, "MessageId: " + remoteMessage.getMessageId());
        Log.i(TAG, "MessageType: " + remoteMessage.getData().get("type"));
        //Log.d(TAG, "Notification Title : " + remoteMessage.getNotification().getTitle());
        //Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        String messageType = "";
        if( remoteMessage.getData().get("type") != null){
            messageType = remoteMessage.getData().get("type");
        }
        Log.i(getClass().getName(), "messageType = "+messageType);

        String contents = null;
        if (remoteMessage.getData().get("contents") != null) {
            contents = remoteMessage.getData().get("contents");
            Log.i(getClass().getName(), "notice contents="+contents);
        }

        Preference preference = new Preference(getApplicationContext());
        if (messageType.equals("habit") || messageType.equals("goodsaying")) {
            sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), messageType, contents);
            Applications.preference.put(CommonUtil.INTENT_TYPE_GOOD_SAYING, remoteMessage.getData().get("body"));
        }else if(messageType.equals("notice")) {
            sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), messageType, contents);
        }else if(messageType.equals("reward")) {
            sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), messageType, contents);
        } else {
            if (preference.getValue(Preference.CASH_POP_ALARM, true)) {
                sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), messageType, contents);
            }
        }
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String title, String messageBody, String type, String contents) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = null;
        String channelId = CommonUtil.CHANNEL_ID_GOOD_SAYING; // defualt 로 설정
        Bundle bundle = new Bundle();

        switch (type) {
            case NOTIFICATION_TYPE_REWARD_GPOINT:
            case NOTIFICATION_TYPE_GIFT:
                channelId = CommonUtil.CHANNEL_ID_REWARD_GPOINT;
                intent = new Intent(getApplicationContext(), PointmallActivity.class);
                intent.setAction(CommonUtil.CLICK_ACTION_REWARD);
                break;
            case NOTIFICATION_TYPE_REWARD_TROPHY:
                channelId = CommonUtil.CHANNEL_ID_REWARD_TROPHY;
                intent = new Intent(getApplicationContext(), DashboardActivity.class);
                intent.setAction(CommonUtil.CLICK_ACTION_TROPHY);
                break;
            case NOTIFICATION_TYPE_GOOD_SAYING:
                channelId = CommonUtil.CHANNEL_ID_GOOD_SAYING;
                intent = new Intent(getApplicationContext(), DashboardActivity.class);
                intent.setAction(CommonUtil.CLICK_ACTION_GOODSAYING);
                bundle.putString(CommonUtil.INTENT_TYPE_GOOD_SAYING, messageBody);
                Log.i(getClass().getName(), "get NOTIFICATION_TYPE_GOOD_SAYING");
                break;
            case NOTIFICATION_TYPE_PURCHASE:
                channelId = CommonUtil.CHANNEL_ID_PURCHASE;
                intent = new Intent(getApplicationContext(), PointmallActivity.class);
                intent.setAction(CommonUtil.CLICK_ACTION_PURCHASE);
                break;
            case NOTIFICATION_TYPE_NOTICE:
                channelId = CommonUtil.CHANNEL_ID_NOTICE;
                intent = new Intent(getApplicationContext(), DashboardActivity.class);
                intent.setAction(CommonUtil.CLICK_ACTION_NOTICE);
                if (contents != null) {
                    bundle.putString(CommonUtil.EXTRA_NOTICE_CONTENTS, contents);
                }
                break;
            default:
                intent = new Intent(getApplicationContext(), DashboardActivity.class);
        }
        intent.putExtras(bundle);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);

        Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.icon_80);
        //Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int requestId = (int) System.currentTimeMillis();

        /*
         * 채널 그룹내에서 계속 쌓인는 것을 의도하고 있음. FLAG_UPDATE_CURRENT로 인해 쌓이지 않고 새로운 것으로 없데이트 되는지 확인 필요
         * Material design 에서는 쌓지말고 업데이트하는 것을 권고하고 있음. 현재는 업데이트로 코딩
         */
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), requestId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentTitle(title)
                .setContentText(messageBody)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                //.setNumber(numberOfMessage++)
                //.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                //.setSound(defaultSoundUri)
                .setSmallIcon(R.drawable.icon_80)
                .setLargeIcon(bitmap)
                //.setBadgeIconType(R.drawable.icon_80)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(messageBody));

        try {
            Objects.requireNonNull(notificationManager).notify(0, builder.build());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

       Log.i(TAG, "end of sendNotification");
    }
}