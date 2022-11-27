package com.example.apt;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lib_annotation.Inject;

/**
 * @author AlexisYin
 */
public class SecondActivity extends AppCompatActivity {
    @Inject
    User user2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerApplicationComponent.create().inject(this);
        Log.d("TAG", "user2: " + user2);
    }
}
