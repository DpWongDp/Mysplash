package com.wangdaye.mysplash.common.ui.widget.preference;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.preference.Preference;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.wangdaye.mysplash.R;
import com.wangdaye.mysplash.common.utils.manager.ThemeManager;

/**
 * Mysplash preference.
 *
 * A Preference that can set style of the texts.
 *
 * */

public class MysplashPreference extends Preference {

    private View view;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MysplashPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public MysplashPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MysplashPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MysplashPreference(Context context) {
        super(context);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        this.view = view;

        TextView title = view.findViewById(android.R.id.title);
        title.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                getContext().getResources().getDimension(R.dimen.title_text_size));
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(ThemeManager.getTitleColor(getContext()));

        TextView summary = view.findViewById(android.R.id.summary);
        summary.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                getContext().getResources().getDimension(R.dimen.subtitle_text_size));
        summary.setTextColor(ThemeManager.getSubtitleColor(getContext()));
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (view != null) {
            if (enabled) {
                view.setAlpha(1F);
            } else {
                view.setAlpha(0.33F);
            }
        }
    }
}
