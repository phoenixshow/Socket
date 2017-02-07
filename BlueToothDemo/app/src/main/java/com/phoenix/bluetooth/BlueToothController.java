package com.phoenix.bluetooth;

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
     * 是否支持蓝牙，模拟器不支持蓝牙
     * @return true支持，false不支持
     */
    public boolean isSupportBlueTooth() {
        if (mAdapter != null){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 判断当前蓝牙状态
     * @return true打开，false关闭
     */
    public boolean getBlueToothStatus() {
        //直接调用方法有可能会空指针，所以这里加个断言
//        assert (mAdapter != null);
        return mAdapter != null ? mAdapter.isEnabled() : false;
    }

    /**
     * 打开蓝牙，需要唤起一个界面，这是官方推荐的方式
     * @param activity
     */
    public void turnOnBlueTooth(Activity activity, int requestCode) {
        //通过Intent去获取蓝牙
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        //需要知道它的返回结果
        activity.startActivityForResult(intent, requestCode);
//        mAdapter.enable();//点击开关的时候由系统去调这个方法，通常情况下程序不要主动去调这个函数，会在不经过人为控制的情况下打开蓝牙，Google不推荐这种方式
    }

    /**
     * 关闭蓝牙
     */
    public void turnOffBlueTooth() {
        mAdapter.disable();
    }
}
