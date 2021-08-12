package io.agritrack.fishtrack.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.ui.bo.GenericListModel;

public class CheckableListAdapter extends BaseAdapter {

    GenericListModel[] data;
    Context context;
    LayoutInflater inflater;
    String value;


    public CheckableListAdapter(Context context, GenericListModel[] data) {
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public GenericListModel getItem(int position) {
        return data[position];
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.checkable_list_item, null);
        final CheckedTextView simpleCheckedTextView = (CheckedTextView) convertView.findViewById(R.id.ctvItem);
        simpleCheckedTextView.setText(data[position].getLabel());
        // perform on Click Event Listener on CheckedTextView
        simpleCheckedTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (simpleCheckedTextView.isChecked()) {
                    // set cheek mark drawable and set checked property to false
                    value = "un-Checked";
                    simpleCheckedTextView.setCheckMarkDrawable(new ColorDrawable(Color.TRANSPARENT));
                    simpleCheckedTextView.setChecked(false);
                } else {
                    // set cheek mark drawable and set checked property to true
                    value = "Checked";
                    simpleCheckedTextView.setCheckMarkDrawable(R.drawable.ic_shortcut_done_outline);
                    simpleCheckedTextView.setChecked(true);
                }
            }
        });
        return convertView;
    }
}
