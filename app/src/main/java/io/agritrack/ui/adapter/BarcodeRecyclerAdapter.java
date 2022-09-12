package io.agritrack.ui.adapter;

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

import io.agritrack.R;

public class BarcodeRecyclerAdapter extends RecyclerView.Adapter<BarcodeRecyclerAdapter.BCViewHolder> {
    private final LayoutInflater mLayoutInflater;
    private Map<String, Integer> mData;
    private List<String> mList;
    private int selectedPos = RecyclerView.NO_POSITION;
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

    public Map<String, Integer> getValues() {
        return this.mData;
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

    public void addItems(String barcode, int items) {
        if (!this.mData.containsKey(barcode)) {
            this.mList.add(barcode);
            this.mData.put(barcode, items);
        } else {
            Integer cnt = this.mData.get(barcode);
            this.mData.put(barcode, cnt + items);
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

    public class BCViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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

        @Override
        public void onClick(View view) {
            if (selectedPos == getAdapterPosition()) {
                selectedPos = RecyclerView.NO_POSITION;
                notifyDataSetChanged();
                return;
            }
            selectedPos = getAdapterPosition();
            notifyDataSetChanged();
        }
    }
}
