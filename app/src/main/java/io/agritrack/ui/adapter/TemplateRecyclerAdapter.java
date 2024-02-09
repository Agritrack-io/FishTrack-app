package io.agritrack.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import io.agritrack.kefalonia.R;

public class TemplateRecyclerAdapter extends RecyclerView.Adapter<TemplateRecyclerAdapter.MyViewHolder> {
    private List<String> mList;
    private final LayoutInflater mLayoutInflater;
    public boolean isClickable = true;
    private boolean isEPC = true;
    private MutableLiveData<String> liveItem;
    private int selectedPos = RecyclerView.NO_POSITION;
    private String selectedValue = null;
    private String selectedLabel = null;

    public TemplateRecyclerAdapter(Context context, List<String> values) {
        this(context,values,true);
    }

    public TemplateRecyclerAdapter(Context context, List<String> values, boolean isEPC) {
        this.mList = values;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.isEPC = isEPC;
    }

    public String getSelectedValue(){
        return this.selectedValue;
    }

    public String getSelectedLabel(){
        return this.selectedLabel;
    }

    public void clearSelectedValue(){
        selectedPos = RecyclerView.NO_POSITION;
        this.selectedValue = null;
    }

    public List<String> getValues() {
        return mList;
    }

    public void setValues(List<String> values) {
        this.mList = values;
    }

    public void setItemObserver(MutableLiveData<String> mld){
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
        View view = mLayoutInflater.inflate(R.layout.simple_recycler_view_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        mList.sort(Comparator.comparing(o -> o.substring(o.length() - 10)));
        String epc = mList.get(position);
        if (epc != null) {
            String tag = isEPC ? epc.substring(epc.length() - 10) : epc;
            holder.itemView.setSelected(selectedPos == position);
            holder.itemView.setBackgroundColor(selectedPos == position ? Color.GRAY : Color.TRANSPARENT);
            if (!isEPC) {
                holder.tvItemName.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            }

            holder.tvItemName.setText(tag);
            holder.tvItemSNo.setText(position + 1 + ".");
        }
    }


    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView tvItemName, tvItemSNo;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvItemName = itemView.findViewById(R.id.tvRecyclerItem);
            tvItemSNo = itemView.findViewById(R.id.tvRecyclerItemSNo);
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
            selectedValue = mList.get(selectedPos);
            selectedLabel = selectedValue.length()>10? selectedValue.substring(selectedValue.length()-10) : selectedValue;
            notifyItemChanged(selectedPos);
            if (liveItem!=null){
                liveItem.setValue(selectedValue);
            }

            // Do your another stuff for your onClick
        }
    }

    public void removeAll() {
        this.mList.clear();
    }
}
