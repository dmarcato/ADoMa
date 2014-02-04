package com.megadevs.adoma;

import com.megadevs.adoma.utils.CircularHashMap;

import java.io.File;
import java.io.Serializable;
import java.net.URL;

public class DownloaderData implements Serializable {

    public interface OnDataUpdateListener {
        public void onDataUpdate();
    }

    protected static final int MAX_ETAS = 1000;
    protected static final int MAX_SPEEDS = 2;

    transient OnDataUpdateListener listener;

    Class<? extends Downloader> downloaderClass;
    URL url;
    File destinationFolder;
    String destinationFilename;
    Status status = Status.IDLE;
    long downloadedSize = 0;
    long lastUpdateDownloadedSize = 0;
    long lastUpdateTime = 0;
    long totalSize = -1;

    protected long currentSpeed = 0;
    protected CircularHashMap<Long, Long> ETAs = new CircularHashMap<Long, Long>(MAX_ETAS);
    protected CircularHashMap<Long, Long> speeds = new CircularHashMap<Long, Long>(MAX_SPEEDS);

    private transient final Object CONSIDER_LOCK = new Object();
    private transient long lastConsideredEventTime = 0L;

    public DownloaderData() {
        this(null);
    }

    public DownloaderData(DownloaderData other) {
        if (other != null) {
            try {
                url = new URL(other.url.toString());
                destinationFolder = new File(other.destinationFolder.getAbsolutePath());
                destinationFilename = other.destinationFilename;
                status = other.status;
                downloadedSize = other.downloadedSize;
                lastUpdateDownloadedSize = other.lastUpdateDownloadedSize;
                lastUpdateTime = other.lastUpdateTime;
                totalSize = other.totalSize;
                currentSpeed = other.currentSpeed;
                //TODO speed
            } catch (Exception e) {}
        }
    }

    private void notifyDataUpdated() {
        if (listener != null) {
            listener.onDataUpdate();
        }
    }

    private void considerNotifyDataUpdated() {
        synchronized (CONSIDER_LOCK) {
            long min = AdomaConfiguration.get().getMinDelayForProgressNotification();
            long now = System.currentTimeMillis();
            if (now - lastConsideredEventTime >= min) {
                lastConsideredEventTime = now;
                notifyDataUpdated();
            }
        }
    }

    public void setOnDataUpdateListener(OnDataUpdateListener listener) {
        this.listener = listener;
    }

    public Class<? extends Downloader> getDownloaderClass() {
        return downloaderClass;
    }

    public void setDownloaderClass(Class<? extends Downloader> downloaderClass) {
        this.downloaderClass = downloaderClass;
    }

    public URL getUrl() {
        return url;
    }

    void setUrl(URL url) {
        this.url = url;
        notifyDataUpdated();
    }

    public File getDestinationFolder() {
        return destinationFolder;
    }

    void setDestinationFolder(File destinationFolder) {
        this.destinationFolder = destinationFolder;
        notifyDataUpdated();
    }

    public String getDestinationFilename() {
        return destinationFilename;
    }

    void setDestinationFilename(String destinationFilename) {
        this.destinationFilename = destinationFilename;
        notifyDataUpdated();
    }

    public Status getStatus() {
        return status;
    }

    void setStatus(Status status) {
        this.status = status;
        notifyDataUpdated();
    }

    public long getDownloadedSize() {
        return downloadedSize;
    }

    void setDownloadedSize(long downloadedSize) {
        this.downloadedSize = downloadedSize;
        considerNotifyDataUpdated();
    }

    public long getLastUpdateDownloadedSize() {
        return lastUpdateDownloadedSize;
    }

    void setLastUpdateDownloadedSize(long lastUpdateDownloadedSize) {
        this.lastUpdateDownloadedSize = lastUpdateDownloadedSize;
        notifyDataUpdated();
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
        notifyDataUpdated();
    }

    public long getTotalSize() {
        return totalSize;
    }

    void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
        notifyDataUpdated();
    }

    public float getProgress() {
        float progress = ((float) getDownloadedSize() / getTotalSize()) * 100;
        if (progress < 0.0f) {
            progress = 0.0f;
        } else if (progress > 100.0f) {
            progress = 100.0f;
        }
        return progress;
    }

    public long getCurrentSpeed() {
        if (speeds.size() > 0) {
            long speed = 0;
            for (long s : speeds) {
                speed += s;
            }
            return speed / speeds.size();
        } else {
            return currentSpeed;
        }
    }

    public long getETA() {
        if (ETAs.size() > 0) {
            return ETAs.get(ETAs.size() - 1);
        } else {
            return -1;
        }
    }

    public void calculateSpeedAndETA(long time) {
        currentSpeed = Math.abs((getDownloadedSize() - getLastUpdateDownloadedSize()) * 1000 / (time - getLastUpdateTime()));
        if (speeds.size() == 0) {
            speeds.add(time, currentSpeed);
        } else if (speeds.size() == 1) {
            speeds.add(time, (speeds.get(0) + currentSpeed) / 2);
        } else {
            long avgSpeed = currentSpeed/2 + speeds.get(speeds.size()-1)/2;
            speeds.add(time, avgSpeed);
        }
        long speedForEta = speeds.get(speeds.size()-1);
        if (speedForEta > 0) {
            ETAs.add(time, (getTotalSize() - getDownloadedSize()) / speedForEta);
        }
        setLastUpdateTime(time);
        setLastUpdateDownloadedSize(getDownloadedSize());
    }

    public enum Status {
        IDLE,
        INITED,
        RUNNING,
        PAUSED,
        COMPLETED,
        CANCELLED,
        ERROR
    }
}
