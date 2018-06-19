package melsion.sansa.joan.pressurealtimeter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

/**
 * Created by joan.sansa.melsion on 03/05/2018.
 * https://github.com/joansansam/PressureAltimeter
 */
public class LocationHelper {

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Location location;
    private double latitude, longitude;
    private MainActivity activity;
    private Context context;

    /**
     * Start location services
     */
    @SuppressLint("MissingPermission")
    public LocationHelper(MainActivity activity){
        this.activity=activity;
        this.context= activity.getApplicationContext();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        createLocationRequest();
        createLocationCallback();

        mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback,null);
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    private void createLocationCallback(){

        if(mLocationCallback!=null){
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }

        //Aqui no entrará nunca si no se tiene la ubicacion activada
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                location = locationResult.getLastLocation();

                latitude = location.getLatitude();
                longitude = location.getLongitude();

                mFusedLocationClient.removeLocationUpdates(mLocationCallback);

                //Run API call
                ApiHelper apiHelper = new ApiHelper();
                JSONObject response = apiHelper.selectService(activity,latitude,longitude);
            }
        };
    }
}
