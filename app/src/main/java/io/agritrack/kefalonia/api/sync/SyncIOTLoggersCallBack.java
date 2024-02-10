package io.agritrack.kefalonia.api.sync;

import static io.agritrack.kefalonia.FishTrackApplication.getAppContext;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.kefalonia.R;
import io.agritrack.kefalonia.data.db.MobileDB;
import io.agritrack.kefalonia.data.dto.common.IotLoggerDTO;
import retrofit2.Call;
import retrofit2.Response;

public class SyncIOTLoggersCallBack extends BaseSyncCallBack<List<IotLoggerDTO>> {
    public SyncIOTLoggersCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<IotLoggerDTO>> call, Response<List<IotLoggerDTO>> response) {
        List<IotLoggerDTO> rs = response.body();

        if (rs != null) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());

            for (IotLoggerDTO loggersDTO : rs) {
                db.iotLoggerDAO().insert(IotLoggerDTO.convert(loggersDTO));
            }
            // IOT Loggers sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.IOTLoggers_sync_completed));
        } else {
            // no IOT Logger found
            syncResult.setValue(getAppContext().getString(R.string.no_IOTLoggers_found_alert));
        }
    }
}