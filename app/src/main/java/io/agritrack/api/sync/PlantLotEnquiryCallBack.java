package io.agritrack.api.sync;

import androidx.lifecycle.MutableLiveData;

import io.agritrack.data.dto.LotDTO;
import retrofit2.Call;
import retrofit2.Response;

public class PlantLotEnquiryCallBack extends BaseEnquiryCallBack<LotDTO>{

    public PlantLotEnquiryCallBack(MutableLiveData<LotDTO> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<LotDTO> call, Response<LotDTO> response) {
        LotDTO plantLot = response.body();

        // Harvest Requests sync succeeded.
        // no Harvest Requests found
        syncResult.setValue(plantLot);
    }
}
