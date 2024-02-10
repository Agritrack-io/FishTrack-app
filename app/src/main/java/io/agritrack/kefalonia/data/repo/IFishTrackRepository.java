package io.agritrack.kefalonia.data.repo;

import io.agritrack.kefalonia.data.db.MobileDB;

public interface IFishTrackRepository {
    void removeAll(MobileDB db);
}
