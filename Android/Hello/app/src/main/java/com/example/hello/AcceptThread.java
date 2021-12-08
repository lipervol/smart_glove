package com.example.hello;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;

public class AcceptThread extends Thread{ //InputSteam处理
    private InputStream is;
    private Handler handler;
    private byte[] text;
    private int i;
    private int ch;

    public AcceptThread(InputStream is,Handler handler) {
        this.is = is;
        this.handler = handler;
    }

    @Override
    public void run() {
        super.run();
        while(true){
            synchronized (this){
                text = new byte[1024];
                i=0;
                while (true) {
                    try {
                        if (((ch=is.read())!='\n')){
                            if(ch!=-1) {
                                text[i]=(byte) ch;
                                i++;
                            }
                        }
                        else{
                            Message msg = new Message();
                            msg.obj = new String(text,"GBK");
                            handler.sendMessage(msg);
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
