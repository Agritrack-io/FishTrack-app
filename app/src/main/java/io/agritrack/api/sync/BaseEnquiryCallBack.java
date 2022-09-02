package io.agritrack.api.sync;

import androidx.lifecycle.MutableLiveData;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseEnquiryCallBack<T> implements Callback<T> {

    protected final MutableLiveData<T> syncResult;
    protected MobileDB db;

    protected BaseEnquiryCallBack(MutableLiveData<T> syncResult) {
        this.syncResult = syncResult;
    }

    @Override
    public abstract void onResponse(Call<T> call, Response<T> response);

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        // Probably Network Communication Error
        syncResult.setValue(null);
    }
}
