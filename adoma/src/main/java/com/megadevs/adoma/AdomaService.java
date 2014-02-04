package com.megadevs.adoma;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import com.megadevs.adoma.events.BaseAdomaKeyEvent;
import com.megadevs.adoma.events.CancelEvent;
import com.megadevs.adoma.events.CompleteEvent;
import com.megadevs.adoma.events.CreateEvent;

import javax.inject.Inject;

public class AdomaService extends Service {

    private static final int NOTIFICATION_ID = 123;

    public static void start(Context appContext) {
        Intent intent = new Intent(appContext, AdomaService.class);
        appContext.startService(intent);
    }

    private NotificationCompat.Builder builder;
    private boolean isForeground = false;

    @Inject
    AdomaKeyStore adomaKeyStore;

    public AdomaService() {
        Adoma.injectMembers(this);

        builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(AdomaConfiguration.get().getSmallIconId());
        builder.setContentText("ADoMa");

        Adoma.registerToEventBus(this, CompleteEvent.class, CreateEvent.class, CancelEvent.class);
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
            } else {
                stopForeground(true);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adomaKeyStore.onDestroy();
        Adoma.unregisterFromEventBus(this);
    }

}
