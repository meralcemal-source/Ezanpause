package app.ezanpause;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Gerçekleşen sessizliklerin kısa kaydı (son 30). */
final class EventLog {
    private static final String KEY = "events";
    private static final int MAX = 30;

    private EventLog() {}

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences("ezanpause_state", Context.MODE_PRIVATE);
    }

    static void add(Context c, String name, long startMs, long endMs) {
        try {
            JSONArray old = new JSONArray(prefs(c).getString(KEY, "[]"));
            JSONArray fresh = new JSONArray();
            JSONObject o = new JSONObject();
            o.put("n", name);
            o.put("s", startMs);
            o.put("sec", Math.max(1, Math.round((endMs - startMs) / 1000f)));
            fresh.put(o);
            for (int i = 0; i < old.length() && fresh.length() < MAX; i++) fresh.put(old.get(i));
            prefs(c).edit().putString(KEY, fresh.toString()).apply();
        } catch (JSONException ignored) {
        }
    }

    /** En yeni başta, okunur satırlar. */
    static List<String> recent(Context c, int limit, ZoneId zone) {
        List<String> out = new ArrayList<>();
        DateTimeFormatter f = DateTimeFormatter.ofPattern("d MMM HH:mm").withZone(zone);
        try {
            JSONArray arr = new JSONArray(prefs(c).getString(KEY, "[]"));
            for (int i = 0; i < arr.length() && out.size() < limit; i++) {
                JSONObject o = arr.getJSONObject(i);
                out.add(f.format(Instant.ofEpochMilli(o.getLong("s"))) + " · " + o.getString("n")
                        + " · " + o.getInt("sec") + " sn");
            }
        } catch (JSONException ignored) {
        }
        return out;
    }

    static void clear(Context c) {
        prefs(c).edit().remove(KEY).apply();
    }
}
