package com.megadevs.adoma;

import android.app.PendingIntent;
import android.os.Environment;

import java.io.File;

public class AdomaConfiguration {

    private static AdomaConfiguration instance;

    public static AdomaConfiguration get() {
        if (instance == null) {
            instance = new AdomaConfiguration();
        }
        return instance;
    }

    private File destinationFolder;
    private long minDelayForProgressNotification = 1000L;       // 1 second
    private int notificationSmallIconId = R.drawable.icon;
    private PendingIntent notificationContentIntent;

    public AdomaConfiguration() {
        setDefaultDestinationFolder();
    }

    private void setDefaultDestinationFolder() throws IllegalStateException {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            destinationFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        } else {
            Environment.getDownloadCacheDirectory();
            //TODO handle this better :/
        }
    }

    public File getDestinationFolder() {
        return destinationFolder;
    }

    public void setDestinationFolder(File destinationFolder) {
        this.destinationFolder = destinationFolder;
    }

    public long getMinDelayForProgressNotification() {
        return minDelayForProgressNotification;
    }

    public void setMinDelayForProgressNotification(long minDelayForProgressNotification) {
        this.minDelayForProgressNotification = minDelayForProgressNotification;
    }

    public int getNotificationSmallIconId() {
        return notificationSmallIconId;
    }

    public void setNotificationSmallIconId(int notificationSmallIconId) {
        this.notificationSmallIconId = notificationSmallIconId;
    }

    public PendingIntent getNotificationContentIntent() {
        return notificationContentIntent;
    }

    public void setNotificationContentIntent(PendingIntent notificationContentIntent) {
        this.notificationContentIntent = notificationContentIntent;
    }
}
