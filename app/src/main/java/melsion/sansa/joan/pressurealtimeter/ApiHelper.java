package melsion.sansa.joan.pressurealtimeter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

/**
 * Created by joan.sansa.melsion on 24/04/2018.
 * https://github.com/joansansam/PressureAltimeter
 */

public class ApiHelper {

    private JSONObject serviceResponseJson=null;
    private MainActivity activity;
    private String urlString="";
    private Context context;

    public JSONObject selectService(MainActivity activity, double lat, double lon){
        this.activity = activity;
        this.context = activity.getApplicationContext();

        //ToDo: controlar el màxim de calls per dia: 50 accuweather, 1000 darksky
        //ToDo: si acaba sent el service definitiu afegir powered by DarkSky https://darksky.net/dev/docs
        urlString = "https://api.darksky.net/forecast/4f237579222e7fd80fa327b8c57c532d/" + lat + "," + lon;
        serviceCall();

        return serviceResponseJson;
    }

    @SuppressLint("StaticFieldLeak")
    private void serviceCall(){

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL url = new URL(urlString);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    StringBuffer json = new StringBuffer(1024);
                    String tmp = "";

                    while((tmp = reader.readLine()) != null)
                        json.append(tmp).append("\n");
                    reader.close();

                    String jsonString = json.toString();
                    //Beautify json in order to construct the JSONObject correctly
                    if(urlString.contains("currentconditions")){
                        jsonString = json.toString().replace("[","").replace("]","");
                    }
                    serviceResponseJson = new JSONObject(jsonString);

                } catch (Exception e) {
                    Log.e("ApiHelper","Exception "+ e.getMessage());
                    return null;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if(serviceResponseJson != null) {
                    updateUI(serviceResponseJson);
                }
            }
        }.execute();
    }

    private void updateUI(JSONObject responseJson){
        try {
            String pressureValue = responseJson.getJSONObject("currently").getString("pressure");

            Toast.makeText(context, "Received from service: pressure="+pressureValue, Toast.LENGTH_SHORT).show();
            FileUtil.addToFile("Calibrated:"+pressureValue,"");

            SharedPreferencesUtils.setString(context, Constants.CALIBRATION_PRESSURE, pressureValue);

            activity.receiveFromService(pressureValue);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ApiHelper",e.getMessage());
        }
    }
}
