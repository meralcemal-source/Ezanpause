package app.ezanpause;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** EzanPause ana ekranı: saniye cinsinden sessizlik süresi, şehir, vakit seçimi, izinler ve deneme. */
public class MainActivity extends Activity {
    private static final int REQ_NOTIFICATIONS = 101;
    private static final int REQ_LOCATION = 102;
    private static final int COLOR_TEXT = Color.parseColor("#E5E7EB");
    private static final int COLOR_MUTED = Color.parseColor("#94A3B8");
    private static final int COLOR_OK = Color.parseColor("#34D399");
    private static final int COLOR_WARN = Color.parseColor("#FBBF24");
    private static final int COLOR_CARD = Color.parseColor("#131C2E");

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Settings s;

    private TextView tvStatus;
    private TextView tvTimes;
    private TextView tvEvents;
    private TextView tvDurationHint;
    private TextView tvExact;
    private TextView tvNotif;
    private TextView tvBattery;
    private TextView tvGps;
    private final TextView[] prayerRows = new TextView[5];
    private Switch swEnabled;
    private Switch swRestore;
    private Switch swResume;
    private Spinner spCity;
    private EditText etDuration;
    private EditText etOffset;
    private CheckBox cbImsak;
    private CheckBox cbOgle;
    private CheckBox cbIkindi;
    private CheckBox cbAksam;
    private CheckBox cbYatsi;
    private CheckBox cbSela;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            refreshStatus();
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        s = Settings.load(this);
        buildUi();
        bindSettings();
        Scheduler.tick(this); // uygulama açılışında alarm zincirinin sağlam olduğundan emin ol
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(ticker);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(ticker);
        super.onPause();
    }

    // ------------------------------------------------------------------ arayüz

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0B1220"));
        scroll.setFillViewport(true);
        // targetSdk 35 kenardan kenara zorunlu: sistem çubuklarının altında kalmasın.
        scroll.setOnApplyWindowInsetsListener((v, insets) -> {
            v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets.consumeSystemWindowInsets();
        });

        LinearLayout root = column();
        root.setPadding(dp(16), dp(16), dp(16), dp(32));
        scroll.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = text("EzanPause", 26, Color.WHITE, true);
        root.addView(title);
        root.addView(text("Ezan vaktinde medya sesini kapatır, bitince açar.", 13, COLOR_MUTED, false));

        // Durum
        LinearLayout statusCard = card(root, null);
        tvStatus = text("", 15, COLOR_TEXT, true);
        statusCard.addView(tvStatus);
        tvTimes = text("", 12, COLOR_MUTED, false);
        tvTimes.setPadding(0, dp(8), 0, 0);
        statusCard.addView(tvTimes);

        // Vakitler: dokununca rekât ve dua bilgisi
        LinearLayout vakit = card(root, "Bugünkü vakitler (bilgi için dokunun)");
        String[] keys = {"imsak", "ogle", "ikindi", "aksam", "yatsi"};
        for (int i = 0; i < keys.length; i++) {
            final String key = keys[i];
            TextView row = text("", 16, COLOR_TEXT, false);
            row.setPadding(dp(4), dp(12), dp(4), dp(12));
            row.setOnClickListener(v -> openGuide(key));
            prayerRows[i] = row;
            vakit.addView(row);
        }
        Button bCuma = button("Cuma namazı bilgisi", Color.parseColor("#1E293B"), COLOR_TEXT);
        bCuma.setOnClickListener(v -> openGuide("cuma"));
        vakit.addView(bCuma, matchWrap(dp(6)));
        Button bDua = button("Dualar ve sûreler (Arapça, okunuş, anlam)", Color.parseColor("#1E293B"), COLOR_TEXT);
        bDua.setOnClickListener(v -> openGuide("dualar"));
        vakit.addView(bDua, matchWrap(dp(6)));

        // Genel ayarlar
        LinearLayout general = card(root, "Ayarlar");
        swEnabled = toggle("Ezan vaktinde otomatik sessize al");
        general.addView(swEnabled);

        general.addView(label("Şehir"));
        spCity = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, PrayerTimes.cityNames());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCity.setAdapter(adapter);
        general.addView(spCity);
        Button bGps = button("📍 Konumdan bul (GPS)", Color.parseColor("#1E293B"), COLOR_TEXT);
        bGps.setOnClickListener(v -> findCityByGps());
        general.addView(bGps, matchWrap(dp(8)));
        tvGps = text("", 12, COLOR_MUTED, false);
        general.addView(tvGps);

        general.addView(label("Sessizlik süresi (saniye)"));
        etDuration = numberField("örn. 240");
        general.addView(etDuration);
        tvDurationHint = text("", 12, COLOR_MUTED, false);
        general.addView(tvDurationHint);
        etDuration.addTextChangedListener(new SimpleWatcher() {
            @Override
            public void afterTextChanged(Editable e) {
                tvDurationHint.setText(humanSeconds(parseInt(e.toString(), -1)));
            }
        });

        general.addView(label("Ezandan kaç saniye ÖNCE başlasın (0 = tam vaktinde)"));
        etOffset = numberField("örn. 0");
        general.addView(etOffset);

        swRestore = toggle("Bitince sesi geri aç");
        general.addView(swRestore);
        swResume = toggle("Çalan müziği duraklat, bitince devam ettir");
        general.addView(swResume);

        // Vakitler
        LinearLayout prayers = card(root, "Hangi vakitlerde");
        cbImsak = check("İmsak / Sabah");
        cbOgle = check("Öğle");
        cbIkindi = check("İkindi");
        cbAksam = check("Akşam");
        cbYatsi = check("Yatsı");
        cbSela = check("Perşembe Selâsı (yatsıdan " + PrayerTimes.SELA_BEFORE_YATSI_MIN + " dk önce)");
        for (CheckBox cb : new CheckBox[]{cbImsak, cbOgle, cbIkindi, cbAksam, cbYatsi, cbSela}) prayers.addView(cb);

        Button save = button("Kaydet", COLOR_OK, Color.parseColor("#06251B"));
        save.setOnClickListener(v -> save());
        root.addView(save, matchWrap(dp(12)));

        // İzinler
        LinearLayout perms = card(root, "İzinler (arka planda çalışması için)");
        tvExact = text("", 13, COLOR_TEXT, false);
        perms.addView(tvExact);
        Button bExact = button("Tam zamanlı alarm izni", Color.parseColor("#1E293B"), COLOR_TEXT);
        bExact.setOnClickListener(v -> openExactAlarmSettings());
        perms.addView(bExact, matchWrap(dp(4)));

        tvNotif = text("", 13, COLOR_TEXT, false);
        tvNotif.setPadding(0, dp(10), 0, 0);
        perms.addView(tvNotif);
        Button bNotif = button("Bildirim izni", Color.parseColor("#1E293B"), COLOR_TEXT);
        bNotif.setOnClickListener(v -> requestNotifications());
        perms.addView(bNotif, matchWrap(dp(4)));

        tvBattery = text("", 13, COLOR_TEXT, false);
        tvBattery.setPadding(0, dp(10), 0, 0);
        perms.addView(tvBattery);
        Button bBattery = button("Pil kısıtlaması ayarları", Color.parseColor("#1E293B"), COLOR_TEXT);
        bBattery.setOnClickListener(v -> startSettings(new Intent(
                android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)));
        perms.addView(bBattery, matchWrap(dp(4)));
        perms.addView(text("Xiaomi, Huawei, Samsung, Oppo gibi telefonlarda ayrıca \"otomatik başlat / arka planda çalışsın\" iznini açın.",
                11, COLOR_MUTED, false));

        // Deneme
        LinearLayout test = card(root, "Deneme");
        test.addView(text("Müzik veya video başlatın, düğmeye basın. 3 saniye sonra sessizlik başlar ve ayarlı süre kadar sürer "
                + "(en fazla 120 sn). Ekranı kilitleyerek de deneyin.", 12, COLOR_MUTED, false));
        Button bTest = button("Denemeyi başlat", Color.parseColor("#0EA5E9"), Color.WHITE);
        bTest.setOnClickListener(v -> startTest());
        test.addView(bTest, matchWrap(dp(8)));

        // Son kayıtlar
        LinearLayout log = card(root, "Son sessizlikler");
        tvEvents = text("", 12, COLOR_TEXT, false);
        log.addView(tvEvents);

        setContentView(scroll);
    }

    private void bindSettings() {
        swEnabled.setChecked(s.enabled);
        String[] names = PrayerTimes.cityNames();
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(s.city)) {
                spCity.setSelection(i);
                break;
            }
        }
        etDuration.setText(String.valueOf(s.durationSec));
        etOffset.setText(String.valueOf(s.offsetSec));
        swRestore.setChecked(s.restore);
        swResume.setChecked(s.resume);
        cbImsak.setChecked(s.imsak);
        cbOgle.setChecked(s.ogle);
        cbIkindi.setChecked(s.ikindi);
        cbAksam.setChecked(s.aksam);
        cbYatsi.setChecked(s.yatsi);
        cbSela.setChecked(s.sela);
    }

    private void save() {
        int dur = parseInt(etDuration.getText().toString(), -1);
        int off = parseInt(etOffset.getText().toString(), 0);
        if (dur < 1 || dur > Settings.MAX_DURATION_SEC) {
            Toast.makeText(this, "Süre 1 ile " + Settings.MAX_DURATION_SEC + " saniye arasında olmalı", Toast.LENGTH_LONG).show();
            return;
        }
        if (off < 0 || off > Settings.MAX_OFFSET_SEC) {
            Toast.makeText(this, "Öncesi 0 ile " + Settings.MAX_OFFSET_SEC + " saniye arasında olmalı", Toast.LENGTH_LONG).show();
            return;
        }
        s.enabled = swEnabled.isChecked();
        s.city = (String) spCity.getSelectedItem();
        s.durationSec = dur;
        s.offsetSec = off;
        s.restore = swRestore.isChecked();
        s.resume = swResume.isChecked();
        s.imsak = cbImsak.isChecked();
        s.ogle = cbOgle.isChecked();
        s.ikindi = cbIkindi.isChecked();
        s.aksam = cbAksam.isChecked();
        s.yatsi = cbYatsi.isChecked();
        s.sela = cbSela.isChecked();
        s.save(this);
        Scheduler.tick(this);
        Toast.makeText(this, "Kaydedildi", Toast.LENGTH_SHORT).show();
        refreshStatus();
    }

    private void openGuide(String key) {
        startActivity(new Intent(this, GuideActivity.class).putExtra(GuideActivity.EXTRA_KEY, key));
    }

    // ------------------------------------------------------------------ GPS ile şehir

    private void findCityByGps() {
        boolean granted = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!granted) {
            // Android 12+ için ikisi birlikte istenir; kullanıcı "yaklaşık" seçse de şehir için yeterli.
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION);
            return;
        }
        tvGps.setText("Konum aranıyor…");
        LocationFinder.find(this, loc -> {
            if (isFinishing() || isDestroyed()) return;
            onLocation(loc);
        });
    }

    private void onLocation(Location loc) {
        if (loc == null) {
            tvGps.setText("Konum alınamadı. Konum (GPS) servisinin açık olduğundan emin olun ve tekrar deneyin; "
                    + "olmazsa şehri listeden seçin.");
            return;
        }
        PrayerTimes.City c = PrayerTimes.nearest(loc.getLatitude(), loc.getLongitude());
        long km = Math.round(PrayerTimes.distanceKm(loc.getLatitude(), loc.getLongitude(), c.lat, c.lng));
        String[] names = PrayerTimes.cityNames();
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(c.name)) {
                spCity.setSelection(i);
                break;
            }
        }
        s.city = c.name;
        s.save(this);
        Scheduler.tick(this);
        tvGps.setText("Bulunan şehir: " + c.name + " (yaklaşık " + km + " km)"
                + (km > 120 ? "\nUyarı: kayıtlı en yakın şehir uzakta; vakitler birkaç dakika farklı olabilir." : ""));
        refreshStatus();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION) {
            boolean ok = false;
            for (int r : grantResults) if (r == PackageManager.PERMISSION_GRANTED) ok = true;
            if (ok) findCityByGps();
            else tvGps.setText("Konum izni verilmedi. Şehri listeden seçebilirsiniz.");
        }
    }

    private void startTest() {
        int dur = Settings.clamp(parseInt(etDuration.getText().toString(), s.durationSec), 5, 120);
        long start = System.currentTimeMillis() + 3000;
        Scheduler.addTestWindow(this, start, start + dur * 1000L);
        Toast.makeText(this, "3 sn sonra " + dur + " sn sessizlik", Toast.LENGTH_SHORT).show();
    }

    // ------------------------------------------------------------------ durum

    private void refreshStatus() {
        Scheduler.Snapshot snap = Scheduler.snapshot(this);
        long now = System.currentTimeMillis();
        PrayerTimes.City city = PrayerTimes.find(s.city);
        DateTimeFormatter hm = DateTimeFormatter.ofPattern("HH:mm").withZone(city.zone);

        if (!s.enabled) {
            tvStatus.setText("Kapalı (otomatik sessize alma devre dışı)");
        } else if (snap.active != null) {
            tvStatus.setText("Şu an sessizde: " + snap.active.name + "\nKalan: " + countdown(snap.active.end - now));
        } else if (snap.next != null) {
            tvStatus.setText("Sonraki: " + snap.next.name + " · " + hm.format(Instant.ofEpochMilli(snap.next.start))
                    + "\nKalan: " + countdown(snap.next.start - now));
        } else {
            tvStatus.setText("Seçili vakit yok");
        }

        LocalDate today = Instant.ofEpochMilli(now).atZone(city.zone).toLocalDate();
        PrayerTimes.Day d = PrayerTimes.compute(city, today);
        tvTimes.setText(city.name + " bugün · Güneş doğuşu " + hm.format(Instant.ofEpochMilli(d.gunes)));
        long[] t = {d.imsak, d.ogle, d.ikindi, d.aksam, d.yatsi};
        String[] n = {"İmsak / Sabah", "Öğle", "İkindi", "Akşam", "Yatsı"};
        for (int i = 0; i < prayerRows.length; i++) {
            prayerRows[i].setText(n[i] + "   " + hm.format(Instant.ofEpochMilli(t[i])) + "   ›");
        }

        setPerm(tvExact, "Tam zamanlı alarm", Scheduler.canScheduleExact(this));
        NotificationManager nm = getSystemService(NotificationManager.class);
        setPerm(tvNotif, "Bildirimler", nm.areNotificationsEnabled());
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        setPerm(tvBattery, "Pil kısıtlaması yok (önerilir)", pm.isIgnoringBatteryOptimizations(getPackageName()));

        List<String> ev = EventLog.recent(this, 6, city.zone);
        tvEvents.setText(ev.isEmpty() ? "Henüz kayıt yok." : String.join("\n", ev));
    }

    private void setPerm(TextView tv, String label, boolean ok) {
        tv.setText((ok ? "✓ " : "✗ ") + label);
        tv.setTextColor(ok ? COLOR_OK : COLOR_WARN);
    }

    private static String countdown(long ms) {
        long sec = Math.max(0, ms / 1000);
        long h = sec / 3600;
        long m = (sec % 3600) / 60;
        long r = sec % 60;
        return h > 0 ? String.format("%d sa %02d dk %02d sn", h, m, r) : String.format("%02d dk %02d sn", m, r);
    }

    private static String humanSeconds(int sec) {
        if (sec < 1) return "";
        int m = sec / 60;
        int r = sec % 60;
        if (m == 0) return "= " + r + " sn";
        return "= " + m + " dk " + r + " sn";
    }

    // ------------------------------------------------------------------ izinler

    private void requestNotifications() {
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIFICATIONS);
        }
        // İzin kalıcı reddedildiyse sistem diyalog göstermez; bildirim ayarını da aç.
        if (Build.VERSION.SDK_INT < 33 || !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            handler.postDelayed(() -> {
                if (!getSystemService(NotificationManager.class).areNotificationsEnabled()) {
                    startSettings(new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, getPackageName()));
                }
            }, 800);
        }
    }

    private void openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startSettings(new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:" + getPackageName())));
        }
    }

    private void startSettings(Intent i) {
        try {
            startActivity(i);
        } catch (RuntimeException e) {
            try {
                startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
            } catch (RuntimeException ignored) {
            }
        }
    }

    // ------------------------------------------------------------------ küçük yardımcılar

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private static int parseInt(String v, int fallback) {
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private LinearLayout.LayoutParams matchWrap(int topMargin) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = topMargin;
        return lp;
    }

    private LinearLayout card(LinearLayout parent, String heading) {
        LinearLayout c = column();
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(COLOR_CARD);
        bg.setCornerRadius(dp(16));
        c.setBackground(bg);
        c.setPadding(dp(14), dp(12), dp(14), dp(14));
        if (heading != null) {
            TextView h = text(heading, 15, Color.WHITE, true);
            h.setPadding(0, 0, 0, dp(6));
            c.addView(h);
        }
        parent.addView(c, matchWrap(dp(14)));
        return c;
    }

    private TextView text(String t, int sp, int color, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(t);
        tv.setTextSize(sp);
        tv.setTextColor(color);
        if (bold) tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
        return tv;
    }

    private TextView label(String t) {
        TextView tv = text(t, 12, COLOR_MUTED, false);
        tv.setPadding(0, dp(12), 0, dp(2));
        return tv;
    }

    private Switch toggle(String t) {
        Switch sw = new Switch(this);
        sw.setText(t);
        sw.setTextColor(COLOR_TEXT);
        sw.setPadding(0, dp(8), 0, dp(8));
        return sw;
    }

    private CheckBox check(String t) {
        CheckBox cb = new CheckBox(this);
        cb.setText(t);
        cb.setTextColor(COLOR_TEXT);
        return cb;
    }

    private EditText numberField(String hint) {
        EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_NUMBER);
        et.setHint(hint);
        et.setTextColor(Color.WHITE);
        et.setHintTextColor(COLOR_MUTED);
        et.setSingleLine(true);
        et.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(5)});
        return et;
    }

    private Button button(String t, int bgColor, int textColor) {
        Button b = new Button(this);
        b.setText(t);
        b.setAllCaps(false);
        b.setTextColor(textColor);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(bgColor);
        bg.setCornerRadius(dp(12));
        b.setBackground(bg);
        b.setGravity(Gravity.CENTER);
        return b;
    }

    private abstract static class SimpleWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int a, int b, int c) {
        }

        @Override
        public void onTextChanged(CharSequence s, int a, int b, int c) {
        }
    }
}
