package io.agritrack.api.sync;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.data.dto.common.SpeciesDTO;
import retrofit2.Call;
import retrofit2.Response;

public class IfcoBatchByIfcoBarcode extends BaseEnquiryCallBack<List<String>>{

    public IfcoBatchByIfcoBarcode(MutableLiveData<List<String>> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<String>> call, Response<List<String>> response) {
        List<String> ifcoBatch = response.body();

        if (ifcoBatch != null) {

            // Species name got successfully.
            syncResult.setValue(ifcoBatch);
        } else {
            // no species found for this pole
            syncResult.setValue(null);
        }
    }
}
