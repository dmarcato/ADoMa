package com.megadevs.adoma;

import de.greenrobot.dao.annotations.SerializedField;

import de.greenrobot.dao.DbUtils;




// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END
/**
 * Entity mapped to table ADOMA_KEY.
 */
abstract public class AdomaKeyBase implements java.lang.Comparable<AdomaKey>, com.megadevs.adoma.DownloaderData.OnDataUpdateListener {

    @com.google.gson.annotations.SerializedName( "internal_key" )
    protected String internalKey;
    @com.google.gson.annotations.SerializedName( "last_update" )
    protected java.util.Date lastUpdate;
    protected byte[] __data;

    //denormalized properties
    @SerializedField
    private com.megadevs.adoma.DownloaderData data;



    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public AdomaKeyBase() {
    }

    public AdomaKeyBase(String internalKey) {
        this.internalKey = internalKey;
    }

    public AdomaKeyBase(String internalKey, java.util.Date lastUpdate, byte[] __data) {
        this.internalKey = internalKey;
        this.lastUpdate = lastUpdate;
        this.__data = __data;
    }

    public String getInternalKey() {
        return internalKey;
    }

    public void setInternalKey(String internalKey) {
        this.internalKey = internalKey;
    }

    public java.util.Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(java.util.Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public byte[] get__data() {
        return __data;
    }

    public void set__data(byte[] __data) {
        this.__data = __data;
    }

    public void updateNotNull(AdomaKey other) {
        if(this == other) {
            return;//both came from db, no need to run this.
        }

        if(other.internalKey != null) {
            this.internalKey = other.internalKey;
        }


        if(other.lastUpdate != null) {
            this.lastUpdate = other.lastUpdate;
        }


        //serialized
                if(other.getData() != null) {
            setData(other.getData());
        }

        // relationships
    }
    public com.megadevs.adoma.DownloaderData getData() {
        if(data == null && __data != null) {
           data  = (com.megadevs.adoma.DownloaderData) DbUtils.deserializeObject(__data, com.megadevs.adoma.DownloaderData.class);
           __data = null; //clear memory, before save, we'll re-serialize anyways if needed
        }
        return data;
    }

    public void setData(com.megadevs.adoma.DownloaderData data) {
        this.data = data;
        __data = null; //onBeforeSave will do serialization
    }


    // KEEP METHODS - put your custom methods here
    // KEEP METHODS END

    public void onBeforeSave() {
        //you can override this method and do some stuff if you want to :)
        if(data != null) {//if property is nulled, its setter will already null the byte array.
            __data = DbUtils.serializeObject(data);
        }

    }
}
