package app.ezanpause;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Namaz vakitleri: Diyanet ihtiyat paylarıyla (öğle +5, ikindi +4, akşam +7, güneş −7), 18°/17°.
 * İnternet gerektirmez. Yalnızca java.time kullanır (Android bağımlılığı yok).
 */
final class PrayerTimes {
    private PrayerTimes() {}

    static final class City {
        final String name;
        final String country;
        final double lat;
        final double lng;
        final ZoneId zone;

        City(String name, String country, double lat, double lng, String zone) {
            this.name = name;
            this.country = country;
            this.lat = lat;
            this.lng = lng;
            this.zone = ZoneId.of(zone);
        }
    }

    /** Bir günün vakitleri (epoch ms). */
    static final class Day {
        final LocalDate date;
        final ZoneId zone;
        final long imsak, gunes, ogle, ikindi, aksam, yatsi, sela;
        final boolean thursday;

        Day(LocalDate date, ZoneId zone, long[] t, boolean thursday) {
            this.date = date;
            this.zone = zone;
            this.imsak = t[0];
            this.gunes = t[1];
            this.ogle = t[2];
            this.ikindi = t[3];
            this.aksam = t[4];
            this.yatsi = t[5];
            this.sela = t[6];
            this.thursday = thursday;
        }
    }

    static final City[] CITIES = {
        new City("İstanbul", "Türkiye", 41.0082, 28.9784, "Europe/Istanbul"),
        new City("Ankara", "Türkiye", 39.9334, 32.8597, "Europe/Istanbul"),
        new City("İzmir", "Türkiye", 38.4237, 27.1428, "Europe/Istanbul"),
        new City("Bursa", "Türkiye", 40.1885, 29.061, "Europe/Istanbul"),
        new City("Antalya", "Türkiye", 36.8969, 30.7133, "Europe/Istanbul"),
        new City("Konya", "Türkiye", 37.8746, 32.4932, "Europe/Istanbul"),
        new City("Adana", "Türkiye", 37.0, 35.3213, "Europe/Istanbul"),
        new City("Gaziantep", "Türkiye", 37.0662, 37.3833, "Europe/Istanbul"),
        new City("Şanlıurfa", "Türkiye", 37.1674, 38.7955, "Europe/Istanbul"),
        new City("Kocaeli", "Türkiye", 40.7654, 29.9408, "Europe/Istanbul"),
        new City("Mersin", "Türkiye", 36.8121, 34.6415, "Europe/Istanbul"),
        new City("Diyarbakır", "Türkiye", 37.9144, 40.2306, "Europe/Istanbul"),
        new City("Hatay", "Türkiye", 36.2023, 36.1606, "Europe/Istanbul"),
        new City("Manisa", "Türkiye", 38.6191, 27.4289, "Europe/Istanbul"),
        new City("Kayseri", "Türkiye", 38.7312, 35.4787, "Europe/Istanbul"),
        new City("Samsun", "Türkiye", 41.2867, 36.33, "Europe/Istanbul"),
        new City("Balıkesir", "Türkiye", 39.6484, 27.8826, "Europe/Istanbul"),
        new City("Kahramanmaraş", "Türkiye", 37.5858, 36.9371, "Europe/Istanbul"),
        new City("Van", "Türkiye", 38.4891, 43.4089, "Europe/Istanbul"),
        new City("Eskişehir", "Türkiye", 39.7767, 30.5206, "Europe/Istanbul"),
        new City("Trabzon", "Türkiye", 41.0027, 39.7168, "Europe/Istanbul"),
        new City("Erzurum", "Türkiye", 39.9055, 41.2658, "Europe/Istanbul"),
        new City("Sivas", "Türkiye", 39.7505, 37.015, "Europe/Istanbul"),
        new City("Malatya", "Türkiye", 38.3552, 38.3095, "Europe/Istanbul"),
        new City("Sakarya", "Türkiye", 40.7569, 30.3783, "Europe/Istanbul"),
        new City("Batman", "Türkiye", 37.8812, 41.1294, "Europe/Istanbul"),
        new City("Elazığ", "Türkiye", 38.6748, 39.2225, "Europe/Istanbul"),
        new City("Tekirdağ", "Türkiye", 40.9833, 27.5167, "Europe/Istanbul"),
        new City("Adıyaman", "Türkiye", 37.7648, 38.2786, "Europe/Istanbul"),
        new City("Afyonkarahisar", "Türkiye", 38.7507, 30.5567, "Europe/Istanbul"),
        new City("Ağrı", "Türkiye", 39.7191, 43.0503, "Europe/Istanbul"),
        new City("Aksaray", "Türkiye", 38.3687, 34.037, "Europe/Istanbul"),
        new City("Amasya", "Türkiye", 40.6499, 35.8353, "Europe/Istanbul"),
        new City("Ardahan", "Türkiye", 41.1105, 42.7022, "Europe/Istanbul"),
        new City("Artvin", "Türkiye", 41.1828, 41.8183, "Europe/Istanbul"),
        new City("Aydın", "Türkiye", 37.8444, 27.8458, "Europe/Istanbul"),
        new City("Bartın", "Türkiye", 41.6344, 32.3375, "Europe/Istanbul"),
        new City("Bayburt", "Türkiye", 40.2552, 40.2249, "Europe/Istanbul"),
        new City("Bilecik", "Türkiye", 40.1506, 29.9792, "Europe/Istanbul"),
        new City("Bingöl", "Türkiye", 38.8855, 40.4983, "Europe/Istanbul"),
        new City("Bitlis", "Türkiye", 38.4006, 42.1095, "Europe/Istanbul"),
        new City("Bolu", "Türkiye", 40.7392, 31.6089, "Europe/Istanbul"),
        new City("Burdur", "Türkiye", 37.7203, 30.2908, "Europe/Istanbul"),
        new City("Çanakkale", "Türkiye", 40.1553, 26.4142, "Europe/Istanbul"),
        new City("Çankırı", "Türkiye", 40.6013, 33.6134, "Europe/Istanbul"),
        new City("Çorum", "Türkiye", 40.5506, 34.9556, "Europe/Istanbul"),
        new City("Denizli", "Türkiye", 37.7765, 29.0864, "Europe/Istanbul"),
        new City("Düzce", "Türkiye", 40.8438, 31.1565, "Europe/Istanbul"),
        new City("Edirne", "Türkiye", 41.6818, 26.5623, "Europe/Istanbul"),
        new City("Erzincan", "Türkiye", 39.75, 39.5, "Europe/Istanbul"),
        new City("Giresun", "Türkiye", 40.9128, 38.3895, "Europe/Istanbul"),
        new City("Gümüşhane", "Türkiye", 40.4603, 39.4814, "Europe/Istanbul"),
        new City("Hakkari", "Türkiye", 37.5744, 43.7408, "Europe/Istanbul"),
        new City("Iğdır", "Türkiye", 39.9167, 44.05, "Europe/Istanbul"),
        new City("Isparta", "Türkiye", 37.7648, 30.5566, "Europe/Istanbul"),
        new City("Karabük", "Türkiye", 41.2061, 32.6204, "Europe/Istanbul"),
        new City("Karaman", "Türkiye", 37.1759, 33.2287, "Europe/Istanbul"),
        new City("Kars", "Türkiye", 40.6013, 43.0975, "Europe/Istanbul"),
        new City("Kastamonu", "Türkiye", 41.3887, 33.7827, "Europe/Istanbul"),
        new City("Kilis", "Türkiye", 36.7184, 37.1212, "Europe/Istanbul"),
        new City("Kırıkkale", "Türkiye", 39.8468, 33.5153, "Europe/Istanbul"),
        new City("Kırklareli", "Türkiye", 41.7333, 27.2167, "Europe/Istanbul"),
        new City("Kırşehir", "Türkiye", 39.1425, 34.1709, "Europe/Istanbul"),
        new City("Kütahya", "Türkiye", 39.4167, 29.9833, "Europe/Istanbul"),
        new City("Mardin", "Türkiye", 37.3212, 40.7245, "Europe/Istanbul"),
        new City("Muğla", "Türkiye", 37.2153, 28.3636, "Europe/Istanbul"),
        new City("Muş", "Türkiye", 38.7432, 41.5064, "Europe/Istanbul"),
        new City("Nevşehir", "Türkiye", 38.6939, 34.6857, "Europe/Istanbul"),
        new City("Niğde", "Türkiye", 37.9667, 34.6833, "Europe/Istanbul"),
        new City("Ordu", "Türkiye", 40.9839, 37.8764, "Europe/Istanbul"),
        new City("Osmaniye", "Türkiye", 37.0742, 36.2478, "Europe/Istanbul"),
        new City("Rize", "Türkiye", 41.0201, 40.5234, "Europe/Istanbul"),
        new City("Siirt", "Türkiye", 37.9333, 41.95, "Europe/Istanbul"),
        new City("Sinop", "Türkiye", 42.0231, 35.1531, "Europe/Istanbul"),
        new City("Şırnak", "Türkiye", 37.5164, 42.4611, "Europe/Istanbul"),
        new City("Tokat", "Türkiye", 40.3167, 36.55, "Europe/Istanbul"),
        new City("Tunceli", "Türkiye", 39.1079, 39.5401, "Europe/Istanbul"),
        new City("Uşak", "Türkiye", 38.6823, 29.4082, "Europe/Istanbul"),
        new City("Yalova", "Türkiye", 40.65, 29.2667, "Europe/Istanbul"),
        new City("Yozgat", "Türkiye", 39.8181, 34.8147, "Europe/Istanbul"),
        new City("Zonguldak", "Türkiye", 41.4564, 31.7987, "Europe/Istanbul"),
        new City("Mekke", "Suudi Arabistan", 21.3891, 39.8579, "Asia/Riyadh"),
        new City("Medine", "Suudi Arabistan", 24.5247, 39.5692, "Asia/Riyadh"),
        new City("Kudüs", "Filistin", 31.7683, 35.2137, "Asia/Jerusalem"),
        new City("Berlin", "Almanya", 52.52, 13.405, "Europe/Berlin"),
        new City("Londra", "İngiltere", 51.5074, -0.1278, "Europe/London"),
        new City("Bakü", "Azerbaycan", 40.4093, 49.8671, "Asia/Baku"),
        new City("Lefkoşa", "KKTC", 35.1856, 33.3823, "Asia/Nicosia"),
    };

    static City find(String name) {
        for (City c : CITIES) if (c.name.equals(name)) return c;
        return CITIES[0];
    }

    /** İki nokta arası büyük daire mesafesi (km, Haversine). */
    static double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * r * Math.asin(Math.min(1.0, Math.sqrt(a)));
    }

    /** Verilen koordinata en yakın kayıtlı şehir. */
    static City nearest(double lat, double lng) {
        City best = CITIES[0];
        double bestD = Double.MAX_VALUE;
        for (City c : CITIES) {
            double d = distanceKm(lat, lng, c.lat, c.lng);
            if (d < bestD) {
                bestD = d;
                best = c;
            }
        }
        return best;
    }

    static String[] cityNames() {
        String[] n = new String[CITIES.length];
        for (int i = 0; i < n.length; i++) n[i] = CITIES[i].name;
        return n;
    }

    // Diyanet ihtiyat payları (dakika)
    private static final int TUNE_GUNES = -7;
    private static final int TUNE_OGLE = 5;
    private static final int TUNE_IKINDI = 4;
    private static final int TUNE_AKSAM = 7;
    static final int SELA_BEFORE_YATSI_MIN = 20;

    static Day compute(City city, LocalDate date) {
        int y = date.getYear();
        int m = date.getMonthValue();
        int d = date.getDayOfMonth();

        long noonUtcMs = date.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli() + 12L * 3600_000L;
        int offsetMin = city.zone.getRules().getOffset(Instant.ofEpochMilli(noonUtcMs)).getTotalSeconds() / 60;
        long midnightUtcMs = date.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli() - offsetMin * 60_000L;

        double jd = julianDay(y, m, d) - city.lng / (15.0 * 24.0);
        double[] sun = sunPosition(jd);
        double decl = sun[0];
        double eqt = sun[1];
        double noon = 12 + offsetMin / 60.0 - city.lng / 15.0 - eqt;

        double sunriseH = noon - hourAngle(city.lat, decl, -0.833);
        double sunsetH = noon + hourAngle(city.lat, decl, -0.833);
        double asrAlt = atanD(1 / (1 + tanD(Math.abs(city.lat - decl))));
        double fajrH = noon - hourAngle(city.lat, decl, -18);
        double ishaH = noon + hourAngle(city.lat, decl, -17);

        // Yüksek enlemde fecr/yatsı oluşmayabilir: gece oranıyla yaklaşık değer
        double night = fixHour(sunriseH - sunsetH);
        if (Double.isNaN(fajrH)) fajrH = sunriseH - night * 18 / 60;
        if (Double.isNaN(ishaH)) ishaH = sunsetH + night * 17 / 60;

        long[] t = new long[7];
        t[0] = toMs(midnightUtcMs, fajrH, 0);
        t[1] = toMs(midnightUtcMs, sunriseH, TUNE_GUNES);
        t[2] = toMs(midnightUtcMs, noon, TUNE_OGLE);
        t[3] = toMs(midnightUtcMs, noon + hourAngle(city.lat, decl, asrAlt), TUNE_IKINDI);
        t[4] = toMs(midnightUtcMs, sunsetH, TUNE_AKSAM);
        t[5] = toMs(midnightUtcMs, ishaH, 0);
        t[6] = t[5] - SELA_BEFORE_YATSI_MIN * 60_000L;

        return new Day(date, city.zone, t, date.getDayOfWeek() == DayOfWeek.THURSDAY);
    }

    private static long toMs(long midnightUtcMs, double hours, int tuneMin) {
        long minutes = Math.round(hours * 60 + tuneMin);
        return midnightUtcMs + minutes * 60_000L;
    }

    /** Güneşin yüksekliği {@code alt} derece olduğu andaki saat açısı (saat); ulaşılamazsa NaN. */
    private static double hourAngle(double lat, double decl, double alt) {
        double c = (sinD(alt) - sinD(lat) * sinD(decl)) / (cosD(lat) * cosD(decl));
        return acosD(c) / 15.0;
    }

    private static double[] sunPosition(double jd) {
        double D = jd - 2451545.0;
        double g = fixAngle(357.529 + 0.98560028 * D);
        double q = fixAngle(280.459 + 0.98564736 * D);
        double L = fixAngle(q + 1.915 * sinD(g) + 0.02 * sinD(2 * g));
        double e = 23.439 - 0.00000036 * D;
        double RA = fixHour(atan2D(cosD(e) * sinD(L), cosD(L)) / 15.0);
        double eqt = q / 15.0 - RA;
        eqt = ((eqt + 12) % 24 + 24) % 24 - 12;
        double decl = asinD(sinD(e) * sinD(L));
        return new double[]{decl, eqt};
    }

    private static double julianDay(int year, int month, int day) {
        if (month <= 2) {
            year -= 1;
            month += 12;
        }
        int A = year / 100;
        int B = 2 - A + A / 4;
        return Math.floor(365.25 * (year + 4716)) + Math.floor(30.6001 * (month + 1)) + day + B - 1524.5;
    }

    private static double sinD(double d) { return Math.sin(Math.toRadians(d)); }
    private static double cosD(double d) { return Math.cos(Math.toRadians(d)); }
    private static double tanD(double d) { return Math.tan(Math.toRadians(d)); }
    private static double asinD(double x) { return Math.toDegrees(Math.asin(x)); }
    private static double acosD(double x) { return Math.toDegrees(Math.acos(x)); }
    private static double atanD(double x) { return Math.toDegrees(Math.atan(x)); }
    private static double atan2D(double y, double x) { return Math.toDegrees(Math.atan2(y, x)); }
    private static double fixAngle(double a) { return a - 360 * Math.floor(a / 360); }
    private static double fixHour(double a) { return a - 24 * Math.floor(a / 24); }
}
