package io.agritrack.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import io.agritrack.kefalonia.databinding.FragmentApplicationSettingsBinding;
import io.agritrack.ui.viewmodel.ConfigViewModel;

public class ApplicationSettingsFragment extends Fragment {

    private ConfigViewModel appSettings;

    public ApplicationSettingsFragment() {
        // Required empty public constructor
    }

    public ApplicationSettingsFragment(ConfigViewModel appSettingsViewModel) {
        this.appSettings = appSettingsViewModel;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate view and obtain an instance of the binding class.
        FragmentApplicationSettingsBinding binding = FragmentApplicationSettingsBinding.inflate(getLayoutInflater());

        // Specify the current activity as the lifecycle owner.
        binding.setAppSettings(appSettings);

        // Specify the current activity as the lifecycle owner.
        binding.setLifecycleOwner(this);

        return binding.getRoot();
    }
}