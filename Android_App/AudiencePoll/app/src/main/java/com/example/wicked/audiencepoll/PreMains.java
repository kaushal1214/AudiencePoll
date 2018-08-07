package com.example.wicked.audiencepoll;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import static com.example.wicked.audiencepoll.Constants.IP_ADDRESS;
import static com.example.wicked.audiencepoll.Constants.SHAREDPREFERENCES_NAME;

public class PreMains extends AppCompatActivity {


    SharedPreferences preferences;

    EditText serverIP;
    Button save;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(SHAREDPREFERENCES_NAME,MODE_PRIVATE);

        if(preferences.getString(IP_ADDRESS,"NotFound")!="NotFound")
        {
            //Kill this Activity and start Main activity
            finish();

            //Device has Server IP already
            startActivity(new Intent(PreMains.this, MainActivity.class));
        }
        else {

            //No Ip address found, Get from the user
            setContentView(R.layout.activity_pre_mains);
        }



        serverIP = (EditText) findViewById(R.id.etServerIP);
        save = (Button) findViewById(R.id.bSave);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String IP_Address = serverIP.getText().toString().trim();

                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(IP_ADDRESS,IP_Address);
                editor.commit();

                //Kill this Activity and start Main activity
                finish();

                startActivity(new Intent(PreMains.this, MainActivity.class));

            }
        });
    }
}
