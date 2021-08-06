package io.agritrack.fishtrack.ui.custom;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.ui.activity.login.LoginActivity;
import io.agritrack.fishtrack.ui.activity.login.api.SiteInfo;
import io.agritrack.fishtrack.ui.service.SharedPreferenceService;

import static io.agritrack.fishtrack.FishTrackApplication.getAppContext;

public class YesNoDialog extends DialogFragment {
    private final SiteInfo mSite;
    private final Class mIntentClass;

    public YesNoDialog(SiteInfo selectedSite, Class clazz) {
        mSite = selectedSite;
        mIntentClass = clazz;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(getText(R.string.accept_selected_site) + mSite.getName())
                .setPositiveButton(R.string.dialog_accept, (dialog, id) -> {
                    // persist selected Site to local Preferences.
                    SharedPreferenceService.writeValue(SharedPreferenceService.SelectedSite_Key, mSite.getName());
                    // move to Login Screen
                    Intent i = new Intent(getAppContext(), mIntentClass);
                    i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // disables back button...
                    startActivity(i);
                })
                .setNegativeButton(R.string.dialog_deny, (dialog, id) -> {
                    return;
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
