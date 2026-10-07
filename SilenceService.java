package app.ezanpause;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

/** Ezan penceresi boyunca süreci canlı tutan ön plan servisi (sessizlik ve ses odağı burada tutulur). */
public class SilenceService extends Service {
    private static final String CHANNEL_ID = "ezanpause_silence";
    private static final int NOTIF_ID = 4107;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        String name = intent != null ? intent.getStringExtra("name") : null;
        long start = intent != null ? intent.getLongExtra("start", 0) : 0;
        long end = intent != null ? intent.getLongExtra("end", 0) : 0;
        if (name == null || start == 0 || end == 0) {
            stopSelf(startId);
            return START_NOT_STICKY;
        }

        createChannel();
        Notification n = buildNotification(name, end);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIF_ID, n);
        }

        Silencer.begin(this, name, start, end);

        // Yedek: alarm gecikse bile pencere bitince servis kendini kapatır (ve sesi geri açar).
        handler.removeCallbacksAndMessages(null);
        long delay = Math.max(1000, end - System.currentTimeMillis()) + 500;
        handler.postDelayed(() -> stopSelf(startId), delay);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        Silencer.end(this);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createChannel() {
        NotificationManager nm = getSystemService(NotificationManager.class);
        NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID, getString(R.string.channel_name), NotificationManager.IMPORTANCE_LOW);
        ch.setDescription(getString(R.string.channel_desc));
        ch.setShowBadge(false);
        nm.createNotificationChannel(ch);
    }

    private Notification buildNotification(String name, long endMs) {
        Intent open = new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, open, PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_ezanpause)
                .setContentTitle("EzanPause · " + name)
                .setContentText("Medya sesi ezan için kapalı")
                .setOngoing(true)
                .setShowWhen(true)
                .setWhen(endMs)
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setContentIntent(pi)
                .build();
    }
}
