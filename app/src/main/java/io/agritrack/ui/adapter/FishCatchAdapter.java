package io.agritrack.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.agritrack.kefalonia.R;

public class FishCatchAdapter extends RecyclerView.Adapter<FishCatchAdapter.MyViewHolder> {

    private final Context context;
    private final LayoutInflater mLayoutInflater;
    private List<FishCatchItem> mList;
    private Handler handler;
    private int selectedPos = RecyclerView.NO_POSITION;
    private int previousSelectedPos = -1;
    private String selectedValue = null;
    private String selectedLabel = null;

    public FishCatchAdapter(Context context, ArrayList<FishCatchItem> values) {
        this.context = context;
        this.mList = values;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.handler = new Handler(Looper.getMainLooper());
    }

    public List<FishCatchItem> getValues() {
        return mList;
    }

    public void setValues(List<FishCatchItem> values) {
        this.mList = values;
    }

    public void addUniqueItem(FishCatchItem val) {
        if (this.mList.stream().noneMatch(x -> x.weight.equals(val.weight))) {
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

    public void addItem(FishCatchItem val) {
        this.mList.add(val);
    }

    public void removeItem(String weight) {
        Optional<FishCatchItem> catchFound = this.mList.stream().filter(x -> x.weight.equals(weight)).findFirst();
        if (catchFound.isPresent()) {
            this.mList.remove(catchFound.get());
        }
    }

    @NonNull
    @Override
    public FishCatchAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.fish_catch_adapter, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (mList.size() <= holder.getAdapterPosition()) {
            return;
        }
        FishCatchItem currCatch = mList.get(holder.getAdapterPosition());

        if (currCatch.weight != null) {
            holder.etCatch.setText(currCatch.weight.toString());
        } else {
            holder.etCatch.setText("");
        }

        holder.etCatch.setTag(holder.getAdapterPosition());

        holder.etCatch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //Clear focus here from edittext
                    holder.etCatch.clearFocus();
                }
                return false;
            }
        });

        holder.etCatch.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable editable) {
                currCatch.weight = editable.toString();
                int position = (int) holder.etCatch.getTag();
                // Check if the text has changed for the first time
                if (editable != null && editable.length() == 1 && position == getItemCount() - 1) {
                    // Create a new text or handle it as needed
                    FishCatchItem fishCatchItem = new FishCatchItem();
                    // Handle the new text, for example, add it to the list
                    updateAdapter(fishCatchItem);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });

        holder.itemView.setBackgroundColor(selectedPos == holder.getAdapterPosition() ? Color.GRAY : R.color.agri_blue);
        holder.tvItemSNo.setText(position + 1 + ".");
    }

    private void updateAdapter(FishCatchItem fishCatchItem) {
        handler.post(() -> {
            mList.add(fishCatchItem);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class FishCatchItem {
        public String weight;

        public FishCatchItem() {
        }

        public FishCatchItem(String catchWeight) {
            this.weight = catchWeight;
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView tvItemSNo;
        private final ImageView ivDelete;
        private final EditText etCatch;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            etCatch = itemView.findViewById(R.id.etCatch);
            tvItemSNo = itemView.findViewById(R.id.tvRecyclerItemSNo);
            ivDelete = itemView.findViewById(R.id.ivDelete);

            itemView.setOnClickListener(this);
//            ibAddTemp.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Below line is just like a safety check, because sometimes holder could be null,
            // in that case, getAdapterPosition() will return RecyclerView.NO_POSITION
            if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

            if (selectedPos == getAdapterPosition()) {
                selectedPos = RecyclerView.NO_POSITION;
                selectedValue = null;
                selectedLabel = null;
                notifyDataSetChanged();
                return;
            }

            // Updating old as well as new positions
            notifyItemChanged(selectedPos);
            selectedPos = getAdapterPosition();
            selectedValue = mList.get(selectedPos).weight;
            selectedLabel = selectedValue;
            notifyItemChanged(selectedPos);

            // Do your another stuff for your onClick
        }
    }
}
