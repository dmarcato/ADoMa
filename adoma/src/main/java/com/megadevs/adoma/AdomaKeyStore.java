package com.megadevs.adoma;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import com.megadevs.adoma.events.CancelEvent;
import com.megadevs.adoma.events.CompleteEvent;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;

@Singleton
public class AdomaKeyStore {

    @Inject
    Gson gson;
    @Inject
    Context context;
    @Inject @Named(AdomaModule.EXECUTOR_DISK)
    ExecutorService diskExecutor;

    private final Object downloadsLock = new Object();
    private CopyOnWriteArraySet<AdomaKey> keyStore; // keys are all instances of AdomaMasterKey
    private Type typeOfKeyStoreMap = new TypeToken<CopyOnWriteArraySet<AdomaMasterKey>>() { }.getType();    // TypeToken must be the same as runtime

    public AdomaKeyStore() {
        Adoma.injectMembers(this);
        loadKeyStore();

        Adoma.registerToEventBus(this, CompleteEvent.class, CancelEvent.class);
    }

    public void onEvent(CompleteEvent event) {
        remove(event.getAdomaKey());
    }

    public void onEvent(CancelEvent event) {
        remove(event.getAdomaKey());
    }

    public int getActiveDownloadCount() {
        int activeCount = 0;
        for (AdomaKey key : keyStore) {
            if (key.getData().getStatus() == DownloaderData.Status.RUNNING) {
                activeCount++;
            }
        }
        return activeCount;
    }

    public int getTotalDownloadCount() {
        return keyStore.size();
    }

    public boolean isEmpty() {
        return keyStore.isEmpty();
    }

    public boolean contains(AdomaKey adomaKey) {
        return keyStore.contains(adomaKey);
    }

    public AdomaMasterKey getMasterKey(AdomaKey adomaKey) {
        for (AdomaKey key : keyStore) {
            if (key.equals(adomaKey)) {
                return (AdomaMasterKey) key;
            }
        }
        return null;
    }

    public void add(AdomaMasterKey key) {
        if (!keyStore.contains(key)) {
            keyStore.add(key);
            key.onCreate();
            saveKeyStore();
        }
    }

    public void remove(AdomaKey key) {
        keyStore.remove(key);
        saveKeyStore();
    }

    public void onDestroy() {
        saveKeyStore();
        diskExecutor.shutdown();
        Adoma.unregisterFromEventBus(this);
    }

    private FileOutputStream openDataFile() throws FileNotFoundException {
        return context.openFileOutput("adoma.dat", Context.MODE_PRIVATE);
    }

    private FileInputStream readDataFile() throws FileNotFoundException {
        return context.openFileInput("adoma.dat");
    }

    private void saveKeyStore() {
        diskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (downloadsLock) {
                    OutputStreamWriter writer = null;
                    try {
                        writer = new OutputStreamWriter(openDataFile(), "UTF-8");
                        gson.toJson(keyStore, typeOfKeyStoreMap, new JsonWriter(writer));
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (writer != null) {
                            try {
                                writer.close();
                            } catch (IOException e) {}
                        }
                    }
                }
            }
        });
    }

    private void loadKeyStore() {
        synchronized (downloadsLock) {
            InputStreamReader reader = null;
            try {
                reader = new InputStreamReader(readDataFile(), "UTF-8");
                keyStore = gson.fromJson(reader, typeOfKeyStoreMap);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {}
                }
            }
            if (keyStore == null) {
                keyStore = new CopyOnWriteArraySet<AdomaKey>();
            }
        }
    }

}
