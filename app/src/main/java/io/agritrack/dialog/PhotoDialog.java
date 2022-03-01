package io.agritrack.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.lifecycle.MutableLiveData;

import io.agritrack.R;
import io.agritrack.fish.ui.quality.receipt.ReceiptQualityInfoActivity;
import io.agritrack.hotel.ui.inventory.HotelInventoryLinenActivity;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoDialog {

    private TextView tvTitle;
    private ImageView ivPhoto;
    private Button btnOk, btnCancel;
    private MutableLiveData<Bitmap> liveItem;
    private final Activity activity;
    private Dialog dialog;
    private String binEpc;

    public PhotoDialog (Activity activity, MutableLiveData<Bitmap> selection, String binEpc, @StringRes int title) {
        this.activity = activity;
        this.liveItem = selection;
        this.binEpc = binEpc;

        setDialog();
        findViews();
        SetCaptions(title);

        ivPhoto.setImageBitmap(liveItem.getValue());
        ivPhoto.setVisibility(View.VISIBLE);

        btnCancel.setOnClickListener(view -> {
            liveItem.setValue(null);
            dismiss();
            CToast(activity.getApplicationContext(), render("Your photo wasn't saved"), Toast.LENGTH_LONG);
        });

        btnOk.setOnClickListener(view -> {
            File file = savebitmap(liveItem.getValue());
            dismiss();
            CToast(activity.getApplicationContext(), render("Your photo was saved locally"), Toast.LENGTH_LONG);
        });
    }

    private File savebitmap(Bitmap bmp) {
        Date currentDate = new Date();
        String compactTSFormat = "yyyyMMddHHmmss";
        SimpleDateFormat sdf = new SimpleDateFormat(compactTSFormat);
        OutputStream outStream = null;
        String fileName = null;
        // create the local jpeg file name
        fileName = String.format("Photo_%s_%s.jpeg", binEpc, sdf.format(currentDate));
        // String temp = null;
        File file = new File(activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
        if (file.exists()) {
            file.delete();
            file = new File(activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);

        }

        try {
            outStream = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return file;
    }


    public void showDialog() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    private void setDialog() {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.photo_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void findViews() {
        tvTitle = dialog.findViewById(R.id.tv_title);
        ivPhoto = (ImageView) dialog.findViewById(R.id.ivPhoto);
        btnOk = (Button) dialog.findViewById(R.id.btnOk);
        btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
    }

    private void SetCaptions(int title) {
        tvTitle.setText(title);
    }
}
