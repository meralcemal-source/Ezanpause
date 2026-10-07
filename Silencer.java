package app.ezanpause;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.KeyEvent;

/**
 * Asıl iş: medya sesini kapatır, ses odağını alarak çalan uygulamaları (Spotify, YouTube…) duraklatır,
 * pencere bitince sesi geri açar. Durum SharedPreferences'ta tutulur; işlem öldürülse bile
 * bir sonraki tick sesi doğru şekilde geri açabilir.
 */
final class Silencer {
    private static final String STATE = "ezanpause_state";
    private static final String K_START = "active_start";
    private static final String K_NAME = "active_name";
    private static final String K_MUTED = "muted_by_us";
    private static final String K_PLAYING = "was_playing";

    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final AudioManager.OnAudioFocusChangeListener FOCUS_LISTENER = change -> { };
    private static AudioFocusRequest focusRequest;

    private Silencer() {}

    private static SharedPreferences state(Context c) {
        return c.getApplicationContext().getSharedPreferences(STATE, Context.MODE_PRIVATE);
    }

    static synchronized void begin(Context ctx, String name, long start, long end) {
        final Context c = ctx.getApplicationContext();
        SharedPreferences p = state(c);
        long activeStart = p.getLong(K_START, 0);
        if (activeStart == start) return; // bu pencere zaten işleniyor
        if (activeStart != 0) endInternal(c, false); // önceki pencere hâlâ açıksa kapat

        final AudioManager am = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
        boolean wasPlaying = am.isMusicActive();
        boolean alreadyMuted = am.isStreamMute(AudioManager.STREAM_MUSIC);

        // 1) Önce sesi kes (anında etki).
        if (!alreadyMuted) {
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
        }

        // 2) Ses odağını al: odağa saygı gösteren oynatıcılar duraklar, bitince kendiliğinden devam eder.
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(attrs)
                .setOnAudioFocusChangeListener(FOCUS_LISTENER)
                .setAcceptsDelayedFocusGain(false)
                .build();
        am.requestAudioFocus(focusRequest);

        // 3) Hâlâ çalıyorsa (odağı yok sayan oynatıcı) "duraklat" tuşu gönder.
        if (wasPlaying) {
            MAIN.postDelayed(() -> {
                if (am.isMusicActive()) sendKey(am, KeyEvent.KEYCODE_MEDIA_PAUSE);
            }, 700);
        }

        p.edit()
                .putLong(K_START, start)
                .putString(K_NAME, name)
                .putBoolean(K_MUTED, !alreadyMuted)
                .putBoolean(K_PLAYING, wasPlaying)
                .apply();
    }

    static synchronized void end(Context ctx) {
        endInternal(ctx.getApplicationContext(), true);
    }

    private static void endInternal(Context c, boolean allowResume) {
        SharedPreferences p = state(c);
        long start = p.getLong(K_START, 0);
        if (start == 0) return; // etkin pencere yok

        final AudioManager am = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
        boolean mutedByUs = p.getBoolean(K_MUTED, false);
        boolean wasPlaying = p.getBoolean(K_PLAYING, false);
        String name = p.getString(K_NAME, "Ezan");
        Settings s = Settings.load(c);

        // Ses zaten kapalıysa (biz kapatmadıysak) dokunma.
        if (mutedByUs && s.restore) {
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
        }

        if (focusRequest != null) {
            am.abandonAudioFocusRequest(focusRequest);
            focusRequest = null;
        }

        p.edit().remove(K_START).remove(K_NAME).remove(K_MUTED).remove(K_PLAYING).apply();
        EventLog.add(c, name, start, System.currentTimeMillis());

        // Odağı geri verince kendiliğinden devam etmeyen oynatıcıyı yalnızca biz durdurduysak başlat.
        if (allowResume && s.restore && s.resume && wasPlaying) {
            MAIN.postDelayed(() -> {
                if (!am.isMusicActive()) sendKey(am, KeyEvent.KEYCODE_MEDIA_PLAY);
            }, 1200);
        }
    }

    static boolean isActive(Context c) {
        return state(c).getLong(K_START, 0) != 0;
    }

    private static void sendKey(AudioManager am, int keyCode) {
        long t = SystemClock.uptimeMillis();
        am.dispatchMediaKeyEvent(new KeyEvent(t, t, KeyEvent.ACTION_DOWN, keyCode, 0));
        am.dispatchMediaKeyEvent(new KeyEvent(t, t, KeyEvent.ACTION_UP, keyCode, 0));
    }
}
