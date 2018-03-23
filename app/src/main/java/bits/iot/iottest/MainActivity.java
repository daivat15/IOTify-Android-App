package bits.iot.iottest;

import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonWriter;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private Switch bigLightSwitch, smallLightSwitch, fanSwitch;
    private ProgressBar progressBar;
    private boolean bigLightStatus, smallLightStatus, fanStatus;
    private final String url = "https://0zgjmounqj.execute-api.us-west-2.amazonaws.com/Beta/amzn1.ask.account.AFUIUTONGTKCILIFZHUQBF5GWGSMOO4Y7XJI7BTKUB76J7U4KA5Y6LSO5W6RZTKW7NAE4BCKLDOV4C6HMUHZQ42ABNU4GS2NMIRTV3T7KIUTY4HRSHZ5LVW3PKWGNNGNE4JYKPRASMF45RZXAX3SXJKVENUSWSZNH5QB7V3QGWEKTTTMPGEYXZU66KHDUXOKPDIZUGATXA4Q6PI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bigLightSwitch = (Switch) findViewById(R.id.bigLight_switch);
        smallLightSwitch = (Switch) findViewById(R.id.smallLight_switch);
        fanSwitch = (Switch) findViewById(R.id.fan_switch);
        progressBar = (ProgressBar) findViewById(R.id.refresh_progressBar);

        bigLightSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bigLightStatus = !bigLightStatus;
                bigLightSwitch.setChecked(bigLightStatus);
                new PutRequest().execute();
            }
        });
        smallLightSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                smallLightStatus = !smallLightStatus;
                smallLightSwitch.setChecked(smallLightStatus);
                new PutRequest().execute();
            }
        });
        fanSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fanStatus = !fanStatus;
                fanSwitch.setChecked(fanStatus);
                new PutRequest().execute();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(this, "Refreshing", Toast.LENGTH_SHORT).show();
        new GetRequest().execute();
    }

    public void onRefreshButtonClicked (View view) {
        new GetRequest().execute();
    }

    private class GetRequest extends AsyncTask<Void, String, Boolean>{

        private JSONObject jsonObject;

        @Override
        protected void onPreExecute(){
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);
            if (Build.VERSION.SDK_INT < 21) {
                progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            }
        }

        @Override
        protected Boolean doInBackground(Void ...voids){
            int resCode;
            InputStream inputStream;
            HttpsURLConnection httpsURLConnection;
            Writer writer;

            try {
                URL mUrl = new URL(url);
                URLConnection urlConnection = mUrl.openConnection();
                httpsURLConnection = (HttpsURLConnection) urlConnection;
                httpsURLConnection.setAllowUserInteraction(false);
                httpsURLConnection.setInstanceFollowRedirects(true);
                httpsURLConnection.setRequestMethod("GET");
                httpsURLConnection.connect();
                resCode = httpsURLConnection.getResponseCode();
            } catch (MalformedURLException e1){
                e1.printStackTrace();
                publishProgress("Unknown URL.");
                return false;
            } catch (UnknownHostException e3) {
                publishProgress("No internet connection detected.");
                return false;
            } catch (IOException e2){
                publishProgress("Error while getting stream.");
                e2.printStackTrace();
                return false;
            }

            if (resCode == HttpsURLConnection.HTTP_OK) {
                char[] buffer = new char[1024];

                try {
                    inputStream = httpsURLConnection.getInputStream();
                    writer = new StringWriter();
                    Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"),1024);
                    int n;
                    while ((n = reader.read(buffer)) != -1) {
                        writer.write(buffer, 0, n);
                    }
                    inputStream.close();

                    jsonObject = new JSONObject(writer.toString());
                    jsonObject = jsonObject.getJSONObject("Item").getJSONObject("mapAttr").getJSONObject("M").getJSONObject("states").getJSONObject("M");
                    bigLightStatus = jsonObject.getJSONObject("big light").getBoolean("BOOL");
                    smallLightStatus = jsonObject.getJSONObject("small light").getBoolean("BOOL");
                    fanStatus = jsonObject.getJSONObject("fan").getBoolean("BOOL");
                } catch (IOException e1) {
                    publishProgress("Error while reading from stream.");
                    e1.printStackTrace();
                    return false;
                } catch (JSONException e2){
                    publishProgress("Error while traversing JSON object.");
                    e2.printStackTrace();
                    return false;
                }
                return true;
            }
            else {
                publishProgress("Wrong response code.");
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(String ...strings){
            Toast.makeText(MainActivity.this, strings[0], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Boolean bool){
            progressBar.setVisibility(View.GONE);
            if (bool){
                bigLightSwitch.setChecked(bigLightStatus);
                smallLightSwitch.setChecked(smallLightStatus);
                fanSwitch.setChecked(fanStatus);
            }
        }
    }

    private class PutRequest extends AsyncTask<Void, String, Boolean> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);
            if (Build.VERSION.SDK_INT < 21) {
                progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            }
        }

        @Override
        protected Boolean doInBackground(Void ...voids) {
            try {
                URL mUrl = new URL(url);
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) mUrl.openConnection();
                httpsURLConnection.setDoOutput(true);
                httpsURLConnection.setRequestMethod("PUT");
                httpsURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpsURLConnection.getOutputStream());
                JsonWriter jsonWriter = new JsonWriter(outputStreamWriter);
                jsonWriter.beginObject();
                jsonWriter.name("big_light").value(bigLightStatus);
                jsonWriter.name("small_light").value(smallLightStatus);
                jsonWriter.name("fan").value(fanStatus);
                jsonWriter.endObject();
                jsonWriter.flush();
                jsonWriter.close();
                httpsURLConnection.getResponseCode();
                return true;
            }  catch (MalformedURLException e1){
                e1.printStackTrace();
                publishProgress("Unknown URL.");
                return false;
            } catch (UnknownHostException e3) {
                publishProgress("No internet connection detected.");
                return false;
            } catch (IOException e2){
                publishProgress("Error while getting stream.");
                e2.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(String ...strings){
            Toast.makeText(MainActivity.this, strings[0], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Boolean b) {
            progressBar.setVisibility(View.GONE);
            if (!b) {
                Toast.makeText(MainActivity.this, "Error!\nRefreshing", Toast.LENGTH_SHORT).show();
                new GetRequest().execute();
            }
        }
    }
}
