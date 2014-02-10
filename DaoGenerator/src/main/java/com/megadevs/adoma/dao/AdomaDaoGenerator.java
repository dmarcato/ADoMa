package com.megadevs.adoma.dao;

import de.greenrobot.daogenerator.Annotation;
import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

import java.io.IOException;

public class AdomaDaoGenerator extends DaoGenerator {

    public AdomaDaoGenerator() throws IOException {
    }

    public static void main(String[] args) {
        Schema schema = new Schema(1, "com.megadevs.adoma");
        schema.setDefaultJavaPackageTest("com.megadevs.adoma.test");
        schema.setDefaultJavaPackageDao("com.megadevs.adoma");
        schema.enableKeepSectionsByDefault();
        addAdomaKey(schema);
        try {
            new DaoGenerator().generateAll(schema, "../adoma/src/generated/java");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class SerializedName extends Annotation {
        public SerializedName(String val) {
            super("com.google.gson.annotations.SerializedName", '"' + val + '"');
        }
    }

    private static void addAdomaKey(Schema schema) {
        Entity adomaKey = schema.addEntity("AdomaKey");
        adomaKey.implementsInterface("java.lang.Comparable<AdomaKey>");
        adomaKey.implementsInterface("com.megadevs.adoma.DownloaderData.OnDataUpdateListener");
        adomaKey.addStringProperty("internalKey").columnName("_key").unique().primaryKey().addFieldAnnotation(new SerializedName("internal_key"));
        adomaKey.addDateProperty("lastUpdate").addFieldAnnotation(new SerializedName("last_update"));
        adomaKey.addSerializedProperty("data", "com.megadevs.adoma.DownloaderData");
    }

}
