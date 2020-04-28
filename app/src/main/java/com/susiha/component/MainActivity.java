package com.susiha.component;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.susiha.annotation.Aroute;
import com.susiha.arouteapi.manager.ARoute;

@Aroute(path = "/app/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void jumppersonal(View view) {

        ARoute.getInstance().build("/personal/PersonalMainActivity")
                .withContext(this).with("name","personal")
                .novigation();
    }

    public void jumpOrder(View view) {
        ARoute.getInstance().build("/order/OrderMainActivity")
                .withContext(this).with("name","order")
                .novigation();
    }
}
