package io.agritrack.philosofish.ui.adapter;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.util.ArrayList;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.fish.ui.bo.GenericListModel;

public class FilterableAdapter extends RecyclerView.Adapter<FilterableAdapter.viewHolder> implements Filterable {

    private final Context context;
    private final ArrayList<GenericListModel> arrayList;
    private ArrayList<GenericListModel> arrayListFiltered;
    private int selectedPos = RecyclerView.NO_POSITION;
    private String selectedValue = null;
    private IEditText listener;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(boolean isCorrelated);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public FilterableAdapter(Context context, ArrayList<GenericListModel> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        this.arrayListFiltered = arrayList;
        //selectedValue = null;
    }

    public FilterableAdapter(Context context, ArrayList<GenericListModel> arrayList, IEditText listener) {
        this.context = context;
        this.arrayList = arrayList;
        this.arrayListFiltered = arrayList;
        this.listener = listener;
        //selectedValue = null;
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
    public void onBindViewHolder(viewHolder holder, int adapterPos) {

        if (adapterPos == 0) {
            holder.code.setText(Html.fromHtml("<b><font color='#16325c'>"
                    + context.getString(R.string.code)
                    + "</font></b>", HtmlCompat.FROM_HTML_MODE_LEGACY));

            holder.rfid.setText(Html.fromHtml("<b><font color='#16325c'>"
                    + context.getString(R.string.barcode)
                    + "</font></b>", HtmlCompat.FROM_HTML_MODE_LEGACY));

            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            return;
        }

        int dataPos = adapterPos - 1;
        GenericListModel item = arrayListFiltered.get(dataPos);

        boolean isCorrelated = !Strings.isEmptyOrWhitespace(item.getRfid());

        holder.code.setText(!Strings.isEmptyOrWhitespace(item.getCode()) ? item.getCode() : item.getLabel());
        holder.code.setTextColor(isCorrelated ? Color.RED : Color.BLACK);

        holder.rfid.setText(isCorrelated ? lastPart(item.getRfid(), 11) : "");
        holder.rfid.setVisibility(isCorrelated ? View.VISIBLE  : View.GONE);

        holder.itemView.setSelected(selectedPos == adapterPos);
        holder.itemView.setBackgroundColor(selectedPos == adapterPos ? Color.GRAY : Color.TRANSPARENT);
    }

    private String lastPart(String value, int keepChars) {
        if (Strings.isEmptyOrWhitespace(value)) return "";
        value = value.trim();

        return value.length() <= keepChars
                ? value
                : value.substring(value.length() - keepChars);
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
        TextView rfid, code;//, netEye, perimeter;

        public viewHolder(View itemView) {
            super(itemView);
            rfid = (TextView) itemView.findViewById(R.id.tvRfid);
            code = (TextView) itemView.findViewById(R.id.tvCode);
//            netEye = (TextView) itemView.findViewById(R.uid.tvNetEye);
//            perimeter = (TextView) itemView.findViewById(R.uid.tvPerimeter);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Below line is just like a safety check, because sometimes holder could be null,
            // in that case, getAdapterPosition() will return RecyclerView.NO_POSITION
            boolean isCorrelated;
            if (this.rfid.getText().toString() == null || this.rfid.getText().toString().isEmpty()) {
                isCorrelated = false;
            } else {
                isCorrelated = true;
            }
            if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

            if (selectedPos == getAdapterPosition()) {
                selectedPos = RecyclerView.NO_POSITION;
                selectedValue = null;
                if (mListener != null) {
                    mListener.onItemClick(false);
                }
                notifyDataSetChanged();
                return;
            }

            // Updating old as well as new positions
            notifyItemChanged(selectedPos);
            selectedPos = getAdapterPosition();
            selectedValue = !Strings.isEmptyOrWhitespace(this.code.getText().toString()) ? this.code.getText().toString() : "";
            notifyItemChanged(selectedPos);
            if (listener != null) {
                listener.addToEditText(selectedValue);
            }

            // Check if no view has focus:
            if (itemView != null) {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(itemView.getWindowToken(), 0);
            }
            if (mListener != null) {
                mListener.onItemClick(isCorrelated);
            }
            // Do your another stuff for your onClick

        }
    }
}
