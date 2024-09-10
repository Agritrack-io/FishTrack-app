package io.agritrack.philosofish.ui.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.fish.ui.fishing.ISummaryActivity;

public class BinLoadAdapter extends RecyclerView.Adapter<BinLoadAdapter.MyViewHolder> {

    private final Context context;
    private final LayoutInflater mLayoutInflater;
    private List<BinLoadItem> mList;
    private int selectedPos = RecyclerView.NO_POSITION;
    private int previousSelectedPos = -1;
    private String selectedValue = null;
    private String selectedLabel = null;
    private boolean showTemp = false;

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

    public String getSelectedValue() {
        return this.selectedValue;
    }

    public String getSelectedLabel() {
        return this.selectedLabel;
    }

    public void clearSelectedValue() {
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
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        mList.sort(Comparator.comparing(o -> o.epc.substring(o.epc.length() - 5)));
        if (mList.size() <= holder.getAdapterPosition()) {
            return;
        }
        BinLoadItem currBin = mList.get(holder.getAdapterPosition());
        String tag = currBin.epc.length() > 5 ? currBin.epc.substring(currBin.epc.length() - 5) : currBin.epc;
        holder.tvRfid.setText(tag);

        if (showTemp) {
            holder.constraintLayout2.setVisibility(View.GONE);
            holder.constraintLayout3.setVisibility(View.VISIBLE);
        } else {
            holder.constraintLayout2.setVisibility(View.VISIBLE);
            holder.constraintLayout3.setVisibility(View.GONE);
        }

        holder.etWeight.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (holder.etWeight.getText() != null && holder.getAdapterPosition() == position) {
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


        holder.etWeight.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
                if (holder.etWeight.getText() != null && holder.getAdapterPosition() == position) {
                    currBin.weight = !Strings.isEmptyOrWhitespace(holder.etWeight.getText().toString()) ? Integer.valueOf(holder.etWeight.getText().toString()) : null;

                    if (context instanceof ISummaryActivity) {
                        ((ISummaryActivity) context).refreshSummary();
                    }
                }
            }
        });

        if (currBin.weight != null) {
            holder.etWeight.setText(String.valueOf(currBin.weight));
        } else {
            holder.etWeight.setText("");
        }

        holder.etBinTemperature.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (holder.etBinTemperature.getText() != null && holder.getAdapterPosition() == position) {
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

        holder.etBinTemperature.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
                if (holder.etBinTemperature.getText() != null && holder.getAdapterPosition() == position) {
                    currBin.temperature = !Strings.isEmptyOrWhitespace(holder.etBinTemperature.getText().toString()) ? Double.valueOf(holder.etBinTemperature.getText().toString()) : null;

                    if (context instanceof ISummaryActivity) {
                        ((ISummaryActivity) context).refreshSummary();
                    }
                }
            }
        });

        if (currBin.temperature != null) {
            holder.etBinTemperature.setText(String.valueOf(currBin.temperature));
        } else {
            holder.etBinTemperature.setText("");
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void showTemp(boolean showTemp) {
        this.showTemp = showTemp;
        notifyDataSetChanged();
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

        public BinLoadItem(String epc, Integer binWeight) {
            this.epc = epc;
            this.weight = binWeight;
        }

        public BinLoadItem(String epc, Integer binWeight, Double temperature) {
            this.epc = epc;
            this.weight = binWeight;
            this.temperature = temperature;
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView tvRfid, tvItemSNo, tvKg, tvCelsius;
        private final EditText etBinTemperature, etWeight;
        private final ConstraintLayout constraintLayout2, constraintLayout3;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRfid = itemView.findViewById(R.id.tvRfid);
            tvItemSNo = itemView.findViewById(R.id.tvRecyclerItemSNo);
            etWeight = itemView.findViewById(R.id.etBinWeight);
            etBinTemperature = itemView.findViewById(R.id.etBinTemperature);
            tvKg = itemView.findViewById(R.id.tvKg);
            tvCelsius = itemView.findViewById(R.id.tvCelsius);
            constraintLayout2 = itemView.findViewById(R.id.constraintLayout2);
            constraintLayout3 = itemView.findViewById(R.id.constraintLayout3);
            etWeight.setSelectAllOnFocus(true);
            etBinTemperature.setSelectAllOnFocus(true);

            constraintLayout2.setOnClickListener(this);
            constraintLayout3.setOnClickListener(v -> {
                etBinTemperature.requestFocus();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etBinTemperature, InputMethodManager.SHOW_IMPLICIT);
            });
        }

        @Override
        public void onClick(View v) {
            // Below line is just like a safety check, because sometimes holder could be null,
            // in that case, getAdapterPosition() will return RecyclerView.NO_POSITION
            if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

            etWeight.requestFocus();
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(etWeight, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}
