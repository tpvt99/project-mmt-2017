package com.exmple.android.projectmmt2017;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;

public class RegisterPage extends AppCompatActivity {

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mProgressBar = (ProgressBar) findViewById(R.id.loading_indicator_register);
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

    }


}
