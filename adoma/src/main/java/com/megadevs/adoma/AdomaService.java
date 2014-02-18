package com.megadevs.adoma;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.megadevs.adoma.events.BaseAdomaKeyEvent;
import com.megadevs.adoma.events.external.AdomaKeysUpdatedEvent;

import java.util.List;

import javax.inject.Inject;

public class AdomaService extends Service {

    private static final int NOTIFICATION_ID = "adoma".hashCode();

    public static void start(Context appContext) {
        Intent intent = new Intent(appContext, AdomaService.class);
        appContext.startService(intent);
    }

    @Inject AdomaKeyStore adomaKeyStore;

    private boolean isForeground = false;
    private int notificationIcon;

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

        notificationIcon = AdomaConfiguration.get().getNotificationSmallIconId();

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
                updateNotification();
                handler.postDelayed(sendUpdateEventRunnable, 0);
            } else {
                handler.removeCallbacks(sendUpdateEventRunnable);
                stopForeground(true);
            }
        } else if (foreground) {
            updateNotification();
        }
    }

    private void updateNotification() {
        List<AdomaKey> activeDownloadList = adomaKeyStore.getActiveDownload();
        int downloadCount = activeDownloadList.size();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(AdomaConfiguration.get().getNotificationSmallIconId())
                .setOnlyAlertOnce(true)
                .setContentIntent(AdomaConfiguration.get().getNotificationContentIntent());
        if (downloadCount == 1) {
            DownloaderData data = adomaKeyStore.getActiveDownload().get(0).getData();
            builder.setContentTitle(data.getDestinationFilename());
            builder.setContentText(getString(R.string.notification_mono_text,
                                             Adoma.humanReadableByteCount(data.getDownloadedSize(),
                                                                          true),
                                             Adoma.humanReadableByteCount(data.getTotalSize(),
                                                                          true),
                                             Adoma.humanReadableByteCount(data.getCurrentSpeed(),
                                                                          true)));
            builder.setContentInfo(getString(R.string.notification_mono_info, Adoma.humanReadableETA(data.getETA())));
            builder.setProgress(100, (int) data.getProgress(), false);
            startForeground(NOTIFICATION_ID, builder.build());
        } else {
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle(builder);
            String title = getString(R.string.notification_n_downloads, downloadCount);
            builder.setContentTitle(title);
            inboxStyle.setBigContentTitle(title);
            float totProgress = 0.0f;
            long totSize = 0L;
            long totDownloaded = 0L;
            long totSpeed = 0L;
            long maxETA = 0L;
            for (int i = 0; i < Math.min(5, downloadCount); i++) {
                DownloaderData data = activeDownloadList.get(i).getData();
                totProgress += data.getProgress();
                totSize += data.getTotalSize();
                totDownloaded += data.getDownloadedSize();
                totSpeed += data.getCurrentSpeed();
                maxETA = (data.getETA() > maxETA) ? data.getETA() : maxETA;
                inboxStyle.addLine(data.getDestinationFilename());
            }
            builder.setProgress(100 * downloadCount, (int) totProgress, false);
            if (downloadCount > 5) {
                inboxStyle.setSummaryText(getString(R.string.notification_multi_more, downloadCount - 5));
            } else {
                inboxStyle.setSummaryText(getString(R.string.notification_mono_text,
                                                    Adoma.humanReadableByteCount(totDownloaded, true),
                                                    Adoma.humanReadableByteCount(totSize, true),
                                                    Adoma.humanReadableByteCount(totSpeed, true)));
            }
            builder.setContentInfo(getString(R.string.notification_mono_info, Adoma.humanReadableETA(maxETA)));
            startForeground(NOTIFICATION_ID, inboxStyle.build());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Adoma.unregisterFromInternalEventBus(this);
    }

}
