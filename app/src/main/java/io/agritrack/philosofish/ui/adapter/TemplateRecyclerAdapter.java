package io.agritrack.philosofish.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.agritrack.philosofish.R;

public class TemplateRecyclerAdapter extends RecyclerView.Adapter<TemplateRecyclerAdapter.MyViewHolder> {
    private List<BinEpc> mList;
    private final LayoutInflater mLayoutInflater;
    public boolean isClickable = true;
    private boolean isEPC = true;
    private MutableLiveData<String> liveItem;
    private int selectedPos = RecyclerView.NO_POSITION;
    private String selectedValue = null;
    private String selectedLabel = null;

    public TemplateRecyclerAdapter(Context context, List<BinEpc> values) {
        this(context,values,true);
    }

    public TemplateRecyclerAdapter(Context context, List<BinEpc> values, boolean isEPC) {
        this.mList = values;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.isEPC = isEPC;
    }
    public void removeItem(String epc) {
        Optional<BinEpc> binFound = this.mList.stream().filter(x -> x.epc.equals(epc)).findFirst();
        if (binFound.isPresent()) {
            this.mList.remove(binFound.get());
        }
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

    public List<BinEpc> getValues() {
        return mList;
    }

    public void setValues(List<BinEpc> values) {
        this.mList = values;
    }

    public void setItemObserver(MutableLiveData<String> mld){
        this.liveItem = mld;
    }

    public void addUniqueItem(BinEpc val) {
        List<String> epcs = this.mList.stream().map(x -> x.epc).collect(Collectors.toList());
        if (!epcs.contains(val.epc)) {
            this.mList.add(val);
        }
    }

    public void addItem(BinEpc val) {
        this.mList.add(val);
    }

    public void removeItem(BinEpc val) {
        this.mList.remove(val);
    }

    public void setAllUnselected() {
        for (BinEpc bin : mList) {
            bin.setSelected(false);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.simple_recycler_view_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        mList.sort(Comparator.comparing(o -> o.epc.substring(o.epc.length() - 10)));
        BinEpc bin = mList.get(position);

        if (bin != null) {
            String tag = isEPC ? bin.epc.substring(bin.epc.length() - 10) : bin.epc;
            //holder.itemView.setSelected(selectedPos == position);
            holder.itemView.setBackgroundColor(bin.isSelected ? Color.GRAY : Color.TRANSPARENT);
            if (!isEPC) {
                holder.tvItemName.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bin.setSelected(!bin.isSelected);
                    holder.itemView.setBackgroundColor(bin.isSelected ? Color.GRAY : Color.TRANSPARENT);
                }
            });
            holder.tvItemName.setText(tag);
            holder.tvItemSNo.setText(position + 1 + ".");
        }
    }

    public static class BinEpc {
        public String epc;

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        public boolean isSelected = false;

        public BinEpc(String epc) {
            this.epc = epc;
            this.isSelected = false;
        }

    }


    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder  {
        private final TextView tvItemName, tvItemSNo;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvItemName = itemView.findViewById(R.id.tvRecyclerItem);
            tvItemSNo = itemView.findViewById(R.id.tvRecyclerItemSNo);

        }

    }

    public void removeAll() {
        this.mList.clear();
    }
}
