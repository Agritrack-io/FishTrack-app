package io.agritrack.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.R;

public class BinWeightCageAdapter extends RecyclerView.Adapter<BinWeightCageAdapter.MyViewHolder> {

    private final LayoutInflater mLayoutInflater;
    public boolean isClickable = true;
    private List<BinDetails> mList;
    private View.OnClickListener itemsClickListener;

    public BinWeightCageAdapter(Context context, List<BinDetails> values) {
        this.mList = values;
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    public BinWeightCageAdapter(Context context, List<BinDetails> values, View.OnClickListener clickListener) {
        this.mList = values;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.itemsClickListener = clickListener;
    }

    public List<BinDetails> getValues() {
        return mList;
    }

    public void setValues(List<BinDetails> values) {
        this.mList = values;
    }

    public List<String> getEpcsList() {
        return mList.stream().map(x -> x.epc).collect(Collectors.toList());
    }

    public void addUniqueItem(BinDetails val) {
        if (!this.mList.contains(val)) {
            this.mList.add(val);
        }
    }

    public void addItem(BinDetails val) {
        this.mList.add(val);
    }

    public void removeItem(BinDetails val) {
        this.mList.remove(val);
    }

    @NonNull
    @Override
    public BinWeightCageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.recycler_item_weight_cage, parent, false);
        return new MyViewHolder(view, this.itemsClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        BinDetails currBin = mList.get(position);
        holder.tvItemName.setText(currBin.epc);
        if (!Strings.isEmptyOrWhitespace(currBin.cage))
            holder.tvCage.setText(currBin.cage);
        if (currBin.weight != null)
            holder.tvWeight.setText(currBin.weight.toString());

        holder.tvItemSNo.setText(String.valueOf(position + 1) + ".");
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class BinDetails {
        public String epc;
        public Double weight;
        public String cage;

        public BinDetails() {
        }

        public BinDetails(String rfid) {
            this.epc = rfid;
        }

        public BinDetails(CharSequence x) {
            this.epc = x.toString();
        }
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
