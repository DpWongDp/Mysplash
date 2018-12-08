package com.wangdaye.mysplash.common.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListView;

import com.wangdaye.mysplash.Mysplash;
import com.wangdaye.mysplash.R;
import com.wangdaye.mysplash.common.basic.activity.MysplashActivity;
import com.wangdaye.mysplash.common.data.api.PhotoApi;
import com.wangdaye.mysplash.common.ui.activity.SettingsActivity;
import com.wangdaye.mysplash.common.ui.dialog.TimePickerDialog;
import com.wangdaye.mysplash.common.ui.widget.preference.MysplashListPreference;
import com.wangdaye.mysplash.common.ui.widget.preference.MysplashPreference;
import com.wangdaye.mysplash.common.ui.widget.preference.MysplashSwitchPreference;
import com.wangdaye.mysplash.common.utils.DisplayUtils;
import com.wangdaye.mysplash.common.utils.helper.DownloadHelper;
import com.wangdaye.mysplash.common.utils.helper.NotificationHelper;
import com.wangdaye.mysplash.common.utils.ValueUtils;
import com.wangdaye.mysplash.common.utils.helper.IntentHelper;
import com.wangdaye.mysplash.common.utils.manager.MuzeiOptionManager;
import com.wangdaye.mysplash.common.utils.manager.SettingsOptionManager;
import com.wangdaye.mysplash.common.utils.manager.ThemeManager;
import com.wangdaye.mysplash.main.view.activity.MainActivity;

import butterknife.ButterKnife;

/**
 * Settings fragment.
 *
 * This fragment is used to show setting options.
 *
 * */

public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener, TimePickerDialog.OnTimeChangedListener, NestedScrollingChild {

    private NestedScrollingChildHelper nestedScrollingChildHelper; // used to dispatch scroll action.
    private ListView listView; // preference list in preference fragment.

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.perference);
        initView();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = ButterKnife.findById(view, android.R.id.list);
        if (listView != null) {
            listView.setOnTouchListener(new ScrollListener(getActivity()));
            nestedScrollingChildHelper = new NestedScrollingChildHelper(listView);
            nestedScrollingChildHelper.setNestedScrollingEnabled(true);
        }
    }

    private void initView() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        initBasicPart(sharedPreferences);
        initFilterPart(sharedPreferences);
        initDownloadPart(sharedPreferences);
        initDisplayPart(sharedPreferences);
    }

    private void initBasicPart(SharedPreferences sharedPreferences) {
        // back to top.
        MysplashListPreference backToTop = (MysplashListPreference) findPreference(getString(R.string.key_back_to_top));
        String backToTopValue = sharedPreferences.getString(getString(R.string.key_back_to_top), "all");
        String backToTopName = ValueUtils.getBackToTopName(getActivity(), backToTopValue);
        backToTop.setSummary(getString(R.string.now) + " : " + backToTopName);
        backToTop.setOnPreferenceChangeListener(this);

        // auto night mode.
        MysplashListPreference autoNightMode = (MysplashListPreference) findPreference(getString(R.string.key_auto_night_mode));
        String autoNightModeValue = sharedPreferences.getString(getString(R.string.key_auto_night_mode), "follow_system");
        String autoNightModeName = ValueUtils.getAutoNightModeName(getActivity(), autoNightModeValue);
        autoNightMode.setSummary(getString(R.string.now) + " : " + autoNightModeName);
        autoNightMode.setOnPreferenceChangeListener(this);

        MysplashPreference nightStartTime = (MysplashPreference) findPreference(getString(R.string.key_night_start_time));
        nightStartTime.setSummary(getString(R.string.now) + " : " + ThemeManager.getInstance(getActivity()).getNightStartTime());
        if (autoNightModeValue.equals("auto")) {
            nightStartTime.setEnabled(true);
        } else {
            nightStartTime.setEnabled(false);
        }

        MysplashPreference nightEndTime = (MysplashPreference) findPreference(getString(R.string.key_night_end_time));
        nightEndTime.setSummary(getString(R.string.now) + " : " + ThemeManager.getInstance(getActivity()).getNightEndTime());
        if (autoNightModeValue.equals("auto")) {
            nightEndTime.setEnabled(true);
        } else {
            nightEndTime.setEnabled(false);
        }

        // language.
        MysplashListPreference language = (MysplashListPreference) findPreference(getString(R.string.key_language));
        String languageValue = sharedPreferences.getString(getString(R.string.key_language), "follow_system");
        String languageName = ValueUtils.getLanguageName(getActivity(), languageValue);
        language.setSummary(getString(R.string.now) + " : " + languageName);
        language.setOnPreferenceChangeListener(this);

        // Muzei.
        MysplashPreference muzei = (MysplashPreference) findPreference("muzei_settings");
        if (!MuzeiOptionManager.isInstalledMuzei(getActivity())) {
            PreferenceCategory display = (PreferenceCategory) findPreference("basic");
            display.removePreference(muzei);
        }
    }

    private void initFilterPart(SharedPreferences sharedPreferences) {
        // default order.
        MysplashListPreference defaultOrder = (MysplashListPreference) findPreference(getString(R.string.key_default_photo_order));
        String orderValue = sharedPreferences.getString(getString(R.string.key_default_photo_order), PhotoApi.ORDER_BY_LATEST);
        String orderName = ValueUtils.getOrderName(getActivity(), orderValue);
        defaultOrder.setSummary(getString(R.string.now) + " : " + orderName);
        defaultOrder.setOnPreferenceChangeListener(this);
    }

    private void initDownloadPart(SharedPreferences sharedPreferences) {
        // downloader.
        MysplashListPreference downloader = (MysplashListPreference) findPreference(getString(R.string.key_downloader));
        String downloaderValue = sharedPreferences.getString(getString(R.string.key_downloader), "mysplash");
        String downloaderName = ValueUtils.getDownloaderName(getActivity(), downloaderValue);
        downloader.setSummary(getString(R.string.now) + " : " + downloaderName);
        downloader.setOnPreferenceChangeListener(this);

        // download scale.
        MysplashListPreference downloadScale = (MysplashListPreference) findPreference(getString(R.string.key_download_scale));
        String scaleValue = sharedPreferences.getString(getString(R.string.key_download_scale), "compact");
        String scaleName = ValueUtils.getScaleName(getActivity(), scaleValue);
        downloadScale.setSummary(getString(R.string.now) + " : " + scaleName);
        downloadScale.setOnPreferenceChangeListener(this);
    }

    private void initDisplayPart(SharedPreferences sharedPreferences) {
        // saturation animation duration.
        MysplashListPreference duration = (MysplashListPreference) findPreference(getString(R.string.key_saturation_animation_duration));
        String durationValue = sharedPreferences.getString(getString(R.string.key_saturation_animation_duration), "2000");
        String durationName = ValueUtils.getSaturationAnimationDurationName(getActivity(), durationValue);
        duration.setSummary(getString(R.string.now) + " : " + durationName);
        duration.setOnPreferenceChangeListener(this);

        // grid list in port.
        MysplashSwitchPreference gridPort = (MysplashSwitchPreference) findPreference(getString(R.string.key_grid_list_in_port));
        gridPort.setOnPreferenceChangeListener(this);
        if (!DisplayUtils.isTabletDevice(getActivity())) {
            PreferenceCategory display = (PreferenceCategory) findPreference("display");
            display.removePreference(gridPort);
        }

        // grid list in land.
        MysplashSwitchPreference gridLand = (MysplashSwitchPreference) findPreference(getString(R.string.key_grid_list_in_land));
        gridLand.setOnPreferenceChangeListener(this);
    }

    private void showRebootSnackbar() {
        NotificationHelper.showActionSnackbar(
                getString(R.string.feedback_notify_restart),
                getString(R.string.restart),
                rebootListener);
    }

    @Nullable
    public ListView getScrolledView() {
        if (listView != null) {
            return listView;
        } else {
            return null;
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals(getString(R.string.key_night_start_time))) {
            Log.d("SettingsFragment", "key_night_start_time");
            TimePickerDialog dialog = new TimePickerDialog();
            dialog.setModel(true);
            dialog.setOnTimeChangedListener(this);
            dialog.show(getFragmentManager(), null);
        } else if (preference.getKey().equals(getString(R.string.key_night_end_time))) {
            Log.d("SettingsFragment", "key_night_end_time");
            TimePickerDialog dialog = new TimePickerDialog();
            dialog.setModel(false);
            dialog.setOnTimeChangedListener(this);
            dialog.show(getFragmentManager(), null);
        } else if (preference.getKey().equals("muzei_settings")) {
            IntentHelper.startMuzeiConfigrationActivity((MysplashActivity) getActivity());
        } else if (preference.getKey().equals(getString(R.string.key_custom_api_key))) {
            IntentHelper.startCustomApiActivity((SettingsActivity) getActivity());
        }
        return true;
    }

    // interface.

    // on preference_widget changed listener.

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        if (preference.getKey().equals(getString(R.string.key_back_to_top))) {
            // back to top.
            SettingsOptionManager.getInstance(getActivity()).setBackToTopType((String) o);
            String backType = ValueUtils.getBackToTopName(getActivity(), (String) o);
            preference.setSummary(getString(R.string.now) + " : " + backType);
        } else if (preference.getKey().equals(getString(R.string.key_auto_night_mode))) {
            // auto night mode.
            SettingsOptionManager.getInstance(getActivity()).setAutoNightMode((String) o);
            String autoNightMode = ValueUtils.getAutoNightModeName(getActivity(), (String) o);
            preference.setSummary(getString(R.string.now) + " : " + autoNightMode);
            if (((String) o).equals("auto")) {
                findPreference(getString(R.string.key_night_start_time)).setEnabled(true);
                findPreference(getString(R.string.key_night_end_time)).setEnabled(true);
            } else {
                findPreference(getString(R.string.key_night_start_time)).setEnabled(false);
                findPreference(getString(R.string.key_night_end_time)).setEnabled(false);
            }
        } else if (preference.getKey().equals(getString(R.string.key_language))) {
            // language.
            SettingsOptionManager.getInstance(getActivity()).setLanguage((String) o);
            String language = ValueUtils.getLanguageName(getActivity(), (String) o);
            preference.setSummary(getString(R.string.now) + " : " + language);
            showRebootSnackbar();
        } else if (preference.getKey().equals(getString(R.string.key_default_photo_order))) {
            // default order.
            SettingsOptionManager.getInstance(getActivity()).setDefaultPhotoOrder((String) o);
            String order = ValueUtils.getOrderName(getActivity(), (String) o);
            preference.setSummary(getString(R.string.now) + " : " + order);
            showRebootSnackbar();
        } else if (preference.getKey().equals(getString(R.string.key_downloader))) {
            // downloader.
            if (DownloadHelper.getInstance(getActivity()).switchDownloader(getActivity(), (String) o)) {
                SettingsOptionManager.getInstance(getActivity()).setDownloader((String) o);
                String downloader = ValueUtils.getDownloaderName(getActivity(), (String) o);
                preference.setSummary(getString(R.string.now) + " : " + downloader);
            } else {
                NotificationHelper.showSnackbar(getString(R.string.feedback_task_in_process));
                return false;
            }
        } else if (preference.getKey().equals(getString(R.string.key_download_scale))) {
            // download scale.
            SettingsOptionManager.getInstance(getActivity()).setDownloadScale((String) o);
            String scale = ValueUtils.getScaleName(getActivity(), (String) o);
            preference.setSummary(getString(R.string.now) + " : " + scale);
        } else if (preference.getKey().equals(getString(R.string.key_saturation_animation_duration))) {
            // saturation animation duration.
            SettingsOptionManager.getInstance(getActivity()).setSaturationAnimationDuration((String) o);
            String duration = ValueUtils.getSaturationAnimationDurationName(getActivity(), (String) o);
            preference.setSummary(getString(R.string.now) + " : " + duration);
        } else if (preference.getKey().equals(getString(R.string.key_grid_list_in_port))
                || preference.getKey().equals(getString(R.string.key_grid_list_in_land))) {
            // grid.
            showRebootSnackbar();
        }
        return true;
    }

    // on action click listener.

    private View.OnClickListener rebootListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ((SettingsActivity) getActivity()).finishSelf(true);
            MainActivity a = Mysplash.getInstance().getMainActivity();
            if (a != null) {
                a.reboot();
            }
        }
    };

    // on time changed listener.

    @Override
    public void timeChanged() {
        MysplashPreference nightStartTime = (MysplashPreference) findPreference(getString(R.string.key_night_start_time));
        nightStartTime.setSummary(
                getString(R.string.now) + " : " + ThemeManager.getInstance(getActivity()).getNightStartTime());

        MysplashPreference nightEndTime = (MysplashPreference) findPreference(getString(R.string.key_night_end_time));
        nightEndTime.setSummary(
                getString(R.string.now) + " : " + ThemeManager.getInstance(getActivity()).getNightEndTime());
    }

    // on touch listener.

    private class ScrollListener implements View.OnTouchListener {

        private float oldY;
        private boolean isBeingDragged;
        private float touchSlop;

        ScrollListener(Context context) {
            this.touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        }

        // interface.

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent ev) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                    oldY = ev.getY();
                    isBeingDragged = false;
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (!isBeingDragged) {
                        if (Math.abs(ev.getY() - oldY) > touchSlop) {
                            isBeingDragged = true;
                        }
                    }
                    if (isBeingDragged) {
                        int[] total = new int[] {0, (int) (oldY - ev.getY())};
                        int[] consumed = new int[] {0, 0};
                        dispatchNestedPreScroll(
                                total[0], total[1], consumed, null);
                        dispatchNestedScroll(
                                consumed[0], consumed[1], total[0] - consumed[0], total[1] - consumed[1], null);
                    }
                    oldY = ev.getY();
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    stopNestedScroll();
                    if (isBeingDragged) {
                        isBeingDragged = false;
                    }
                    break;
            }
            return false;
        }
    }

    // nested scrolling child.

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        nestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return nestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return nestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        nestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return nestedScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed,
                                        int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return nestedScrollingChildHelper.dispatchNestedScroll(
                dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return nestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return nestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return nestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }
}
