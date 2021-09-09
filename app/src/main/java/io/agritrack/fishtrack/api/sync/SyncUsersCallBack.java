package io.agritrack.fishtrack.api.sync;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.dto.AppUserDTO;
import retrofit2.Call;
import retrofit2.Response;

import static io.agritrack.fishtrack.FishTrackApplication.getAppContext;

public class SyncUsersCallBack extends BaseSyncCallBack<List<AppUserDTO>> {

    public SyncUsersCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<AppUserDTO>> call, Response<List<AppUserDTO>> response) {
        List<AppUserDTO> rs = response.body();

        if (rs != null) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());

            for (AppUserDTO userDTO : rs) {
                db.userDAO().insert(AppUserDTO.convert(userDTO));
            }
            // Users sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.users_sync_completed));
        } else {
            // no Users found
            syncResult.setValue(getAppContext().getString(R.string.no_users_found_alert));
        }
    }
}
