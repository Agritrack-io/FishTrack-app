package io.agritrack.api.sync;

import androidx.lifecycle.MutableLiveData;

import io.agritrack.data.dto.common.SpeciesDTO;
import retrofit2.Call;
import retrofit2.Response;

public class SpeciesByPoleRfidEnquiryCallBack extends BaseEnquiryCallBack<SpeciesDTO>{

    public SpeciesByPoleRfidEnquiryCallBack(MutableLiveData<SpeciesDTO> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<SpeciesDTO> call, Response<SpeciesDTO> response) {
        SpeciesDTO speciesDTO = response.body();

        if (speciesDTO != null) {

            // Species name got successfully.
            syncResult.setValue(speciesDTO);
        } else {
            // no species found for this pole
            syncResult.setValue(null);
        }
    }
}
