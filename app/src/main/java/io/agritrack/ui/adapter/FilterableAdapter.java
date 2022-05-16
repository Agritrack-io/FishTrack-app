package io.agritrack.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.agritrack.R;
import io.agritrack.ui.bo.GenericListModel;

public class FilterableAdapter extends RecyclerView.Adapter<FilterableAdapter.viewHolder> implements Filterable {

    private final Context context;
    private final ArrayList<GenericListModel> arrayList;
    private ArrayList<GenericListModel> arrayListFiltered;
    private int selectedPos = RecyclerView.NO_POSITION;
    private String selectedValue = null;

    public FilterableAdapter(Context context, ArrayList<GenericListModel> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        this.arrayListFiltered = arrayList;
    }

    public String getSelectedValue(){
        return this.selectedValue;
    }

    @Override
    public viewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.simple_recycler_view_item, viewGroup, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(viewHolder viewHolder, int position) {
        viewHolder.label.setText(arrayListFiltered.get(position).getLabel());
        viewHolder.itemView.setSelected(selectedPos == position);

        viewHolder.itemView.setBackgroundColor(selectedPos == position ? Color.GRAY : Color.TRANSPARENT);
    }

    @Override
    public int getItemCount() {
        return arrayListFiltered != null ? arrayListFiltered.size() : 0;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                ArrayList<GenericListModel> arrayListFilter = new ArrayList<GenericListModel>();

                if (constraint == null || constraint.length() == 0) {
                    results.count = arrayList.size();
                    results.values = arrayList;
                } else {
                    for (GenericListModel item : arrayList) {
                        if (item != null && item.getLabel() != null && item.getLabel().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            arrayListFilter.add(item);
                        }
                    }
                    results.count = arrayListFilter.size();
                    results.values = arrayListFilter;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                arrayListFiltered = (ArrayList<GenericListModel>) results.values;
                notifyDataSetChanged();

                if (arrayListFiltered == null || arrayListFiltered.size() == 0) {
                    Toast.makeText(context, "Not Found", Toast.LENGTH_SHORT).show();
                }
            }
        };
        return filter;
    }

    public class viewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView label;

        public viewHolder(View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.tvRecyclerItem);
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
            selectedValue = this.label.getText().toString();
            notifyItemChanged(selectedPos);

            // Do your another stuff for your onClick
        }
    }
}
