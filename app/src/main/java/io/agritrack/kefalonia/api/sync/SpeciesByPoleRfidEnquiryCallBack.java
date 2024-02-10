package io.agritrack.kefalonia.api.sync;

import androidx.lifecycle.MutableLiveData;

import io.agritrack.kefalonia.data.dto.common.SpeciesDTO;
import retrofit2.Call;
import retrofit2.Response;

public class SpeciesByPoleRfidEnquiryCallBack extends BaseEnquiryCallBack<SpeciesDTO> {

    public SpeciesByPoleRfidEnquiryCallBack(MutableLiveData<SpeciesDTO> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<SpeciesDTO> call, Response<SpeciesDTO> response) {
        SpeciesDTO speciesDTO = response.body();

        // Species name got successfully.
        // no species found for this pole
        syncResult.setValue(speciesDTO);
    }
}
