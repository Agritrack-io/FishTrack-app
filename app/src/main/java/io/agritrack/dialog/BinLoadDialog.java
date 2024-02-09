package io.agritrack.dialog;

import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import io.agritrack.kefalonia.R;
import io.agritrack.ui.adapter.BinLoadAdapter;
import io.agritrack.ui.adapter.FishCatchAdapter;

public class BinLoadDialog {

    private final Context context;
    private final String binEpc;
    private TextView tvTitle;
    private ImageView ivCancel;
    private RecyclerView rvWeightBatchesBin;
    private FishCatchAdapter fishCatchAdapter;
    private Dialog dialog;

    public BinLoadDialog(Context context, String binEpc) {
        this.context = context;
        this.binEpc = binEpc;

        setDialog();
        findViews();
        SetCaptions(binEpc);

        FishCatchAdapter.FishCatchItem fishCatchItem = new FishCatchAdapter.FishCatchItem();

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        rvWeightBatchesBin.setLayoutManager(layoutManager);
        rvWeightBatchesBin.setItemAnimator(new DefaultItemAnimator());
        rvWeightBatchesBin.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        fishCatchAdapter = new FishCatchAdapter(context, new ArrayList<FishCatchAdapter.FishCatchItem>(Collections.singletonList(fishCatchItem)));
        rvWeightBatchesBin.setAdapter(fishCatchAdapter);
        rvWeightBatchesBin.setNestedScrollingEnabled(false);

        ivCancel.setOnClickListener(v ->
                dismiss()
        );

    }

    public void showDialog() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    private void setDialog() {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bin_load_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void findViews() {
        tvTitle = dialog.findViewById(R.id.tv_title);
        ivCancel = (ImageView) dialog.findViewById(R.id.ivCancel);
        rvWeightBatchesBin = (RecyclerView) dialog.findViewById(R.id.rvWeightBatchesBin);
    }

    private void SetCaptions(String title) {
        tvTitle.setText(title);
    }
}
