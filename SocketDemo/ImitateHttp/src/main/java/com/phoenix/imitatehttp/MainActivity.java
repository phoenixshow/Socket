package com.phoenix.imitatehttp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private Button btn;
    private String host = "cloud.bmob.cn";
    private int port = 80;
    private String masthead = "GET /c284de47021c899d/getMemberBySex?sex=boy HTTP/1.1\r\nHost: cloud.bmob.cn\r\n\r\n";

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
                    sockeyTest();
                    break;
                default:
                    break;
            }
        }
    }

    private void sockeyTest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(host, port);
                    OutputStream outputStream = socket.getOutputStream();
                    OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                    BufferedWriter bw = new BufferedWriter(writer);
                    bw.write(masthead);
                    bw.flush();

                    InputStream inputStream = socket.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader br = new BufferedReader(inputStreamReader);
                    StringBuffer result = new StringBuffer();
                    String line = "";
                    while((line = br.readLine()) != null){
                        result.append(line);
                        Log.e("TAG", line);
                    }
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
