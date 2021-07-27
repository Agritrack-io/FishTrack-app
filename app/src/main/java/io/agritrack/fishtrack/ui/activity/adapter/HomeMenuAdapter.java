package io.agritrack.fishtrack.ui.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.agritrack.fishtrack.R;

public class HomeMenuAdapter extends ArrayAdapter<HomeMenuItem> {
    private List<HomeMenuItem> items = new LinkedList<>();

    public HomeMenuAdapter(@NonNull Context ctx, ArrayList<HomeMenuItem> menuItemsList) {
        super(ctx, 0, menuItemsList);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View menuItemView = convertView;

        if (menuItemView == null) {
            // Layout Inflater inflates each item to be displayed in GridView.
            menuItemView = LayoutInflater.from(getContext()).inflate(R.layout.home_menu_item, parent, false);
        }
        HomeMenuItem menuItem = getItem(position);
        TextView courseTV = menuItemView.findViewById(R.id.tvMenuCaption);
        ImageView courseIV = menuItemView.findViewById(R.id.ivMenuThumb);
        courseTV.setText(menuItem.getName());
        courseIV.setImageResource(menuItem.getImgId());

        return menuItemView;
    }
}
