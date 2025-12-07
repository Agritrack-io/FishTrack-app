package io.agritrack.philosofish.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.agritrack.philosofish.R;

public class InventoryMenuAdapter extends ArrayAdapter<MenuItem> {

    public InventoryMenuAdapter(@NonNull Context ctx, ArrayList<MenuItem> menuItemsList) {
        super(ctx, 0, menuItemsList);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View menuItemView = (convertView != null ? convertView : createView(parent));

        final InventoryMenuAdapter.MenuItemViewHolder viewHolder = (InventoryMenuAdapter.MenuItemViewHolder)menuItemView.getTag();
        viewHolder.setMenuItem(getItem(position));

        return menuItemView;
    }

    private View createView(ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View view = inflater.inflate(R.layout.item_menu, parent, false);

        final InventoryMenuAdapter.MenuItemViewHolder viewHolder = new InventoryMenuAdapter.MenuItemViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    private static class MenuItemViewHolder {
        private final TextView tvMenuCaptionUp;
        private final TextView tvMenuCaptionDown;
        private final ImageView ivCheck;

        public MenuItemViewHolder(View v) {
            tvMenuCaptionUp = (TextView) v.findViewById(R.id.tvMenuCaptionUp);
            tvMenuCaptionDown = (TextView) v.findViewById(R.id.tvMenuCaptionDown);
            ivCheck = v.findViewById(R.id.ivCheck);
        }

        public void setMenuItem(MenuItem menuItem) {
            tvMenuCaptionUp.setText(menuItem.getName());
            tvMenuCaptionDown.setText(menuItem.getDescription());

            if (tvMenuCaptionDown.getText().toString().isEmpty()) {
                tvMenuCaptionDown.setVisibility(View.GONE);
            }

            // parent view reference
            View root = (View) tvMenuCaptionUp.getParent().getParent();

            // show done appearance
            if (menuItem.isDone) {
                ivCheck.setVisibility(View.VISIBLE);
                root.setBackgroundResource(R.drawable.green_background);   // highlight whole tile
            } else {
                ivCheck.setVisibility(View.GONE);
                root.setBackgroundResource(R.drawable.button_press_effect); // your default
            }
        }


    }
}