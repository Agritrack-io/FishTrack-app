package io.agritrack.api.sync;

import androidx.lifecycle.MutableLiveData;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static io.agritrack.FishTrackApplication.getAppContext;

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
        syncResult.setValue(getAppContext().getString(R.string.synch_failed));
    }
}
