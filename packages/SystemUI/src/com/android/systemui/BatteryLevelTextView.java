/*
 * Copyright (C) 2014 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import com.android.systemui.statusbar.policy.BatteryController;

import java.text.NumberFormat;

public class BatteryLevelTextView extends TextView implements
        BatteryController.BatteryStateChangeCallback{

    private static final String TAG = BatteryLevelTextView.class.getSimpleName();
    private static final boolean DEBUG = false;

    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";

    private BatteryController mBatteryController;
    private boolean mBatteryCharging;
    private boolean mShow;
    private boolean mForceShow;
    private boolean mAttached;
    private boolean mUseClockTextStyle;
    private int mRequestedVisibility;

    private Typeface mDefaultTypeface;
    private Typeface mCustomTypeface;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.v(TAG, "onReceive: " + intent);
            String action = intent.getAction();
            if (Intent.ACTION_USER_SWITCHED.equals(action)) {
                loadShowBatteryTextSetting();
            }
        }
    };

    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri) {
            loadShowBatteryTextSetting();
        }
    };

    public BatteryLevelTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDefaultTypeface = getTypeface();
        mCustomTypeface = Typeface.create("sans-serif-medium", Typeface.NORMAL);
        mRequestedVisibility = getVisibility();
        loadShowBatteryTextSetting();
    }

    public void setForceShown(boolean forceShow) {
        mForceShow = forceShow;
        reloadTextStyle();
        updateVisibility();
    }

    public void setBatteryController(BatteryController batteryController) {
        mBatteryController = batteryController;
        if (mAttached) {
            mBatteryController.addStateChangedCallback(this);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        mRequestedVisibility = visibility;
        updateVisibility();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        reloadTextStyle();
     }

    @Override
    public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
        String percentage = NumberFormat.getPercentInstance().format((double) level / 100.0);
        setText(percentage);
        boolean changed = mBatteryCharging != charging;
        mBatteryCharging = charging;
        if (changed) {
            loadShowBatteryTextSetting();
        }
    }

    @Override
    public void onPowerSaveChanged() {
        // Not used
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mBatteryController != null) {
            mBatteryController.addStateChangedCallback(this);
        }

        getContext().getContentResolver().registerContentObserver(Settings.System.getUriFor(
                STATUS_BAR_BATTERY_STYLE), false, mObserver);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_USER_SWITCHED);
        getContext().registerReceiver(mBroadcastReceiver, filter);

        mAttached = true;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mAttached = false;

        getContext().getContentResolver().unregisterContentObserver(mObserver);

        getContext().unregisterReceiver(mBroadcastReceiver);

        if (mBatteryController != null) {
            mBatteryController.removeStateChangedCallback(this);
        }
    }

    private void updateVisibility() {
        if (mShow || mForceShow) {
            super.setVisibility(mRequestedVisibility);
        } else {
            super.setVisibility(GONE);
        }
    }

    private void loadShowBatteryTextSetting() {
        ContentResolver resolver = getContext().getContentResolver();
        int currentUserId = ActivityManager.getCurrentUser();
        int mode = Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, 0, currentUserId);

        boolean clockTextStyle = false; // default

        boolean showNextPercent = (mBatteryCharging && mode == 1) || mode == 2;
        int batteryStyle = Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_BATTERY_STYLE, 0, currentUserId);

        switch (batteryStyle) {
            case 4: // BATTERY_METER_GONE
                showNextPercent = false;
                break;
            case 5: // BATTERY_METER_TEXT
                showNextPercent = true;
                clockTextStyle = true;
                break;
            default:
                break;
        }

        mShow = showNextPercent;
        mUseClockTextStyle = clockTextStyle;

        reloadTextStyle();
        updateVisibility();
    }

    private void reloadTextStyle() {
        setTypeface(mUseClockTextStyle
                ? mCustomTypeface
                : mDefaultTypeface);

        setTextSize(TypedValue.COMPLEX_UNIT_PX, mUseClockTextStyle
                ? getResources().getDimensionPixelSize(R.dimen.status_bar_clock_size)
                : getResources().getDimensionPixelSize(R.dimen.battery_level_text_size));
    }
}
