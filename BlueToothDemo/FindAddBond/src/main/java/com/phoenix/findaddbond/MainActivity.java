package com.phoenix.findaddbond;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){//开始查找
                //开启一个圆形进度条，开始转动
                progressBar.setVisibility(View.VISIBLE);
                //初始化数据列表
                mDeviceList.clear();
                mAdapter.notifyDataSetChanged();
            }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){//结束查找
                //查找完成后要把进度条隐藏
                progressBar.setVisibility(View.GONE);
            }else if (BluetoothDevice.ACTION_FOUND.equals(action)){//查找到设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //找到一个，添加一个
                mDeviceList.add(device);
                mAdapter.notifyDataSetChanged();
            }else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)){//设备扫描模式改变
                int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, 0);
                //如果是可发现的
                if (scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
                    //让进度条开始转动
                    progressBar.setVisibility(View.VISIBLE);
                }else {
                    //如果时间到了、不可见了，把进度条关闭
                    progressBar.setVisibility(View.GONE);
                }
            }else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){//绑定状态
                BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (remoteDevice == null){
                    showToast("no device");
                    return;
                }
                int status = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0);
                if (status == BluetoothDevice.BOND_BONDED){
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
                //对于已经绑定好的设备不需要做任何事情，所以设为空，或者在这里把它解绑定也可以
                mListView.setOnItemClickListener(null);
            }
            return true;
        }
    };

    private void initUI() {
        mListView = (ListView) findViewById(R.id.device_list);
        mAdapter = new DeviceAdapter(mDeviceList, this);
        mListView.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private void showToast(String text) {
        if (mToast == null){
            mToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        }else{
            mToast.setText(text);
        }
        mToast.show();
    }

    private AdapterView.OnItemClickListener bindDeviceClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice device = mDeviceList.get(position);
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                device.createBond();
            }
        }
    };
}
