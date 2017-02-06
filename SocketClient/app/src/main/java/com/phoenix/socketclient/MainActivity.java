package com.phoenix.socketclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Button btn1;
    private Button btn2;

    private ConnectedThread connectedThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        btn1.setOnClickListener(new ButtonClickListener());
        btn2.setOnClickListener(new ButtonClickListener());
    }

    class ButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn1:
                    new ClientSocketThread().start();
                    break;
                case R.id.btn2:
                    String data = "hello server: " + new Random().nextInt();
                    connectedThread.write(data.getBytes());
                    break;
                default:
                    break;
            }
        }
    }

    class ClientSocketThread extends Thread {
        @Override
        public void run() {
            try {
                Socket socket = new Socket("127.0.0.1", 1234);
                connected(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void connected(Socket socket) {
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
        Log.e("TAG", "client connect");
    }

    class ConnectedThread extends Thread {
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public ConnectedThread(Socket socket) {
            this.socket = socket;
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    Log.e("TAG", "client read: " + new String(buffer, 0, bytes));
                    Log.e("TAG", "----------------------------------------------");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                outputStream.write(buffer);
                Log.e("TAG", "client write: " + new String(buffer));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
