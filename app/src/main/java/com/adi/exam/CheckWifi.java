package com.adi.exam;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.adi.exam.fragments.WifiFragment;

public class CheckWifi extends AppCompatActivity {
    private FragmentManager fm;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_fagment);

        fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        WifiFragment wifiFragment= WifiFragment.newInstance("Login");
        ft.replace(R.id.main_container,wifiFragment);
        ft.commit();
    }
}
