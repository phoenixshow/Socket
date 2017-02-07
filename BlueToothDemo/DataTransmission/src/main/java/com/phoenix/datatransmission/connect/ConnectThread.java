package com.phoenix.datatransmission.connect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * 连接服务端线程
 */
public class ConnectThread extends Thread {
    private static final UUID MY_UUID = UUID.fromString(Constant.CONNECTTION_UUID);
    private final BluetoothSocket mSocket;
//    private final BluetoothDevice mDevice;
    private BluetoothAdapter mBluetoothAdapter;
    private final Handler mHandler;
    private ConnectedThread mConnectedThread;

    public ConnectThread(BluetoothDevice device, BluetoothAdapter adapter, Handler handler) {
//        mDevice = device;
        mBluetoothAdapter = adapter;
        mHandler = handler;

        BluetoothSocket tmp = null;
        try {
            //从远程设备中创建一个连接
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) { }
        mSocket = tmp;
    }

    public void run() {
        //把发现设备关闭掉，如果同时发现设备、和传输数据会有冲突，会降低蓝牙传输的效率，所以要关掉
        mBluetoothAdapter.cancelDiscovery();

        try {
            //连接服务器，也是个阻塞函数
            mSocket.connect();
        } catch (Exception connectException) {
            connectException.printStackTrace();
            //如果出错要发送消息通知UI线程
            mHandler.sendMessage(mHandler.obtainMessage(Constant.MSG_ERROR, connectException));
            try {
                mSocket.close();
            } catch (IOException closeException) {
                closeException.printStackTrace();
            }
            return;
        }

        manageConnectedSocket(mSocket);
    }

    private void manageConnectedSocket(BluetoothSocket mSocket) {
        mHandler.sendEmptyMessage(Constant.MSG_CONNECTED_TO_SERVER);
        if( mConnectedThread != null) {
            mConnectedThread.cancel();
        }
        mConnectedThread = new ConnectedThread(mSocket, mHandler);
        mConnectedThread.start();
    }

    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendData(byte[] data) {
        if( mConnectedThread!=null){
            mConnectedThread.write(data);
        }
    }
}