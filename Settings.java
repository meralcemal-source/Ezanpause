package app.ezanpause;

import android.content.Context;
import android.content.SharedPreferences;

/** Kullanıcı ayarları. Süreler SANİYE cinsindendir. */
final class Settings {
    static final int MAX_DURATION_SEC = 7200; // 2 saat
    static final int MAX_OFFSET_SEC = 3600;   // ezandan en fazla 1 saat önce

    private static final String PREFS = "ezanpause_settings";

    boolean enabled = true;
    String city = "İstanbul";
    /** Ezan vaktinden sonra sessizliğin süreceği süre (saniye). */
    int durationSec = 240;
    /** Ezandan kaç saniye ÖNCE sessizlik başlasın. */
    int offsetSec = 0;
    boolean restore = true;
    boolean resume = true;
    boolean imsak = true;
    boolean ogle = true;
    boolean ikindi = true;
    boolean aksam = true;
    boolean yatsi = true;
    boolean sela = true;

    static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static Settings load(Context c) {
        SharedPreferences p = prefs(c);
        Settings s = new Settings();
        s.enabled = p.getBoolean("enabled", s.enabled);
        s.city = p.getString("city", s.city);
        s.durationSec = clamp(p.getInt("durationSec", s.durationSec), 1, MAX_DURATION_SEC);
        s.offsetSec = clamp(p.getInt("offsetSec", s.offsetSec), 0, MAX_OFFSET_SEC);
        s.restore = p.getBoolean("restore", s.restore);
        s.resume = p.getBoolean("resume", s.resume);
        s.imsak = p.getBoolean("imsak", s.imsak);
        s.ogle = p.getBoolean("ogle", s.ogle);
        s.ikindi = p.getBoolean("ikindi", s.ikindi);
        s.aksam = p.getBoolean("aksam", s.aksam);
        s.yatsi = p.getBoolean("yatsi", s.yatsi);
        s.sela = p.getBoolean("sela", s.sela);
        return s;
    }

    void save(Context c) {
        durationSec = clamp(durationSec, 1, MAX_DURATION_SEC);
        offsetSec = clamp(offsetSec, 0, MAX_OFFSET_SEC);
        prefs(c).edit()
                .putBoolean("enabled", enabled)
                .putString("city", city)
                .putInt("durationSec", durationSec)
                .putInt("offsetSec", offsetSec)
                .putBoolean("restore", restore)
                .putBoolean("resume", resume)
                .putBoolean("imsak", imsak)
                .putBoolean("ogle", ogle)
                .putBoolean("ikindi", ikindi)
                .putBoolean("aksam", aksam)
                .putBoolean("yatsi", yatsi)
                .putBoolean("sela", sela)
                .apply();
    }

    static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
