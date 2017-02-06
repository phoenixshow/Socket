package com.phoenix.socketserver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Button btn;

    private ConnectedThread connectedThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new ButtonClickListener());
    }

    class ButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn:
                    new ServerSocketThread().start();
                    break;
                default:
                    break;
            }
        }
    }

    class ServerSocketThread extends Thread {
        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket();
                serverSocket.bind(new InetSocketAddress("127.0.0.1", 1234));
                while (true) {
                    Log.e("TAG", "server wait");
                    Socket socket = serverSocket.accept();
                    connected(socket);
                    break;
                }
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
                    Log.e("TAG", "server read: " + new String(buffer, 0, bytes));

                    String data = "hello client: " + new Random().nextInt();
                    write(data.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                outputStream.write(buffer);
                Log.e("TAG", "server write: " + new String(buffer));
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
