package io.agritrack.kefalonia.api.sync;

import androidx.lifecycle.MutableLiveData;

import io.agritrack.kefalonia.data.dto.LotDTO;
import retrofit2.Call;
import retrofit2.Response;

public class CollectionLotEnquiryCallBack extends BaseEnquiryCallBack<LotDTO> {

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
