package io.agritrack.api.sync;

import static io.agritrack.FishTrackApplication.getAppContext;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.BinInfoDTO;
import retrofit2.Call;
import retrofit2.Response;

public class SyncBinsByPackagingSite extends BaseSyncCallBack<List<BinInfoDTO>>{

    public SyncBinsByPackagingSite(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<BinInfoDTO>> call, Response<List<BinInfoDTO>> response) {
        List<BinInfoDTO> rs = response.body();

        if (rs != null) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());

            for (BinInfoDTO binDTO : rs) {
                db.binInfoDAO().insert(BinInfoDTO.convert(binDTO));
            }
            // Cage Details sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.cage_details_sync_completed));
        } else {
            // no Cage Details found
            syncResult.setValue(getAppContext().getString(R.string.no_cage_details_found_alert));
        }
    }
}
