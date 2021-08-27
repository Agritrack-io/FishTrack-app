package io.agritrack.fishtrack.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.agritrack.fishtrack.R;

/**
 * Used to create Menus along the app.
 * It uses a home_menu_item layout which comprises
 * of an ImageView (thumb) and a TextView (caption).
 */
public class HomeMenuAdapter extends ArrayAdapter<MenuItem> {

    public HomeMenuAdapter(@NonNull Context ctx, ArrayList<MenuItem> menuItemsList) {
        super(ctx, 0, menuItemsList);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View menuItemView = (convertView != null ? convertView : createView(parent));

        final MenuItemViewHolder viewHolder = (MenuItemViewHolder)menuItemView.getTag();
        viewHolder.setMenuItem(getItem(position));

        return menuItemView;
    }

    private View createView(ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View view = inflater.inflate(R.layout.item_home_adapter, parent, false);

        final MenuItemViewHolder viewHolder = new MenuItemViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    private static class MenuItemViewHolder {
        private ImageView ivMenuThumb;
        private TextView tvMenuCaption;

        public MenuItemViewHolder(View v) {
            ivMenuThumb = (ImageView) v.findViewById(R.id.ivMenuThumb);
            tvMenuCaption = (TextView) v.findViewById(R.id.tvMenuCaption);
        }

        public void setMenuItem(MenuItem menuItem) {
            ivMenuThumb.setImageResource(menuItem.getImgId());
            tvMenuCaption.setText(menuItem.getName());
        }
    }
}