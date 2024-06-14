package io.agritrack.philosofish.settings.adapter;

import android.content.Context;

public class ConfigPersistenceFactory {

    public enum PersistenceType{
        FILE, MANUAL, WEB
    }

    public static IConfigPersistenceAdapter getInstance(Context context, PersistenceType persistenceType){
        switch (persistenceType){
            case WEB:
                return new ConfigWebPersistence(context);
            case FILE:
                return new ConfigFilePersistence(context);
            case MANUAL:
                return new ConfigManualPersistence(context);
            default:
                return null;
        }
    }

}
