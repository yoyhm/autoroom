package com.ycxy.ymh.autoroom.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.ycxy.ymh.autoroom.R;
import com.ycxy.ymh.autoroom.bean.BuildingBean;
import com.ycxy.ymh.autoroom.bean.FloorBean;
import com.ycxy.ymh.autoroom.bean.Person;
import com.ycxy.ymh.autoroom.bean.Total;
import com.ycxy.ymh.autoroom.util.Constant;
import com.ycxy.ymh.autoroom.util.JSONUtils;
import com.ycxy.ymh.autoroom.util.SPUtil;
import com.ycxy.ymh.autoroom.util.TextUtil;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import okhttp3.Call;

public class LoginActivity extends AppCompatActivity{

    private EditText et_username;
    private EditText et_password;
    private CheckBox cb_remb;
    private Button   btn_register;
    private Button   btn_login;

    private String username;
    private String password;
    private static final String TAG = "LoginActivity";
    private Total total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();

    }

    private void initView() {
        Log.d(TAG, "initView: ");
        et_username = (EditText) this.findViewById(R.id.et_username);
        et_password = (EditText) this.findViewById(R.id.et_password);
        cb_remb = (CheckBox) this.findViewById(R.id.cb_remb);
        btn_register = (Button) this.findViewById(R.id.btn_register);
        btn_login = (Button) this.findViewById(R.id.btn_login);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void register() {
        Toast.makeText(this,"注册",Toast.LENGTH_SHORT).show();
        String username = et_username.getText().toString();
        String password = et_password.getText().toString();

        if (TextUtil.isEmpty(username) || TextUtil.isEmpty(password)) {
            return;
        }

        // 联网注册

    }

    private void login() {


        username = et_username.getText().toString();

        password = et_password.getText().toString();

        if (TextUtil.isEmpty(username) || TextUtil.isEmpty(password)) {
            Toast.makeText(this,"账号或者密码不能为空",Toast.LENGTH_SHORT).show();
            return;
        }

        checkOnNet();

    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        this.finish();
    }

    public void checkOnNet(){
        OkHttpUtils
                .post()
                .url(Constant.URLLOGIN)
                .addParams("username", username)
                .addParams("password", password)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.d(TAG, "onError: ");
                        Toast.makeText(LoginActivity.this,"网络异常，请稍后再试",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.d(TAG, "onResponse: " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            // 数据是否返回成功
                            String result = jsonObject.getString("result");
                            if (result.equals("1")) {
                                // 登陆成功,将数据存储到sp中
                                loginSuccess(response);
                            } else {
                                String message = jsonObject.getString("message");
                                Toast.makeText(LoginActivity.this, message,Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 1. 数据保存到本地
     * 2. 关闭本页面，跳转到首页
     * @param response
     */
    public void loginSuccess(String response){
        // 保存数据，方便以后取出数据
        SPUtil.saveInfoToLocal(LoginActivity.this,
                response,Constant.BUILDINGJSON);
        SPUtil.saveInfoToLocal(this,username,Constant.USERNAME);
        // 保存到本地数据库
        String usernameAndPassword = username + "," + password;
        SPUtil.saveInfoToLocal(this,usernameAndPassword,Constant.USERNAMEANDPASSWORD);
        // 解析json数据
        parseJson(response);
        Toast.makeText(this,"登陆成功",Toast.LENGTH_SHORT).show();
        startMainActivity();
    }

    public void parseJson(String response){

        total = JSONUtils.parseTotalJSON(response);

        List<BuildingBean> buildingBeanList = total.getBuilding();
        for (int i = 0; i < buildingBeanList.size(); i++) {
            BuildingBean buildingBean = buildingBeanList.get(i);
            boolean ress = buildingBean.save();
            Log.d(TAG, "parseJson: " + ress);
            for (int j = 0; j < buildingBean.getFloor().size(); j++) {
                List<FloorBean> floorBeanList = buildingBean.getFloor().get(j);
                for (int k = 0; k < floorBeanList.size(); k++) {
                    FloorBean floorBean = floorBeanList.get(k);
                }
            }
        }
    }

    private long mExitTime;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exit() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Toast.makeText(LoginActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        } else {
            //MyConfig.clearSharePre(this, "users");
            finish();
            System.exit(0);
        }
    }
}
