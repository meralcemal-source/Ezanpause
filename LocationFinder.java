package app.ezanpause;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.atomic.AtomicBoolean;

/** Tek seferlik konum alır (önce son bilinen, yoksa taze). Konum yalnızca şehir seçmek için kullanılır. */
final class LocationFinder {
    interface Callback {
        /** loc == null: konum alınamadı (kapalı, zaman aşımı veya izin yok). */
        void onResult(Location loc);
    }

    private static final long TIMEOUT_MS = 15000;
    private static final long FRESH_ENOUGH_MS = 6L * 3600 * 1000; // şehir için 6 saatlik konum yeterli

    private LocationFinder() {}

    @SuppressWarnings("deprecation")
    static void find(Context ctx, final Callback cb) {
        final Context c = ctx.getApplicationContext();
        final LocationManager lm = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
        if (lm == null) {
            cb.onResult(null);
            return;
        }

        Location best = null;
        String provider = null;
        try {
            for (String p : lm.getProviders(true)) {
                Location l = lm.getLastKnownLocation(p);
                if (l != null && (best == null || l.getTime() > best.getTime())) best = l;
            }
            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) provider = LocationManager.NETWORK_PROVIDER;
            else if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) provider = LocationManager.GPS_PROVIDER;
        } catch (SecurityException e) {
            cb.onResult(null);
            return;
        }

        if (best != null && System.currentTimeMillis() - best.getTime() < FRESH_ENOUGH_MS) {
            cb.onResult(best);
            return;
        }
        final Location fallback = best; // eski de olsa hiç yoktan iyi
        if (provider == null) {
            cb.onResult(fallback);
            return;
        }

        final AtomicBoolean done = new AtomicBoolean(false);
        final Handler main = new Handler(Looper.getMainLooper());
        final CancellationSignal cancel = new CancellationSignal();
        final LocationListener[] legacy = new LocationListener[1];

        final Callback once = loc -> {
            if (!done.compareAndSet(false, true)) return;
            main.removeCallbacksAndMessages(null);
            if (legacy[0] != null) {
                try {
                    lm.removeUpdates(legacy[0]);
                } catch (SecurityException ignored) {
                }
            }
            cb.onResult(loc != null ? loc : fallback);
        };

        main.postDelayed(() -> {
            cancel.cancel();
            once.onResult(null);
        }, TIMEOUT_MS);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                lm.getCurrentLocation(provider, cancel, c.getMainExecutor(), once::onResult);
            } else {
                legacy[0] = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        once.onResult(location);
                    }

                    @Override
                    public void onStatusChanged(String p, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String p) {
                    }

                    @Override
                    public void onProviderDisabled(String p) {
                    }
                };
                lm.requestSingleUpdate(provider, legacy[0], Looper.getMainLooper());
            }
        } catch (SecurityException | IllegalArgumentException e) {
            once.onResult(null);
        }
    }
}
