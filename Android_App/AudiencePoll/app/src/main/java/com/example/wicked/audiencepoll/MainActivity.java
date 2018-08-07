package com.example.wicked.audiencepoll;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import static com.example.wicked.audiencepoll.Constants.BROADCAST_NAME;
import static com.example.wicked.audiencepoll.Constants.DEVICE_ID;
import static com.example.wicked.audiencepoll.Constants.IP_ADDRESS;
import static com.example.wicked.audiencepoll.Constants.KEY_ID;
import static com.example.wicked.audiencepoll.Constants.KEY_NAME;
import static com.example.wicked.audiencepoll.Constants.REQUEST_READ_PHONE_STATE;
import static com.example.wicked.audiencepoll.Constants.REST_GET;
import static com.example.wicked.audiencepoll.Constants.REST_POST;
import static com.example.wicked.audiencepoll.Constants.SHAREDPREFERENCES_NAME;
import static com.example.wicked.audiencepoll.Constants.VOTINGS_SUBMITTED;

public class MainActivity extends AppCompatActivity {

    RatingBar ratingBar;
    Spinner names;
    Button submit;

    BroadcastReceiver listArrived, votingSubmitted;
    ArrayList namesList, idList;
    ArrayAdapter<CharSequence> adapter;
    static int position = 0;
    static float rate = 0.0f;

    //Progress bar
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get Sharedpreferences data
        SharedPreferences preferences = getSharedPreferences(SHAREDPREFERENCES_NAME, MODE_PRIVATE);
        String IP_Address = preferences.getString(IP_ADDRESS, "NotFound");

        //Set the Url
        REST_GET = "http://" + IP_Address + ":3300/names";
        REST_POST = "http://" + IP_Address + ":3300/votings";

        Toast.makeText(this, REST_GET, Toast.LENGTH_LONG).show();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        }
        else {
            //Set the unique ID for Device
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

            DEVICE_ID = telephonyManager.getDeviceId();
            //Permission granted
            mainFunction();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //Permission granted
                    mainFunction();
                }
                break;

            default:
                break;
        }
    }

    private void mainFunction() {
        //Set UI
        setContentView(R.layout.activity_main);
        ratingBar = (RatingBar) findViewById(R.id.rbRate);
        names = (Spinner) findViewById(R.id.sNames);
        submit = (Button) findViewById(R.id.bSubmit);

        //Set progress bar
        progressDialog = new ProgressDialog(MainActivity.this);


        if (!DEVICE_ID.isEmpty()) {

            //Enable UI
            setUIEnabled(true);

            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {

                    rate = v;
                }
            });

            names.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    position = i;
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            namesList = new ArrayList();
            idList = new ArrayList();

            namesList.add("Select the Contestant!");
            // Create an ArrayAdapter using the string array and a default spinner layout
            adapter = new ArrayAdapter<CharSequence>(MainActivity.this, R.layout.support_simple_spinner_dropdown_item, namesList);
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            names.setAdapter(adapter);


            //Show progressdialog
            progressDialog.setMessage("Loading...");
            progressDialog.show();

            //Request to Server
            RestCalls getLists = new RestCalls(this, REST_GET);
            getLists.getNameList();

            //Disable the layout before server connection
            submit.setEnabled(false);
            ratingBar.setEnabled(false);

            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //Show progressdialog
                    progressDialog.setMessage("submitting...");
                    progressDialog.show();

                    RestCalls postRatings = new RestCalls(MainActivity.this, REST_POST + "/" + idList.get(position));
                    postRatings.postPolls(MainActivity.this, rate);
                }
            });

            votingSubmitted = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    //Stop the progressdialog
                    progressDialog.cancel();

                    Bundle data = intent.getExtras();
                    int result = data.getInt("status");
                    String message = data.getString("message");
                    if(result==201)
                    {
                        //Success submission
                        showDialog("Thank You!",message);
                    }
                    else
                    {
                        //Failure
                        showDialog("Error", message);
                    }

                }
            };


            listArrived = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    //Stop the progressdialog
                    progressDialog.cancel();

                    //Clear the list
                    namesList.clear();
                    //Receiving the data
                    Bundle data = intent.getExtras();
                    namesList = data.getStringArrayList(KEY_NAME);
                    idList = data.getStringArrayList(KEY_ID);

                    // Create an ArrayAdapter using the string array and a default spinner layout
                    adapter = new ArrayAdapter<CharSequence>(MainActivity.this, R.layout.support_simple_spinner_dropdown_item, namesList);

                    // Specify the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    // Apply the adapter to the spinner
                    names.setAdapter(adapter);


                    if (namesList.isEmpty()) {
                        //Disable the layout before server connection
                        submit.setEnabled(false);
                        ratingBar.setEnabled(false);

                        showDialog("Connection Timeout!", "Server not available at given IP address. Try again or change the server IP.");
                    } else {
                        //Enable the layout after server connection
                        submit.setEnabled(true);
                        ratingBar.setEnabled(true);
                    }

                }
            };
        } else {
            //Show error dialog
            showDialog("Error", "Some error while registration. Close the app and Try again");
            setUIEnabled(false);
        }
    }

    private void showDialog(String title, String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(listArrived, new IntentFilter(BROADCAST_NAME));
        registerReceiver(votingSubmitted, new IntentFilter(VOTINGS_SUBMITTED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(listArrived);
        unregisterReceiver(votingSubmitted);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optionmenu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.mRefresh)
        {
            //Show progressdialog
            progressDialog.setMessage("Reloading...");
            progressDialog.show();


            //Request to Server
            RestCalls getLists = new RestCalls(MainActivity.this, REST_GET);
            getLists.getNameList();
            return true;
        }
        else if(item.getItemId() == R.id.mSettings)
        {
            //Remove Server IP
            //Get Sharedpreferences data
            SharedPreferences preferences = getSharedPreferences(SHAREDPREFERENCES_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(IP_ADDRESS);
            editor.commit();
            //Kill this Activity
            finish();

            //Start the Server IP actiivity
            startActivity(new Intent(MainActivity.this, PreMains.class));

            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }

    public void setUIEnabled(boolean UIEnabled) {
         ratingBar.setEnabled(UIEnabled);
         names.setEnabled(UIEnabled);
         submit.setEnabled(UIEnabled);
    }
}
