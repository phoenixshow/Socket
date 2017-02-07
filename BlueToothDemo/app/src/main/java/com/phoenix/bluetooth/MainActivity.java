package com.phoenix.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 0;
    private BlueToothController mController = new BlueToothController();
    private Toast mToast;

    //为了监听蓝牙的真实状态，需要监听广播
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //如果没有取到status的值，则默认返回-1
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            switch (state) {
                case BluetoothAdapter.STATE_OFF://蓝牙关闭
                    showToast("STATE_OFF");
                    break;
                case BluetoothAdapter.STATE_ON://蓝牙打开
                    showToast("STATE_ON");
                    break;
                case BluetoothAdapter.STATE_TURNING_ON://蓝牙正在打开
                    showToast("STATE_TURNING_ON");
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF://蓝牙正在关闭
                    showToast("STATE_TURNING_OFF");
                    break;
                default:
                    showToast("Unkown STATE");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, filter);
    }

    public void isSupportBlueTooth(View view) {
        boolean ret = mController.isSupportBlueTooth();
        showToast("support Bluetooth? " + ret);
    }

    public void isBlueToothEnable(View view) {
        boolean ret = mController.getBlueToothStatus();
        showToast("Bluetooth enable? " + ret);
    }

    public void requestTurnOnBlueTooth(View view){
        mController.turnOnBlueTooth(this, REQUEST_CODE);
    }

    public void turnOffBlueTooth(View view){
        mController.turnOffBlueTooth();
    }

    private void showToast(String text) {
        //因为都是UI线程，不存在多线程的问题，所以没有加同步
        if (mToast == null){
            mToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        }else{
            mToast.setText(text);
        }
        mToast.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            showToast("打开成功");
        }else {
            showToast("打开失败");
        }
    }
}
