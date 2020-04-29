package com.example.indoornav;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import java.util.List;

public class RSSICalculator extends Service {

    private final Handler handler = new Handler();
    private Intent intent;
    private long timestamp ;

    private WifiManager wifiManager;

    @Override
    public void onCreate() {
        super.onCreate();
        timestamp = System.currentTimeMillis();
        intent = new Intent(Helper.RSSICalculator_Broadcast);
        handler.removeCallbacks(sendRSSIData);
        handler.postDelayed(sendRSSIData, 1000);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

    }

    private Runnable sendRSSIData = new Runnable() {
        public void run() {
            sendData();
            handler.postDelayed(this, 1000);
        }
    };

    private void sendData() {
        boolean scanSuccess = wifiManager.startScan();
        if(scanSuccess) {
            timestamp = System.currentTimeMillis();
            List<ScanResult> scanResults = wifiManager.getScanResults();
            String[] BSSIDString = new String[scanResults.size()];
            int[] RSSIString = new int[scanResults.size()];
            for(int i=0;i<scanResults.size();i++) {
                BSSIDString[i] = scanResults.get(i).BSSID;
                RSSIString[i] = scanResults.get(i).level;
            }
            Bundle extra = new Bundle();
            extra.putStringArray(Helper.BSSID, BSSIDString);
            extra.putIntArray(Helper.RSSI, RSSIString);
            extra.putLong(Helper.TimeStamp,timestamp);
            intent.putExtras(extra);
            sendBroadcast(intent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(sendRSSIData);
        super.onDestroy();
    }

}
