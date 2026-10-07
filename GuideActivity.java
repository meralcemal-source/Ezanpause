package app.ezanpause;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Bir vakte dokununca açılan bilgi ekranı: rekât sayısı, sünnet/farz/vacip ayrımı, hangi rekâtta ne okunduğu
 * ve okunan dualar (Arapça, okunuşu, anlamı). extra "key" = imsak|ogle|ikindi|aksam|yatsi|cuma|dualar.
 */
public class GuideActivity extends Activity {
    static final String EXTRA_KEY = "key";
    private static final int COLOR_TEXT = Color.parseColor("#E5E7EB");
    private static final int COLOR_MUTED = Color.parseColor("#94A3B8");
    private static final int COLOR_ACCENT = Color.parseColor("#34D399");
    private static final int COLOR_GOLD = Color.parseColor("#FBBF24");
    private static final int COLOR_CARD = Color.parseColor("#131C2E");
    private static final int COLOR_INNER = Color.parseColor("#0F1727");

    private LinearLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String key = getIntent().getStringExtra(EXTRA_KEY);
        if (key == null) key = "ogle";

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0B1220"));
        scroll.setFillViewport(true);
        scroll.setOnApplyWindowInsetsListener((v, insets) -> {
            v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets.consumeSystemWindowInsets();
        });
        root = column();
        root.setPadding(dp(16), dp(12), dp(16), dp(32));
        scroll.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Button back = new Button(this);
        back.setText("‹ Geri");
        back.setAllCaps(false);
        back.setTextColor(COLOR_TEXT);
        back.setBackground(round(Color.parseColor("#1E293B"), 12));
        back.setOnClickListener(v -> finish());
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        root.addView(back, blp);

        if ("dualar".equals(key)) buildDuaList(); else buildPrayer(PrayerGuide.prayer(key));
        setContentView(scroll);
    }

    // ------------------------------------------------------------------ vakit sayfası

    private void buildPrayer(PrayerGuide.Prayer p) {
        root.addView(text(p.title, 24, Color.WHITE, true, 12));
        root.addView(text("Toplam " + p.totalRekat() + " rekât (sünnet + farz"
                + (p.key.equals("yatsi") ? " + vacip" : "") + ")", 14, COLOR_ACCENT, true, 4));

        LinearLayout info = card();
        info.addView(text(p.timeInfo, 13, COLOR_TEXT, false, 0));
        TextView note = text(p.note, 12, COLOR_MUTED, false, 8);
        info.addView(note);

        // Özet tablo
        LinearLayout sum = card();
        sum.addView(text("Rekât özeti", 15, Color.WHITE, true, 0));
        for (PrayerGuide.Part part : p.parts) {
            TextView t = text("• " + part.name + ": " + part.rekat + " rekât  —  " + part.ruling, 13, COLOR_TEXT, false, 6);
            sum.addView(t);
        }

        // Her bölüm: rekât rekât ne okunur
        for (PrayerGuide.Part part : p.parts) {
            LinearLayout c = card();
            c.addView(text(part.name + " · " + part.rekat + " rekât", 16, Color.WHITE, true, 0));
            c.addView(text(part.ruling, 12, COLOR_GOLD, false, 2));
            for (String step : part.steps) {
                c.addView(text(step, 13, COLOR_TEXT, false, 10));
            }
            c.addView(text("Bu bölümde okunanlar", 12, COLOR_MUTED, true, 12));
            StringBuilder sb = new StringBuilder();
            for (String id : part.duaIds) {
                PrayerGuide.Dua d = PrayerGuide.dua(id);
                if (d != null) {
                    if (sb.length() > 0) sb.append(" · ");
                    sb.append(d.name);
                }
            }
            c.addView(text(sb.toString(), 12, COLOR_TEXT, false, 2));
        }

        root.addView(text("Okunan dualar ve sûreler", 18, Color.WHITE, true, 20));
        // Bu vakitte geçen tüm dualar (tekrarsız), sırayla
        java.util.LinkedHashSet<String> seen = new java.util.LinkedHashSet<>();
        for (PrayerGuide.Part part : p.parts) java.util.Collections.addAll(seen, part.duaIds);
        for (String id : seen) {
            PrayerGuide.Dua d = PrayerGuide.dua(id);
            if (d != null) root.addView(duaCard(d));
        }
        root.addView(text("Not: Anlatım Hanefî mezhebine ve Diyanet ilmihaline göredir. Kesin hüküm ve özel durumlar için "
                + "ilmihale veya il/ilçe müftülüğüne başvurun.", 11, COLOR_MUTED, false, 16));
    }

    private void buildDuaList() {
        root.addView(text("Dualar ve sûreler", 24, Color.WHITE, true, 12));
        root.addView(text("Namazda okunan dualar; Arapçası, okunuşu ve anlamıyla.", 13, COLOR_MUTED, false, 4));
        for (PrayerGuide.Dua d : PrayerGuide.DUAS) root.addView(duaCard(d));
    }

    private View duaCard(PrayerGuide.Dua d) {
        LinearLayout c = card();
        c.addView(text(d.name, 15, Color.WHITE, true, 0));
        c.addView(text(d.when, 12, COLOR_GOLD, false, 2));

        TextView ar = text(d.arabic, 22, Color.WHITE, false, 10);
        ar.setTextDirection(View.TEXT_DIRECTION_RTL);
        ar.setGravity(Gravity.END);
        ar.setLineSpacing(0, 1.3f);
        c.addView(ar);

        c.addView(text("Okunuşu", 11, COLOR_MUTED, true, 10));
        c.addView(text(d.translit, 13, COLOR_ACCENT, false, 2));
        c.addView(text("Anlamı", 11, COLOR_MUTED, true, 10));
        c.addView(text(d.meaning, 13, COLOR_TEXT, false, 2));
        return c;
    }

    // ------------------------------------------------------------------ yardımcılar

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private GradientDrawable round(int color, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radiusDp));
        return g;
    }

    private LinearLayout card() {
        LinearLayout c = column();
        c.setBackground(round(COLOR_CARD, 16));
        c.setPadding(dp(14), dp(12), dp(14), dp(14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(12);
        root.addView(c, lp);
        return c;
    }

    private TextView text(String t, int sp, int color, boolean bold, int topPaddingDp) {
        TextView tv = new TextView(this);
        tv.setText(t);
        tv.setTextSize(sp);
        tv.setTextColor(color);
        tv.setPadding(0, dp(topPaddingDp), 0, 0);
        if (bold) tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        tv.setTextIsSelectable(true);
        return tv;
    }
}
