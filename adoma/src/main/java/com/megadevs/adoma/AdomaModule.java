package com.megadevs.adoma;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Module(injects = {
        AdomaKeyStore.class,
        AdomaKeyBuilder.class,
        AdomaService.class,
        BaseDownloader.class
})
public class AdomaModule {

    public static final String EXECUTOR_DISK = "ExecutorDisk";
    public static final String EXECUTOR_DOWNLOAD = "ExecutorDownload";

    private Context appContext;

    public AdomaModule(Context appContext) {
        this.appContext = appContext;
    }

    @Provides
    Context provideContext() {
        return appContext;
    }

    @Provides @Singleton
    Gson provideGson() {
        return new GsonBuilder()
                .registerTypeAdapter(AdomaKey.class, new AdomaKey.AdomaKeyTypeAdapter())
                .registerTypeAdapter(AdomaMasterKey.class, new AdomaMasterKey.AdomaMasterKeyTypeAdapter())
                .create();
    }

    @Provides @Singleton
    OkHttpClient provideHttpClient() {
        return new OkHttpClient();
    }

    @Provides @Singleton @Named(EXECUTOR_DISK)
    ExecutorService provideDiskExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    @Provides @Singleton @Named(EXECUTOR_DOWNLOAD)
    ExecutorService provideDownloadExecutor() {
        return Executors.newCachedThreadPool();
    }

}
