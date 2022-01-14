package io.agritrack.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.agritrack.R;

public class BinWeightCageAdapter extends  RecyclerView.Adapter<BinWeightCageAdapter.MyViewHolder> {

    private List<String> mList;
    private final LayoutInflater mLayoutInflater;
    public boolean isClickable = true;

    private View.OnClickListener itemsClickListener;

    public BinWeightCageAdapter(Context context, List<String> values) {
        this.mList = values;
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    public BinWeightCageAdapter(Context context, List<String> values, View.OnClickListener clickListener) {
        this.mList = values;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.itemsClickListener = clickListener;
    }

    public List<String> getValues() {
        return mList;
    }

    public void setValues(List<String> values) {
        this.mList = values;
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
    public BinWeightCageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.recycler_item_weight_cage, parent, false);
        return new BinWeightCageAdapter.MyViewHolder(view, this.itemsClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull BinWeightCageAdapter.MyViewHolder holder, int position) {
        holder.tvItemName.setText(mList.get(position));
        holder.tvItemSNo.setText(String.valueOf(position + 1) + ".");
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvItemName, tvItemSNo, tvCage, tvWeight;

        public MyViewHolder(@NonNull View itemView, View.OnClickListener itemsClickListener) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvRecyclerItem);
            tvItemSNo = itemView.findViewById(R.id.tvRecyclerItemSNo);
            tvCage = itemView.findViewById(R.id.tvCage);
            tvWeight = itemView.findViewById(R.id.tvWeight);

            if (itemsClickListener != null) {
                itemView.setOnClickListener(itemsClickListener);
            }
        }
    }
}
