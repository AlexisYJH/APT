package com.example.manualcode;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lib_annotation.Inject;

public class MainActivity extends AppCompatActivity { //TypeElement
    @Inject("lilwen") //VariableElement
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) { //ExecutableElement
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DaggerApplicationComponent.create().inject(this);
        Log.d("TAG", "user: " + user);
    }
}