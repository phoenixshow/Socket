package com.phoenix.bluetoothchat.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;

import com.phoenix.bluetoothchat.connect.AcceptThread;
import com.phoenix.bluetoothchat.connect.ConnectThread;
import com.phoenix.bluetoothchat.connect.ProtocolHandler;

import java.io.UnsupportedEncodingException;

/**
 * 聊天的业务逻辑
 */
public class ChatController {
    private ConnectThread mConnectThread;
    private AcceptThread mAcceptThread;

    /**
     * 网络协议的处理函数（解包和封包的过程）
     */
    private  class ChatProtocol implements ProtocolHandler<String> {
        private static final String CHARSET_NAME = "utf-8";

        /**
         * 封包（把发送的字符串转为byte数组）
         * @param data
         * @return
         */
        @Override
        public byte[] encodePackage(String data) {
            if( data == null) {
                return new byte[0];
            }
            else {
                try {
                    return data.getBytes(CHARSET_NAME);//UTF-8编码
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return new byte[0];
                }
            }
        }

        /**
         * 解包（把获取的byte数组转为字符串）
         * @param netData
         * @return
         */
        @Override
        public String decodePackage(byte[] netData) {
            if( netData == null) {
                return "";
            }
            try {
                return new String(netData, CHARSET_NAME);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return "";
            }
        }
    }

    /**
     * 协议处理
     */
    private ChatProtocol mProtocol = new ChatProtocol();

    /**
     * 与服务器连接进行聊天
     * @param device
     * @param adapter
     * @param handler
     */
    public void startChatWith(BluetoothDevice device, BluetoothAdapter adapter, Handler handler) {
        mConnectThread = new ConnectThread(device,adapter,handler);
        mConnectThread.start();
    }

    /**
     * 等待客户端来连接
     * @param adapter
     * @param handler
     */
    public void waitingForFriends(BluetoothAdapter adapter, Handler handler) {
        mAcceptThread = new AcceptThread(adapter,handler);
        mAcceptThread.start();
    }


    /**
     * 发出消息
     * @param msg
     */
    public void sendMessage(String msg) {
        byte[] data = mProtocol.encodePackage(msg);
        //客户端
        if(mConnectThread != null) {
            mConnectThread.sendData(data);
        }
        //服务端
        else if( mAcceptThread != null) {
            mAcceptThread.sendData(data);
        }
    }


    /**
     * 网络数据解码
     * @param data
     * @return
     */
    public String decodeMessage(byte[] data) {
        return  mProtocol.decodePackage(data);
    }

    /**
     * 停止聊天
     */
    public void stopChat() {
        if(mConnectThread != null) {
            mConnectThread.cancel();
        }
        else if( mAcceptThread != null) {
            mAcceptThread.cancel();
        }
    }

    /**
     * 单例的写法
     */
    private static class ChatControlHolder {
        private static ChatController mInstance = new ChatController();
    }

    public static ChatController getInstance() {
        return ChatControlHolder.mInstance;
    }


}
