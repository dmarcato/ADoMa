package com.megadevs.adoma;

import com.google.gson.Gson;
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

public class AdomaMasterKey extends AdomaKey implements DownloaderData.OnDataUpdateListener {

    private final transient EventBus mEventBus = new EventBus();
    private transient Downloader downloader;
    private transient boolean inited = false;

    public AdomaMasterKey(AdomaKey adomaKey) {
        super(adomaKey.internalKey);
        data = new DownloaderData(adomaKey.getData());
        getData().setOnDataUpdateListener(this);
        bindKey(adomaKey);
        initDownloader();
        registerEventBus();
        inited = true;
    }

    private AdomaMasterKey(String key, DownloaderData data) {
        super(key);
        this.data = data;
        getData().setOnDataUpdateListener(this);
        initDownloader();
        registerEventBus();
        inited = true;
    }

    void bindKey(AdomaKey adomaKey) {
        try {
            mEventBus.register(adomaKey);
        } catch (Exception ignored) {}
        adomaKey.masterEventBus = mEventBus;
        adomaKey.merge(this);
    }

    private void registerEventBus() {
        mEventBus.register(this, PauseEvent.class, ResumeEvent.class, CancelEvent.class);
    }

    public void onEvent(PauseEvent event) {
        getDownloader().pause();
        Adoma.postToInternalEventBus(event);
    }

    public void onEvent(ResumeEvent event) {
        getDownloader().resume();
        Adoma.postToInternalEventBus(event);
    }

    public void onEvent(CancelEvent event) {
        getDownloader().cancel();
        Adoma.postToInternalEventBus(event);
    }

    private void postIfInited(Object event) {
        if (inited) {
            mEventBus.post(event);
        }
    }

    public void onError(String message) {
        postIfInited(new ErrorEvent(this, message));
    }

    public void onComplete() {
        postIfInited(new CompleteEvent(this));
    }

    public void onCreate() {
        postIfInited(new CreateEvent(this));
    }

    @Override
    public void onDataUpdate() {
        lastUpdate = System.currentTimeMillis();
        postIfInited(new UpdateEvent(this));
    }

    private Downloader getDownloader() {
        if (downloader == null) {
            try {
                downloader = getData().getDownloaderClass().newInstance();
            } catch (Exception e) {
                downloader = new BaseDownloader();
            }
            downloader.setMasterKey(this);
        }
        return downloader;
    }

    private void initDownloader() {
        if (getData().getStatus() == DownloaderData.Status.RUNNING) {
            getData().setStatus(DownloaderData.Status.INITED);
            getDownloader().resume();
        }
    }

    public static class AdomaMasterKeyTypeAdapter extends TypeAdapter<AdomaMasterKey> {

        private Gson gson = new Gson();

        @Override
        public void write(JsonWriter jsonWriter, AdomaMasterKey adomaMasterKey) throws IOException {
            if (adomaMasterKey == null) {
                jsonWriter.nullValue();
                return;
            }
            jsonWriter.beginObject();
            jsonWriter.name("key");
            jsonWriter.value(adomaMasterKey.internalKey);
            jsonWriter.name("data");
            gson.toJson(gson.toJsonTree(adomaMasterKey.data), jsonWriter);
            jsonWriter.endObject();
        }

        @Override
        public AdomaMasterKey read(JsonReader jsonReader) throws IOException {
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                return null;
            }

            String internalKey = null;
            DownloaderData data = null;

            jsonReader.beginObject();
            for (int i = 0; i < 2; i++) {
                String name = jsonReader.nextName();
                if (name.equals("key")) {
                    internalKey = jsonReader.nextString();
                } else if (name.equals("data")) {
                    data = gson.fromJson(jsonReader, DownloaderData.class);
                }
            }
            jsonReader.endObject();

            if (internalKey == null || data == null) {
                return null;
            } else {
                return new AdomaMasterKey(internalKey, data);
            }
        }
    }

}
