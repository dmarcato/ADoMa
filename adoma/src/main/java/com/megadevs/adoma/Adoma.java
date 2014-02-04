package com.megadevs.adoma;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import dagger.ObjectGraph;
import de.greenrobot.event.EventBus;

import java.lang.ref.WeakReference;
import java.util.List;

public class Adoma {

    static Adoma instance;
    static EventBus localEventBus;
    static EventBus adomaInternalEventBus = new EventBus();

    private static void checkInstance(Context context) {
        if (instance == null) {
            instance = new Adoma(context.getApplicationContext());
        }
    }

    private static void failIfNoInstance() {
        if (instance == null) {
            throw new IllegalStateException("You need to call ensureDownloads first!");
        }
    }

    /**
     * Ensure pending downloads are running, must be called at each fresh startup of the app
     * @param context
     */
    public static void ensureDownloads(Context context) {
        checkInstance(context);
        AdomaService.start(context.getApplicationContext());
    }

    public static void setLocalEventBus(EventBus eventBus) {
        localEventBus = eventBus;
    }

    public static void reconnectKeys(List<AdomaKey> adomaKeys) {
        for (AdomaKey adomaKey : adomaKeys) {
            AdomaKeyBuilder.bindMasterAndSlave(adomaKey);
        }
    }

    static void postToLocalEventBus(Object event) {
        if (localEventBus != null) {
            localEventBus.post(event);
        }
    }

    static void postToInternalEventBus(Object event) {
        adomaInternalEventBus.post(event);
    }

    static void registerToEventBus(Object subscriber, Class<?> eventType, Class<?>... moreEventType) {
        adomaInternalEventBus.register(subscriber, eventType, moreEventType);
    }

    static void unregisterFromEventBus(Object subscriber) {
        adomaInternalEventBus.unregister(subscriber);
    }

    static void onNetworkConnected() {
        //TODO manage network
    }

    static void onNetworkNotConnected() {
        //TODO manage network
    }

    private ObjectGraph objectGraph;
    WeakReference<Context> contextRef;

    private boolean permissionsChecked = false;

    private Adoma(Context context) {
        objectGraph = ObjectGraph.create(new AdomaModule(context.getApplicationContext()));

        contextRef = new WeakReference<Context>(context);
    }

    static <T> T injectMembers(T object) {
        return Adoma.instance.objectGraph.inject(object);
    }

    static <T> T inject(Class<T> klass) {
        return Adoma.instance.objectGraph.get(klass);
    }

    Context getContext() {
        if (contextRef == null) {
            throw new IllegalStateException("Context ref is null");
        }
        Context context = contextRef.get();
        if (context == null) {
            throw new IllegalStateException("Context is no longer available to start download");
        }
        return context;
    }

    void checkPermissions() {
        if (!permissionsChecked) {
            Context context = getContext();
            if (!(context.checkCallingOrSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
                    && context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED)
                    && context.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                throw new IllegalStateException("Permissions not setted!");
            }
            List resolveInfo = context.getPackageManager().queryIntentServices(new Intent(context, AdomaService.class), PackageManager.MATCH_DEFAULT_ONLY);
            if (resolveInfo.size() == 0) {
                throw new IllegalStateException("Service not declared in manifest!");
            }
            permissionsChecked = true;
        }
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String humanReadableETA(long totSec) {
        if (totSec == -1) return "";	//TODO better
//		return new PrettyTime(new Date(0), Locale.getDefault()).format(new Date(totSec*1000));
        int x = (int) totSec;
        int seconds = x % 60;
        x /= 60;
        int minutes = x % 60;
        x /= 60;
        int hours = x % 24;
        x /= 24;
        int days = x;
        return String.format("%dd, %dh, %dm, %ds", days, hours, minutes, seconds);
        /*Resources res = getResources();
        if (days > 0) {
            return res.getQuantityString(R.plurals.downloadmanager_days, days, days);
        } else if (hours > 0) {
            return res.getQuantityString(R.plurals.downloadmanager_hours, hours, hours);
        } else if (minutes > 0) {
            return res.getQuantityString(R.plurals.downloadmanager_minutes, minutes, minutes);
        } else {
            return res.getQuantityString(R.plurals.downloadmanager_seconds, seconds, seconds);
        }*/
    }

}
