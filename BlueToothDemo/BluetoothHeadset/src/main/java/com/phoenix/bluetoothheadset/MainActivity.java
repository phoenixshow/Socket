package com.phoenix.bluetoothheadset;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private BluetoothHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helper = new BluetoothHelper(this);
    }

    public void mute(View view){
        helper.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        helper.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        helper.stop();
    }
}
