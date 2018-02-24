package com.example.qian.cs446project;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onJoinSession(View v) {
        Intent joinSessionIntent = new Intent(this, ChooseSessionActivity.class);
        startActivity(joinSessionIntent);
    }

}
