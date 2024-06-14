package io.agritrack.philosofish.api.sync;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;

import androidx.lifecycle.MutableLiveData;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.data.db.MobileDB;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseSyncCallBack<T> implements Callback<T> {

    protected final MutableLiveData<String> syncResult;
    protected MobileDB db;

    protected BaseSyncCallBack(MutableLiveData<String> syncResult) {
        this.syncResult = syncResult;
    }

    @Override
    public abstract void onResponse(Call<T> call, Response<T> response);

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        // Probably Network Communication Error
        call.request().url();
        syncResult.setValue(getAppContext().getString(R.string.synch_failed));
    }
}
