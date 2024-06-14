package io.agritrack.philosofish.api.sync;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.common.EmployeeDTO;
import retrofit2.Call;
import retrofit2.Response;

public class SyncEmployeesCallBack extends BaseSyncCallBack<List<EmployeeDTO>> {

    public SyncEmployeesCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<EmployeeDTO>> call, Response<List<EmployeeDTO>> response) {
        List<EmployeeDTO> rs = response.body();

        if (rs != null) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());

            for (EmployeeDTO employeeDTO : rs) {
                db.employeeDAO().insert(EmployeeDTO.convert(employeeDTO));
            }
            // Employees sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.employees_sync_completed));
        } else {
            // No Employees found
            syncResult.setValue(getAppContext().getString(R.string.no_employees_found_alert));
        }
    }
}
