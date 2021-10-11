package io.agritrack.api.sync;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.AppUserDTO;
import retrofit2.Call;
import retrofit2.Response;

import static io.agritrack.FishTrackApplication.getAppContext;

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
