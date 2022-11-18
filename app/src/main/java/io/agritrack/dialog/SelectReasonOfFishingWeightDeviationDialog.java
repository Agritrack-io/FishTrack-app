package io.agritrack.dialog;

import static io.agritrack.FishTrackApplication.IsOnline;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recFishing;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.annotation.SuppressLint;
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

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.Site;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.fishing.FishingTeamActivity;
import io.agritrack.ui.service.AuthenticationService;
import io.agritrack.ui.service.LocalPreferences;

public class SelectReasonOfFishingWeightDeviationDialog implements AdapterView.OnItemClickListener {
    private final MutableLiveData<String> syncResult = new MutableLiveData<>();
    private final Activity activity;
    private MobileDB db;
    private TextView tvTitle;
    private ListView lvReasons;
    private Button btnOk;
    private Dialog dialog;
    private int checked;

    public SelectReasonOfFishingWeightDeviationDialog(Activity activity) {
        this.activity = activity;

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        setDialog();
        findViews();

        String[] reasonList = {this.activity.getString(R.string.bad_weather), this.activity.getString(R.string.inability_to_fish), this.activity.getString(R.string.empty_cage)};

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
            if (checked<0){
                CToast(activity.getApplicationContext(), render(R.string.select_reason), Toast.LENGTH_LONG);
                return;
            } else {
                recFishing.reasonOfDeviation = (String) (lvReasons.getItemAtPosition(checked));
                dismiss();
            }
        });
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
        dialog.setContentView(R.layout.select_reason_of_fishing_weight_deviation_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @SuppressLint("StringFormatMatches")
    private void findViews() {
        tvTitle = dialog.findViewById(R.id.tv_title);
        tvTitle.setText(getAppContext().getString(R.string.select_reason_of_weight_deviation, String.valueOf(Math.abs(recFishing.reqWeight-recFishing.totalFishWeight))));
        lvReasons = dialog.findViewById(R.id.lvReasons);
        btnOk = (Button) dialog.findViewById(R.id.btnOk);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
