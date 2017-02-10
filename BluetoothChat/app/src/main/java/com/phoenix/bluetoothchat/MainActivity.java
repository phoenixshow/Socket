package com.phoenix.bluetoothchat;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.phoenix.bluetoothchat.connect.Constant;
import com.phoenix.bluetoothchat.controller.BlueToothController;
import com.phoenix.bluetoothchat.controller.ChatController;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 0;
    private List<BluetoothDevice> mDeviceList = new ArrayList<>();
    private List<BluetoothDevice> mBondedDeviceList = new ArrayList<>();

    private BlueToothController mController = new BlueToothController();
    private ListView mListView;
    private DeviceAdapter mAdapter;
    private Toast mToast;

    private View mChatPanel;
    private Button mSendBt;
    private EditText mInputBox;
    private TextView mChatContent;
    private StringBuilder mChatText = new StringBuilder();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initActionBar();
        setContentView(R.layout.activity_main);
        initUI();

        registerBluetoothReceiver();
        mController.turnOnBlueTooth(this, REQUEST_CODE);

    }

    private void registerBluetoothReceiver() {
        IntentFilter filter = new IntentFilter();
        //开始查找
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        //结束查找
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //查找设备
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        //设备扫描模式改变
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        //绑定状态
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        registerReceiver(mReceiver, filter);
    }

    private Handler mUIHandler = new MyHandler();

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("TAG", "广播收到当前action--->"+action);
            //开始发现设备
            if( BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action) ) {
                Log.e("TAG", "广播--->开始发现设备");
                setProgressBarIndeterminateVisibility(true);
                //初始化数据列表
                mDeviceList.clear();
                mAdapter.notifyDataSetChanged();
            }
            //发现设备结束
            else if( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.e("TAG", "广播--->发现设备结束");
                setProgressBarIndeterminateVisibility(false);
            }
            //找到一个设备
            else if( BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.e("TAG", "广播--->找到一个设备");
                //取出设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //找到一个，添加一个
                mDeviceList.add(device);
                //更新列表
                mAdapter.notifyDataSetChanged();
            }
            //设备扫描模式改变
            else if( BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
                Log.e("TAG", "广播--->设备扫描模式改变");
                int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,0);
                if( scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    setProgressBarIndeterminateVisibility(true);
                }
                else {
                    setProgressBarIndeterminateVisibility(false);
                }
            }
            //绑定了设备
            else if( BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action) ) {
                Log.e("TAG", "广播--->绑定了设备");
                //从Parcelable中去获取一个远程设备
                BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if( remoteDevice == null ) {
                    showToast("no device");
                    return;
                }
                //得到绑定的状态
                int status = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,0);
                //绑定成功
                if( status == BluetoothDevice.BOND_BONDED) {
                    showToast("Bonded " + remoteDevice.getName());
                }
                //绑定中
                else if( status == BluetoothDevice.BOND_BONDING){
                    showToast("Bonding " + remoteDevice.getName());
                }
                //未绑定
                else if(status == BluetoothDevice.BOND_NONE){
                    showToast("Not bond " + remoteDevice.getName());
                }
            }
        }
    };


    private void initUI() {
        mListView = (ListView) findViewById(R.id.device_list);
        mAdapter = new DeviceAdapter(mDeviceList, this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(bindDeviceClick);
        mChatPanel = findViewById(R.id.chat_panel);
        mSendBt = (Button) findViewById(R.id.bt_send);
        mSendBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取输入框的文字
                String ext = mInputBox.getText().toString();
                //调用业务层发送消息
                ChatController.getInstance().sendMessage(ext);
                //把现有的聊天记录追加一条
                mChatText.append(ext).append("\n");
                mChatContent.setText(mChatText.toString());
                //清空输入框
                mInputBox.setText("");
            }
        });
        mInputBox = (EditText) findViewById(R.id.chat_edit);
        mChatContent = (TextView) findViewById(R.id.chat_content);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ChatController.getInstance().stopChat();

        unregisterReceiver(mReceiver);
    }

    public void enterChatMode() {
        Log.e("TAG", "进入聊天模式");
        mListView.setVisibility(View.GONE);
        mChatPanel.setVisibility(View.VISIBLE);
    }

    public void exitChatMode() {
        Log.e("TAG", "退出聊天模式");
        mListView.setVisibility(View.VISIBLE);
        mChatPanel.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == REQUEST_CODE) {
            if( resultCode != RESULT_OK) {
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void showToast(String text) {
        if( mToast == null) {
            mToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        }
        else {
            mToast.setText(text);
        }
        mToast.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.enable_visiblity) {//打开设备可见性
            mController.enableVisibly(this);
        }
        else if( id == R.id.find_device) {//查找设备
            Log.e("TAG", "查找设备----》》》退出聊天模式");
            exitChatMode();
            //查找设备
            mAdapter.refresh(mDeviceList);
            mController.findDevice();
            mListView.setOnItemClickListener(bindDeviceClick);
        }
        else if (id == R.id.bonded_device) {
            Log.e("TAG", "查看已绑定设备----》》》退出聊天模式");
            exitChatMode();
            //查看已绑定设备
            mBondedDeviceList = mController.getBondedDeviceList();
            mAdapter.refresh(mBondedDeviceList);
            mListView.setOnItemClickListener(bindedDeviceClick);
        }
        else if( id == R.id.listening) {//等待别人来聊天
            ChatController.getInstance().waitingForFriends(mController.getAdapter(), mUIHandler);
        }
        else if( id == R.id.stop_listening) {
            ChatController.getInstance().stopChat();
            Log.e("TAG", "停止监听----》》》退出聊天模式");
            exitChatMode();
        }
        else if( id == R.id.disconnect) {
            ChatController.getInstance().stopChat();
            Log.e("TAG", "断开连接----》》》退出聊天模式");
            exitChatMode();
        }

        return super.onOptionsItemSelected(item);
    }


    private AdapterView.OnItemClickListener bindDeviceClick = new AdapterView.OnItemClickListener() {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            BluetoothDevice device = mDeviceList.get(i);
            //当版本大于等于4.4时才支持绑定设备
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                device.createBond();
            }
        }
    };

    //点击已绑定设备开始聊天
    private AdapterView.OnItemClickListener bindedDeviceClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            BluetoothDevice device = mBondedDeviceList.get(i);
            ChatController.getInstance().startChatWith(device, mController.getAdapter(),mUIHandler);
        }
    };

    private void initActionBar() {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
//        getActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        setProgressBarIndeterminate(true);
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class
                    .getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constant.MSG_START_LISTENING:
                    setProgressBarIndeterminateVisibility(true);
                    break;
                case Constant.MSG_FINISH_LISTENING:
                    setProgressBarIndeterminateVisibility(false);
                    Log.e("TAG", "Handler结束监听----》》》");
//                    Log.e("TAG", "Handler结束监听----》》》退出聊天模式");
//                    exitChatMode();
                    break;
                case Constant.MSG_GOT_DATA://接收数据
                    byte[] data = (byte[]) msg.obj;
                    //传给业务层处理（业务层调用解包方法处理），拿到要显示的字符串
                    mChatText.append(ChatController.getInstance().decodeMessage(data)).append("\n");
                    mChatContent.setText(mChatText.toString());
                    break;
                case Constant.MSG_ERROR:
                    Log.e("TAG", "Handler错误----》》》退出聊天模式");
                    exitChatMode();
                    showToast("error: "+String.valueOf(msg.obj));
                    break;
                case Constant.MSG_CONNECTED_TO_SERVER://当连上服务端
                    Log.e("TAG", "当连上服务端----》》》进入聊天模式");
                    enterChatMode();//就进入聊天模式
                    showToast("Connected to Server");
                    break;
                case Constant.MSG_GOT_A_CLINET://当收到客户端的连接请求
                    Log.e("TAG", "当收到客户端的连接请求----》》》进入聊天模式");
                    enterChatMode();//就进入聊天模式
                    showToast("Got a Client");
                    break;
            }
        }
    }
}
