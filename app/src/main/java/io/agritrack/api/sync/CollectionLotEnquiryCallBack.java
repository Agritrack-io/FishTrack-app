package io.agritrack.api.sync;

import static io.agritrack.FishTrackApplication.getAppContext;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.tx.CollectTxDTO;
import retrofit2.Call;
import retrofit2.Response;

public class CollectionLotEnquiryCallBack extends BaseEnquiryCallBack<String>{

    public CollectionLotEnquiryCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<String> call, Response<String> response) {
        String collectionLot = response.body();

        if (collectionLot != null) {

            // Harvest Requests sync succeeded.
            syncResult.setValue(collectionLot);
        } else {
            // no Harvest Requests found
            syncResult.setValue(null);
        }
    }
}
