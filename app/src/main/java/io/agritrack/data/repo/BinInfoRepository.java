package io.agritrack.data.repo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.BinInfo;

public class BinInfoRepository implements IFishTrackRepository{
    private ExecutorService service = Executors.newSingleThreadExecutor();

    @Override
    public void removeAll(MobileDB db) {
        this.service.execute(() -> db.binInfoDAO().deleteAll());
    }

    public void removeOneBin(MobileDB db, BinInfo bin) {
        this.service.execute(() -> db.binInfoDAO().delete(bin));
    }
}
