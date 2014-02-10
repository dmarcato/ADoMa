package com.megadevs.adoma;

import java.io.File;
import java.net.URL;

public class AdomaKeyBuilder {

    public static AdomaKey find(String internalKey) {
        return Adoma.inject(AdomaKeyStore.class).get(internalKey);
    }

    public static AdomaKeyBuilder build() {
        return new AdomaKeyBuilder();
    }

    private AdomaKey adomaKey;

    private AdomaKeyBuilder() {
        adomaKey = new AdomaKey();
        adomaKey.init();
        adomaKey.getData().setDownloaderClass(BaseDownloader.class);
        adomaKey.getData().setDestinationFolder(AdomaConfiguration.get().getDestinationFolder());
    }

    public AdomaKeyBuilder withCustomKey(String key) {
        AdomaKey newKey = new AdomaKey();
        newKey.init(key);
        newKey.setData(adomaKey.getData());
        adomaKey = newKey;
        return this;
    }

    public AdomaKeyBuilder withDownloaderClass(Class<? extends Downloader> downloaderClass) {
        adomaKey.getData().setDownloaderClass(downloaderClass);
        return this;
    }

    public AdomaKeyBuilder withDestinationFolder(File destinationFolder) {
        adomaKey.getData().setDestinationFolder(destinationFolder);
        return this;
    }

    public AdomaKeyBuilder withDestinationFilename(String destinationFilename) {
        adomaKey.getData().setDestinationFilename(destinationFilename);
        return this;
    }

    public AdomaKey create(URL url) {
        if (url == null) {
            throw new IllegalStateException("URL must be specified");
        }
        adomaKey.getData().setUrl(url);
        if (Adoma.instance == null) {
            throw new RuntimeException("Adoma must be initialized by calling Adoma.ensureDownloads(context);");
        }
        Adoma.instance.checkPermissions();
        adomaKey.onCreate();
        return adomaKey;
    }

}
