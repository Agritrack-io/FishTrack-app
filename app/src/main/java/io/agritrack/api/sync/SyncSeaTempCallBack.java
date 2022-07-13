package io.agritrack.api.sync;

import static io.agritrack.FishTrackApplication.getAppContext;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.tx.SeaTemperatureTxDTO;
import io.agritrack.data.dto.wh.FoodSkuDTO;
import retrofit2.Call;
import retrofit2.Response;

public class SyncSeaTempCallBack extends BaseSyncCallBack<List<SeaTemperatureTxDTO>> {
    public SyncSeaTempCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<SeaTemperatureTxDTO>> call, Response<List<SeaTemperatureTxDTO>> response) {
        List<SeaTemperatureTxDTO> rs = response.body();

        if (rs != null) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());

            for (SeaTemperatureTxDTO seaTempDTO : rs) {
                db.seaTemperatureTransactionDAO().insert(SeaTemperatureTxDTO.convert(seaTempDTO));
            }
            // Sea temp sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.sea_temp_sync_completed));
        } else {
            // no Sea temp found
            syncResult.setValue(getAppContext().getString(R.string.no_sea_temp_found_alert));
        }
    }
}