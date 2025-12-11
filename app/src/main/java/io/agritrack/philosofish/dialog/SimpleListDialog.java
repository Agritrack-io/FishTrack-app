package io.agritrack.philosofish.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.ui.adapter.TemplateRecyclerAdapter;

public class SimpleListDialog {
    private final TemplateRecyclerAdapter itemsAdapter;
    private final Activity activity;
    private TextView tvTitle;
    private RecyclerView rvItems;

    private final MutableLiveData<String> selectedSite;
    private Dialog dialog;

    public SimpleListDialog(Activity activity, List<String> data, MutableLiveData<String> liveData, @StringRes int title) {
        this.activity = activity;
        this.selectedSite = liveData;


        setDialog();
        findViews();
        SetCaptions(title);

        // setting list adapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.activity);
        rvItems.setLayoutManager(layoutManager);
        rvItems.setItemAnimator(new DefaultItemAnimator());
        rvItems.addItemDecoration(new DividerItemDecoration(this.activity, DividerItemDecoration.VERTICAL));
        itemsAdapter = new TemplateRecyclerAdapter(this.activity, data.stream().map(x -> new TemplateRecyclerAdapter.BinEpc(x)).collect(Collectors.toList()), false);
        rvItems.setAdapter(itemsAdapter);
        rvItems.setNestedScrollingEnabled(false);

        itemsAdapter.setItemObserver(liveData);

        // since there Sites available, display them in  a list.
        rvItems.setVisibility(View.VISIBLE);

//        // Set item click callback
//        itemsAdapter.setOnItemClickListener(new TemplateRecyclerAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(String selectedValue) {
//                selectedSite.setValue(selectedValue);
//                dialog.dismiss();
//            }
//        });
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
        dialog.setContentView(R.layout.simple_list_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void findViews() {
        tvTitle = dialog.findViewById(R.id.tv_title);
        rvItems = dialog.findViewById(R.id.rvItems);
    }

    private void SetCaptions(int title) {
        tvTitle.setText(title);
    }
}
