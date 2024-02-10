package io.agritrack.api.sync;

import static io.agritrack.kefalonia.FishTrackApplication.getAppContext;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.kefalonia.R;
import io.agritrack.kefalonia.api.sync.BaseSyncCallBack;
import io.agritrack.kefalonia.data.db.MobileDB;
import io.agritrack.kefalonia.data.dto.EncodingSchemeDTO;
import retrofit2.Call;
import retrofit2.Response;

public class EncodingSchemeCallBack extends BaseSyncCallBack<List<EncodingSchemeDTO>> {

    public EncodingSchemeCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<EncodingSchemeDTO>> call, Response<List<EncodingSchemeDTO>> response) {
        List<EncodingSchemeDTO> rs = response.body();

        if (rs != null) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());

            for (EncodingSchemeDTO schemeDTO : rs) {
                db.encodingSchemeDAO().insert(EncodingSchemeDTO.convert(schemeDTO));
            }
            // Encoding Scheme sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.EncodeScheme_sync_completed));
        } else {
            // no Encoding Scheme  found
            syncResult.setValue(getAppContext().getString(R.string.no_EncodeScheme_found_alert));
        }
    }
}