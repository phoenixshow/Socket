package com.phoenix.datatransmission;

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
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.phoenix.datatransmission.connect.AcceptThread;
import com.phoenix.datatransmission.connect.ConnectThread;
import com.phoenix.datatransmission.connect.Constant;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 0;
    private BlueToothController mController = new BlueToothController();
    private Toast mToast;

    private Toolbar toolbar;
    private ProgressBar progressBar;
    private List<BluetoothDevice> mDeviceList = new ArrayList<>();
    private List<BluetoothDevice> mBondedDeviceList = new ArrayList<>();
    private ListView mListView;
    private DeviceAdapter mAdapter;

    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;

    private Handler mUIHandler = new MyHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        initActionBar();
        setContentView(R.layout.activity_main);

        initToolbar();
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

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if( BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action) ) {
//                setProgressBarIndeterminateVisibility(true);
                progressBar.setVisibility(View.VISIBLE);
                //初始化数据列表
                mDeviceList.clear();
                mAdapter.notifyDataSetChanged();
            }else if( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//                setProgressBarIndeterminateVisibility(false);
                progressBar.setVisibility(View.GONE);
            }else if( BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //找到一个，添加一个
                mDeviceList.add(device);
                mAdapter.notifyDataSetChanged();
            }else if( BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
                int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,0);
                if( scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
//                    setProgressBarIndeterminateVisibility(true);
                    progressBar.setVisibility(View.VISIBLE);
                }else {
//                    setProgressBarIndeterminateVisibility(false);
                    progressBar.setVisibility(View.GONE);
                }
            }else if( BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action) ) {
                BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if( remoteDevice == null ) {
                    showToast("no device");
                    return;
                }
                int status = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,0);
                if( status == BluetoothDevice.BOND_BONDED) {
                    showToast("Bonded " + remoteDevice.getName());
                }else if( status == BluetoothDevice.BOND_BONDING){
                    showToast("Bonding " + remoteDevice.getName());
                }else if(status == BluetoothDevice.BOND_NONE){
                    showToast("Not bond " + remoteDevice.getName());
                }
            }
        }
    };

    private void initToolbar() {
        progressBar = (ProgressBar) findViewById(R.id.pb);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(onMenuItemClickListener);
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            int id = menuItem.getItemId();
            if (id == R.id.enable_visiblity){//打开可见
                mController.enableVisibly(MainActivity.this);
            }else if (id == R.id.find_device){//查找设备
                mAdapter.refresh(mDeviceList);
                mController.findDevice();
                //用于绑定设备
                mListView.setOnItemClickListener(bindDeviceClick);
            }else if (id == R.id.bonded_device){//查看已绑定设备
                mBondedDeviceList = mController.getBondedDeviceList();
                mAdapter.refresh(mBondedDeviceList);
                mListView.setOnItemClickListener(bindedDeviceClick);
            }else if(id == R.id.listening) {//等待连接
                if( mAcceptThread != null) {
                    mAcceptThread.cancel();
                }
                mAcceptThread = new AcceptThread(mController.getAdapter(), mUIHandler);
                mAcceptThread.start();
            }else if(id == R.id.stop_listening) {//停止监听
                if( mAcceptThread != null) {
                    mAcceptThread.cancel();
                }
            }else if(id == R.id.disconnect) {//断开
                if( mConnectThread != null) {
                    mConnectThread.cancel();
                }
            }else if(id == R.id.say_hello) {//Hello
                say("Hello");
            }else if(id == R.id.say_hi) {//Hi
                say("Hi");
            }
            return true;
        }
    };

    private void initUI() {
        mListView = (ListView) findViewById(R.id.device_list);
        mAdapter = new DeviceAdapter(mDeviceList, this);
        mListView.setAdapter(mAdapter);
//        mListView.setOnItemClickListener(bindDeviceClick);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if( mAcceptThread != null) {
            mAcceptThread.cancel();
        }
        if( mConnectThread != null) {
            mConnectThread.cancel();
        }

        unregisterReceiver(mReceiver);
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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.enable_visiblity) {
//            mController.enableVisibly(this);
//        }
//        else if( id == R.id.find_device) {
//            //查找设备
//            mAdapter.refresh(mDeviceList);
//            mController.findDevice();
//            mListView.setOnItemClickListener(bindDeviceClick);
//        }
//        else if (id == R.id.bonded_device) {
//            //查看已绑定设备
//            mBondedDeviceList = mController.getBondedDeviceList();
//            mAdapter.refresh(mBondedDeviceList);
//            mListView.setOnItemClickListener(bindedDeviceClick);
//        }
//        else if( id == R.id.listening) {//等待连接
//            if( mAcceptThread != null) {
//                mAcceptThread.cancel();
//            }
//            mAcceptThread = new AcceptThread(mController.getAdapter(), mUIHandler);
//            mAcceptThread.start();
//        }
//        else if( id == R.id.stop_listening) {
//            if( mAcceptThread != null) {
//                mAcceptThread.cancel();
//            }
//        }
//        else if( id == R.id.disconnect) {
//            if( mConnectThread != null) {
//                mConnectThread.cancel();
//            }
//        }
//        else if( id == R.id.say_hello) {
//            say("Hello");
//        }
//        else if( id == R.id.say_hi) {
//            say("Hi");
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    private void say(String word) {
        if( mAcceptThread != null) {
            try {
                mAcceptThread.sendData(word.getBytes("utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }else if( mConnectThread != null) {
            try {
                mConnectThread.sendData(word.getBytes("utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private AdapterView.OnItemClickListener bindDeviceClick = new AdapterView.OnItemClickListener() {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            BluetoothDevice device = mDeviceList.get(i);
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                device.createBond();
            }
        }
    };

    private AdapterView.OnItemClickListener bindedDeviceClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            BluetoothDevice device = mBondedDeviceList.get(i);
            if( mConnectThread != null) {
                mConnectThread.cancel();
            }

            mConnectThread = new ConnectThread(device, mController.getAdapter(), mUIHandler);
            mConnectThread.start();
        }
    };

//    private void initActionBar() {
//        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
////        getActionBar().setDisplayUseLogoEnabled(false);
//        getSupportActionBar().setDisplayUseLogoEnabled(false);
//        setProgressBarIndeterminate(true);
//        try {
//            ViewConfiguration config = ViewConfiguration.get(this);
//            Field menuKeyField = ViewConfiguration.class
//                    .getDeclaredField("sHasPermanentMenuKey");
//            if (menuKeyField != null) {
//                menuKeyField.setAccessible(true);
//                menuKeyField.setBoolean(config, false);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constant.MSG_START_LISTENING:
//                    setProgressBarIndeterminateVisibility(true);
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case Constant.MSG_FINISH_LISTENING:
//                    setProgressBarIndeterminateVisibility(false);
                    progressBar.setVisibility(View.GONE);
                    break;
                case Constant.MSG_GOT_DATA:
                    showToast("data: "+String.valueOf(msg.obj));
                    break;
                case Constant.MSG_ERROR:
                    showToast("error: "+String.valueOf(msg.obj));
                    break;
                case Constant.MSG_CONNECTED_TO_SERVER:
                    showToast("Connected to Server");
                    break;
                case Constant.MSG_GOT_A_CLINET:
                    showToast("Got a Client");
                    break;
            }
        }
    }
}
