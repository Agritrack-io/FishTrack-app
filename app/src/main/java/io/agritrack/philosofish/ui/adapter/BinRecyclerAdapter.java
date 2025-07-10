package io.agritrack.philosofish.ui.adapter;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.model.wh.Asset;
import io.agritrack.philosofish.fish.ui.bo.GenericListModel;

public class BinRecyclerAdapter extends RecyclerView.Adapter<BinRecyclerAdapter.MyViewHolder> {
    private final LayoutInflater mLayoutInflater;
    public boolean isClickable = true;
    private List<String> mList;
    private boolean isEPC = true;
    private MutableLiveData<String> liveItem;
    private ArrayList<GenericListModel> arrayListFiltered;
    private int selectedPos = RecyclerView.NO_POSITION;
    private String selectedValue = null;
    private String selectedLabel = null;
    private MobileDB db;

    public BinRecyclerAdapter(Context context, List<String> values) {
        this(context, values, true);
    }

    public BinRecyclerAdapter(Context context, List<String> values, boolean isEPC) {
        this.mList = values;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.isEPC = isEPC;
        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());
    }

    public String getSelectedValue() {
        return this.selectedValue;
    }

    public String getSelectedLabel() {
        return this.selectedLabel;
    }

    public void clearSelectedValue() {
        selectedPos = RecyclerView.NO_POSITION;
        this.selectedValue = null;
    }

    public List<String> getValues() {
        return mList;
    }

    public void setValues(List<String> values) {
        this.mList = values;
    }

    public void setItemObserver(MutableLiveData<String> mld) {
        this.liveItem = mld;
    }

    public void addUniqueItem(String val) {
        if (!this.mList.contains(val)) {
            this.mList.add(val);
        }
    }

    public void addItem(String val) {
        this.mList.add(val);
    }

    public void removeItem(String val) {
        this.mList.remove(val);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.simple_filterable_view_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int position) {
        mList.sort(Comparator.comparing(o -> o.substring(o.length() - 10)));

        if (position == 0) {
            viewHolder.rfid.setText(Html.fromHtml("<b>" + "<font color='#16325c'>"
                            + getAppContext().getResources().getString(R.string.barcode) + "</font>" + "</b>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY));
            viewHolder.code.setText(Html.fromHtml("<b>" + "<font color='#16325c'>"
                            + getAppContext().getResources().getString(R.string.code) + "</font>" + "</b>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else if (position > 0 && position <= getItemCount() - 1) {
            position = position - 1;
            String epc = mList.get(position);
            if (epc.length() >= 24) {
                Asset selectedAsset = db.assetDAO().getAssetByEpc(epc);
                if (selectedAsset != null) {
                    viewHolder.code.setText(selectedAsset.code);
                }
            }
            if (epc != null) {
                String tag = isEPC ? epc.substring(epc.length() - 10) : epc;
                viewHolder.itemView.setSelected(selectedPos == position + 1);
                viewHolder.itemView.setBackgroundColor(selectedPos == position + 1 ? Color.GRAY : Color.TRANSPARENT);
                viewHolder.rfid.setText(tag);
            }
//            viewHolder.netEye.setVisibility(View.GONE);
//            viewHolder.perimeter.setVisibility(View.GONE);
            viewHolder.itemView.setSelected(selectedPos == position + 1);
            viewHolder.itemView.setBackgroundColor(selectedPos == position + 1 ? Color.GRAY : Color.TRANSPARENT);
        }
    }


    @Override
    public int getItemCount() {
        return mList != null ? mList.size() + 1 : 0;
    }

    public void removeAll() {
        this.mList.clear();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView rfid, code, perimeter, netEye;// tvItemName, tvItemSNo;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

//            tvItemName = itemView.findViewById(R.uid.tvRecyclerItem);
//            tvItemSNo = itemView.findViewById(R.uid.tvRecyclerItemSNo);

            rfid = itemView.findViewById(R.id.tvRfid);
            code = itemView.findViewById(R.id.tvCode);
            perimeter = itemView.findViewById(R.id.tvPerimeter);
            netEye = itemView.findViewById(R.id.tvNetEye);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Below line is just like a safety check, because sometimes holder could be null,
            // in that case, getAdapterPosition() will return RecyclerView.NO_POSITION
            if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

            if (selectedPos == getAdapterPosition()) {
                selectedPos = RecyclerView.NO_POSITION;
                selectedValue = null;
                selectedLabel = null;
                notifyDataSetChanged();
                return;
            }

            // Updating old as well as new positions
            notifyItemChanged(selectedPos);
            selectedPos = getAdapterPosition();
            selectedValue = !Strings.isEmptyOrWhitespace(this.rfid.getText().toString()) ? mList.get(selectedPos - 1) : this.code.getText().toString();
            selectedLabel = selectedValue.length() > 10 ? selectedValue.substring(selectedValue.length() - 10) : selectedValue;
            notifyItemChanged(selectedPos);
            if (liveItem != null) {
                liveItem.setValue(selectedValue);
            }

            // Do your another stuff for your onClick
        }
    }
}
