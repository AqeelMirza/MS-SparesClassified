package com.webprobity.ms_spares_classified.Notification;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.RemoteViews;

import com.webprobity.ms_spares_classified.R;
import com.webprobity.ms_spares_classified.home.HomeActivity;
import com.webprobity.ms_spares_classified.messages.ChatActivity;
import com.webprobity.ms_spares_classified.utills.SettingsMain;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;

public class NotificationUtils {
    private static String TAG = NotificationUtils.class.getSimpleName();
    SettingsMain settingsMain;
    private Context mContext;

    NotificationUtils(Context mContext) {
        this.mContext = mContext;
        settingsMain = new SettingsMain(mContext);
    }

    /**
     * Method checks if the app is in background or not
     */
    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    // Clears notification tray messages
    public static void clearNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    void showNotificationMessage(final String title, final String message, final Date timeStamp, String ad_id,
                                 String recieverId, String senderId, String type, String imageURL, String topic) {
//        if (TextUtils.isEmpty(message))
//            return;

        // notification icon
        PendingIntent resultPendingIntent = null;
        final int icon = R.drawable.logo;
        if (!settingsMain.getUserLogin().equals("0") && topic.equals("broadcast")) {
            if (isAppIsInBackground(mContext)) {
                Intent in = new Intent(mContext, HomeActivity.class);
                settingsMain.setNotificationTitle(title);
                settingsMain.setNotificationMessage(message);
                resultPendingIntent = PendingIntent.getActivity(mContext, 0, in, PendingIntent.FLAG_UPDATE_CURRENT);
            }
            final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    mContext);
            if (!TextUtils.isEmpty(imageURL)) {

                if (imageURL != null && imageURL.length() > 4 && Patterns.WEB_URL.matcher(imageURL).matches()) {

                    Bitmap bitmap = getBitmapFromURL(imageURL);

                    if (bitmap != null) {
                        showBigNotification(bitmap, mBuilder, icon, title, message, timeStamp, resultPendingIntent, senderId);
                    } else {
                        showSmallNotification(mBuilder, icon, title, message, timeStamp, resultPendingIntent, ad_id);
                    }
                }
            } else {
                showSmallNotification(mBuilder, icon, title, message, timeStamp, resultPendingIntent, ad_id);
            }
        }
        if (!settingsMain.getUserLogin().equals("0") && topic.equals("chat")) {
            Intent in = new Intent(mContext, ChatActivity.class);
            in.putExtra("adId", ad_id);
            in.putExtra("senderId", senderId);
            in.putExtra("recieverId", recieverId);
            in.putExtra("type", type);

            resultPendingIntent = PendingIntent.getActivity(mContext, 0, in, PendingIntent.FLAG_UPDATE_CURRENT);
            final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    mContext);
            showSmallNotification(mBuilder, icon, title, message, timeStamp, resultPendingIntent, ad_id);
        }

    }

    private void showSmallNotification(NotificationCompat.Builder mBuilder, int icon, String title, String message, Date timeStamp, PendingIntent resultPendingIntent, String ad_ID) {

        NotificationCompat.BigTextStyle inboxStyle = new NotificationCompat.BigTextStyle();

        inboxStyle.bigText(message);
//        inboxStyle.addLine(message);

        Notification notification;
        notification = mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
//                .setStyle(inboxStyle)
                .setWhen(timeStamp.getTime())
                .setSmallIcon(R.drawable.logo)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon))
                .setContentText(message)
//                .setContentInfo("contentinfo")
//                .setSubText(message)
                .build();
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Integer.parseInt(ad_ID), notification);
    }

    private void showBigNotification(Bitmap bitmap, NotificationCompat.Builder mBuilder, int icon, String title,
                                     String message, Date timeStamp, PendingIntent resultPendingIntent, String senderID) {
        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
        bigPictureStyle.setBigContentTitle(title);
        bigPictureStyle.setSummaryText(Html.fromHtml(message).toString());
        bigPictureStyle.bigPicture(bitmap);

        Notification notification;
        notification = mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setStyle(bigPictureStyle)
                .setWhen(timeStamp.getTime())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon))
                .setContentText(message)
                .build();

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Integer.parseInt(senderID), notification);
    }

    private Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Playing notification sound
    void playNotificationSound() {
        Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(mContext, defaultRingtoneUri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });
            mediaPlayer.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        try {
//            Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
//                    + "://" + mContext.getPackageName() + "/raw/notification");
//            Ringtone r = RingtoneManager.getRingtone(mContext, alarmSound);
//            r.play();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

}
