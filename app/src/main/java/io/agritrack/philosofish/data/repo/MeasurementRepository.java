package io.agritrack.data.repo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.repo.IFishTrackRepository;

public class MeasurementRepository implements IFishTrackRepository {
    private ExecutorService service = Executors.newSingleThreadExecutor();

    @Override
    public void removeAll(MobileDB db) {
        this.service.execute(() -> db.measurementsDAO().deleteAll());
    }
}
