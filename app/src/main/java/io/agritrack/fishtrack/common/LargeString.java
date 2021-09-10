package io.agritrack.fishtrack.common;

import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;

import androidx.annotation.StringRes;

import static io.agritrack.fishtrack.FishTrackApplication.getAppContext;

/**
 *  Toast Notification texts have very small font size.
 *  This class offers larger texts...
 */
public class LargeString {

    public static SpannableStringBuilder render(CharSequence text) {
        SpannableStringBuilder biggerText = new SpannableStringBuilder(text);
        biggerText.setSpan(new RelativeSizeSpan(1.35f), 0, text.length(), 0);
        return biggerText;
    }

    public static SpannableStringBuilder render(@StringRes int resId) {
        return render(getAppContext().getText(resId));
    }

    public static String renderString(@StringRes int resId) {
        return render(getAppContext().getText(resId)).toString();
    }

}
