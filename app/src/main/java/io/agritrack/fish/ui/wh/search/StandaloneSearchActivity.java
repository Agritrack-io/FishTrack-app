package io.agritrack.fish.ui.wh.search;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.android.gms.common.util.Strings;
import com.google.gson.Gson;

import java.util.Date;

import io.agritrack.R;
import io.agritrack.data.model.wh.Asset;
import io.agritrack.ui.service.LocalPreferences;

public class StandaloneSearchActivity extends SearchActivity {

    private static final String ASSET_CODE = "ASSET_CODE";
    private static final String ASSET = "ASSET";
    private static final String ERROR = "ERROR";
    private static final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!validateJwtToken()) {
            Intent intent = new Intent();
            intent.putExtra(ERROR, "[FISHTRACK]: User not authenticated. Please try logging into the application first.");
            setResult(RESULT_CANCELED, intent);
            finish();
        }

        spAssetType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedAssetType = parent.getItemAtPosition(position).toString(); //this is your selected item
                code = schemeSvc.codeOf(selectedAssetType);
                tvHeaders.setText(selectedAssetType.equalsIgnoreCase("HARVEST_BIN") || selectedAssetType.equalsIgnoreCase("PLATFORM") ?
                        getString(R.string.header_search_bin_platform) : (selectedAssetType.equalsIgnoreCase("NET") ? getString(R.string.header_search_net) : getString(R.string.header_search_cage)));
                loadAssetsByTypeFromLocalDB(selectedAssetType);
                adapterAssets.clearSelectedValue();
                etAssetBarcode.setText("");
                pbProximity.setProgress(0);
                tvProximity.setText(R.string.proximity);
                if (adapterAssets == null) {
                    svSearchAsset.setVisibility(View.GONE);
                } else {
                    svSearchAsset.setVisibility(View.VISIBLE);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Bundle extras = getIntent().getExtras();
        svSearchAsset.setQuery(extras.getString(ASSET_CODE, ""), true);
        svSearchAsset.setIconified(false);

    }

    @Override
    protected void configFooter() {
        ImageView ivBack = findViewById(R.id.ivBackToWhMenu);
        ivBack.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        String selectedValue = adapterAssets.getSelectedValue();
        Asset asset = assetsList.stream()
                .filter(x->x.rfid.substring(x.rfid.length()-10).equals(selectedValue))
                .findFirst().orElse(null);
        if(asset != null) {
            data.putExtra(ASSET, gson.toJson(asset));
        }
        setResult(RESULT_OK, data);
        finish();
    }

    private boolean validateJwtToken() {
        try {
            String authToken = LocalPreferences.getToken();
            DecodedJWT jwt = JWT.decode(authToken);
            if( jwt.getExpiresAt().before(new Date())) {
                return false;
            }
            return true;
        } catch (Exception e) {
            Log.e("Invalid JWT: {}", e.getMessage());
        }

        return false;
    }
}
