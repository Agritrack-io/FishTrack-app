package io.agritrack.philosofish.api.sync;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.api.sync.BaseSyncCallBack;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.EncodingSchemeDTO;
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
            db = MobileDB.getInstance(getAppContext());

            for (EncodingSchemeDTO schemeDTO : rs) {
                db.encodingSchemeDAO().insert(EncodingSchemeDTO.convert(schemeDTO));
            }

            set(getAppContext().getString(R.string.EncodeScheme_sync_completed));

        } else {

            set(getAppContext().getString(R.string.no_EncodeScheme_found_alert));
        }
    }
}
