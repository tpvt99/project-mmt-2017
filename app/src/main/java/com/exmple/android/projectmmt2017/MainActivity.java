package com.exmple.android.projectmmt2017;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button mButtonLogin;
    private Button mButtonRegister;
    private Button mButtonAbout;

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(LOG_TAG, "Start in MainActivity");

        mButtonLogin = (Button) findViewById(R.id.login_id);

        mButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              Intent intent = new Intent(getBaseContext(), LoginPage.class);
                startActivity(intent);
            }
        });

/*        mButtonRegister = (Button) findViewById(R.id.register_id);

        mButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), RegisterPage.class);
                startActivity(intent);
            }
        });*/

        mButtonAbout = (Button) findViewById(R.id.about_button);
        mButtonAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), AboutActivity.class);
                startActivity(intent);
            }
        });
    }
}
