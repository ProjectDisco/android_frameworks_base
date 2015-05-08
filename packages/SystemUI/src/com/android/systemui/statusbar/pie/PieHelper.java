/*
 * Copyright (C) 2014-2015 ParanoidAndroid Project
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

package com.android.systemui.statusbar.pie;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.SearchManager;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import com.android.systemui.R;
import com.android.systemui.statusbar.BaseStatusBar;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Pie Helper
 * Util class: handles system status changes and getting system state.
 * Singleton that must be intialized.
 */
public class PieHelper {
    private static PieHelper mInstance;

    private boolean mTelephony;
    private int mBatteryLevel = 0;
    private BaseStatusBar mBar;
    private Context mContext;
    private OnClockChangedListener mClockChangedListener;

    private KeyguardManager mKeyguardManager;

    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            mBatteryLevel = intent.getIntExtra("level", 0);
        }
    };

    private final BroadcastReceiver mClockReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mClockChangedListener == null) return;
            mClockChangedListener.onChange(getSimpleTime());
        }
    };

    public interface OnClockChangedListener {
        void onChange(String s);
    }

    protected void init(Context context, BaseStatusBar bar) {
        mBar = bar;
        mContext = context;
        mContext.registerReceiver(mBatteryReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        mContext.registerReceiver(mClockReceiver, filter);
        mTelephony = mContext.getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
        mKeyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
    }

    protected static PieHelper getInstance() {
        if (mInstance == null) mInstance = new PieHelper();
        return mInstance;
    }

    protected void setOnClockChangedListener(OnClockChangedListener l) {
        mClockChangedListener = l;
    }

    protected boolean supportsTelephony() {
        return mTelephony;
    }

    protected int getCount() {
        return mBar.getNotificationCount();
    }

    protected String getWifiSsid() {
        String ssid = mContext.getString(R.string.quick_settings_wifi_not_connected);
        ConnectivityManager connManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) mContext
                    .getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            ssid = connectionInfo.getSSID();
        }
        return ssid.toUpperCase();
    }

    protected String getNetworkProvider() {
        String operatorName = mContext.getString(R.string.quick_settings_wifi_no_network);
        TelephonyManager telephonyManager = (TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
        operatorName = telephonyManager.getNetworkOperatorName();
        if (operatorName == null) {
            operatorName = telephonyManager.getSimOperatorName();
        }
        return operatorName.toUpperCase();
    }

    protected String getNetworkType() {
        return TelephonyManager.getDefault().getNetworkTypeName();
    }

    protected String getSimpleDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(
                mContext.getString(R.string.pie_date_format));
        String date = sdf.format(new Date());
        return date.toUpperCase();
    }

    protected boolean is24Hours() {
        return DateFormat.is24HourFormat(mContext);
    }

    protected String getSimpleTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(
                mContext.getString(is24Hours() ? R.string.pie_hour_format_24 :
                        R.string.pie_hour_format_12));
        String time = sdf.format(new Date());
        return time.toUpperCase();
    }

    protected String getAmPm() {
        String amPm = "";
        if (!is24Hours()) {
            SimpleDateFormat sdf = new SimpleDateFormat(
                    mContext.getString(R.string.pie_am_pm));
            amPm = sdf.format(new Date()).toUpperCase();
        }
        return amPm.replace(".", ""); // show either PM or AM, not P.M. or A.M.
    }

    protected int getBatteryLevel() {
        return mBatteryLevel;
    }

    protected String getBatteryLevelReadable() {
        return mContext.getString(R.string.battery_low_percent_format, mBatteryLevel)
                .toUpperCase();
    }

    protected boolean isAssistantAvailable() {
        Intent intent = ((SearchManager) mContext.getSystemService(Context.SEARCH_SERVICE))
                .getAssistIntent(mContext, true, UserHandle.USER_CURRENT);
        return intent != null;
    }

    protected void startAssistActivity() {
        mBar.getSearchPanelView().startAssistActivity();
    }

    protected boolean isKeyguardShowing() {
        return mKeyguardManager.isKeyguardLocked();
    }
}
