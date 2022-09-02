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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.agritrack.R;

public class BinWeightCageAdapter extends RecyclerView.Adapter<BinWeightCageAdapter.MyViewHolder> {

    private final LayoutInflater mLayoutInflater;
    private List<BinDetails> mList;
    private int selectedPos = RecyclerView.NO_POSITION;
    private String selectedValue = null;
    private String selectedLabel = null;

    public BinWeightCageAdapter(Context context, List<BinDetails> values) {
        this.mList = values;
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    public List<BinDetails> getValues() {
        return mList;
    }

    public void setValues(List<BinDetails> values) {
        this.mList = values;
    }

    public void addUniqueItem(BinDetails val) {
        if (this.mList.stream().noneMatch(x -> x.epc.equals(val.epc))) {
            this.mList.add(val);
        }
    }

    public void addExpectedItem(BinDetails val) {
        val.flag = 1;
        if (this.mList.stream().noneMatch(x -> x.epc.equals(val.epc))) {
            this.mList.add(val);
        }
    }

    public void markReceived(List<String> epcs) {
        for (String epc : epcs) {
            Optional<BinDetails> curItem = this.mList.stream().filter(x -> x.epc.equals(epc)).findFirst();
            if (curItem.isPresent()) {
                BinDetails bin = curItem.get();
                bin.flag = bin.flag!=2 ? 0 : bin.flag;
            } else {
                BinDetails bin = new BinDetails(epc);
                bin.flag = 2;
                addItem(bin);
            }
        }
    }

    public String getSelectedValue(){
        return this.selectedValue;
    }

    public String getSelectedLabel(){
        return this.selectedLabel;
    }

    public void addItem(BinDetails val) {
        this.mList.add(val);
    }

    public void removeItem(String epc) {
        Optional<BinDetails> binFound = this.mList.stream().filter(x -> x.epc.equals(epc)).findFirst();
        if (binFound.isPresent()) {
            this.mList.remove(binFound.get());
        }
    }

    @NonNull
    @Override
    public BinWeightCageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.recycler_item_weight_cage, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        BinDetails currBin = mList.get(position);
        String tag = currBin.epc.length() > 10 ? currBin.epc.substring(currBin.epc.length() - 10) : currBin.epc;
        holder.tvItemName.setText(tag);

        if (!Strings.isEmptyOrWhitespace(currBin.cage)) {
            holder.tvCage.setText(currBin.cage);
        } else {
            holder.tvCage.setText("");
        }
        if (currBin.weight != null) {
            holder.tvWeight.setText(currBin.weight.toString());
        }  else {
            holder.tvWeight.setText("");
        }

        if (currBin.flag == 0) {
            //holder.itemView.setBackgroundColor(Color.WHITE);
            holder.tvItemName.setTextColor(Color.BLACK);
        } else if (currBin.flag == 1) {
            //holder.itemView.setBackgroundColor(Color.WHITE);
            holder.tvItemName.setTextColor(Color.GREEN);
        } else if (currBin.flag == 2) {
            //holder.itemView.setBackgroundColor(Color.WHITE);
            holder.tvItemName.setTextColor(Color.RED);
        }

        holder.itemView.setBackgroundColor(selectedPos == position ? Color.GRAY : Color.TRANSPARENT);
        holder.tvItemSNo.setText(position + 1 + ".");
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class BinDetails {
        public String epc;
        public Double weight;
        public String cage;
        public int flag = 0; //0: Received, 1: Expected, 2: Not exists

        public BinDetails() {
        }

        public BinDetails(String rfid) {
            this.epc = rfid;
        }

        public BinDetails(CharSequence x) {
            this.epc = x.toString();
        }

        public BinDetails(String epc, Double binWeight, String cageCode) {
            this.epc = epc;
            this.weight = binWeight;
            this.cage = cageCode;
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView tvItemName, tvItemSNo, tvCage, tvWeight;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvRecyclerItem);
            tvItemSNo = itemView.findViewById(R.id.tvRecyclerItemSNo);
            tvCage = itemView.findViewById(R.id.tvCage);
            tvWeight = itemView.findViewById(R.id.tvWeight);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Below line is just like a safety check, because sometimes holder could be null,
            // in that case, getAdapterPosition() will return RecyclerView.NO_POSITION
            if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

            // Updating old as well as new positions
            notifyItemChanged(selectedPos);
            selectedPos = getAdapterPosition();
            selectedValue = mList.get(selectedPos).epc;
            selectedLabel = selectedValue.length()>10? selectedValue.substring(selectedValue.length()-10) : selectedValue;
            notifyItemChanged(selectedPos);

            // Do your another stuff for your onClick
        }
    }
}
