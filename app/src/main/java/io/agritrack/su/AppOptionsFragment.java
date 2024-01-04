package io.agritrack.su;

import static io.agritrack.FishTrackApplication.getAppContext;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ToggleButton;

import androidx.fragment.app.DialogFragment;

import com.google.android.gms.common.util.Strings;

import io.agritrack.AgritrackProducts;
import io.agritrack.FishTrackApplication;
import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.ui.config.ConfigActivity;
import io.agritrack.ui.service.LocalPreferences;


public class AppOptionsFragment extends DialogFragment {
    public static String TAG = "CaenLoggerDialogFragment";
    private int check = 0;
    private Spinner spProducts;
    private Button btnSiteSelection, btnRT0012, btnImportRT0012, btnDelCfg, btnTruncDB;
    private ToggleButton tbEnvironment;
    private String selectedProduct;

    private final View.OnClickListener btSiteSelectionClickListener = v -> gotoSiteSelection(v);

    private final View.OnClickListener btDelCfgClickListener = v -> delCfg(v);

    private final View.OnClickListener btTruncDBClickListener = v -> trunLocalDB(v);


    public AppOptionsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AppOptionsFragment.
     */
    public static AppOptionsFragment newInstance() {
        AppOptionsFragment fragment = new AppOptionsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_app_options, container, false);

        spProducts = rootView.findViewById(R.id.spProducts);
        btnSiteSelection = rootView.findViewById(R.id.btnSiteSelection);
        btnRT0012 = rootView.findViewById(R.id.btnRT0012);
        btnImportRT0012 = rootView.findViewById(R.id.btnImportRT0012);
        btnDelCfg = rootView.findViewById(R.id.btnDelCfg);
        btnTruncDB = rootView.findViewById(R.id.btnTruncDB);
        tbEnvironment = rootView.findViewById(R.id.tbEnvironment);


        // set onClick listeners for the menu buttons
        btnSiteSelection.setOnClickListener(btSiteSelectionClickListener);
        btnDelCfg.setOnClickListener(btDelCfgClickListener);
        btnTruncDB.setOnClickListener(btTruncDBClickListener);
        tbEnvironment.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // The toggle is enabled
            } else {
                // The toggle is disabled
            }
        });

        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter productsAdapter = new ArrayAdapter(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, AgritrackProducts.values());
        productsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Setting the ArrayAdapter data on the Spinner
        spProducts.setAdapter(productsAdapter);

        // fetch previously selected product.
        selectedProduct = FishTrackApplication.getProduct();
        //spProducts.setSelection(2);
        if(!Strings.isEmptyOrWhitespace(selectedProduct)) {
            spProducts.setSelection(productsAdapter.getPosition(AgritrackProducts.valueOf(selectedProduct)));
        }

        //Getting the instance of Spinner and applying OnItemSelectedListener on it
        spProducts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (++check > 1) {
                    selectedProduct = parent.getItemAtPosition(position).toString();

                    AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(getActivity())
                            .setTitle("Change Product")
                            .setMessage(String.format("App context will switch to %s.\nAre you sure?", selectedProduct))
                            .setPositiveButton("Yes", (dialog, which) -> {
                                FishTrackApplication.setProduct(selectedProduct);
                                dismiss(); })
                            .setNegativeButton("No", null);
                    dlgBuilder.show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        return rootView;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
        super.onDismiss(dialog);
    }

    public void gotoSiteSelection(View v) {
        Intent i = new Intent(getActivity(), ConfigActivity.class);
        i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // disables back button...
        startActivity(i);
        dismiss();
    }

    public void delCfg(View v) {
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(getActivity())
                .setTitle("Erasing Local Cache")
                .setMessage("Are you sure you want to erase local cache?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    LocalPreferences.Reset();
                    dismiss();
                })
                .setNegativeButton("No", null);
        dlgBuilder.show();
    }

    public void trunLocalDB(View v) {
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(getActivity())
                .setTitle("Erasing Local DB")
                .setMessage("Are you sure you want to empty local database?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // get an instance of local DB
                    MobileDB db = MobileDB.getInstance(getAppContext());
                    db.clearAllTables();
                    dismiss();
                })
                .setNegativeButton("No", null);
        dlgBuilder.show();
    }
}