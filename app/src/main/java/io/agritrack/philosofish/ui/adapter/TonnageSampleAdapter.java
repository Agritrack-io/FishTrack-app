package io.agritrack.philosofish.ui.adapter;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.sql.Time;
import java.util.List;
import java.util.Optional;

import io.agritrack.philosofish.R;

public class TonnageSampleAdapter extends RecyclerView.Adapter<TonnageSampleAdapter.MyViewHolder>{

    private final LayoutInflater mLayoutInflater;
    private List<TonnageSampleAdapter.TonnageDetails> mList;
    private String selectedValue = null;

    public TonnageSampleAdapter(Context context, List<TonnageSampleAdapter.TonnageDetails> values) {
        this.mList = values;
        this.mLayoutInflater = LayoutInflater.from(context);
    }


    public List<TonnageSampleAdapter.TonnageDetails> getValues() {
        return mList;
    }

    public void setValues(List<TonnageSampleAdapter.TonnageDetails> values) {
        this.mList = values;
    }

    public void addUniqueItem(TonnageSampleAdapter.TonnageDetails val) {
       // if (this.mList.stream().noneMatch(x -> x.timestamp.equals(val.timestamp))) {
            this.mList.add(val);
        //}
    }


    public void removeItem(Time timestamp) {
        Optional<TonnageSampleAdapter.TonnageDetails> sampleFound = this.mList.stream().filter(x -> x.timestamp.equals(timestamp)).findFirst();
        if (sampleFound.isPresent()) {
            this.mList.remove(sampleFound.get());
        }
    }

    @NonNull
    @Override
    public TonnageSampleAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.recycler_item_tonnage_sample, parent, false);
        return new TonnageSampleAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TonnageSampleAdapter.MyViewHolder holder, int position) {
        if (position == 0) {
            holder.tvFishTemp.setText(Html.fromHtml("<b>" + "<font color='#16325c'>"
                            + getAppContext().getResources().getString(R.string.fish_temp_label) + "</font>" + "</b>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY));
            holder.tvFishTemp.setTextSize(18);
            holder.tvTime.setTextSize(18);
            holder.tvItemNo.setTextSize(18);
            holder.tvItemNo.setText(Html.fromHtml("<b>" + "<font color='#16325c'>"
                            + "#" + "</font>" + "</b>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY));
            holder.tvTime.setText(Html.fromHtml("<b>" + "<font color='#16325c'>"
                            + getAppContext().getResources().getString(R.string.control_time) + "</font>" + "</b>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else {
            if (mList.size() <= position) {
                return;
            }
            TonnageSampleAdapter.TonnageDetails sample = mList.get(position);

            if (!Strings.isEmptyOrWhitespace(sample.timestamp.toString())) {
                holder.tvTime.setText(sample.timestamp.toString());
            } else {
                holder.tvTime.setText("");
            }

            if (!Strings.isEmptyOrWhitespace(sample.fishTemp.toString())) {
                holder.tvFishTemp.setText(sample.fishTemp.toString());
            } else {
                holder.tvFishTemp.setText("");
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sample.setSelected(!sample.isSelected);
                    holder.itemView.setBackgroundColor(sample.isSelected ? Color.GRAY : Color.TRANSPARENT);
                }
            });

//        holder.itemView.setBackgroundColor(selectedPos == position ? Color.GRAY : Color.TRANSPARENT);
            holder.tvItemNo.setText(position + ".");
        }
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }


    public static class TonnageDetails {
        public Double fishTemp;
        public String corrAction;
        public Time timestamp;
        public boolean isSelected = false;


        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        public TonnageDetails() {
            timestamp = new Time(System.currentTimeMillis());
        }

        public TonnageDetails(Time timestamp, Double fishTemp, String corrAction) {
            this.timestamp = timestamp;
            this.corrAction = corrAction;
            this.fishTemp = fishTemp;
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder  {
        private final TextView tvTime, tvFishTemp, tvItemNo;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvItemNo = itemView.findViewById(R.id.tvSampleNUmber);
            tvFishTemp = itemView.findViewById(R.id.tvFishTemp);

        }


    }

}
