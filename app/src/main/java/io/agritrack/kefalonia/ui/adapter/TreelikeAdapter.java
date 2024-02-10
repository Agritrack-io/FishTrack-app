package io.agritrack.kefalonia.ui.adapter;

import static io.agritrack.kefalonia.FishTrackApplication.getAppContext;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import io.agritrack.kefalonia.R;
import io.agritrack.kefalonia.data.db.MobileDB;
import io.agritrack.kefalonia.data.model.wh.Asset;
import io.agritrack.kefalonia.data.service.EncodingSchemeService;

public class TreelikeAdapter extends BaseExpandableListAdapter {
    private static final EncodingSchemeService schemeSvc = EncodingSchemeService.getInstance();

    private final Context mCtx;
    private MobileDB db;
    // child data in format of: <Type, List of children<Type>>
    private TreeMap<String, List<String>> mValues = new TreeMap<>();
    private List<String> keys;

    public TreelikeAdapter(Context context, Map<String, List<String>> listData) {
        this.mCtx = context;
        this.mValues.putAll(listData);
        this.keys = new LinkedList<>(this.mValues.keySet());
        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());
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
        return this.mValues.get(this.keys.get(groupPosition)).size() + 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.keys.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        List<String> children = this.mValues.get(this.keys.get(groupPosition));
        if (childPosition == 0) {
            return "";
        } else {
            childPosition = childPosition - 1;
            String selectedChild = children.get(childPosition);
            if (selectedChild.length() >= 24) {
                Asset selectedAsset = db.assetDAO().getAssetByEpc(selectedChild);
                if (selectedAsset != null) {
                    return selectedAsset.netEyeGirth != null && selectedAsset.netEyeGirth != 0.0 && selectedAsset.perimeter != null
                            ? String.format("%s/%s/%.2f/%.2f", selectedAsset.rfid.substring(14), selectedAsset.code, selectedAsset.perimeter, selectedAsset.netEyeGirth)
                            : ((selectedAsset.netEyeGirth != null && selectedAsset.netEyeGirth == 0.0) && selectedAsset.perimeter != null
                            ? String.format("%s/%s/%.2f", selectedAsset.rfid.substring(14), selectedAsset.code, selectedAsset.perimeter)
                            : selectedAsset.rfid.substring(14) + "/" + selectedAsset.code);
                } else {
                    return selectedChild.substring(14);
                }
            } else {
                return selectedChild;
            }
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
        lbClusterSize.setText(getChildrenCount(groupPosition) - 1 + "");

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String child = (String) getChild(groupPosition, childPosition);
        final String group = (String) getGroup(groupPosition);

        LayoutInflater inflater = (LayoutInflater) this.mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.simple_filterable_view_item, null);
        TextView txtRfid = convertView.findViewById(R.id.tvRfid);
        TextView txtCode = convertView.findViewById(R.id.tvCode);
        TextView txtPerimeter = convertView.findViewById(R.id.tvPerimeter);
        TextView txtNetEye = convertView.findViewById(R.id.tvNetEye);
        String[] childSplit = child.split("/");

        //the first row is used as header
        if (childPosition == 0) {
            if (group.equalsIgnoreCase("1410") || group.equalsIgnoreCase("1414")) {
                txtRfid.setText(Html.fromHtml("<b>" + "<font color='#16325c'>"
                                + getAppContext().getResources().getString(R.string.barcode) + "</font>" + "</b>",
                        HtmlCompat.FROM_HTML_MODE_LEGACY));
                txtCode.setText(Html.fromHtml("<b>" + "<font color='#16325c'>"
                                + getAppContext().getResources().getString(R.string.code) + "</font>" + "</b>",
                        HtmlCompat.FROM_HTML_MODE_LEGACY));
                txtPerimeter.setVisibility(View.GONE);
                txtNetEye.setVisibility(View.GONE);
            } else if (group.equalsIgnoreCase("1412")) {
                txtRfid.setText(Html.fromHtml("<b>" + "<font color='#16325c'>"
                                + getAppContext().getResources().getString(R.string.barcode) + "</font>" + "</b>",
                        HtmlCompat.FROM_HTML_MODE_LEGACY));
                txtCode.setText(Html.fromHtml("<b>" + "<font color='#16325c'>"
                                + getAppContext().getResources().getString(R.string.code) + "</font>" + "</b>",
                        HtmlCompat.FROM_HTML_MODE_LEGACY));
                txtPerimeter.setText(Html.fromHtml("<b>" + "<font color='#16325c'>"
                                + getAppContext().getResources().getString(R.string.perimeter) + "</font>" + "</b>",
                        HtmlCompat.FROM_HTML_MODE_LEGACY));
                txtNetEye.setVisibility(View.GONE);
            } else if (group.equalsIgnoreCase("1411")) {
                txtRfid.setText(Html.fromHtml("<b>" + "<font color='#16325c'>"
                                + getAppContext().getResources().getString(R.string.barcode) + "</font>" + "</b>",
                        HtmlCompat.FROM_HTML_MODE_LEGACY));
                txtCode.setText(Html.fromHtml("<b>" + "<font color='#16325c'>"
                                + getAppContext().getResources().getString(R.string.code) + "</font>" + "</b>",
                        HtmlCompat.FROM_HTML_MODE_LEGACY));
                txtPerimeter.setText(Html.fromHtml("<b>" + "<font color='#16325c'>"
                                + getAppContext().getResources().getString(R.string.perimeter) + "</font>" + "</b>",
                        HtmlCompat.FROM_HTML_MODE_LEGACY));
                txtNetEye.setText(Html.fromHtml("<b>" + "<font color='#16325c'>"
                                + getAppContext().getResources().getString(R.string.eye) + "</font>" + "</b>",
                        HtmlCompat.FROM_HTML_MODE_LEGACY));
            }
        }

        //Here is the ListView of the ChildView
        if (childPosition > 0 && childPosition <= getChildrenCount(groupPosition) - 1) {
            txtRfid.setText(childSplit[0]);
            if (group.equalsIgnoreCase("1410") || group.equalsIgnoreCase("1414")) {
                txtCode.setText(childSplit.length == 2 ? childSplit[1] : "");
                txtPerimeter.setVisibility(View.GONE);
                txtNetEye.setVisibility(View.GONE);
            } else if (group.equalsIgnoreCase("1412")) {
                txtCode.setText(childSplit.length == 2 || childSplit.length == 3 ? childSplit[1] : "");
                txtPerimeter.setText(childSplit.length == 3 ? childSplit[2] : "");
                txtNetEye.setVisibility(View.GONE);
            } else if (group.equalsIgnoreCase("1411")) {
                txtCode.setText(childSplit.length == 2 || childSplit.length == 3 || childSplit.length == 4 ? childSplit[1] : "");
                txtPerimeter.setText(childSplit.length == 3 || childSplit.length == 4 ? childSplit[2] : "");
                txtNetEye.setText(childSplit.length == 4 ? childSplit[3] : "");
            }
        }
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
        children.remove(childPosition - 1);

        if (getChildrenCount(parentPosition) == 1) {
            this.mValues.remove(key);
            this.keys.remove(parentPosition);
        }
    }

    public void removeAll() {
        this.mValues.clear();
        this.keys.removeAll(this.mValues.keySet());
    }
}
