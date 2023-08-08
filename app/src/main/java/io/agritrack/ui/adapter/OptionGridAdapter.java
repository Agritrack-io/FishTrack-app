package io.agritrack.ui.adapter;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

import io.agritrack.R;

public class OptionGridAdapter<T extends OptionGridAdapter.IDrawableWithText> extends BaseAdapter {

    final int paddingDp = 25;
    final float density;
    final int paddingPixel;
    final int drawableWidth;
    private Activity mActivity;
    private List<T> mOptions;
    private IOnItemClickListener<T> mOnItemClickListener;

    public OptionGridAdapter(Activity activity, List<T> options, IOnItemClickListener onItemClickListener) {
        mActivity = activity;
        mOptions = options;
        mOnItemClickListener = onItemClickListener;
        density = mActivity.getResources().getDisplayMetrics().density;
        paddingPixel = (int) (paddingDp * density);
        drawableWidth = (int) (80 * density + 0.5f);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView i = new ImageView(mActivity);
        T option = mOptions.get(position);
        i.setBackground(mActivity.getDrawable(R.drawable.button_press_effect_round));
        i.setImageResource(option.getResourceId());
        i.setImageTintList(ColorStateList.valueOf(mActivity.getColor(R.color.white)));
        i.setPadding(paddingPixel, paddingPixel, paddingPixel, paddingPixel);
        i.setScaleType(ImageView.ScaleType.FIT_CENTER);
        i.setLayoutParams(new GridView.LayoutParams(drawableWidth * 2, drawableWidth * 2));
        i.setOnClickListener(v -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(position, option);
            }
        });
        return i;
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
