package io.agritrack.settings.adapter;

public interface IConfigPersistenceAdapter<T> {

    void saveConfig(T appSettings) throws Exception;

    T loadConfig() throws Exception;

}
