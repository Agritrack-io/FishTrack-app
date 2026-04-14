package io.agritrack.philosofish.fish.ui.fishing;

import static io.agritrack.philosofish.FishTrackApplication.IsDemo;
import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.fish.state.GlobalState.recFishing;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import com.google.android.gms.common.util.Strings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.model.FishingRequest;
import io.agritrack.philosofish.dialog.SelectReasonOutOfSystemFishingDialog;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.fish.state.GlobalState;
import io.agritrack.philosofish.fish.ui.FishHomeActivity;
import io.agritrack.philosofish.fish.ui.bo.GenericListModel;
import io.agritrack.philosofish.ui.custom.ToggleGroup;
import io.agritrack.philosofish.ui.service.AuthenticationService;
import io.agritrack.philosofish.ui.service.LocalPreferences;

public class HarvestRequestsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, ToggleGroup.OnCheckedChangeListener {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat formatString = new SimpleDateFormat("dd-MM-yyyy");
    private SimpleDateFormat formatDateTime = new SimpleDateFormat("yyyy-MM-dd hh:mm");
    private SimpleDateFormat formatStringDateTime = new SimpleDateFormat("dd-MM-yyyy hh:mm");
    private MobileDB db;
    private ListView lvFishingRequests;
    private GenericListModel[] fishingRQs;
    private ToggleGroup tgChooseDate;
    private ImageButton ibSelectReason, ibSplitRequest;
    private SelectReasonOutOfSystemFishingDialog selectReasonDialog;
    private ImageView ivSupport;
    private FishingRequest harvestRq;
    private SupportDialog supportDialog;
    private CheckedTextView v;
    private GenericListModel member;
    private int lastPosition = -1;
    private String requesterName;

    private long harvestRQcnt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_harvest_requests);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderHarvestRequests);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get main controls references
        this.lvFishingRequests = findViewById(R.id.lvHarvestRequests);

        // define if single or multiple choice mode will be used to display the checkboxes.
        this.lvFishingRequests.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        tgChooseDate = findViewById(R.id.tgChooseDate);
        tgChooseDate.setOnCheckedChangeListener(this);
        tgChooseDate.check(R.id.tbToday);
        getTodayHarvestReq();

        ibSplitRequest = findViewById(R.id.ibSplitRequest);

        ivSupport = findViewById(R.id.ivSupport);
        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(HarvestRequestsActivity.this);
            supportDialog.showDialog();
        });

        ibSelectReason = findViewById(R.id.ibSelectReason);
        ibSelectReason.setOnClickListener(view -> {
            selectReasonDialog = new SelectReasonOutOfSystemFishingDialog(HarvestRequestsActivity.this);
            selectReasonDialog.showDialog();
        });

        // create Footer
        configFooter();
    }

    // Define 'back' / 'next' Buttons functionality
    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToStartFishing);
        ivNext.setOnClickListener(view -> {
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render(v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), FishingBinsActivity.class);
                startActivity(i);
            }
        });


        ImageView ivBack = findViewById(R.id.ivBackToHomeMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
            startActivity(i);
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ibSplitRequest.setVisibility(View.VISIBLE);
        v = (CheckedTextView) view;
        boolean currentCheck = v.isChecked();
        member = (GenericListModel) this.lvFishingRequests.getItemAtPosition(position);
        member.setChecked(!currentCheck);

        harvestRq = db.fishingRequestsDAO().getById(member.getRequestId());

        ibSplitRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastPosition = position;
                confirmSplitRequestDlg();
                ibSplitRequest.setVisibility(View.INVISIBLE);
            }
        });

        if (harvestRq != null) {

            recFishing.fishingRq = harvestRq.requestId;
            recFishing.requesterName = harvestRq.requester;
            recFishing.speciesName = harvestRq.species;
            recFishing.cageCode = harvestRq.cageCode;
            recFishing.typedCageCode = harvestRq.cageCode;
            recFishing.expectedCageRFID = harvestRq.cageRFID;
            recFishing.averageWeight = harvestRq.averageWeight;
            recFishing.reqWeight = harvestRq.quantity;
            recFishing.notes = harvestRq.notes;
            recFishing.parentItinSno = harvestRq.parentItinSno != null ? harvestRq.parentItinSno : null;
            recFishing.packagingPlant = harvestRq.packagingPlant;
        }
    }

    private void duplicate(FishingRequest fishingRequest) {
        FishingRequest duplicatedFishReq = new FishingRequest();

        String harvReq = fishingRequest.requestId.substring(0, fishingRequest.requestId.length() - 2);
        recFishing.parentItinSno = Short.valueOf(fishingRequest.requestId.substring(fishingRequest.requestId.length() - 2));
        List<FishingRequest> fishReqs = db.fishingRequestsDAO().getAllFishReqWithSameHarvReq(harvReq);
        if (!fishReqs.isEmpty()) {
            Short maxItin = fishReqs.stream().map(x -> x.itinSNo).max(Short::compare).get();
            int newItinerary = maxItin != null ? maxItin + 1 : fishReqs.size() + 1;
            duplicatedFishReq.requestId = String.format("%s%02d", harvReq, newItinerary);
            duplicatedFishReq.itinSNo = (short) newItinerary;
        }
        duplicatedFishReq.requester = requesterName;
        duplicatedFishReq.parentItinSno = recFishing.parentItinSno;
        duplicatedFishReq.species = fishingRequest.species;
        duplicatedFishReq.cageCode = fishingRequest.cageCode;
        duplicatedFishReq.harvestDate = fishingRequest.harvestDate;
        duplicatedFishReq.farmArrival = fishingRequest.farmArrival;
        duplicatedFishReq.reqQty = (double) 0;
        duplicatedFishReq.driver = fishingRequest.driver;
        duplicatedFishReq.site = fishingRequest.site;
        duplicatedFishReq.cageRFID = fishingRequest.cageRFID;
        duplicatedFishReq.averageWeight = fishingRequest.averageWeight;
        duplicatedFishReq.quantity = (double) 0;
        duplicatedFishReq.notes = "Split Request";
        duplicatedFishReq.packagingPlant = fishingRequest.packagingPlant;

        db.fishingRequestsDAO().insert(duplicatedFishReq);
    }

    private void confirmSplitRequestDlg() {
        // Get custom login form view.
        final View confirmFormView = getLayoutInflater().inflate(R.layout.confirm_split_req_or_out_of_system_fishing_dlg, null);

        // assign variables to ui controls.
        final EditText supervisor = confirmFormView.findViewById(R.id.etSupervisorName);
        final EditText pin = confirmFormView.findViewById(R.id.etPin);
        final TableRow plant = confirmFormView.findViewById(R.id.packagingPlant);

        TextView title = new TextView(this);
// You Can Customise your Title here
        title.setText(Html.fromHtml("<b>" + getString(R.string.split_add_supervisor_and_confirm) + "</b>" + "<br>" + getString(R.string.fill_all_fields), HtmlCompat.FROM_HTML_MODE_LEGACY));
        title.setBackgroundColor(Color.WHITE);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setTextSize(20);

        plant.setVisibility(View.GONE);

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        supervisor.setInputType(InputType.TYPE_CLASS_TEXT);

        final AlertDialog dialog = new AlertDialog.Builder(HarvestRequestsActivity.this)
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
                            CToast(getAppContext(), render(getString(R.string.fill_all_fields)), Toast.LENGTH_LONG);
                            return;
                        }

                        if (Strings.isEmptyOrWhitespace(insertedPin)) {
                            CToast(getAppContext(), render(getString(R.string.missing_pin)), Toast.LENGTH_LONG);
                            return;
                        }

                        // use typed-in PIN to compare credentials with those stored in the Local DB.
                        AuthenticationService authSvc = new AuthenticationService();
                        boolean authentication = authSvc.authenticateUser(db, login, insertedPin);
                        if (authentication) {
                            dialog.dismiss();
                            requesterName = supervisor.getText().toString();
                            duplicate(harvestRq);
                            getTodayHarvestReq();
                        } else {
                            CToast(getAppContext(), render(getString(R.string.invalid_password)), Toast.LENGTH_LONG);
                            return;
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {
        LocalDate now = LocalDate.now();
        String nowDate = now.format(formatter);
        LocalDate yesterday = now.minusDays(1);
        String yesterdayDate = yesterday.format(formatter);
        LocalDate date = now.minusDays(2);
        String previousDate = date.format(formatter);

        if (checkedId == R.id.tbToday) {
            getTodayHarvestReq();
        } else if (checkedId == R.id.tbYesterday) {
            getYesterdayHarvestReq();
        } else if (checkedId == R.id.tbOlderDays) {
            getPreviousHarvestReq();
        }
    }

    private void getTodayHarvestReq() {
        this.lvFishingRequests.setAdapter(null);
        List<FishingRequest> fishingRequests = db.fishingRequestsDAO().getTodayRecord();
        if (fishingRequests != null && !fishingRequests.isEmpty()) {
            List<FishingRequest> newFishingReq = fishingRequests.stream().map(
                    fr -> {
                        try {
                            Date harvestDate = formatDate.parse(fr.harvestDate);
                            fr.harvestDate = formatString.format(harvestDate);
                            if (fr.farmArrival != null) {
                                Date farmArrival = formatDateTime.parse(fr.farmArrival);
                                fr.farmArrival = formatStringDateTime.format(farmArrival);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return fr;
                    }).collect(Collectors.toList());

            this.fishingRQs = newFishingReq.stream().sorted(Comparator.comparing(lc -> String.format("%s:%s:%s", lc.cageCode, lc.farmArrival, lc.itinSNo)))
                    .map(x -> new GenericListModel(x.requestId,
                            String.format("%s, %s, %s, %s kg, %s", x.itinSNo, x.farmArrival != null ? x.farmArrival : x.harvestDate, x.cageCode, x.quantity, x.species),
                            "Split Request".equalsIgnoreCase(x.notes) ? GenericListModel.origin.Split : GenericListModel.origin.Normal)).toArray(GenericListModel[]::new);

            ArrayAdapter<GenericListModel> candidatesAdapter = new ArrayAdapter<GenericListModel>(this, R.layout.simple_list_checked_item_1, fishingRQs) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView tvItemContent = view.findViewById(android.R.id.text1);
                    tvItemContent.setTextSize(22);

                    GenericListModel currObj = this.getItem(position);
                    if (currObj.getType() == GenericListModel.origin.Split) {
                        String tmp = tvItemContent.getText().toString();
                        tvItemContent.setText(Html.fromHtml(tmp + "<b><font color='red'> NEO</font></b>", HtmlCompat.FROM_HTML_MODE_LEGACY));
                    }
                    return view;
                }
            };
            this.lvFishingRequests.setAdapter(candidatesAdapter);
            this.lvFishingRequests.setOnItemClickListener(this);
            this.harvestRQcnt = fishingRequests.size();
        }
        if (lastPosition > -1)
            this.lvFishingRequests.setItemChecked(lastPosition, true);
    }

    private void getYesterdayHarvestReq() {
        this.lvFishingRequests.setAdapter(null);
        List<FishingRequest> fishingRequests = db.fishingRequestsDAO().getYesterdayRecord();
        if (fishingRequests != null && !fishingRequests.isEmpty()) {
            List<FishingRequest> newFishingReq = fishingRequests.stream().map(
                    fr -> {
                        try {
                            Date harvestDate = formatDate.parse(fr.harvestDate);
                            fr.harvestDate = formatString.format(harvestDate);
                            if (fr.farmArrival != null) {
                                Date farmArrival = formatDateTime.parse(fr.farmArrival);
                                fr.farmArrival = formatStringDateTime.format(farmArrival);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return fr;
                    }).collect(Collectors.toList());
            //this.fishingRQs = fishingRequests.stream().map(x -> new GenericListModel(x.requestId, String.format("%s, %s, %s, %s kg, %s", x.itinSNo, x.harvestDate.substring(0, x.harvestDate.indexOf("T")), x.cageCode, x.reqQty, x.species))).toArray(GenericListModel[]::new);
            this.fishingRQs = newFishingReq.stream().sorted(Comparator.comparing(lc -> String.format("%s:%s:%s", lc.cageCode, lc.farmArrival, lc.itinSNo)))
                    .map(x -> new GenericListModel(x.requestId, String.format("%s, %s, %s, %s kg, %s", x.itinSNo, x.farmArrival != null ? x.farmArrival : x.harvestDate, x.cageCode, x.quantity, x.species))).toArray(GenericListModel[]::new);

            ArrayAdapter<GenericListModel> candidatesAdapter = new ArrayAdapter<GenericListModel>(this, R.layout.simple_list_checked_item_1, fishingRQs) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text = view.findViewById(android.R.id.text1);
                    text.setTextSize(22);
                    return view;
                }
            };
            this.lvFishingRequests.setAdapter(candidatesAdapter);
            this.lvFishingRequests.setOnItemClickListener(this);
            this.harvestRQcnt = fishingRequests.size();
        }
    }

    private void getPreviousHarvestReq() {
        this.lvFishingRequests.setAdapter(null);
        List<FishingRequest> fishingRequests = db.fishingRequestsDAO().getPreviousRecord();
        if (fishingRequests != null && !fishingRequests.isEmpty()) {
            List<FishingRequest> newFishingReq = fishingRequests.stream().map(
                    fr -> {
                        try {
                            Date harvestDate = formatDate.parse(fr.harvestDate);
                            fr.harvestDate = formatString.format(harvestDate);
                            if (fr.farmArrival != null) {
                                Date farmArrival = formatDateTime.parse(fr.farmArrival);
                                fr.farmArrival = formatStringDateTime.format(farmArrival);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return fr;
                    }).collect(Collectors.toList());
            this.fishingRQs = newFishingReq.stream().sorted(Comparator.comparing(lc -> String.format("%s:%s:%s", lc.cageCode, lc.farmArrival, lc.itinSNo)))
                    .map(x -> new GenericListModel(x.requestId, String.format("%s, %s, %s, %s kg, %s", x.itinSNo, x.farmArrival != null ? x.farmArrival : x.harvestDate, x.cageCode, x.quantity, x.species))).toArray(GenericListModel[]::new);

            ArrayAdapter<GenericListModel> candidatesAdapter = new ArrayAdapter<GenericListModel>(this, R.layout.simple_list_checked_item_1, fishingRQs) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text = view.findViewById(android.R.id.text1);
                    text.setTextSize(22);
                    return view;
                }
            };
            //
            this.lvFishingRequests.setAdapter(candidatesAdapter);
            this.lvFishingRequests.setOnItemClickListener(this);
            this.harvestRQcnt = fishingRequests.size();
        }
    }

    private String validate() {
        if (IsDemo) return "";
        StringBuilder sb = new StringBuilder();
        if (this.harvestRQcnt == 0) {
            sb.append(getString(R.string.no_harvest));
        } else if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(recFishing.speciesName)) {
                sb.append(getString(R.string.select_harvest));
            }
        }

        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}