package io.agritrack.philosofish.settings.adapter;

public interface IConfigPersistenceAdapter<T> {

    void saveConfig(T appSettings) throws Exception;

    T loadConfig() throws Exception;

}
