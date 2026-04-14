package io.agritrack.philosofish.dialog;

import static io.agritrack.philosofish.FishTrackApplication.IsOnline;
import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.fish.state.GlobalState.recFishing;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.text.HtmlCompat;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.util.Strings;

import java.util.List;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.model.Site;
import io.agritrack.philosofish.fish.state.GlobalState;
import io.agritrack.philosofish.ui.service.AuthenticationService;
import io.agritrack.philosofish.ui.service.LocalPreferences;

public class SelectReasonOutOfSystemFishingDialog implements AdapterView.OnItemClickListener {
    private final MutableLiveData<String> syncResult = new MutableLiveData<>();
    private final Activity activity;
    private MobileDB db;
    private TextView tvTitle;
    private ListView lvReasons;
    private Button btnOk;
    private Dialog dialog;
    private int checked;

    public SelectReasonOutOfSystemFishingDialog(Activity activity) {
        this.activity = activity;

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        setDialog();
        findViews();

        String[] reasonList = {this.activity.getString(R.string.out_of_network), this.activity.getString(R.string.bad_weather), this.activity.getString(R.string.lack_of_sufficient_biomass), this.activity.getString(R.string.fed_fish), this.activity.getString(R.string.inability_to_fish), this.activity.getString(R.string.empty_cage)};

        ArrayAdapter<String> hrAdapter = new ArrayAdapter<String>(activity, R.layout.simple_list_checked_item_1, reasonList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                text.setTextSize(22);
                return view;
            }
        };
        this.lvReasons.setAdapter(hrAdapter);
        this.lvReasons.setOnItemClickListener(this);

        // define if single or multiple choice mode will be used to display the checkboxes.
        this.lvReasons.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        btnOk.setOnClickListener(view -> {
            checked = lvReasons.getCheckedItemPosition();
            if (checked < 0) {
                CToast(activity.getApplicationContext(), render(R.string.select_reason), Toast.LENGTH_LONG);
                return;
            } else {
                if (checked == 0 && IsOnline) {
                    CToast(activity.getApplicationContext(), render(R.string.you_are_online), Toast.LENGTH_LONG);
                    dismiss();
                    return;
                }
                addDetailsAndConfirmDialog();
            }
            //dismiss();
        });
    }

    private void addDetailsAndConfirmDialog() {
        // Get custom login form view.
        final View confirmFormView = this.activity.getLayoutInflater().inflate(R.layout.confirm_split_req_or_out_of_system_fishing_dlg, null);

        // assign variables to ui controls.
        final EditText supervisor = confirmFormView.findViewById(R.id.etSupervisorName);
        final Spinner plants = confirmFormView.findViewById(R.id.spPackagingPlant);
        final EditText pin = confirmFormView.findViewById(R.id.etPin);

        TextView title = new TextView(this.activity);
// You Can Customise your Title here
        title.setText(Html.fromHtml("<b>" + getAppContext().getResources().getString(R.string.out_of_system_fishing_add_supervisor_and_confirm) + "</b>" + "<br>" + getAppContext().getResources().getString(R.string.fill_all_fields), HtmlCompat.FROM_HTML_MODE_LEGACY));
        title.setBackgroundColor(Color.WHITE);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setTextSize(20);

        // Specify the type of input expected; this, for example, sets the input as a text, and will mask the text
        supervisor.setInputType(InputType.TYPE_CLASS_TEXT);

        // load all sites with (Packaging role?) and fill in the spPackagingSite Spinner.
        List<Site> packagingSites = db.siteDAO().getAllProcessingPlants();
        if (packagingSites != null && !packagingSites.isEmpty()) {
            String[] packagingSite = packagingSites.stream().map(x -> x.name).sorted().toArray(String[]::new);
            ArrayAdapter<String> hrAdapter = new ArrayAdapter<>(this.activity, R.layout.simple_spinner_item, packagingSite);
            hrAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
            plants.setAdapter(hrAdapter);
            plants.setSelection(hrAdapter.getPosition("VONITSA PP"));
        }

        final AlertDialog dialog = new AlertDialog.Builder(this.activity)
                .setView(confirmFormView)
                .setCustomTitle(title)
                .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(true)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String insertedPin = pin.getText().toString().trim();
                        String login = LocalPreferences.getLoggedInUser("").trim();

                        if (Strings.isEmptyOrWhitespace(supervisor.getText().toString())) {
                            CToast(getAppContext(), render(R.string.fill_all_fields), Toast.LENGTH_LONG);
                            return;
                        }

                        if (Strings.isEmptyOrWhitespace(insertedPin)) {
                            CToast(getAppContext(), render(R.string.missing_pin), Toast.LENGTH_LONG);
                            return;
                        }

                        // use typed-in PIN to compare credentials with those stored in the Local DB.
                        AuthenticationService authSvc = new AuthenticationService();
                        boolean authentication = authSvc.authenticateUser(db, login, insertedPin);
                        if (authentication) {
                            dialog.dismiss();
                            recFishing.outOfSystemFishing = true;
                            recFishing.requesterName = supervisor.getText().toString();
                            recFishing.packagingPlant = plants.getSelectedItem().toString();

                            // Redirect directly to HarvestRequestsActivity instead
                            Intent i = new Intent(activity, io.agritrack.philosofish.fish.ui.fishing.HarvestRequestsActivity.class);
                            i.putExtra("reason", (String) lvReasons.getItemAtPosition(checked));
                            activity.startActivity(i);
                        } else {
                            CToast(getAppContext(), render(R.string.invalid_password), Toast.LENGTH_LONG);
                            return;
                        }

                    }
                });
            }
        });
        dialog.show();
    }

    public void showDialog() {
        dialog.show();
    }

    public void hide() {
        dialog.hide();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    private void setDialog() {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.select_reason_out_of_system_fishing_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void findViews() {
        tvTitle = dialog.findViewById(R.id.tv_title);
        lvReasons = dialog.findViewById(R.id.lvReasons);
        btnOk = (Button) dialog.findViewById(R.id.btnOk);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
