package com.megadevs.adoma;

import java.io.File;
import java.io.Serializable;
import java.net.URL;

public interface Downloader extends Serializable, Runnable {

    public void setMasterKey(AdomaMasterKey adomaMasterKey);
    public AdomaKey getKey();

    public void resume();
    public void pause();
    public void cancel();

    public URL getSourceURL();
    public DownloaderData.Status getStatus();
    public long getTotalSize();
    public long getDownloadedSize();
    public float getProgress();
    public long getCurrentSpeed();
    public long getETA();
    public File getDestinationFile();

}
