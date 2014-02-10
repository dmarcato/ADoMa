package com.megadevs.adoma;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.megadevs.adoma.events.BaseAdomaKeyEvent;
import com.megadevs.adoma.events.external.AdomaKeysUpdatedEvent;

import javax.inject.Inject;

public class AdomaService extends Service {

    private static final int NOTIFICATION_ID = "adoma".hashCode();

    public static void start(Context appContext) {
        Intent intent = new Intent(appContext, AdomaService.class);
        appContext.startService(intent);
    }

    @Inject AdomaKeyStore adomaKeyStore;

    private NotificationCompat.Builder builder;
    private boolean isForeground = false;

    private final Handler handler = new Handler();
    private Runnable sendUpdateEventRunnable = new Runnable() {
        @Override
        public void run() {
            Adoma.postToExternalEventBus(new AdomaKeysUpdatedEvent(adomaKeyStore.getActiveDownload()));
            handler.postDelayed(this, AdomaConfiguration.get().getMinDelayForProgressNotification());
        }
    };

    public AdomaService() {
        Adoma.injectMembers(this);

        builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(AdomaConfiguration.get().getSmallIconId());
        builder.setContentText("ADoMa");

        Adoma.registerToInternalEventBus(this);
    }

    public void onEvent(BaseAdomaKeyEvent event) {
        if (adomaKeyStore.getActiveDownloadCount() == 0) {
            setForeground(false);
        } else {
            setForeground(true);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (adomaKeyStore.getActiveDownloadCount() == 0) {
            setForeground(false);
        } else {
            setForeground(true);
        }
        return START_STICKY;
    }

    private void setForeground(boolean foreground) {
        if (isForeground != foreground) {
            isForeground = foreground;
            if (foreground) {
                startForeground(NOTIFICATION_ID, builder.build());
                handler.postDelayed(sendUpdateEventRunnable, 0);
            } else {
                handler.removeCallbacks(sendUpdateEventRunnable);
                stopForeground(true);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Adoma.unregisterFromInternalEventBus(this);
    }

}
