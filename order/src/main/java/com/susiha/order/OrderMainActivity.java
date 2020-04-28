package com.susiha.order;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.susiha.annotation.Aroute;
import com.susiha.arouteapi.manager.ARoute;

@Aroute(path = "/order/OrderMainActivity")
public class OrderMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_main);

    }


    //去个人信息
    public void jumppersonal(View view) {


        ARoute.getInstance()
                .build("/personal/PersonalMainActivity")
                .withContext(this)
                .with("Hello","susiha").novigation();
    }

    //回首页
    public void jumpMain(View view) {


        ARoute.getInstance()
                .build("/app/MainActivity")
                .withContext(this)
                .with("Hello","susiha").novigation();
    }
}
