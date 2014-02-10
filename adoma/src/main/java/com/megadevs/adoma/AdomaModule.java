package com.megadevs.adoma;

import android.content.Context;

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

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

    @Provides @Singleton @Named("adoma")
    DbHelper provideDbHelper(Context context) {
        return new DbHelper(context);
    }

    @Provides @Singleton @Named("adoma")
    DaoSession provideDaoSession(@Named("adoma") DbHelper dbHelper) {
        return dbHelper.getDaoSession();
    }

    @Provides @Singleton
    AdomaKeyStore provideAdomaKeystore(@Named("adoma") DaoSession daoSession) {
        return new AdomaKeyStore(daoSession);
    }

    @Provides @Singleton
    OkHttpClient provideHttpClient() {
        return new OkHttpClient();
    }

//    @Provides @Singleton @Named(EXECUTOR_DISK)
//    ExecutorService provideDiskExecutor() {
//        return Executors.newSingleThreadExecutor();
//    }

    @Provides @Singleton @Named(EXECUTOR_DOWNLOAD)
    ExecutorService provideDownloadExecutor() {
        return Executors.newCachedThreadPool();
    }

}
