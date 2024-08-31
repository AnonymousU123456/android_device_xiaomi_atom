/*
 * Copyright (C) 2015 The CyanogenMod Project
 *               2017-2018 The LineageOS Project
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

package org.lineageos.settings.doze;

import android.app.Service;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Notification;
import android.telephony.TelephonyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import androidx.preference.PreferenceManager;
import androidx.core.app.NotificationCompat;
import android.content.SharedPreferences;
import java.net.HttpURLConnection;
import java.net.URL;

import org.lineageos.settings.utils.FileUtils;

import vendor.xiaomi.hardware.displayfeature.V1_0.IDisplayFeature;

public class DozeService extends Service {
    private static final String TAG = "DozeService";
    private static final String DC_ENABLE_KEY = "dc_dimming_mode";
    private static final boolean DEBUG = false;

    private ProximitySensor mProximitySensor;
    private PickupSensor mPickupSensor;
    private HBMObserver hbmObserver;
    private UdfpsViewObserver udfpsViewObserver;
    private IDisplayFeature mDisplayFeature;
    private DCBinder mBinder;
    private SharedPreferences sharedPrefs;
    private TelephonyManager mTelephonyManager;
    private NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        if (DEBUG)
            Log.d(TAG, "Creating service");
        mProximitySensor = new ProximitySensor(this);
        mPickupSensor = new PickupSensor(this);

        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenStateReceiver, screenStateFilter);

        hbmObserver = new HBMObserver(new Handler());
                
        getContentResolver().registerContentObserver(Settings.System.getUriFor(DozeUtils.HBM_SWITCH), false, hbmObserver);

        udfpsViewObserver = new UdfpsViewObserver(new Handler());
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPrefs.getBoolean(DC_ENABLE_KEY, false)) {
            getContentResolver().registerContentObserver(Settings.System.getUriFor(DozeUtils.UDFPS_SWITCH), false,
                    udfpsViewObserver);
        }
        mBinder = new DCBinder();

        NotificationChannel notificationChannel = new NotificationChannel("001", "IMS Check",
                NotificationManager.IMPORTANCE_DEFAULT);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannel(notificationChannel);
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG)
            Log.d(TAG, "Starting service");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (DEBUG)
            Log.d(TAG, "Destroying service");
        super.onDestroy();
        this.unregisterReceiver(mScreenStateReceiver);
        this.getContentResolver().unregisterContentObserver(hbmObserver);
        mProximitySensor.disable();
        mPickupSensor.disable();
        if (sharedPrefs.getBoolean(DC_ENABLE_KEY, false)) {
            getContentResolver().unregisterContentObserver(udfpsViewObserver);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void onDisplayOn() {
        if (DEBUG)
            Log.d(TAG, "Display on");
        DozeUtils.enableHBMDelayed(this, 120);
        if (DozeUtils.isPickUpEnabled(this)) {
            mPickupSensor.disable();
        }
        if (DozeUtils.isHandwaveGestureEnabled(this) ||
                DozeUtils.isPocketGestureEnabled(this)) {
            mProximitySensor.disable();
        }
        if (!mTelephonyManager.isImsRegistered()) {
            Log.e(TAG, "isImsRegistered == false!");
            Notification notify = new NotificationCompat.Builder(this, "001")
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle("IMS异常")
                    .setContentText("IMS服务未注册，请立即重启设备，以免错过重要来电和短信！建议设置首选网络为LTE")
                    .setSmallIcon(android.R.drawable.sym_def_app_icon)
                    .build();
            mNotificationManager.notify(1, notify);
        }
    }

    private void onDisplayOff() {
        if (DEBUG)
            Log.d(TAG, "Display off");
        if (DozeUtils.isPickUpEnabled(this)) {
            mPickupSensor.enable();
        }
        if (DozeUtils.isHandwaveGestureEnabled(this) ||
                DozeUtils.isPocketGestureEnabled(this)) {
            mProximitySensor.enable();
        }
    }

    private final BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                onDisplayOn();
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                onDisplayOff();
            }
        }
    };

    class HBMObserver extends ContentObserver {

        private Thread mThread = null;

        public HBMObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            int hbmEnabled_i = Settings.System.getInt(getContentResolver(), DozeUtils.HBM_SWITCH, 0);
            if (DEBUG)
                Log.d(TAG, "hbmEnabled: " + hbmEnabled_i);
            if (hbmEnabled_i == 0) {
                FileUtils.writeLine(DozeUtils.HBM_NODE, "0xE0000");
                if (mThread != null && mThread.isAlive()) {
                    mThread.interrupt();
                    mThread = null;
                }
                return;
            }
            if (mThread == null || !mThread.isAlive())
                mThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(230);
                        } catch (InterruptedException e) {
                            return;
                        }
                        int hbmEnabled_ii = Settings.System.getInt(getContentResolver(), DozeUtils.HBM_SWITCH, 0);
                        if (DEBUG)
                            Log.d(TAG, "Thread: hbmEnabled: " + hbmEnabled_ii);
                        FileUtils.writeLine(DozeUtils.HBM_NODE, hbmEnabled_ii == 1 ? "0x20000" : "0xE0000");
                    }
                });
            mThread.start();
        }
    }

    class UdfpsViewObserver extends ContentObserver {
        public UdfpsViewObserver(Handler handle) {
            super(handle);
        }

        @Override
        public void onChange(boolean selfChange) {
            boolean flag = Settings.System.getInt(getContentResolver(), DozeUtils.UDFPS_SWITCH, 0) == 0;
            if (DEBUG)
                Log.d(TAG, "UdfpsView state changed. Change dc dimming status!");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(200);
                        mDisplayFeature = IDisplayFeature.getService();
                        mDisplayFeature.setFeature(0, 20, flag ? 1 : 0, 255);
                    } catch (Exception e) {
                        Log.e(TAG, "Error on call xiaomiDisplayFeature!", e);
                    }
                }
            }).start();
        }
    }

    public class DCBinder extends Binder {
        public void onDCSwitch(boolean isopen) {
            if (isopen)
                getContentResolver().registerContentObserver(Settings.System.getUriFor(DozeUtils.UDFPS_SWITCH), false,
                        udfpsViewObserver);
            else
                getContentResolver().unregisterContentObserver(udfpsViewObserver);
        }
    }
}
