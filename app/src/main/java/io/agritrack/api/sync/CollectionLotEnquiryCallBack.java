package io.agritrack.api.sync;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.data.dto.LotDTO;
import retrofit2.Call;
import retrofit2.Response;

public class CollectionLotEnquiryCallBack extends BaseEnquiryCallBack<LotDTO>{

    public CollectionLotEnquiryCallBack(MutableLiveData<LotDTO> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<LotDTO> call, Response<LotDTO> response) {
        LotDTO collectionLot = response.body();

        // Harvest Requests sync succeeded.
        // no Harvest Requests found
        syncResult.setValue(collectionLot);
    }
}
