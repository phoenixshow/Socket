package com.phoenix.socket;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class MainActivity extends AppCompatActivity {
    private Button btn;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = (Button) findViewById(R.id.btn);
        tv = (TextView) findViewById(R.id.tv);
        btn.setOnClickListener(new ButtonClickListener());
    }

    class ButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn:
                    scanPorts();
                    break;
                default:
                    break;
            }
        }
    }

    private void scanPorts() {
        new ScanPorts(0, 5000).start();
    }

    class ScanPorts extends Thread {
        private int minPort;
        private int maxPort;

        public ScanPorts(int minPort, int maxPort) {
            this.maxPort = maxPort;
            this.minPort = minPort;
        }

        @Override
        public void run() {
            for (int i = minPort; i < maxPort; i++) {
                Socket socket = new Socket();
                SocketAddress socketAddress = new InetSocketAddress("192.168.56.1", i);
                try {
                    socket.connect(socketAddress, 50);
                    handler.sendEmptyMessage(i);
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            tv.append(String.valueOf(msg.what) + ":OK\n");
//            super.handleMessage(msg);
        }
    };
}
