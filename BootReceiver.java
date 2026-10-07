package app.ezanpause;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Yeniden başlatma, güncelleme, saat/saat dilimi değişiminde alarm zincirini yeniden kurar. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String a = intent != null ? intent.getAction() : null;
        if (Intent.ACTION_BOOT_COMPLETED.equals(a)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(a)
                || Intent.ACTION_TIME_CHANGED.equals(a)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(a)) {
            Scheduler.tick(context);
        }
    }
}
