package io.agritrack.fish.ui.wh.correlation;

import static java.security.AccessController.getContext;

import static io.agritrack.common.LargeString.render;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.agritrack.FishTrackApplication;
import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.api.sync.SyncApi;
import io.agritrack.api.sync.SyncBinInfo;
import io.agritrack.api.sync.SyncCageDetailsCallBack;
import io.agritrack.api.sync.SyncClusterSitesCallBack;
import io.agritrack.api.sync.SyncCustomersCallBack;
import io.agritrack.api.sync.SyncEmployeesCallBack;
import io.agritrack.api.sync.SyncFoodSkuCallBack;
import io.agritrack.api.sync.SyncHarvestRequestCallBack;
import io.agritrack.api.sync.SyncIOTLoggersCallBack;
import io.agritrack.api.sync.SyncSpeciesCallBack;
import io.agritrack.api.sync.SyncSuppliersCallBack;
import io.agritrack.api.sync.SyncUsersCallBack;
import io.agritrack.data.dto.AppUserDTO;
import io.agritrack.data.dto.BinInfoDTO;
import io.agritrack.data.dto.CageDetailsDTO;
import io.agritrack.data.dto.HarvestRequestDTO;
import io.agritrack.data.dto.SiteDTO;
import io.agritrack.data.dto.common.CustomerDTO;
import io.agritrack.data.dto.common.EmployeeDTO;
import io.agritrack.data.dto.common.IotLoggerDTO;
import io.agritrack.data.dto.common.SpeciesDTO;
import io.agritrack.data.dto.common.SupplierDTO;
import io.agritrack.data.dto.wh.FoodSkuDTO;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.SyncAssetDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.FishHomeActivity;
import io.agritrack.fish.ui.WhMenuActivity;
import io.agritrack.ui.adapter.HomeMenuAdapter;
import io.agritrack.ui.adapter.MenuItem;
import io.agritrack.ui.login.LoginActivity;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;

public class CorrelationMenuActivity extends AppCompatActivity {

    private static final int Cage_Idx = 0, Net_Idx = 1, Cage_Net_Idx = 2, Bin_Idx = 3, Platform_Idx = 4;
    GridView gvCorrelationMenu;
    private ImageView ivSupport;
    private SupportDialog supportDialog;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correlation_menu);
        gvCorrelationMenu = findViewById(R.id.gvCorrelationMenu);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderCorrelationMenu);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(CorrelationMenuActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        ArrayList<MenuItem> menuItemsList = new ArrayList<MenuItem>();
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_cage), CorrelationCageActivity.class, R.drawable.cage));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_net), CorrelationNetActivity.class, R.drawable.net));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_cage_net), CorrelationCageNetActivity.class, R.drawable.cage_net));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_bin), CorrelationBinActivity.class, R.drawable.bin));
        menuItemsList.add(new MenuItem(getString(R.string.platform), CorrelationPlatformActivity.class, R.drawable.platform));

        HomeMenuAdapter adapter = new HomeMenuAdapter(this, menuItemsList);
        gvCorrelationMenu.setAdapter(adapter);

        gvCorrelationMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final Context appCtx = getApplicationContext();
                Intent i = new Intent(appCtx, LoginActivity.class);

                switch (position) {
                    case Cage_Idx:
                        GlobalState.initWHCorrelationRecord();
                        i = new Intent(appCtx, CorrelationCageActivity.class);
                        break;
                    case Net_Idx:
                        GlobalState.initWHCorrelationRecord();
                        i = new Intent(appCtx, CorrelationNetActivity.class);
                        break;
                    case Cage_Net_Idx:
                        GlobalState.initWHCorrelationRecord();
                        i = new Intent(appCtx, CorrelationCageNetActivity.class);
                        break;
                    case Bin_Idx:
                        GlobalState.initWHCorrelationRecord();
                        i = new Intent(appCtx, CorrelationBinActivity.class);
                        break;
                    case Platform_Idx:
                        GlobalState.initWHCorrelationRecord();
                        i = new Intent(appCtx, CorrelationPlatformActivity.class);
                        break;
                    default:
                }

                // Pass image index
                i.putExtra("id", position);
                startActivity(i);
            }
        });

        ivSupport = findViewById(R.id.ivSupport);
        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(CorrelationMenuActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToWhMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });
    }
}