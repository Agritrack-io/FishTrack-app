package io.agritrack.api.sync;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class RfidBatchByRfidBarcode extends BaseEnquiryCallBack<List<String>>{

    public RfidBatchByRfidBarcode(MutableLiveData<List<String>> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<String>> call, Response<List<String>> response) {
        List<String> rfidBatch = response.body();

        // Species name got successfully.
        // no species found for this pole
        syncResult.setValue(rfidBatch);
    }
}
