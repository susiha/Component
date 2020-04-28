package com.susiha.personal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.susiha.annotation.Aroute;
import com.susiha.annotation.Parameter;
import com.susiha.annotation.module.ArouteBean;
import com.susiha.arouteapi.ArouteLoadGroup;
import com.susiha.arouteapi.ArouteLoadPath;
import com.susiha.arouteapi.manager.ARoute;

import java.util.Map;

@Aroute(path = "/personal/PersonalMainActivity")
public class PersonalMainActivity extends AppCompatActivity {
    @Parameter()
    String hello;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_main);

    }
    //去订单页面
    public void jumpOrder(View view) {
        ARoute.getInstance().build("/order/OrderMainActivity")
                .withContext(this).with("name","order")
                .novigation();
    }

    //回首页
    public void jumpMain(View view) {
        ARoute.getInstance()
                .build("/app/MainActivity")
                .withContext(this)
                .with("Hello","susiha").novigation();
    }






}
