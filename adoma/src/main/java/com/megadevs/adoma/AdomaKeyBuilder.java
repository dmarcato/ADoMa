package com.megadevs.adoma;

import java.io.File;
import java.net.URL;

public class AdomaKeyBuilder {

    public static AdomaKeyBuilder build() {
        return new AdomaKeyBuilder();
    }

    static AdomaKey getSlaveKey(AdomaKey adomaKey) {
        AdomaKey slaveKey = new AdomaKey(adomaKey.internalKey);
        slaveKey.merge(adomaKey);
        bindMasterAndSlave(adomaKey);
        return adomaKey;
    }

    static void bindMasterAndSlave(AdomaKey adomaKey) {
        AdomaKeyStore adomaKeyStore = Adoma.inject(AdomaKeyStore.class);
        AdomaMasterKey masterKey;
        if (adomaKeyStore.contains(adomaKey)) {
            masterKey = adomaKeyStore.getMasterKey(adomaKey);
            masterKey.bindKey(adomaKey);
        } else {
            masterKey = new AdomaMasterKey(adomaKey);
            adomaKeyStore.add(masterKey);
        }
    }

    private AdomaKey adomaKey;

    private AdomaKeyBuilder() {
        Adoma.injectMembers(this);
        adomaKey = new AdomaKey();
        adomaKey.getData().setDownloaderClass(BaseDownloader.class);
        adomaKey.getData().setDestinationFolder(AdomaConfiguration.get().getDestinationFolder());
    }

    public AdomaKeyBuilder withCustomKey(String key) {
        AdomaKey newKey = new AdomaKey(key);
        newKey.data = adomaKey.data;
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
        bindMasterAndSlave(adomaKey);
        return adomaKey;
    }

}
