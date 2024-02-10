package io.agritrack.kefalonia.data.repo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.agritrack.kefalonia.data.db.MobileDB;

public class FishingRequestRepository implements IFishTrackRepository {

    private ExecutorService service = Executors.newSingleThreadExecutor();

    @Override
    public void removeAll(MobileDB db) {
        this.service.execute(() -> db.fishingRequestsDAO().deleteAll());
    }
}
