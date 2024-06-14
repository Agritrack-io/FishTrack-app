package io.agritrack.philosofish.data.model;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BackendURLViewModel extends ViewModel {
    private MutableLiveData<String> text = new MutableLiveData<>();

    public LiveData<String> getText() {
        return text;
    }

    public void setText(String newText) {
        text.setValue(newText);
    }
}