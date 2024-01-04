package io.agritrack.dialog;

import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.agritrack.R;

public class PhotoDialog {

    private final MutableLiveData<Bitmap> liveItem;
    private final Activity activity;
    private final String binEpc;
    private TextView tvTitle;
    private ImageView ivPhoto;
    private Button btnOk, btnCancel;
    private Dialog dialog;

    public PhotoDialog(Activity activity, MutableLiveData<Bitmap> selection, String binEpc, @StringRes int title) {
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
        fileName = String.format("Photo_%s_%s.png", binEpc, sdf.format(currentDate));
        // String temp = null;

        File path = new File(String.valueOf(activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)));
        if (!path.isFile()) {
            if (!(path.isDirectory())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        Files.createDirectory(Paths.get(path.getAbsolutePath()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    path.mkdir();
                }
            }
        }
        File file = new File(path, fileName);

        if (file.exists()) {
            file.delete();
            file = new File(path, fileName);

        }

        try {
            outStream = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, outStream);
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
