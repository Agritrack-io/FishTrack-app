package io.agritrack.ui.adapter;

import static io.agritrack.fish.state.GlobalState.recFishing;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.agritrack.R;
import io.agritrack.dialog.BinLoadDialog;
import io.agritrack.fish.state.FishingRecord;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.fishing.ISummaryActivity;

public class BinLoadAdapter extends RecyclerView.Adapter<BinLoadAdapter.MyViewHolder> {

    private final Context context;
    private final LayoutInflater mLayoutInflater;
    private List<BinLoadItem> mList;
    private int selectedPos = RecyclerView.NO_POSITION;
    private int previousSelectedPos = -1;
    private String selectedValue = null;
    private String selectedLabel = null;

    public BinLoadAdapter(Context context, ArrayList<BinLoadItem> values) {
        this.context = context;
        this.mList = values;
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    public List<BinLoadItem> getValues() {
        return mList;
    }

    public void setValues(List<BinLoadItem> values) {
        this.mList = values;
    }

    public void addUniqueItem(BinLoadItem val) {
        if (this.mList.stream().noneMatch(x -> x.epc.equals(val.epc))) {
            this.mList.add(val);
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

    public void addItem(BinLoadItem val) {
        this.mList.add(val);
    }

    public void removeItem(String epc) {
        Optional<BinLoadItem> binFound = this.mList.stream().filter(x -> x.epc.equals(epc)).findFirst();
        if (binFound.isPresent()) {
            this.mList.remove(binFound.get());
        }
    }

    @NonNull
    @Override
    public BinLoadAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.bin_load_adapter, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (mList.size()<=holder.getAdapterPosition()){
            return;
        }
        BinLoadItem currBin = mList.get(holder.getAdapterPosition());
        String tag = currBin.epc.length() > 10 ? currBin.epc.substring(currBin.epc.length() - 10) : currBin.epc;
        holder.tvRfid.setText(tag);

        holder.etWeight.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (holder.etWeight.getText()!=null && holder.getAdapterPosition() == position) {
                        currBin.weight = !Strings.isEmptyOrWhitespace(holder.etWeight.getText().toString()) ? Integer.valueOf(holder.etWeight.getText().toString()) : null;

                        if (context instanceof ISummaryActivity) {
                            ((ISummaryActivity) context).refreshSummary();
                        }
                    }
                    //Clear focus here from edittext
                    holder.etWeight.clearFocus();
                }
                return false;
            }
        });

        if (currBin.weight != null) {
            holder.etWeight.setText(String.valueOf(currBin.weight));
        }  else {
            holder.etWeight.setText("");
        }

        holder.etBinTemperature.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (holder.etBinTemperature.getText()!=null && holder.getAdapterPosition() == position) {
                        currBin.temperature = !Strings.isEmptyOrWhitespace(holder.etBinTemperature.getText().toString()) ? Double.valueOf(holder.etBinTemperature.getText().toString()) : null;

                        if (context instanceof ISummaryActivity) {
                            ((ISummaryActivity) context).refreshSummary();
                        }
                    }
                    //Clear focus here from edittext
                    holder.etBinTemperature.clearFocus();
                }
                return false;
            }
        });

        if (currBin.temperature != null) {
            holder.etBinTemperature.setText(String.valueOf(currBin.temperature));
        }  else {
            holder.etBinTemperature.setText("");
        }

        holder.itemView.setBackgroundColor(selectedPos == holder.getAdapterPosition() ? Color.GRAY : R.color.agri_blue);
        holder.tvItemSNo.setText(position + 1 + ".");
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class BinLoadItem {
        public String epc;
        public Integer weight;
        public Double temperature;

        public BinLoadItem() {
        }

        public BinLoadItem(String rfid) {
            this.epc = rfid;
        }

        public BinLoadItem(CharSequence x) {
            this.epc = x.toString();
        }

        public BinLoadItem(String epc, Integer binWeight, Double temperature) {
            this.epc = epc;
            this.weight = binWeight;
            this.temperature = temperature;
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView tvRfid, tvItemSNo, tvBinWeightLabel;
        private final EditText etBinTemperature, etWeight;
        private final ImageButton ibAddTemp;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRfid = itemView.findViewById(R.id.tvRfid);
            tvItemSNo = itemView.findViewById(R.id.tvRecyclerItemSNo);
            ibAddTemp = itemView.findViewById(R.id.ibAddTemp);
            etWeight = itemView.findViewById(R.id.etBinWeight);
            tvBinWeightLabel = itemView.findViewById(R.id.tvBinWeightLabel);
            etBinTemperature = itemView.findViewById(R.id.etBinTemperature);
            etWeight.setSelectAllOnFocus(true);
            etBinTemperature.setSelectAllOnFocus(true);

            tvBinWeightLabel.setOnClickListener(this);
            ibAddTemp.setOnClickListener(v -> {
                etBinTemperature.requestFocus();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etBinTemperature, InputMethodManager.SHOW_IMPLICIT);
            });
//            ibAddTemp.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Below line is just like a safety check, because sometimes holder could be null,
            // in that case, getAdapterPosition() will return RecyclerView.NO_POSITION
            if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

//            if (selectedPos == getAdapterPosition()) {
//                selectedPos = RecyclerView.NO_POSITION;
//                selectedValue = null;
//                selectedLabel = null;
//                notifyDataSetChanged();
//                return;
//            }

            // Updating old as well as new positions
//            notifyItemChanged(selectedPos);
//            selectedPos = getAdapterPosition();
//            selectedValue = mList.get(selectedPos).epc;
//            selectedLabel = selectedValue.length()>10? selectedValue.substring(selectedValue.length()-10) : selectedValue;
//            notifyItemChanged(selectedPos);

            etWeight.requestFocus();
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(etWeight, InputMethodManager.SHOW_IMPLICIT);

//            BinLoadDialog binDialog = new BinLoadDialog(context, selectedLabel);
//            binDialog.showDialog();



            // Do your another stuff for your onClick
        }
    }
}
