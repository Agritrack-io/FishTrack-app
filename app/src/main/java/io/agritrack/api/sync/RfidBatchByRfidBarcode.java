package io.agritrack.api.sync;

import static io.agritrack.FishTrackApplication.getAppContext;

import androidx.lifecycle.MutableLiveData;

import java.util.Collections;
import java.util.List;

import io.agritrack.kefalonia.R;
import retrofit2.Call;
import retrofit2.Response;

public class RfidBatchByRfidBarcode extends BaseEnquiryCallBack<List<String>>{

    public RfidBatchByRfidBarcode(MutableLiveData<List<String>> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<String>> call, Response<List<String>> response) {
        List<String> rfidBatch = response.body();

        if (rfidBatch != null && !rfidBatch.isEmpty()) {
            // rfidBatch sync succeeded.
            syncResult.setValue(rfidBatch);
        } else {
            // no rfidBatch found
            syncResult.setValue(null);
        }
    }

    @Override
    public void onFailure(Call<List<String>> call, Throwable t) {
        // Probably Network Communication Error
        syncResult.setValue(Collections.singletonList(getAppContext().getResources().getString(R.string.change_position_to_find_network_coverage_and_scan_again)));
    }
}
