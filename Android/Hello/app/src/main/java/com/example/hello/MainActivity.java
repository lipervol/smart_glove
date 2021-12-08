package com.example.hello;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.dynamicanimation.animation.SpringAnimation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Array;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private BluetoothAdapter bluetoothadapter;
    private static final int REQUEST_ENABLE = 1;
    private List<String> blutoothnamelist = new ArrayList<>();
    private List<String> blutoothaddresslist = new ArrayList<>();
    private TextView textView3;
    private TextView textView5;
    private ArrayAdapter<String> arrayAdapter;
    private BluetoothDevice selectDevice;
    private BluetoothSocket clientSocket;
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private InputStream is;
    private AcceptThread thread;
    private Handler handler;
    private TextView textView4;
    private TextView textView8;
    private DataReceive dataReceive = new DataReceive();
    private String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS};
    private static final int OPEN_SET_REQUEST_CODE = 100;

    private String[] label = {"零","一","二","三","四","五","六","七","八","九"};
    private MediaPlayer mediaPlayer = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        textView3 = findViewById(R.id.textView3);
        textView5 = findViewById(R.id.textView5);
        bluetoothadapter = BluetoothAdapter.getDefaultAdapter();
        textView4 = findViewById(R.id.textView4);
        textView8 = findViewById(R.id.textView8);
        arrayAdapter = new ArrayAdapter(MainActivity.this, R.layout.mytextview,blutoothnamelist);
        textView4.setMovementMethod(ScrollingMovementMethod.getInstance());
        textView5.setMovementMethod(ScrollingMovementMethod.getInstance());
        dataReceive.dataInit();
        handler = new Handler() { //利用线程更新显示
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if(dataReceive.dataCheck(msg.obj+"")){
                    textView4.setText(getTimeStamp()+'\n'+msg.obj);
                }
                try {
                    Log.e("lpb", "id:"+dataReceive.id);
                    dataReceive.dataInsert(msg.obj+"");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(dataReceive.stats){
                    float[][] output = new float[1][10];
                    int loc=0;
                    TFLiteLoader.newInstance(getApplicationContext()).get().run(dataReceive.data,output);
                    dataReceive.dataInit();
                    loc = sortOutput(output,10);
                    if(output[0][loc]>0.5){
                        textView8.setText(textView8.getText()+label[loc]+"("+String.valueOf(output[0][loc])+")"+"  ");
                        try {
                            VoicePlayer(MainActivity.this,String.valueOf(loc)+".mp3");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        Toast.makeText(MainActivity.this,"无效手势", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        initPermissions();
        setbuttonicon();
        registerReceiver();
        }

    //蓝牙适配器初始化
    private void bluetoothinit(){
        if(!bluetoothadapter.isEnabled()){
            Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enabler, REQUEST_ENABLE);
        }
        setbuttonicon();
    }

    //检测蓝牙连接状态设置按钮图标
    @SuppressLint("UseCompatLoadingForDrawables")
    private void setbuttonicon() {
        if(bluetoothadapter.isEnabled()){
            button.setForeground(getDrawable(R.drawable.ic_baseline_bluetooth_24));
        }
        else{
            button.setForeground(getDrawable(R.drawable.ic_baseline_bluetooth_disabled_24));
        }
    }

    //点击按钮
    public void bluetoothstart(View view) {
        bluetoothinit();
        if(!bluetoothadapter.isDiscovering()) {
            bluetoothadapter.startDiscovery();
        }
        else {
            bluetoothadapter.cancelDiscovery();
        }
        View inflate = getLayoutInflater().inflate(R.layout.mypopup, null);
        ListView listView = inflate.findViewById(R.id.lv1);
        listView.setAdapter(arrayAdapter);
        PopupWindow popupWindow = new PopupWindow(inflate, 800,ViewGroup.LayoutParams.WRAP_CONTENT,true);
        popupWindow.setBackgroundDrawable(getDrawable(R.drawable.listbackground));
        popupWindow.showAsDropDown(view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (bluetoothadapter.isDiscovering()) {
                    bluetoothadapter.cancelDiscovery();
                }
                selectDevice=bluetoothadapter.getRemoteDevice(blutoothaddresslist.get(i));
                try{
                    clientSocket=selectDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                    clientSocket.connect();
                    is=clientSocket.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(is != null){
                    thread = new AcceptThread(is,handler);
                    thread.start();
                    Toast.makeText(MainActivity.this,"连接成功", Toast.LENGTH_SHORT).show();
                    textView3.setText(blutoothnamelist.get(i));
                    textView5.setText(blutoothaddresslist.get(i));
                }
                else{
                    Toast.makeText(MainActivity.this,"连接失败",Toast.LENGTH_SHORT).show();
                }
            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                bluetoothadapter.cancelDiscovery();
                }
        });
    }

    //接收适配器
    private final BroadcastReceiver bluetoothreceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice scanDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(scanDevice == null || scanDevice.getName() == null) return;
                if(blutoothnamelist.contains(scanDevice.getName())) return;
                blutoothnamelist.add(scanDevice.getName());
                blutoothaddresslist.add(scanDevice.getAddress());
                arrayAdapter.notifyDataSetChanged();
                //Log.e("lpb", "搜索到了: name="+scanDevice.getName()+"  address="+scanDevice.getAddress());
            }
        }

    };

    //注册接收
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothreceiver, filter);
    }

    //获取最大值位置
    private int sortOutput(float[][] out,int len){
        float max=0;
        int re=0;
        for(int i=0;i<len;i++){
            if(out[0][i]>max) {
                max=out[0][i];
                re=i;
            }
        }
        return re;
    }

    //获取时间戳
    private String getTimeStamp(){
        long time = System.currentTimeMillis();
        Date data = new Date(time);
        DateFormat df = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");
        return df.format(data);
    }

    //获取定位权限
    private void initPermissions() {
        if (lacksPermission(permissions)) {
            ActivityCompat.requestPermissions(this, permissions, OPEN_SET_REQUEST_CODE);
        } else {
        }
    }

    //检查是否有定位权限
    public boolean lacksPermission(String[] permissions) {
        for (String permission : permissions) {
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case OPEN_SET_REQUEST_CODE:
                if (grantResults.length > 0) {
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"获取定位权限失败",Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                } else {
                    Toast.makeText(this,"获取定位权限失败",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    //播放声音
    private void VoicePlayer(Context context,String voicepath) throws IOException {
        AssetFileDescriptor fd = context.getAssets().openFd(voicepath);
        mediaPlayer.reset();
        mediaPlayer.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength());
        mediaPlayer.prepare();
        mediaPlayer.start();
    }

    //顶部菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu,menu);
        return true;
    }

    //跳转到About界面
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu1:
                if(clientSocket!=null) {
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                textView3.setText("设备名称");
                textView5.setText("设备地址");
            case R.id.menu2:
                if(dataReceive!=null) dataReceive.dataInit();
                textView4.setText("接收数据在此显示...");
                textView8.setText("识别结果在此显示...\n");
                break;
            case R.id.menu3:
                blutoothnamelist = new ArrayList<>();
                blutoothaddresslist = new ArrayList<>();
                arrayAdapter = new ArrayAdapter(MainActivity.this, R.layout.mytextview,blutoothnamelist);
                break;
            case R.id.menu4:
                Intent intent = new Intent(this, About.class);
                startActivity(intent);
                break;
            case R.id.menu5:
                finish();
                break;
            default:
        }
        return true;
    }

}