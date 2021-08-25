package io.agritrack.fishtrack.ui.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.ui.bo.GenericListModel;

public class FilterableAdapter extends RecyclerView.Adapter<FilterableAdapter.viewHolder> implements Filterable {

    private final Context context;
    private final ArrayList<GenericListModel> arrayList;
    private ArrayList<GenericListModel> arrayListFiltered;

    public FilterableAdapter(Context context, ArrayList<GenericListModel> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        this.arrayListFiltered = arrayList;
    }

    @Override
    public viewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.simple_recycler_view_item, viewGroup, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(viewHolder viewHolder, int position) {
        viewHolder.label.setText(arrayListFiltered.get(position).getLabel());
    }

    @Override
    public int getItemCount() {
        return arrayListFiltered.size();
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
                        if (item.getLabel().toLowerCase().contains(constraint.toString().toLowerCase())) {
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

                if (arrayListFiltered.size() == 0) {
                    Toast.makeText(context, "Not Found", Toast.LENGTH_LONG).show();
                }
            }
        };
        return filter;
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        TextView label;

        public viewHolder(View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.tvRecyclerItem);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, arrayListFiltered.get(getAdapterPosition()).getLabel(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
