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
        this.syncResult = syncResult; // may be null in silent mode
    }

    protected void set(String msg) {
        if (syncResult != null && msg != null) {
            syncResult.setValue(msg);
        }
    }

    protected void post(String msg) {
        if (syncResult != null && msg != null) {
            syncResult.postValue(msg);
        }
    }

    @Override
    public abstract void onResponse(Call<T> call, Response<T> response);

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        String msg = getAppContext().getString(R.string.synch_failed);
        set(msg); // safe, does nothing if syncResult == null
    }
}
