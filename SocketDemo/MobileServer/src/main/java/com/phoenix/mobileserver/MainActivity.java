package com.phoenix.mobileserver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private Button btn;

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
                serverSocket.bind(new InetSocketAddress("192.168.0.100", 8888));

                while (true) {
                    Log.e("TAG", "等待连接");
                    Socket socket = serverSocket.accept();
                    String remind = "connect success";
                    remind = "HTTP/1.0 200 OK\r\nContent-Type:text/html\r\nContent-Length:" + remind.getBytes().length + "\r\n\r\n" + remind + "\r\n";
                    Log.e("TAG", "连接成功");
                    OutputStream os = socket.getOutputStream();
                    os.write(remind.getBytes());
                    os.flush();
                    socket.close();
                    Log.e("TAG", "socket已关闭");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
