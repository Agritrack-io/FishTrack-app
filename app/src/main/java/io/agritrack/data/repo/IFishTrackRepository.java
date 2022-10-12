package io.agritrack.data.repo;

import io.agritrack.data.db.MobileDB;

public interface IFishTrackRepository {
    void removeAll(MobileDB db);
}
