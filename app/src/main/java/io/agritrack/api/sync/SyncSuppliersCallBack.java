package io.agritrack.api.sync;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.common.SupplierDTO;
import retrofit2.Call;
import retrofit2.Response;

import static io.agritrack.FishTrackApplication.getAppContext;

public class SyncSuppliersCallBack extends BaseSyncCallBack<List<SupplierDTO>> {

    public SyncSuppliersCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<SupplierDTO>> call, Response<List<SupplierDTO>> response) {
        List<SupplierDTO> rs = response.body();

        if (rs != null) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());

            for (SupplierDTO SupplierDTO : rs) {
                db.supplierDAO().insert(SupplierDTO.convert(SupplierDTO));
            }
            // Employees sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.employees_sync_completed));
        } else {
            // No Employees found
            syncResult.setValue(getAppContext().getString(R.string.no_employees_found_alert));
        }
    }
}
