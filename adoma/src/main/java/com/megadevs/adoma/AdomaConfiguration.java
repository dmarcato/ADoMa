package com.megadevs.adoma;

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
    private int smallIconId = R.drawable.icon;

    public AdomaConfiguration() {
        setDefaultDestinationFolder();
    }

    private void setDefaultDestinationFolder() throws IllegalStateException {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            destinationFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        } else {
            throw new IllegalStateException("External storage not available");
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

    public int getSmallIconId() {
        return smallIconId;
    }

    public void setSmallIconId(int smallIconId) {
        this.smallIconId = smallIconId;
    }
}
