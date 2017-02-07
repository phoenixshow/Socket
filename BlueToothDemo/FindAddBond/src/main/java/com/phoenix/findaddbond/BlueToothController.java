package com.phoenix.findaddbond;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

/**
 * 蓝牙控制器
 */
public class BlueToothController {
    private BluetoothAdapter mAdapter;

    public BlueToothController() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * 打开蓝牙
     * @param activity
     */
    public void turnOnBlueTooth(Activity activity, int requestCode) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, requestCode);
//        mAdapter.enable();
    }

    /**
     * 打开蓝牙可见性
     */
    public void enableVisibly(Context context) {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);//300秒=5分钟，即设备在5分钟内是可见的
        context.startActivity(discoverableIntent);//这里不需要startActivityForResult，因为它的设备不止一个，startActivityForResult不能完成功能，它是通过广播来做的
    }

    /**
     * 查找设备
     */
    public void findDevice() {
        assert (mAdapter != null);
        mAdapter.startDiscovery();
    }

    /**
     * 获取绑定设备
     */
    public List<BluetoothDevice> getBondedDeviceList() {
        return new ArrayList<>(mAdapter.getBondedDevices());
    }
}
