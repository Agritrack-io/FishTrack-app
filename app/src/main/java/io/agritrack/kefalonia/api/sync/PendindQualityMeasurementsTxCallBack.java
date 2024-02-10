package io.agritrack.kefalonia.api.sync;

import static io.agritrack.kefalonia.FishTrackApplication.getAppContext;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.kefalonia.R;
import io.agritrack.kefalonia.data.db.MobileDB;
import io.agritrack.kefalonia.data.dto.common.TemperatureTimeSeriesDTO;
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

            db.temperatureDataDAO().deleteAll();
            int rowsAffected = db.measurementsDAO().deleteAll();
            Log.i("Pending measurements.", String.format("deleted %s rows from MeasurementsTransactions...", rowsAffected));

            // Sites sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.pending_measurement_tx_upload_completed));
        } else {
            //  no Sites found
            syncResult.setValue(getAppContext().getString(R.string.pending_measurement_tx_upload_failure_alert));
        }
    }
}
