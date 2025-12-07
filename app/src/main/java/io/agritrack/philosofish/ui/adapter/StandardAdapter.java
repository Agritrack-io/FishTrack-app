package io.agritrack.philosofish.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class StandardAdapter extends ArrayAdapter<String> {

    private String otherText;

    public StandardAdapter(Context context, String[] values) {
        super(context, android.R.layout.simple_spinner_dropdown_item, values);
    }

    public void setOtherText(String txt) {
        this.otherText = txt;
        notifyDataSetChanged(); // refresh spinner view
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        TextView tv = (TextView) v;

        String item = getItem(position);

        if ("OTHER".equals(item) && otherText != null && !otherText.isEmpty()) {
            tv.setText(otherText); // SHOW USER INPUT INSTEAD OF "OTHER"
        } else {
            tv.setText(item);
        }
        return v;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}