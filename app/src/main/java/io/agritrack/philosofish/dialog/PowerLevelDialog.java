package io.agritrack.philosofish.dialog;

import static io.agritrack.philosofish.ui.service.LocalPreferences.getCurrentPower;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import io.agritrack.philosofish.R;


public class PowerLevelDialog extends DialogFragment {

    public interface PowerLevelListener {
        void onPowerLevelSelected(int powerLevel);
    }

    private PowerLevelListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof PowerLevelListener) {
            listener = (PowerLevelListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement PowerLevelListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.choose_rfid_level));

        View view = LayoutInflater.from(getContext()).inflate(R.layout.change_power_dialog, null);
        builder.setView(view);

        RadioGroup powerRadioGroup = view.findViewById(R.id.powerRadioGroup);
        RadioButton lowPowerButton = view.findViewById(R.id.lowPowerButton);
        RadioButton medPowerButton = view.findViewById(R.id.medPowerButton);
        RadioButton highPowerButton = view.findViewById(R.id.highPowerButton);

        int currentPower = getCurrentPower();
        if (currentPower < 25) {
            lowPowerButton.setChecked(true);
        } else if (currentPower < 30) {
            medPowerButton.setChecked(true);
        } else {
            highPowerButton.setChecked(true);
        }

        // Apply immediately when changed
        powerRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int selectedPower;
            if (checkedId == R.id.lowPowerButton) {
                selectedPower = 1;
            } else if (checkedId == R.id.medPowerButton) {
                selectedPower = 2;
            } else {
                selectedPower = 3;
            }

            if (listener != null) {
                listener.onPowerLevelSelected(selectedPower);
            }

            dismiss(); // Close the dialog right after selection
        });

        return builder.create(); // No OK/Cancel buttons
    }
}

