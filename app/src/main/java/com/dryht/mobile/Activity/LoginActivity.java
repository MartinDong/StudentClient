package com.dryht.mobile.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dryht.mobile.R;
import com.dryht.mobile.Util.Utils;
import com.xuexiang.xui.utils.StatusBarUtils;
import com.xuexiang.xui.widget.button.ButtonView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText account;
    private EditText password;
    private TextView forget;
    private ButtonView login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initcomponent();
        checkPermission();

        // 在要调用权限的activity中插入该方法。可以写到onCreate()中。
    // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // 判断是否有这个权限，是返回PackageManager.PERMISSION_GRANTED，否则是PERMISSION_DENIED
            // 这里我们要给应用授权所以是!= PackageManager.PERMISSION_GRANTED
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // 如果应用之前请求过此权限但用户拒绝了请求,且没有选择"不再提醒"选项 (后显示对话框解释为啥要这个权限)，此方法将返回 true。
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {


                } else {

                    // requestPermissions以标准对话框形式请求权限。123是识别码（任意设置的整型），用来识别权限。应用无法配置或更改此对话框。
                    //当应用请求权限时，系统将向用户显示一个对话框。当用户响应时，系统将调用应用的 onRequestPermissionsResult() 方法，向其传递用户响应。您的应用必须替换该方法，以了解是否已获得相应权限。回调会将您传递的相同请求代码传递给 requestPermissions()。
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            123);

                }
            }

        }
        //获取权限（如果没有开启权限，会弹出对话框，询问是否开启权限）
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION },
                    1315);
        } else {

        }
        login.setOnClickListener(new loginListener());
        forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,ChangePwdActivity.class);
                startActivity(intent);
            }
        });

    }

    private void checkPermission() {

        if (Build.VERSION.SDK_INT >= 23) {
            int write = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (write != PackageManager.PERMISSION_GRANTED || read != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 300);
            } else {
                String name = "CrashDirectory";
                File file1 = new File(Environment.getExternalStorageDirectory(), name);
                if (file1.mkdirs()) {
                    Log.i("wytings", "permission -------------> " + file1.getAbsolutePath());
                } else {
                    Log.i("wytings", "permission -------------fail to make file ");
                }
            }
        } else {
            Log.i("wytings", "------------- Build.VERSION.SDK_INT < 23 ------------");
        }
    }

    private void initcomponent(){
        account = findViewById(R.id.account);
        password = findViewById(R.id.password);
        forget = findViewById(R.id.forget);
        login = findViewById(R.id.login_btn);
        StatusBarUtils.setStatusBarLightMode(this);
        getWindow().setStatusBarColor(getResources().getColor(R.color.xui_btn_blue_normal_color));
//        this.getWindow().setNavigationBarColor(getResources().getColor(R.color.thiscolor));
//        StatusBarUtils.setNavigationBarColor(this,getResources().getColor(R.color.thiscolor));
    }


    private class loginListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            OkHttpClient mOkHttpClient=new OkHttpClient();

            FormBody mFormBody=new FormBody.Builder()
                    .add("account", String.valueOf(account.getText()))
                    .add("password",String.valueOf(password.getText()))
                    .build();

            Request mRequest=new Request.Builder()
                    .url(Utils.generalUrl+"login/")
                    .post(mFormBody)
                    .build();

            mOkHttpClient.newCall(mRequest).enqueue(new Callback(){
                @Override
                public void onResponse(@NotNull okhttp3.Call call, @NotNull Response response) throws IOException {
                    Looper.prepare();
                    //String转JSONObject
                    JSONObject result = null;
                    try {
                        result = new JSONObject(response.body().string());
                        //取数据
                        if(result.get("auth")!=null)
                        {
                            Toast.makeText(getBaseContext(),String.valueOf(result.get("result")),Toast.LENGTH_SHORT).show();
                            //步骤1：创建一个SharedPreferences对象
                            SharedPreferences sharedPreferences= getSharedPreferences("data", Context.MODE_PRIVATE);
                            //步骤2： 实例化SharedPreferences.Editor对象
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            //步骤3：将获取过来的值放入文件
                            editor.putString("auth", result.get("auth").toString());
                            //获取登录人得身份1为学生0为老师
                            editor.putString("identity", result.get("identity").toString());
                            editor.putString("name", result.get("name").toString());
                            editor.putString("intro", result.get("intro").toString());
                            editor.putString("pic", result.get("pic").toString());
                            //步骤4：提交
                            editor.commit();
                            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                            startActivity(intent);
                        }
                        else
                        {
                            Toast.makeText(LoginActivity.this,String.valueOf(result.get("result")),Toast.LENGTH_SHORT).show();

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Looper.loop();

                }

                @Override
                public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                    Looper.prepare();
                    Toast.makeText(LoginActivity.this,"登录失败",Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            });
        }
    }


}
