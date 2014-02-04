package com.megadevs.adoma;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.megadevs.adoma.events.CancelEvent;
import com.megadevs.adoma.events.CompleteEvent;
import com.megadevs.adoma.events.CreateEvent;
import com.megadevs.adoma.events.ErrorEvent;
import com.megadevs.adoma.events.PauseEvent;
import com.megadevs.adoma.events.ResumeEvent;
import com.megadevs.adoma.events.UpdateEvent;
import de.greenrobot.event.EventBus;

import java.io.IOException;
import java.io.Serializable;
import java.util.Random;

public class AdomaKey implements Serializable, Comparable<AdomaKey>  {

    transient EventBus masterEventBus;

    final String internalKey;

    DownloaderData data = new DownloaderData();
    long lastUpdate;

    AdomaKey() {
        Random rand = new Random(System.currentTimeMillis());
        internalKey = new String(String.valueOf(rand)).intern(); // if not wrapped into new string, String.valueOf() may set an internal offset of 2 (that voids every equals() check :/)
    }

    AdomaKey(String key) {
        internalKey = key.intern();
    }

    void merge(AdomaKey other) {
        if (other != null && other.equals(this) && other.lastUpdate > lastUpdate) {
            data = new DownloaderData(other.data);
            lastUpdate = other.lastUpdate;
        }
    }

    public void onEvent(UpdateEvent event) {
        merge(event.getAdomaKey());
        Adoma.postToLocalEventBus(new UpdateEvent(this));
    }

    public void onEvent(ErrorEvent event) {
        merge(event.getAdomaKey());
        Adoma.postToLocalEventBus(new ErrorEvent(this, event.getMessage()));
    }

    public void onEvent(CompleteEvent event) {
        merge(event.getAdomaKey());
        Adoma.postToLocalEventBus(new CompleteEvent(this));
    }

    public void onEvent(CreateEvent event) {
        merge(event.getAdomaKey());
        Adoma.postToLocalEventBus(new CreateEvent(this));
    }

    public DownloaderData getData() {
        return data;
    }

    public void pause() {
        checkMasterKey();
        masterEventBus.post(new PauseEvent(this));
    }

    public void resume() {
        checkMasterKey();
        masterEventBus.post(new ResumeEvent(this));
    }

    public void cancel() {
        checkMasterKey();
        masterEventBus.post(new CancelEvent(this));
    }

    private void checkMasterKey() {
        if (masterEventBus == null) {
            AdomaKeyBuilder.bindMasterAndSlave(this);
        }
    }

    @Override
    public String toString() {
        return internalKey;
    }

    @Override
    public int hashCode() {
        return internalKey.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return (this == o) || (o instanceof AdomaKey && internalKey.equals(((AdomaKey)o).internalKey));
    }

    @Override
    public int compareTo(AdomaKey another) {
        return internalKey.compareTo(another.internalKey);
    }

    public static class AdomaKeyTypeAdapter extends TypeAdapter<AdomaKey> {

        @Override
        public void write(JsonWriter jsonWriter, AdomaKey adomaKey) throws IOException {
            if (adomaKey == null) {
                jsonWriter.nullValue();
                return;
            }
            jsonWriter.value(adomaKey.internalKey);
        }

        @Override
        public AdomaKey read(JsonReader jsonReader) throws IOException {
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                return null;
            }
            String internalKey = jsonReader.nextString();
            AdomaKey adomaKey = new AdomaKey(internalKey);
            AdomaKeyBuilder.bindMasterAndSlave(adomaKey);
            return adomaKey;
        }
    }
}
