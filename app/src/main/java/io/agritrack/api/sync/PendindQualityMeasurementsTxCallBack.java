package io.agritrack.api.sync;

import static io.agritrack.FishTrackApplication.getAppContext;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.common.TemperatureTimeSeriesDTO;
import io.agritrack.data.dto.tx.PostPackageQualityTxDTO;
import retrofit2.Call;
import retrofit2.Response;

public class PendindQualityMeasurementsTxCallBack extends BaseSyncCallBack<List<TemperatureTimeSeriesDTO>> {

    public PendindQualityMeasurementsTxCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<TemperatureTimeSeriesDTO>> call, Response<List<TemperatureTimeSeriesDTO>> response) {
        List<TemperatureTimeSeriesDTO> dto = response.body();

        if (dto != null) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());

            int rowsAffected = db.postPackageQualityTransactionDAO().deleteAll();
            Log.i("Pending post qualities.", String.format("deleted %s rows from PostQualityTransactions...", rowsAffected));

            // Sites sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.pending_quality_tx_upload_completed));
        } else {
            //  no Sites found
            syncResult.setValue(getAppContext().getString(R.string.pending_quality_tx_upload_failure_alert));
        }
    }
}
