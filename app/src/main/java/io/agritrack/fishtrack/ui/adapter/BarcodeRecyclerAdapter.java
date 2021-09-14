package io.agritrack.fishtrack.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.agritrack.fishtrack.R;

public class BarcodeRecyclerAdapter extends RecyclerView.Adapter<BarcodeRecyclerAdapter.BCViewHolder> {
    private final LayoutInflater mLayoutInflater;
    private Map<String, Integer> mData;
    private List<String> mList;
    private View.OnClickListener itemsClickListener;

    public BarcodeRecyclerAdapter(Context context, List<String> values) {
        this.mList = values;
        this.mData = values.stream().collect(Collectors.toMap(s -> s, s -> 1));
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    public BarcodeRecyclerAdapter(Context context, List<String> values, View.OnClickListener clickListener) {
        this(context, values);
        this.itemsClickListener = clickListener;
    }

    public List<String> getValues() {
        return this.mList;
    }

    public void setValues(List<String> values) {
        this.mList = values;
        this.mData = values.stream().collect(Collectors.toMap(s -> s, s -> 1));
    }

    public void addItem(String barcode) {
        if (!this.mData.containsKey(barcode)) {
            this.mList.add(barcode);
            this.mData.put(barcode, 1);
        } else {
            Integer cnt = this.mData.get(barcode);
            this.mData.put(barcode, cnt + 1);
        }
    }

    public void removeItem(String key) {
        Integer cnt = this.mData.get(key);
        if (cnt > 1) {
            this.mData.put(key, cnt - 1);
        } else {
            this.mList.remove(key);
            this.mData.remove(key);
        }
    }

    @NonNull
    @Override
    public BCViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.barcode_recycler_view_item, parent, false);
        return new BCViewHolder(view, this.itemsClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull BCViewHolder holder, int position) {
        String barcode = mList.get(position);
        holder.tvItemSNo.setText((position + 1) + ".");
        holder.tvItemDescription.setText(barcode);
        holder.tvItemCount.setText(mData.get(barcode).toString());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class BCViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvItemSNo, tvItemDescription, tvItemCount;

        public BCViewHolder(@NonNull View itemView, View.OnClickListener itemsClickListener) {
            super(itemView);
            tvItemDescription = itemView.findViewById(R.id.tvItemDescription);
            tvItemSNo = itemView.findViewById(R.id.tvItemSNo);
            tvItemCount = itemView.findViewById(R.id.tvItemCount);

            if (itemsClickListener != null) {
                itemView.setOnClickListener(itemsClickListener);
            }
        }
    }
}
