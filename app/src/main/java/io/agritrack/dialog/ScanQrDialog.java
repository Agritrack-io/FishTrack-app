package io.agritrack.dialog;

import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.UUID;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.api.sync.SyncApi;
import io.agritrack.api.sync.SyncAssetsCallBack;
import io.agritrack.data.dto.wh.AssetDTO;
import io.agritrack.fish.ui.FishHomeActivity;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;

public class ScanQrDialog implements AdapterView.OnItemClickListener{
    private TextView tvTitle;
    private ImageView ivQrcode;;
    private Button btnOk;
    private final Activity activity;
    private Dialog dialog;

    public ScanQrDialog(Activity activity, Bitmap bitmap) {
        this.activity = activity;

        setDialog();
        findViews();
        ivQrcode.setImageBitmap(bitmap);

        btnOk.setOnClickListener(view -> {
            Intent i = new Intent(activity.getApplicationContext(), FishHomeActivity.class);
            activity.startActivity(i);
            dismiss();
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
        dialog.setContentView(R.layout.scan_qr_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void findViews() {
        tvTitle = dialog.findViewById(R.id.tv_title);
        ivQrcode = dialog.findViewById(R.id.ivQrcode);
        btnOk = (Button) dialog.findViewById(R.id.btnOk);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
