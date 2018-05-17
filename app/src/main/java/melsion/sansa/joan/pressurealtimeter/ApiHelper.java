package melsion.sansa.joan.pressurealtimeter;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by joan.sansa.melsion on 24/04/2018.
 */

public class ApiHelper {

    private JSONObject serviceResponseJson=null;
    private MainActivity activity;
    private String urlString="";

    public JSONObject selectService(MainActivity activity, String service, double lat, double lon){
        this.activity = activity;

        switch (service) {
            case Constants.ACCUWEATHER:
                urlString = "http://dataservice.accuweather.com/locations/v1/cities/geoposition/search?apikey=TQcwTT9Mw9VMsXPuIKBtgJCjhLZDRh8e&q=" + lat + "%2C" + lon + "&details=false";
                serviceCall();

                break;
            case Constants.OPENWEATHERMAP:
                urlString = "http://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&type=accurate&appid=679b8bcfee89ed57c2bd5ebed2389690";
                serviceCall();
                break;
            case Constants.DARKSKY:
                urlString = "https://api.darksky.net/forecast/4f237579222e7fd80fa327b8c57c532d/" + lat + "," + lon;
                serviceCall();
                break;
        }

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
                if(urlString.contains("accuweather") && urlString.contains("locations")){
                    try {
                        String locationKey = serviceResponseJson.getString("Key");
                        urlString="http://dataservice.accuweather.com/currentconditions/v1/"+locationKey+"?apikey=TQcwTT9Mw9VMsXPuIKBtgJCjhLZDRh8e&details=true";
                        serviceCall();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("ApiHelper", e.getMessage());
                    }
                } else {
                    updateUI(serviceResponseJson);
                }
            }
        }.execute();
    }

    private void updateUI(JSONObject responseJson){
        String pressureValue="?";
        String temperature="?";
        try {
            if(urlString.contains("openweathermap")) {
                pressureValue = responseJson.getJSONObject("main").getString("pressure");
                String temperatureString = responseJson.getJSONObject("main").getString("temp");
                double convertedTemp = Double.valueOf(temperatureString)-273.15;
                temperature = String.valueOf(convertedTemp);
            }
            else if(urlString.contains("accuweather")){
                pressureValue = responseJson.getJSONObject("Pressure").getJSONObject("Metric").getString("Value");
                temperature = responseJson.getJSONObject("Temperature").getJSONObject("Metric").getString("Value");
            }
            else if (urlString.contains("darksky")){
                pressureValue = responseJson.getJSONObject("currently").getString("pressure");
                String temperatureString = responseJson.getJSONObject("currently").getString("temperature");
                double convertedTemp= 5*(Double.valueOf(temperatureString)-32)/9;
                temperature = String.valueOf(convertedTemp);
            }

            Toast.makeText(activity.getApplicationContext(), "Pressure from service="+pressureValue, Toast.LENGTH_SHORT).show();

            SharedPreferencesUtils.setString(activity, Constants.CALIBRATION_PRESSURE, pressureValue);
            SharedPreferencesUtils.setString(activity, Constants.CALIBRATION_TEMPERATURE, temperature);

            //Send value to UI
            activity.receiveFromService(pressureValue,temperature);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ApiHelper",e.getMessage());
        }
    }
}
