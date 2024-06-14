package io.agritrack.philosofish.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.ui.adapter.FishCatchAdapter;

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
