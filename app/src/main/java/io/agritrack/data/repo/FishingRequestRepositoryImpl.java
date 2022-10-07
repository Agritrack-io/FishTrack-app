package io.agritrack.data.repo;

import android.app.Application;
import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.agritrack.data.dao.FishingRequestDAO;
import io.agritrack.data.db.MobileDB;

public class FishingRequestRepositoryImpl implements FishingRequestRepository {

    private FishingRequestDAO fishingRequestDAO;
    private ExecutorService service = Executors.newSingleThreadExecutor();

    public FishingRequestRepositoryImpl(Context context){
        MobileDB db = MobileDB.getInstance(context);
        this.fishingRequestDAO = db.fishingRequestsDAO();
    }

    @Override
    public void removeAll() {
        this.service.execute(() -> fishingRequestDAO.deleteAll());
    }
}
