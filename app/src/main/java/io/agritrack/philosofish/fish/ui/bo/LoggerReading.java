package io.agritrack.philosofish.fish.ui.bo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Map;

public class LoggerReading extends ViewModel {
    private final MutableLiveData<Map<String, Object>> selectedReading = new MutableLiveData<>();

    public void setReading(Map<String, Object> reading) {
        selectedReading.postValue(reading);
    }

    public LiveData<Map<String, Object>> getReading() {
        return selectedReading;
    }
}
