package io.agritrack.dialog;

import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.lifecycle.MutableLiveData;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import io.agritrack.FishTrackApplication;
import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.api.sync.SyncApi;
import io.agritrack.api.sync.SyncAssetsCallBack;
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
import io.agritrack.data.dto.wh.AssetDTO;
import io.agritrack.data.dto.wh.FoodSkuDTO;
import io.agritrack.data.model.HarvestRequest;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;

public class SyncAssetDialog implements AdapterView.OnItemClickListener{
    private TextView tvTitle;
    private ListView lvAssetsToSync;
    private Button btnOk;
    private final MutableLiveData<String> syncResult = new MutableLiveData<>();
    private final Activity activity;
    private Dialog dialog;

    public SyncAssetDialog(Activity activity) {
        this.activity = activity;
        SyncApi syncService = APIServiceGenerator.createAPI(SyncApi.class);
        String token = LocalPreferences.getToken();
        UUID siteId = LocalPreferences.getCurrentSiteId();
        String clusterId = LocalPreferences.getCurrentClusterId();

        setDialog();
        findViews();

        String[] assetList = {SyncAssetDialog.this.activity.getString(R.string.menu_title_cage), SyncAssetDialog.this.activity.getString(R.string.menu_title_net)};

        ArrayAdapter<String> hrAdapter = new ArrayAdapter<String>(activity, R.layout.simple_list_checked_item_1, assetList){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                text.setTextSize(22);
                return view;
            }
        };
        this.lvAssetsToSync.setAdapter(hrAdapter);
        this.lvAssetsToSync.setOnItemClickListener(this);

        // define if single or multiple choice mode will be used to display the checkboxes.
        this.lvAssetsToSync.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        btnOk.setOnClickListener(view -> {
            SparseBooleanArray checked = lvAssetsToSync.getCheckedItemPositions();
            for (int i = 0; i < assetList.length; i++) {
                if (checked.get(i) == true) {
                    Object o = hrAdapter.getItem(i);
                    String name = o.toString();
                    // if the arraylist does not contain the name, add it
                    if (name.equalsIgnoreCase(SyncAssetDialog.this.activity.getString(R.string.menu_title_net))){
                        // sync only Nets assets
                        Call<List<AssetDTO>> syncNetsAsyncCall = syncService.getAssetsByNetType("Bearer " + token);
                        syncNetsAsyncCall.enqueue(new SyncAssetsCallBack(this.syncResult));
                    } else if (name.equalsIgnoreCase(SyncAssetDialog.this.activity.getString(R.string.menu_title_cage))){
                        // sync only Cages assets
                        Call<List<AssetDTO>> syncCagesAsyncCall = syncService.getAssetsByCageType("Bearer " + token);
                        syncCagesAsyncCall.enqueue(new SyncAssetsCallBack(this.syncResult));
                    }
                }
            }
            dismiss();
            for (int i=0; i < 4; i++) {
                CToast(activity.getApplicationContext(), render(SyncAssetDialog.this.activity.getString(R.string.sync_asset_message_2)), Toast.LENGTH_LONG);
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
        dialog.setContentView(R.layout.sync_asset_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void findViews() {
        tvTitle = dialog.findViewById(R.id.tv_title);
        lvAssetsToSync = dialog.findViewById(R.id.lvAssetsToSync);
        btnOk = (Button) dialog.findViewById(R.id.btnOk);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
