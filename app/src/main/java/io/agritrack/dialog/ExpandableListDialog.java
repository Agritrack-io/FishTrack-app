package io.agritrack.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.lifecycle.MutableLiveData;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.agritrack.R;
import io.agritrack.ui.config.ClusterListViewAdapter;
import io.agritrack.ui.login.api.SiteInfoRS;

public class ExpandableListDialog {

    private TextView tvTitle;
    private ExpandableListView xvClusters;
    private final ClusterListViewAdapter clustersAdapter;
    private List<String> clusterIDs;
    private MutableLiveData<SiteInfoRS> selectedSite;

    private final Activity activity;
    private Dialog dialog;

    public ExpandableListDialog(Activity activity, Map<String, List<SiteInfoRS>> data, MutableLiveData<SiteInfoRS> selection, @StringRes int title) {
        this.activity = activity;
        this.selectedSite = selection;

        setDialog();
        findViews();
        SetCaptions(title);
        clustersAdapter = new ClusterListViewAdapter(this.activity, data);
        clusterIDs = new LinkedList<>(data.keySet());

        // setting list adapter
        xvClusters.setAdapter(clustersAdapter);
        // since there Sites available, display them in  a list.
        xvClusters.setVisibility(View.VISIBLE);

        // Listview on child click listener
        xvClusters.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String clusterKey = clusterIDs.get(groupPosition);
                selectedSite.setValue(data.get(clusterKey).get(childPosition));

                return false;
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
        dialog.setContentView(R.layout.expandable_list_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void findViews() {
        tvTitle = dialog.findViewById(R.id.tv_title);
        xvClusters = dialog.findViewById(R.id.xvClusters);
    }

    private void SetCaptions(int title) {
        tvTitle.setText(title);
    }
}