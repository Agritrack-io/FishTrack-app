package io.agritrack.fishtrack.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.agritrack.fishtrack.R;

public class TemplateRecyclerAdapter extends RecyclerView.Adapter<TemplateRecyclerAdapter.MyViewHolder> {
    private List<String> mList;
    private final LayoutInflater mLayoutInflater;
    private final Context context;

    public TemplateRecyclerAdapter(Context context, ArrayList<String> values) {
        mList = values;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.context = context;
    }

    public List<String> getValues() {
        return mList;
    }

    public void setValues(List<String> vals) {
        this.mList = vals;
    }

    public void addItem(String val) {
        if (!this.mList.contains(val)) {
            this.mList.add(val);
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
        holder.tvItemName.setText(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvItemName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvRecyclerItem);
        }
    }
//public class TextAdapter extends ArrayAdapter<String> {


}
