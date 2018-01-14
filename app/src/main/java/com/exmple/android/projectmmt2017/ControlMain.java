package com.exmple.android.projectmmt2017;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

public class ControlMain extends AppCompatActivity implements View.OnClickListener{

    public static final String LOG_TAG = ControlMain.class.getSimpleName();
    private static final String REQUEST_URL = "https://projectmmt2017.firebaseio.com/rest/led";

    private Switch redSwitch;
    private Switch greenSwitch;
    private Switch blueSwitch;

    private Boolean redLight;
    private Boolean greenLight;
    private Boolean blueLight;

    private Button signOut;

    private FirebaseAuth mAuth;

    private MMTAsyncTask task;
    private FirebaseUser currentUser;
    private String userUID;


    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        userUID = currentUser.getUid();

        // Kick off an {@link AsyncTask} to perform the network request
        task = new MMTAsyncTask();
        task.execute();

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                MMTAsyncTask task = new MMTAsyncTask();
                task.execute();
            }
        }, 0, 2000);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_main);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        redSwitch = (Switch) findViewById(R.id.red_switch);
        greenSwitch = (Switch) findViewById(R.id.green_switch);
        blueSwitch = (Switch) findViewById(R.id.blue_switch);
        signOut = (Button) findViewById(R.id.sign_out);

        signOut.setOnClickListener(this);

        redSwitch.setOnClickListener(this);
        greenSwitch.setOnClickListener(this);
        blueSwitch.setOnClickListener(this);



    }


    @Override
    protected void onPause() {
        super.onPause();
        task.cancel(true);
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(intent);

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

        redLight = redSwitch.isChecked();
        greenLight = greenSwitch.isChecked();
        blueLight = blueSwitch.isChecked();

        if (i == R.id.red_switch) {
            redLight = redSwitch.isChecked();
        } else if (i == R.id.green_switch) {
            greenLight = greenSwitch.isChecked();
        } else if (i == R.id.blue_switch) {
            blueLight = blueSwitch.isChecked();
        } else if (i == R.id.sign_out) {
            signOut();
            task.cancel(true);
            finish();
        }

        MMTAsyncTaskPUT task = new MMTAsyncTaskPUT();
        task.execute();

    }

    private void signOut() {
        mAuth.signOut();

    }

    /**
     * Update the screen to display information from the given {@link Event}.
     */
    private void updateUi(Event led) {

        Boolean red = led.redLed;
        Boolean blue = led.blueLed;
        Boolean green = led.greenLed;

        redSwitch.setChecked(red);
        greenSwitch.setChecked(green);
        blueSwitch.setChecked(blue);

    }

    /**
     * Returns new URL object from the given string URL.
     */
    private URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Error with creating URL", exception);
            return null;
        }
        return url;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return an {@link Event} object by parsing out information
     * about the first earthquake from the input earthquakeJSON string.
     */
    private Event extractFeatureFromJson(String ledJSON) {
        try {
            JSONObject baseJsonResponse = new JSONObject(ledJSON);

            Boolean redStatus = baseJsonResponse.getBoolean("red");
            Boolean greenStatus = baseJsonResponse.getBoolean("green");
            Boolean blueStatus = baseJsonResponse.getBoolean("blue");

            return new Event(redStatus, greenStatus, blueStatus);

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the led JSON results", e);
        }
        return null;
    }


    /**
     * {@link AsyncTask} to perform the network request on a background thread, and then
     * update the UI with the first earthquake in the response.
     */
    private class MMTAsyncTask extends AsyncTask<URL, Void, Event> {

        @Override
        protected Event doInBackground(URL... urls) {
            // Create URL object

            URL url = createUrl(REQUEST_URL + userUID +".json");

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                // TODO Handle the IOException
            }

            Log.d(LOG_TAG, String.valueOf(jsonResponse.length()));
            Log.d(LOG_TAG, "a" + jsonResponse +"b");


            // This means there is no data to attach to the user. So we have to create one
            if (jsonResponse.equals("null") || jsonResponse.isEmpty() || jsonResponse.length() == 4) {

                try {
                    Log.d(LOG_TAG, "In to make Initial HTTP");
                    jsonResponse =  makeInitialHttpRequest(url);
                } catch (IOException e) {
                    // to
                }
            }

            // Extract relevant fields from the JSON response and create an {@link Event} object
            Event led = extractFeatureFromJson(jsonResponse);

            // Return the {@link Event} object as the result fo the {@link TsunamiAsyncTask}
            return led;

        }

        /**
         * Update the screen with the given earthquake (which was the result of the
         * {@link MMTAsyncTask}).
         */
        @Override
        protected void onPostExecute(Event led) {
            if (led == null) {
                return;
            }

            updateUi(led);
        }

        /**
         * Make an HTTP request to the given URL and return a String as the response.
         */
        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;

            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();

                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);

            } catch (IOException e) {
                // TODO: Handle the exception
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        /**
         * Make an HTTP request to the given URL and return a String as the response.
         */
        private String makeInitialHttpRequest(URL url) throws IOException {
            String jsonResponse = "";
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;

            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("PUT");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.setRequestProperty("Content-Type" ,"application/json");
                urlConnection.setRequestProperty("Accept-Charset", "UTF-8");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                JSONObject data = new JSONObject();
                data.put("red", true);
                data.put("green", true);
                data.put("blue", true);


                OutputStream os = urlConnection.getOutputStream();

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(data.toString());
                writer.flush();
                writer.close();
                os.close();

                urlConnection.connect();
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);

            } catch (IOException e) {
                // TODO: Handle the exception
                e.printStackTrace();
            } catch (JSONException e) {

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return jsonResponse;
        }


    }

    /**
     * {@link AsyncTask} to perform the network request on a background thread, and then
     * update the UI with the first earthquake in the response.
     */
    private class MMTAsyncTaskPUT extends AsyncTask<URL, Void, Event> {

        @Override
        protected Event doInBackground(URL... urls) {
            // Create URL object

            URL url = createUrl(REQUEST_URL + userUID + ".json");

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                // TODO Handle the IOException
            }
            // Extract relevant fields from the JSON response and create an {@link Event} object
            Event led = extractFeatureFromJson(jsonResponse);
            Log.d(LOG_TAG, "Value of Event");
            Log.d(LOG_TAG, jsonResponse);

            // Return the {@link Event} object as the result fo the {@link TsunamiAsyncTask}
            return led;
        }

        /**
         * Update the screen with the given earthquake (which was the result of the
         * {@link MMTAsyncTaskPUT}).
         */
        @Override
        protected void onPostExecute(Event led) {
            if (led == null) {
                return;
            }

            updateUi(led);

        }


        /**
         * Make an HTTP request to the given URL and return a String as the response.
         */
        private String makeHttpRequest(URL url) throws IOException {

            String jsonResponse = "";
            HttpsURLConnection urlConnection = null;
            InputStream inputStream = null;

            try {
                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("PUT");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.setRequestProperty("Content-Type" ,"application/json");
                urlConnection.setRequestProperty("Accept-Charset", "UTF-8");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                JSONObject data = new JSONObject();
                data.put("red", redLight);
                data.put("green", greenLight);
                data.put("blue", blueLight);


                OutputStream os = urlConnection.getOutputStream();

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(data.toString());
                writer.flush();
                writer.close();
                os.close();

                urlConnection.connect();
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);


            } catch (IOException e) {
                // TODO: Handle the exception
                e.printStackTrace();
            } catch( JSONException e) {

            }
            finally {
                Log.d(LOG_TAG, String.valueOf(urlConnection.getResponseCode()));
                Log.d(LOG_TAG, String.valueOf(urlConnection.getResponseMessage()));
                Log.d(LOG_TAG, String.valueOf(urlConnection.getInputStream()));

                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

            }
            return jsonResponse;
        }

    }
}
