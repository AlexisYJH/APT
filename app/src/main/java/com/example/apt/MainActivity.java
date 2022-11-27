package com.example.apt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.lib_annotation.Inject;

public class MainActivity extends AppCompatActivity { //TypeElement
    @Inject("name") //VariableElement
    User user;

    @Inject
    Student student; //同样会生成Student_Factory.java文件

    @Override
    protected void onCreate(Bundle savedInstanceState) { //ExecutableElement
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DaggerApplicationComponent.create().inject(this);
        Log.d("TAG", "user: " + user);
        Log.d("TAG", "student: " + student);
        startActivity(new Intent(this, SecondActivity.class));
    }
}