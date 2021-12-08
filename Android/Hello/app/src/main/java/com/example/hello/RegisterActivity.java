package com.example.hello;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText et_regusrname;
    private EditText et_regpasswd1;
    private EditText et_regpasswd2;
    private EditText et_regphone;
    private Button btn_back;
    private Button btn_toreg;
    private String regname;
    private String passwd1;
    private String passwd2;
    private String phonenbr;
    private User registeruser = new User();
    private Handler registerHandler = new Handler();
    private DBLib dbLib = new DBLib();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();

        registerHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 2:
                        Toast.makeText(RegisterActivity.this,"注册成功！",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                        break;
                    case 1:
                        Toast.makeText(RegisterActivity.this,"该账号已注册！",Toast.LENGTH_SHORT).show();
                        break;
                    case 0:
                        Toast.makeText(RegisterActivity.this,"注册失败,请检查网络设置！",Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });

        btn_toreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                regname = et_regusrname.getText().toString().trim();
                passwd1 = et_regpasswd1.getText().toString().trim();
                passwd2 = et_regpasswd2.getText().toString().trim();
                phonenbr = et_regphone.getText().toString().trim();

                if(TextUtils.isEmpty(regname)){
                    Toast.makeText(RegisterActivity.this,"账号不能为空！",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(passwd1)){
                    Toast.makeText(RegisterActivity.this,"密码不能为空！",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!passwd1.equals(passwd2)){
                    Toast.makeText(RegisterActivity.this,"两次输入密码不同！",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(phonenbr)){
                    Toast.makeText(RegisterActivity.this,"联系电话不能为空！",Toast.LENGTH_SHORT).show();
                    return;
                }

                registeruser.setName(regname);
                registeruser.setPasswd(passwd1);
                registeruser.setPhone(phonenbr);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        switch (dbLib.checkrep(registeruser)){
                            case 0:
                                msg.what=0;
                                break;
                            case 1:
                                msg.what=1;
                                break;
                            case 2:
                                if(dbLib.adduser(registeruser)) msg.what=2;
                                else msg.what=0;
                                break;
                        }
                        registerHandler.sendMessage(msg);
                    }
                }).start();

            }
        });
    }

    private void initView(){
        et_regusrname = findViewById(R.id.editText_registerusrname);
        et_regpasswd1 = findViewById(R.id.editText_registerpasswd1);
        et_regpasswd2 = findViewById(R.id.editText_registerpasswd2);
        et_regphone = findViewById(R.id.editText_registerphone);
        btn_back = findViewById(R.id.button_back);
        btn_toreg = findViewById(R.id.button_toregister);
    }
}
