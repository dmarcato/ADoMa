package com.megadevs.adoma;

import com.megadevs.adoma.events.BaseAdomaKeyEvent;
import com.megadevs.adoma.events.CancelEvent;
import com.megadevs.adoma.events.CompleteEvent;

import de.greenrobot.dao.Query;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class AdomaKeyStore {

    private final DaoSession daoSession;
    private final AdomaKeyDao adomaKeyDao;
    private final Query<AdomaKey> getAllKeysQuery;
    private List<AdomaKey> lazyAdomaKeyList;
    private boolean dirty = true;

    @Inject
    public AdomaKeyStore(@Named("adoma") DaoSession daoSession) {
        this.daoSession = daoSession;
        adomaKeyDao = daoSession.getAdomaKeyDao();
        getAllKeysQuery = adomaKeyDao.queryBuilder().orderDesc(AdomaKeyDao.Properties.LastUpdate).build();
        Adoma.registerToInternalEventBus(this);
    }

    public void onEvent(BaseAdomaKeyEvent event) {
        if (event instanceof CompleteEvent || event instanceof CancelEvent) {
            remove(event.getAdomaKey());
        } else {
            save(event.getAdomaKey());
        }
    }

    private void updateKeysList() {
        lazyAdomaKeyList = getAllKeysQuery.listLazy();
        dirty = false;
    }

    public List<AdomaKey> getAllKeys() {
        if (dirty) {
            updateKeysList();
        }
        return lazyAdomaKeyList;
    }

    public boolean contains(String internalKey) {
        return (get(internalKey) != null);
    }

    public AdomaKey get(String internalKey) {
        return adomaKeyDao.load(internalKey);
    }

    public int getActiveDownloadCount() {
        int activeCount = 0;
        for (AdomaKey key : getAllKeys()) {
            if (key.getData().getStatus() == DownloaderData.Status.RUNNING) {
                activeCount++;
            }
        }
        return activeCount;
    }

    public List<AdomaKey> getActiveDownload() {
        List<AdomaKey> keys = new ArrayList<AdomaKey>();
        for (AdomaKey key : getAllKeys()) {
            if (key.getData().getStatus() == DownloaderData.Status.RUNNING) {
                keys.add(key);
            }
        }
        return keys;
    }

    public int getTotalDownloadCount() {
        return getAllKeys().size();
    }

    public boolean isEmpty() {
        return getAllKeys().isEmpty();
    }

    public void save(AdomaKey adomaKey) {
        adomaKeyDao.insertOrReplace(adomaKey);
        dirty = true;
    }

    public void remove(AdomaKey adomaKey) {
        adomaKeyDao.delete(adomaKey);
        dirty = true;
    }

    public void remove(String internalKey) {
        adomaKeyDao.deleteByKey(internalKey);
        dirty = true;
    }

}
