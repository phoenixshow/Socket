package com.phoenix.datatransmission.connect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * 服务端监听线程
 */
public class AcceptThread extends Thread {
    private static final String NAME = "BlueToothClass";//连接的名字，可以随便写
    private static final UUID MY_UUID = UUID.fromString(Constant.CONNECTTION_UUID);

    private final BluetoothServerSocket mServerSocket;
    private final BluetoothAdapter mBluetoothAdapter;
    private final Handler mHandler;
    private ConnectedThread mConnectedThread;//新起一个线程来通信（传输数据），当前线程还要继续监听

    public AcceptThread(BluetoothAdapter adapter, Handler handler) {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        mBluetoothAdapter = adapter;
        mHandler = handler;
        BluetoothServerSocket tmp = null;
        try {
            //创建一个服务端的Socket
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) { }
        mServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket = null;
        //服务端一般是不退出的，所以用死循环，当有异常或者手动关闭时才退出
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                //发一个消息，要进入监听状态
                mHandler.sendEmptyMessage(Constant.MSG_START_LISTENING);
                //阻塞等待客户端连接，当有新的客户端连接时获取一个新的Socket
                socket = mServerSocket.accept();
            } catch (IOException e) {
                Log.e("TAG", "<=============AcceptThread run============>");
                e.printStackTrace();
                //当accept失败的时候退出
                mHandler.sendMessage(mHandler.obtainMessage(Constant.MSG_ERROR, e));
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                //把Socket放在方法中管理
                // Do work to manage the connection (in a separate thread)
                manageConnectedSocket(socket);
                /*//注掉下面代码，断开还可再连
                try {
                    mServerSocket.close();
                    mHandler.sendEmptyMessage(Constant.MSG_FINISH_LISTENING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;*/
            }else{
                //获取的Socket对象出问题的时候也会退出
                break;
            }
        }
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        //只支持同时处理一个连接（为了简单起见，服务端只支持一个客户端的连接，实际项目中会使用连接池，这里为了简化代码，当有新的客户端连接时就把原来的客户端连接踢掉）
        if( mConnectedThread != null) {
            mConnectedThread.cancel();
        }
        //发送一个消息：有一个客户端连接过来了
        mHandler.sendEmptyMessage(Constant.MSG_GOT_A_CLINET);
        //创建读取数据的线程
        mConnectedThread = new ConnectedThread(socket, mHandler);
        mConnectedThread.start();
    }

    //取消监听，结束线程
    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            mServerSocket.close();
            mHandler.sendEmptyMessage(Constant.MSG_FINISH_LISTENING);
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