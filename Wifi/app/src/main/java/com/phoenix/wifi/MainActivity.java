package com.phoenix.wifi;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final int WIFI_SCAN_PERMISSION_CODE = 0;
    private TextView tv;
    private WifiUtils wifiUtils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.tv);
        wifiUtils = new WifiUtils(this);
    }

    public void start(View view){
        wifiUtils.startWifi();
    }

    public void stop(View view){
        wifiUtils.stopWifi();
    }

    public void check(View view){
        tv.append(getString(R.string.current_state, new Object[]{wifiUtils.checkWifiState()}));
    }

    public void scan(View view){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission_group.LOCATION)!= PackageManager.PERMISSION_GRANTED){
            // 获取wifi连接需要定位权限,没有获取权限
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_WIFI_STATE,
            },WIFI_SCAN_PERMISSION_CODE);
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case WIFI_SCAN_PERMISSION_CODE:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // 允许
//                    initData();
                    tv.setText(wifiUtils.scan());
                }else{
                    // 不允许
                    Toast.makeText(this,getString(R.string.permisstion_deny),Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void settings(View view){
        startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
    }

    public void connect(View view){
        tv.setText(wifiUtils.connect());
    }

    public void disconnect(View view){
        wifiUtils.disconnectWifi();
    }

    public void checkNetworkState(View view){
        tv.setText(wifiUtils.checkNetWorkState());
    }

    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }

    protected void onResume() {
        registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            tv.setText(wifiUtils.getScanResult());
        }
    };
}
