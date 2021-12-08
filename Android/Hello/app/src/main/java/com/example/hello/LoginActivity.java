package com.example.hello;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;

public class LoginActivity extends AppCompatActivity {

    private EditText et_username;
    private EditText et_password;
    private Button btn_login;
    private Button btn_register;
    private String userName;
    private String password;
    private DBLib dbLib = new DBLib();
    private User userinfo = new User();
    private Handler loginHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();

        loginHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 0:
                        Toast.makeText(LoginActivity.this,"无法连接服务器，请检查网络设置！",Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Toast.makeText(LoginActivity.this,"登录成功！",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                        break;
                    case 2:
                        Toast.makeText(LoginActivity.this,"账号或密码错误！",Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        btn_login.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("HandlerLeak")
            @Override
            public void onClick(View view) {

                userinfo.setName(et_username.getText().toString().trim());
                userinfo.setPasswd(et_password.getText().toString().trim());

                if(TextUtils.isEmpty(userinfo.getName())){
                    Toast.makeText(LoginActivity.this,"请输入账号！",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(userinfo.getPasswd())){
                    Toast.makeText(LoginActivity.this,"请输入密码！",Toast.LENGTH_SHORT).show();
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        msg.what=dbLib.checkUser(userinfo);
                        loginHandler.sendMessage(msg);
                    }
                }).start();

            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });

    }

    private void initView(){
        et_username = findViewById(R.id.editText_username);
        et_password = findViewById(R.id.editText_password);
        btn_login = findViewById(R.id.button_login);
        btn_register = findViewById(R.id.button_register);
    }


}
