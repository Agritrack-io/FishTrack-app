package io.agritrack.fishtrack.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.agritrack.fishtrack.R;

public class TemplateRecyclerAdapter extends RecyclerView.Adapter<TemplateRecyclerAdapter.MyViewHolder> {
    private ArrayList<String> mList;
    private LayoutInflater mLayoutInflater;
    private Context context;

    public TemplateRecyclerAdapter(Context context, ArrayList<String> values) {
        mList = values;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.context = context;
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
        private TextView tvItemName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = (TextView) itemView.findViewById(R.id.tvRecyclerItem);
        }
    }
//public class TextAdapter extends ArrayAdapter<String> {


}
