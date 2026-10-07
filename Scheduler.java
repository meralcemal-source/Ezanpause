package app.ezanpause;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Tek alarm zinciri. Her tick'te ayarlardan sessizlik pencerelerini HESAPLAR (takvim saklamaz, bitmez),
 * etkin pencere varsa sessizliği başlatır, yoksa durdurur ve bir sonraki sınır için tek alarm kurar.
 */
final class Scheduler {
    static final String ACTION_TICK = "app.ezanpause.TICK";

    static final class Win {
        final String name;
        final long start;
        final long end;

        Win(String name, long start, long end) {
            this.name = name;
            this.start = start;
            this.end = end;
        }
    }

    /** Arayüz için anlık durum. */
    static final class Snapshot {
        Win active;
        Win next;
    }

    private Scheduler() {}

    /** Bugünden -1..+2 gün için pencereleri üretir (gece yarısını aşanlar ve sonraki gün dahil). */
    static List<Win> windows(Context c, Settings s, long now) {
        List<Win> out = new ArrayList<>();

        long testEnd = Settings.prefs(c).getLong("test_end", 0);
        if (testEnd > now) {
            out.add(new Win("Deneme", Settings.prefs(c).getLong("test_start", 0), testEnd));
        }
        if (!s.enabled) return out;

        PrayerTimes.City city = PrayerTimes.find(s.city);
        LocalDate today = Instant.ofEpochMilli(now).atZone(city.zone).toLocalDate();
        long before = s.offsetSec * 1000L;
        long after = s.durationSec * 1000L;

        for (int i = -1; i <= 2; i++) {
            PrayerTimes.Day d = PrayerTimes.compute(city, today.plusDays(i));
            if (s.imsak) out.add(new Win("İmsak", d.imsak - before, d.imsak + after));
            if (s.ogle) out.add(new Win("Öğle", d.ogle - before, d.ogle + after));
            if (s.ikindi) out.add(new Win("İkindi", d.ikindi - before, d.ikindi + after));
            if (s.aksam) out.add(new Win("Akşam", d.aksam - before, d.aksam + after));
            if (s.yatsi) out.add(new Win("Yatsı", d.yatsi - before, d.yatsi + after));
            if (s.sela && d.thursday) out.add(new Win("Perşembe Selâsı", d.sela - before, d.sela + after));
        }
        return out;
    }

    static Snapshot snapshot(Context c) {
        long now = System.currentTimeMillis();
        Snapshot snap = new Snapshot();
        for (Win w : windows(c, Settings.load(c), now)) {
            if (now >= w.start && now < w.end) {
                if (snap.active == null || w.end > snap.active.end) snap.active = w;
            } else if (w.start > now && (snap.next == null || w.start < snap.next.start)) {
                snap.next = w;
            }
        }
        return snap;
    }

    static void tick(Context ctx) {
        Context c = ctx.getApplicationContext();
        long now = System.currentTimeMillis();
        Snapshot snap = snapshot(c);

        long next = snap.next != null ? snap.next.start : Long.MAX_VALUE;
        if (snap.active != null) {
            next = Math.min(next, snap.active.end + 300);
            startSilence(c, snap.active);
        } else {
            stopSilence(c);
        }

        if (next != Long.MAX_VALUE) {
            arm(c, Math.max(next, now + 500));
        } else {
            cancelAlarm(c);
        }
    }

    static void addTestWindow(Context c, long start, long end) {
        Settings.prefs(c).edit().putLong("test_start", start).putLong("test_end", end).apply();
        tick(c);
    }

    private static void startSilence(Context c, Win w) {
        Intent i = new Intent(c, SilenceService.class)
                .putExtra("name", w.name)
                .putExtra("start", w.start)
                .putExtra("end", w.end);
        try {
            c.startForegroundService(i);
        } catch (RuntimeException e) {
            // Arka plandan ön plan servisi başlatılamadı (tam zamanlı alarm izni yok vb.):
            // servis olmadan doğrudan sessize al; bitişteki tick sesi geri açar.
            Silencer.begin(c, w.name, w.start, w.end);
        }
    }

    private static void stopSilence(Context c) {
        c.stopService(new Intent(c, SilenceService.class));
        Silencer.end(c); // servis olmadan sessize alındıysa ya da işlem öldüyse güvenli temizlik
    }

    private static PendingIntent pending(Context c) {
        Intent i = new Intent(c, AlarmReceiver.class).setAction(ACTION_TICK);
        return PendingIntent.getBroadcast(c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static void arm(Context c, long atMs) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (canScheduleExact(c)) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMs, pending(c));
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMs, pending(c)); // sistem kaydırabilir
        }
    }

    static void cancelAlarm(Context c) {
        ((AlarmManager) c.getSystemService(Context.ALARM_SERVICE)).cancel(pending(c));
    }

    static boolean canScheduleExact(Context c) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true;
        return ((AlarmManager) c.getSystemService(Context.ALARM_SERVICE)).canScheduleExactAlarms();
    }
}
