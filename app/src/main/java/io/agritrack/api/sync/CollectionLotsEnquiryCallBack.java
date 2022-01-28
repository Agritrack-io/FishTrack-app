package io.agritrack.api.sync;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.data.dto.LotDTO;
import retrofit2.Call;
import retrofit2.Response;

public class CollectionLotsEnquiryCallBack extends BaseEnquiryCallBack<List<LotDTO>>{

    public CollectionLotsEnquiryCallBack(MutableLiveData<List<LotDTO>> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<LotDTO>> call, Response<List<LotDTO>> response) {
        List<LotDTO> collectionLots = response.body();

        if (collectionLots != null) {

            // Harvest Requests sync succeeded.
            syncResult.setValue(collectionLots);
        } else {
            // no Harvest Requests found
            syncResult.setValue(null);
        }
    }
}
