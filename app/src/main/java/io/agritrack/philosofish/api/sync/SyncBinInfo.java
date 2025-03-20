package io.agritrack.philosofish.api.sync;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;

import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.BinInfoDTO;
import retrofit2.Call;
import retrofit2.Response;

public class SyncBinInfo extends BaseSyncCallBack<List<BinInfoDTO>> {

    public SyncBinInfo(MutableLiveData<String> syncResult) {
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
    @Override
    public void onFailure(Call<List<BinInfoDTO>> call, Throwable t) {
        System.out.println(t);

        CToast(getAppContext(), render("Please Check WIFI connection.."), Toast.LENGTH_LONG);
        syncResult.setValue(null);
    }

}
