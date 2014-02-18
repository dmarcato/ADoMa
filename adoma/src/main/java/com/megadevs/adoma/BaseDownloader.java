package com.megadevs.adoma;

import com.google.common.base.Strings;

import com.squareup.okhttp.OkHttpClient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Named;

public class BaseDownloader implements Downloader {

    // Max size of download buffer.
    protected static final int MAX_BUFFER_SIZE = 1024 * 4;

    @Inject
    protected transient OkHttpClient httpClient;
    @Inject @Named(AdomaModule.EXECUTOR_DOWNLOAD)
    protected transient ExecutorService executor;
    protected AdomaKey key;

    public BaseDownloader() {
        Adoma.injectMembers(this);
    }

    @Override
    public void setKey(AdomaKey adomaKey) {
        key = adomaKey;
        if (key.getData().getStatus() == DownloaderData.Status.IDLE) {
            processExistingFile();
            key.getData().setStatus(DownloaderData.Status.INITED);
        }
    }

    @Override
    public AdomaKey getKey() {
        return key;
    }

    private void __execute() {
        executor.execute(this);
    }

    @Override
    public void resume() {
        switch (key.getData().getStatus()) {
            case INITED:
            case PAUSED:
            case ERROR:
                key.getData().setStatus(DownloaderData.Status.RUNNING);
                __execute();
                break;
            default:
                //TODO log error
                break;
        }
    }

    @Override
    public void pause() {
        key.getData().setStatus(DownloaderData.Status.PAUSED);
    }

    @Override
    public void cancel() {
        key.getData().setStatus(DownloaderData.Status.CANCELLED);
        try {
            getDestinationFile().delete(); // Remove temp file from disk
        } catch (Throwable ignored) {}
    }

    @Override
    public URL getSourceURL() {
        return key.getData().getUrl();
    }

    @Override
    public DownloaderData.Status getStatus() {
        return key.getData().getStatus();
    }

    @Override
    public long getTotalSize() {
        return key.getData().getTotalSize();
    }

    @Override
    public long getDownloadedSize() {
        return key.getData().getDownloadedSize();
    }

    @Override
    public float getProgress() {
        return key.getData().getProgress();
    }

    @Override
    public long getCurrentSpeed() {
        return key.getData().getCurrentSpeed();
    }

    @Override
    public long getETA() {
        return key.getData().getETA();
    }

    @Override
    public File getDestinationFile() {
        if (key.getData().getDestinationFolder() == null) {
            throw new IllegalStateException("Destination folder can't be null!");
        }
        if (Strings.isNullOrEmpty(key.getData().getDestinationFilename())) {
            //TODO add safe checks!!!
            URL url = key.getData().getUrl();
            key.getData().setDestinationFilename(url.getFile().substring(url.getFile().lastIndexOf('/') + 1));
        }
        return new File(key.getData().getDestinationFolder(), key.getData().getDestinationFilename());
    }

    private void processExistingFile() {
        final File destinationFile = getDestinationFile();
        if (destinationFile.exists()) {
            key.getData().setDownloadedSize(destinationFile.length());
        }
    }

    @Override
    public void run() {
        RandomAccessFile file = null;
        InputStream stream = null;
        HttpURLConnection connection = null;
        int responseCode = 0;

        try {
            // Open connection to URL.
            connection = httpClient.open(key.getData().getUrl());

            // Specify what portion of file to download.
            connection.setRequestProperty("Range", "bytes=" + key.getData().getDownloadedSize() + "-");

            // Connect to server.
            //			connection.connect();

            // Make sure response code is in the 200 range.
            responseCode = connection.getResponseCode();
            if (responseCode / 100 != 2) {
                throwError("Response code not valid: " + connection.getResponseCode());
            }

            // Check for valid content length.
            int contentLength = connection.getContentLength();
            if (contentLength < 1) {
                throwError("Content-Length not valid: " + contentLength);
            }

            /* Set the size for this download if it
            hasn't been already set. */
            if (key.getData().getTotalSize() == -1) {
                key.getData().setTotalSize(contentLength);
            }

            // Open file and seek to the end of it.
            file = new RandomAccessFile(getDestinationFile(), "rw");
            file.seek(key.getData().getDownloadedSize());

            /* Size buffer according to how much of the
            file is left to download. */
            byte buffer[] = new byte[MAX_BUFFER_SIZE];
            stream = new BufferedInputStream(connection.getInputStream());
            while (key.getData().getStatus() == DownloaderData.Status.RUNNING) {
                // Read from server into buffer.
                int read = stream.read(buffer);
                if (read == -1) {
                    break;
                } else if (read == 0) {
                    error("Read 0 bytes from stream");
                    break;
                }

                // Write buffer to file.
                file.write(buffer, 0, read);
                key.getData().setDownloadedSize(key.getData().getDownloadedSize() + read);
            }

            /* Change status to complete if this point was
            reached because downloading has finished. */
            if (key.getData().getStatus() == DownloaderData.Status.RUNNING) {
                key.getData().setStatus(DownloaderData.Status.COMPLETED);
                sendCompleteEvent();
            }
        } catch (Exception e) {
            error("Exception: "+e.getMessage());
            e.printStackTrace();
        } finally {
            // Close file.
            if (file != null) {
                try {
                    file.close();
                } catch (Exception e) {}
            }

            // Close connection to server.
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {}
            }

            if (connection != null) {
                connection.disconnect();
            }
        }

        if (responseCode == 416) {
            // range not valid
            key.getData().setDownloadedSize(0);
            resume();
        }
    }

    protected void throwError(String message) {
        throw new RuntimeException(message);
    }

    /**
     * Mark the download as ERROR
     * @param message
     */
    protected void error(String message) {
        //TODO add log
        System.out.println(message);
        key.getData().setStatus(DownloaderData.Status.ERROR);
        sendErrorEvent(message);
    }

    private void sendErrorEvent(String message) {
        key.onError(message);
    }

    private void sendCompleteEvent() {
        key.onComplete();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Downloader && key.equals(((Downloader) o).getKey());
    }

    @Override
    public String toString() {
        return String.format("Key: %s - Downloading from %s to %s - total size: %s, downloaded size: %s (%.2f%% - %s - %s)",
                key,
                key.getData().getUrl(),
                getDestinationFile().getAbsolutePath(),
                Adoma.humanReadableByteCount(key.getData().getTotalSize(), false),
                Adoma.humanReadableByteCount(key.getData().getDownloadedSize(), false),
                getProgress(),
                Adoma.humanReadableByteCount(getCurrentSpeed(), false) + "/s",
                Adoma.humanReadableETA(getETA()));
    }

}
