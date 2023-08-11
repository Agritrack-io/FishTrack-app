package io.agritrack.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.util.ArrayList;

import io.agritrack.R;
import io.agritrack.fish.ui.bo.GenericListModel;

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

    public String getSelectedValue() {
        return this.selectedValue;
    }

    public void clearSelectedValue() {
        this.selectedValue = null;
        selectedPos = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }

    @Override
    public viewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.simple_filterable_view_item, viewGroup, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(viewHolder viewHolder, int position) {
        if (position == 0) {
            viewHolder.rfid.setText(R.string.epc);
            viewHolder.code.setText(R.string.code);
            viewHolder.netEye.setText(R.string.eye);
            viewHolder.perimeter.setText(R.string.perimeter);
        } else if (position > 0 && position <= getItemCount() - 1) {
            position = position -1;
            viewHolder.rfid.setText(arrayListFiltered.get(position).getRfid());
            viewHolder.code.setText(!Strings.isEmptyOrWhitespace(arrayListFiltered.get(position).getCode()) ? arrayListFiltered.get(position).getCode() : arrayListFiltered.get(position).getLabel());
            viewHolder.netEye.setVisibility(arrayListFiltered.get(position).getNetEyeGirth() != null ? View.VISIBLE : View.GONE);
            viewHolder.netEye.setText(arrayListFiltered.get(position).getNetEyeGirth() != null ? String.valueOf(arrayListFiltered.get(position).getNetEyeGirth()) : "");
            viewHolder.netEye.setVisibility(arrayListFiltered.get(position).getPerimeter() != null ? View.VISIBLE : View.GONE);
            viewHolder.perimeter.setText(arrayListFiltered.get(position).getPerimeter() != null ? String.valueOf(arrayListFiltered.get(position).getPerimeter()) : "");

            viewHolder.itemView.setSelected(selectedPos == position + 1);

            viewHolder.itemView.setBackgroundColor(selectedPos == position + 1 ? Color.GRAY : Color.TRANSPARENT);
        }
        if (arrayListFiltered.size()>0) {
            viewHolder.rfid.setVisibility(!Strings.isEmptyOrWhitespace(arrayListFiltered.get(position).getRfid()) ? View.VISIBLE : View.GONE);
            viewHolder.netEye.setVisibility(arrayListFiltered.get(position).getNetEyeGirth() != null ? View.VISIBLE : View.GONE);
            viewHolder.perimeter.setVisibility(arrayListFiltered.get(position).getPerimeter() != null ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return arrayListFiltered != null ? arrayListFiltered.size() + 1 : 0;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                ArrayList<GenericListModel> arrayListFilter = new ArrayList<GenericListModel>();

                if (constraint == null || constraint.length() == 0) {
                    results.count = arrayList.size();
                    results.values = arrayList;
                } else {
                    for (GenericListModel item : arrayList) {
                        String strToSearch;
                        if (item.getRfid() != null) {
                            strToSearch = String.format("%s%s%s%s", item.getRfid(), item.getCode(), item.getNetEyeGirth(), item.getPerimeter());
                        } else {
                            strToSearch = String.format("%s%s%s", item.getCode(), item.getNetEyeGirth(), item.getPerimeter()).toLowerCase();
                        }
                        if (strToSearch.toLowerCase().contains(constraint.toString().toLowerCase())) { //item != null &&
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
    }

    public class viewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView rfid, code, netEye, perimeter;

        public viewHolder(View itemView) {
            super(itemView);
            rfid = (TextView) itemView.findViewById(R.id.tvRfid);
            code = (TextView) itemView.findViewById(R.id.tvCode);
            netEye = (TextView) itemView.findViewById(R.id.tvNetEye);
            perimeter = (TextView) itemView.findViewById(R.id.tvPerimeter);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Below line is just like a safety check, because sometimes holder could be null,
            // in that case, getAdapterPosition() will return RecyclerView.NO_POSITION
            if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

            if (selectedPos == getAdapterPosition()) {
                selectedPos = RecyclerView.NO_POSITION;
                selectedValue = null;
                notifyDataSetChanged();
                return;
            }

            // Updating old as well as new positions
            notifyItemChanged(selectedPos);
            selectedPos = getAdapterPosition();
            selectedValue = !Strings.isEmptyOrWhitespace(this.rfid.getText().toString()) ? this.rfid.getText().toString() : this.code.getText().toString();
            notifyItemChanged(selectedPos);

            // Check if no view has focus:
            if (itemView != null) {
                InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(itemView.getWindowToken(), 0);
            }

            // Do your another stuff for your onClick
        }
    }
}
