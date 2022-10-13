package io.agritrack.ui.adapter;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import io.agritrack.R;
import io.agritrack.common.Filters;
import io.agritrack.data.service.EncodingSchemeService;

public class TreelikeAdapter extends BaseExpandableListAdapter {
    private static final EncodingSchemeService schemeSvc = EncodingSchemeService.getInstance();

    private final Context mCtx;
    // child data in format of: <Type, List of children<Type>>
    private TreeMap<String, List<String>> mValues = new TreeMap<>();
    private List<String> keys;

    public TreelikeAdapter(Context context, Map<String, List<String>> listData) {
        this.mCtx = context;
        this.mValues.putAll(listData);
        this.keys = new LinkedList<>(this.mValues.keySet());
    }

    @Override
    public int getGroupCount() {
        return this.mValues != null ? this.mValues.size() : 0;
    }

    public long getItemsCount() {
        return mValues.values()
                .stream()
                .flatMap(Collection::stream).count();
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
        if (_sites.get(childPosition).length()>=24) {
            return _sites.get(childPosition).substring(14);
        } else {
            return _sites.get(childPosition);
        }
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

        TextView lbClusterDescription = convertView.findViewById(R.id.tvClusterDescription);
        lbClusterDescription.setText(getAssetTypeName(clusterName));
        TextView lbClusterSize = convertView.findViewById(R.id.tvClusterSize);
        lbClusterSize.setText(getChildrenCount(groupPosition) + "");

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String child = (String) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.site_layout, null);
        }
        TextView txtListChild = convertView.findViewById(R.id.tvSiteName);
        txtListChild.setText(child);
        return convertView;
    }

    public Map<String, List<String>> getValues() {
        return mValues;
    }

    public void setValues(Map<String, List<String>> items) {
        this.mValues = new TreeMap<>();
        this.mValues.putAll(items);
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private String getAssetTypeName(String type) {
        return schemeSvc.nameOf(type);
    }

    public void appendItems(Map<String, List<String>> values) {
        for (String key : values.keySet()) {
            if (!this.mValues.containsKey((key))) {
                this.mValues.put(key, values.get(key));
            } else {
                Set<String> tmp = new TreeSet<>(this.mValues.get(key));
                tmp.addAll(values.get(key));
                this.mValues.put(key, new ArrayList<>(tmp));
            }
        }
        this.keys = new ArrayList<>(this.mValues.keySet());
    }

    public void removeItem(int parentPosition, int childPosition) {
        String key = this.keys.get(parentPosition);
        List<String> children = this.mValues.get(key);
        children.remove(childPosition);

        if(getChildrenCount(parentPosition)==0){
            this.mValues.remove(key);
            this.keys.remove(parentPosition);
        }
    }

    public void removeAll() {
        this.mValues.clear();
        this.keys.removeAll(this.mValues.keySet());
    }
}
