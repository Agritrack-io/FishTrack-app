package io.agritrack.api.sync;

import static io.agritrack.FishTrackApplication.getAppContext;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.common.CustomerDTO;
import io.agritrack.data.dto.common.EmployeeDTO;
import retrofit2.Call;
import retrofit2.Response;

public class SyncCustomersCallBack extends BaseSyncCallBack<List<CustomerDTO>>{

    public SyncCustomersCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<CustomerDTO>> call, Response<List<CustomerDTO>> response) {
        List<CustomerDTO> rs = response.body();

        if (rs != null) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());

            for (CustomerDTO customerDTO : rs) {
                db.customerDAO().insert(CustomerDTO.convert(customerDTO));
            }
            // Employees sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.customers_sync_completed));
        } else {
            // No Employees found
            syncResult.setValue(getAppContext().getString(R.string.no_customers_found_alert));
        }
    }
}
