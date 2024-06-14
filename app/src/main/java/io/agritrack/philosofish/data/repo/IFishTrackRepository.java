package io.agritrack.philosofish.data.repo;

import io.agritrack.philosofish.data.db.MobileDB;

public interface IFishTrackRepository {
    void removeAll(MobileDB db);
}
