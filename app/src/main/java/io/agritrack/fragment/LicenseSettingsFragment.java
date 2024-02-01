package io.agritrack.fragment;

import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import io.agritrack.R;
import io.agritrack.databinding.FragmentLicenseSettingsBinding;
import io.agritrack.ui.viewmodel.ConfigViewModel;

public class LicenseSettingsFragment extends Fragment {

    private ConfigViewModel appSettings;

    public LicenseSettingsFragment() {
        // Required empty public constructor
    }

    public LicenseSettingsFragment(ConfigViewModel appSettingsViewModel) {
        this.appSettings = appSettingsViewModel;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate view and obtain an instance of the binding class.
        FragmentLicenseSettingsBinding binding = FragmentLicenseSettingsBinding.inflate(getLayoutInflater());

        // Specify the current activity as the lifecycle owner.
        binding.setAppSettings(appSettings);

        // Specify the current activity as the lifecycle owner.
        binding.setLifecycleOwner(this);

        // Bind Copy/Paste/Clear Buttons
        binding.getRoot().findViewById(R.id.ivCopyItem).setOnClickListener(view -> copyLicenseToClipBoard());

        binding.getRoot().findViewById(R.id.ivPasteItem).setOnClickListener(view -> pasteLicenseFromClipBoard());

        binding.getRoot().findViewById(R.id.ivClear).setOnClickListener(view -> clearLicenseField());

        return binding.getRoot();
    }

    private void copyLicenseToClipBoard() {
        if (appSettings == null || TextUtils.isEmpty(appSettings.getCurrentLicense())) {
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("copiedLicense", appSettings.getCurrentLicense());
        clipboard.setPrimaryClip(clip);

        CToast(getContext(), render("License copied"), Toast.LENGTH_LONG);
    }

    private void pasteLicenseFromClipBoard() {
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);

        if (clipboard.getPrimaryClip().getItemCount() <= 0) {
            return;
        }

        ClipData.Item lastCopiedItem = clipboard.getPrimaryClip().getItemAt(0);
        if (lastCopiedItem.getText() != null && !TextUtils.isEmpty(lastCopiedItem.getText().toString())) {
            appSettings.setCurrentLicense(lastCopiedItem.getText().toString());
            CToast(getContext(), render("License pasted"), Toast.LENGTH_LONG);

            // Clear clipboard
            ClipData clipData = ClipData.newPlainText("", "");
            clipboard.setPrimaryClip(clipData);
        }
    }

    private void clearLicenseField() {
        if (appSettings == null || TextUtils.isEmpty(appSettings.getCurrentLicense())) {
            return;
        }

        appSettings.setCurrentLicense("");
    }
}