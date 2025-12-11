package io.agritrack.philosofish.api.sync;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.common.CustomerDTO;
import retrofit2.Call;
import retrofit2.Response;

public class SyncCustomersCallBack extends BaseSyncCallBack<List<CustomerDTO>> {

    public SyncCustomersCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<CustomerDTO>> call, Response<List<CustomerDTO>> response) {
        List<CustomerDTO> rs = response.body();

        if (rs != null) {
            db = MobileDB.getInstance(getAppContext());

            for (CustomerDTO dto : rs) {
                db.customerDAO().insert(CustomerDTO.convert(dto));
            }

            // SAFE
            set(getAppContext().getString(R.string.customers_sync_completed));

        } else {
            // SAFE
            set(getAppContext().getString(R.string.no_customers_found_alert));
        }
    }
}
