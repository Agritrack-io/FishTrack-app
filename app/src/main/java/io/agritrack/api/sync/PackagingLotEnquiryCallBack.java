package io.agritrack.api.sync;

import androidx.lifecycle.MutableLiveData;

import io.agritrack.data.dto.LotDTO;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PackagingLotEnquiryCallBack extends BaseEnquiryCallBack<LotDTO> {

    public PackagingLotEnquiryCallBack(MutableLiveData<LotDTO> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<LotDTO> call, Response<LotDTO> response) {
        LotDTO packagingLot = response.body();

        // Harvest Requests sync succeeded.
        // no Harvest Requests found
        syncResult.setValue(packagingLot);
    }
}
