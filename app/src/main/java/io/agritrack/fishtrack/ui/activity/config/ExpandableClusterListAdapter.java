package io.agritrack.fishtrack.ui.activity.config;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.ui.activity.login.api.SiteInfo;

public class ExpandableClusterListAdapter extends BaseExpandableListAdapter {

    private final Context mCtx;
    private final List<String> mClusters; // Cluster titles
    // child data in format of Cluster title, Site title
    private final Map<String, List<SiteInfo>> mSites;

    public ExpandableClusterListAdapter(Context context, List<String> listClustersData, Map<String, List<SiteInfo>> listSitesData) {
        this.mCtx = context;
        this.mClusters = listClustersData;
        this.mSites = listSitesData;
    }

    @Override
    public int getGroupCount() {
        return this.mClusters != null ? this.mClusters.size() : 0;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (this.mSites == null || this.mClusters == null) {
            return 0;
        }
        return this.mSites.get(this.mClusters.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.mClusters.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.mSites.get(this.mClusters.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String clusterName = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.cluster_layout, null);
        }

        TextView lblListHeader = convertView.findViewById(R.id.tvClusterHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(clusterName);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final SiteInfo child = (SiteInfo) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.site_layout, null);
        }
        TextView txtListChild = convertView.findViewById(R.id.tvSiteName);
        txtListChild.setText(child.getName());
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
