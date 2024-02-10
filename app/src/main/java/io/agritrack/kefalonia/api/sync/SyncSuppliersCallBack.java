package io.agritrack.kefalonia.api.sync;

import static io.agritrack.kefalonia.FishTrackApplication.getAppContext;
import static io.agritrack.kefalonia.data.dto.common.SupplierDTO.convert;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.kefalonia.R;
import io.agritrack.kefalonia.data.db.MobileDB;
import io.agritrack.kefalonia.data.dto.common.SupplierDTO;
import retrofit2.Call;
import retrofit2.Response;

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
                db.supplierDAO().insert(convert(SupplierDTO));
            }
            // Employees sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.employees_sync_completed));
        } else {
            // No Employees found
            syncResult.setValue(getAppContext().getString(R.string.no_employees_found_alert));
        }
    }
}
