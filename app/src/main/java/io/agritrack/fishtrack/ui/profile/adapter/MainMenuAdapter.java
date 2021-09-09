package io.agritrack.fishtrack.ui.profile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.ui.bo.MenuItemData;

import static io.agritrack.fishtrack.FishTrackApplication.getAppContext;

public class MainMenuAdapter extends RecyclerView.Adapter<MainMenuAdapter.ViewHolder> {
    private final List<MenuItemData> mData;

    public MainMenuAdapter(@NonNull List<MenuItemData> objects) {
        this.mData = objects;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_adapter, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuItemData menuItem = mData.get(position);
        holder.getTitle().setText(menuItem.getTitle());
        // holder.getDesc1().setText(menuItem.getDescription1());
        // holder.getDesc2().setText(menuItem.getDescription2());
        holder.getImage().setImageResource(menuItem.getImage());


        holder.getTitle().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getAppContext(), "clicked on " + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtTitle;
        //private final TextView txtDesc1;
        //private final TextView txtDesc2;
        private final ImageView ivImage;

        public ViewHolder(View view) {
            super(view);

            txtTitle = view.findViewById(R.id.tvMenuCaption);
            // txtDesc1 = (TextView) view.findViewById(R.id.tvDesc1);
            // txtDesc2 = (TextView) view.findViewById(R.id.tvDesc2);
            ivImage = view.findViewById(R.id.ivMenuThumb);
        }

        public TextView getTitle() {
            return txtTitle;
        }

        //public TextView getDesc1() { return txtDesc1; }

        // public TextView getDesc2() { return txtDesc2; }

        public ImageView getImage() {
            return ivImage;
        }
    }
}
