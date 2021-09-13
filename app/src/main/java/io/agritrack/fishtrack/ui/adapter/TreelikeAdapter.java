package io.agritrack.fishtrack.ui.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.ui.login.api.SiteInfo;

public class TreelikeAdapter extends BaseExpandableListAdapter {

    private final Context mCtx;
    // child data in format of: <Type, List of children<Type>>
    private final Map<String, List<String>> mValues;
    private final List<String> keys;

    public TreelikeAdapter(Context context, Map<String, List<String>> listData) {
        this.mCtx = context;
        this.mValues = listData;
        this.keys = new ArrayList<>(this.mValues.keySet());
    }

    @Override
    public int getGroupCount() {
        return this.mValues != null ? this.mValues.size() : 0;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (this.mValues == null) {
            return 0;
        }
        return this.mValues.get(this.keys.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.keys.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        List<String> _sites = this.mValues.get(this.keys.get(groupPosition));
        return _sites.get(childPosition);
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
