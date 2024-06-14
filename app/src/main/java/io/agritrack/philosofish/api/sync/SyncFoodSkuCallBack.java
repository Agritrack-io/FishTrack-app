package io.agritrack.philosofish.api.sync;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.wh.FoodSkuDTO;
import retrofit2.Call;
import retrofit2.Response;

public class SyncFoodSkuCallBack extends BaseSyncCallBack<List<FoodSkuDTO>> {
    public SyncFoodSkuCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<FoodSkuDTO>> call, Response<List<FoodSkuDTO>> response) {
        List<FoodSkuDTO> rs = response.body();

        if (rs != null) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());

            for (FoodSkuDTO foodSkuDTO : rs) {
                db.foodSkuDAO().insert(FoodSkuDTO.convert(foodSkuDTO));
            }
            // Food sku sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.food_sku_sync_completed));
        } else {
            // no Food sku found
            syncResult.setValue(getAppContext().getString(R.string.no_food_sku_found_alert));
        }
    }
}