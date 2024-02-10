package io.agritrack.kefalonia.ui.adapter;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.agritrack.kefalonia.R;

public class OptionGridAdapter<T extends OptionGridAdapter.IDrawableWithText> extends BaseAdapter {

    final int paddingDp = 25;
    final float density;
    final int paddingPixel;
    private LayoutInflater mLayoutInflater = null;
    final int drawableWidth;
    private Activity mActivity;
    private TextView tvMenuCaptionDown, tvNoItem;
    private ImageView ivImage;
    private List<T> mOptions;
    private IOnItemClickListener<T> mOnItemClickListener;

    public OptionGridAdapter(Activity activity, List<T> options, IOnItemClickListener onItemClickListener) {
        mActivity = activity;
        mOptions = options;
        mOnItemClickListener = onItemClickListener;
        density = mActivity.getResources().getDisplayMetrics().density;
        paddingPixel = (int) (paddingDp * density);
        drawableWidth = (int) (80 * density + 0.5f);
        this.mLayoutInflater = LayoutInflater.from(mActivity.getApplicationContext());
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = mLayoutInflater.inflate(R.layout.grid_item_menu, parent, false);
        tvMenuCaptionDown = view.findViewById(R.id.tvMenuCaptionDown);
        tvNoItem = view.findViewById(R.id.tvNoItem);
        ivImage = view.findViewById(R.id.ivImage);
        T option = mOptions.get(position);
        tvMenuCaptionDown.setText(option.getText());
        tvNoItem.setText(""+(position+1));
        ivImage.setImageResource(option.getResourceId());
        view.setBackground(mActivity.getDrawable(R.drawable.button_press_effect_round));
        ivImage.setImageTintList(ColorStateList.valueOf(mActivity.getColor(R.color.white)));
        view.setPadding(paddingPixel, paddingPixel, paddingPixel, paddingPixel);
        ivImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        view.setLayoutParams(new GridView.LayoutParams(drawableWidth * 2, drawableWidth * 2));
        view.setOnClickListener(v -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(position, option);
            }
        });
        return view;
    }

    public final int getCount() {
        return mOptions.size();
    }

    public final T getItem(int position) {
        return mOptions.get(position);
    }

    public final long getItemId(int position) {
        return position;
    }

    public interface IDrawableWithText {
        String getText();

        int getResourceId();
    }
}
