package io.agritrack.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.agritrack.R;
import io.agritrack.ui.adapter.TemplateRecyclerAdapter;

public class SimpleListDialog {
    private TextView tvTitle;
    private RecyclerView rvItems;
    private final TemplateRecyclerAdapter itemsAdapter;
    private MutableLiveData<String> liveItem;
    private String selectedItem;

    private final Activity activity;
    private Dialog dialog;

    private final View.OnClickListener itemsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ConstraintLayout view = (ConstraintLayout) v;
            TextView tvRecyclerItem = view.findViewById(R.id.tvRecyclerItem);
            selectedItem = tvRecyclerItem.getText().toString();

            liveItem.setValue(itemsAdapter.getSelectedValue());
        }
    };

    public SimpleListDialog(Activity activity, List<String> data, MutableLiveData<String> selection, @StringRes int title) {
        this.activity = activity;
        this.liveItem = selection;


        setDialog();
        findViews();
        SetCaptions(title);

        // setting list adapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.activity);
        rvItems.setLayoutManager(layoutManager);
        rvItems.setItemAnimator(new DefaultItemAnimator());
        rvItems.addItemDecoration(new DividerItemDecoration(this.activity, DividerItemDecoration.VERTICAL));
        itemsAdapter = new TemplateRecyclerAdapter(this.activity, data);
        rvItems.setAdapter(itemsAdapter);
        rvItems.setNestedScrollingEnabled(false);
        // since there Sites available, display them in  a list.
        rvItems.setVisibility(View.VISIBLE);
        liveItem.setValue(itemsAdapter.getSelectedValue());
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
