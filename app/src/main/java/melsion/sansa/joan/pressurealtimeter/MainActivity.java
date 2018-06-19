package melsion.sansa.joan.pressurealtimeter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Locale;

/**
 * Created by joan.sansa.melsion on 05/05/2018.
 * https://github.com/joansansam/PressureAltimeter
 */

public class MainActivity extends AppCompatActivity {

    //--------------------------------------------------------------------------
    // VARIABLES
    //--------------------------------------------------------------------------

    private PressureSensorClass pressureSensorClass;
    private TextView pressureBaroTV, heightBaroTV, calibrationPressureTV;
    private ToggleButton barometerButton;
    private Button callServiceButton;
    private ProgressBar sensorProgressBar;
    private ProgressBar serviceProgressBar;
    private static AlertDialog permissionsAlertDialog;

    private double sensorPressure;

    //--------------------------------------------------------------------------
    // LIFE CYCLE
    //--------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pressureBaroTV = findViewById(R.id.pressure_baro_tv);
        heightBaroTV = findViewById(R.id.height_baro_tv);
        barometerButton = findViewById(R.id.baro_btn);
        callServiceButton = findViewById(R.id.call_service_btn);
        calibrationPressureTV = findViewById(R.id.calibration_pressure_tv);
        sensorProgressBar = findViewById(R.id.sensor_progress_bar);
        serviceProgressBar = findViewById(R.id.service_progress_bar);
        sensorProgressBar.bringToFront();
        serviceProgressBar.bringToFront();

        FileUtil.createFile(getApplicationContext());

        String calibrationPressure = SharedPreferencesUtils.getString(this, Constants.CALIBRATION_PRESSURE,String.valueOf(Constants.STANDARD_PRESSURE));
        calibrationPressureTV.setText(calibrationPressure);

        barometerButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sensorProgressBar.bringToFront();
                if (isChecked) {
                    // The toggle is enabled
                    //Start smartphone sensor listener
                    if(pressureSensorClass == null) {
                        //Start progress bar
                        sensorProgressBar.setProgress(0);
                        if(sensorProgressBar.getVisibility() != View.VISIBLE) {
                            sensorProgressBar.setVisibility(View.VISIBLE);
                        }

                        pressureSensorClass = new PressureSensorClass(MainActivity.this);
                        pressureSensorClass.start();
                    }

                } else {
                    //Stop progress bar
                    if(sensorProgressBar.getVisibility() != View.GONE) {
                        sensorProgressBar.setVisibility(View.GONE);
                    }
                    // The toggle is disabled
                    //Liberate sensor listeners
                    if(pressureSensorClass!=null) {
                        pressureSensorClass.stop();
                        pressureSensorClass=null;
                    }
                }
            }
        });

        callServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean neededConfig = checkGPSandConnection();
                if (neededConfig) {
                    serviceProgressBar.setProgress(0);
                    if(serviceProgressBar.getVisibility() != View.VISIBLE) {
                        serviceProgressBar.setVisibility(View.VISIBLE);
                    }
                    new LocationHelper(MainActivity.this);
                } else {
                    //Show dialog
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertDialogBuilder.setTitle("GPS and Internet connection required")
                            .setMessage("You must enable Location and have Internet connection to procedure.")
                            .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //Open permissions settings
                                    Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                    startActivityForResult(intent,0);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setCancelable(false)
                            .create()
                            .show();

                    //Use standard pressure if gps is not enabled
                    SharedPreferencesUtils.setString(getApplicationContext(), Constants.CALIBRATION_PRESSURE, String.valueOf(Constants.STANDARD_PRESSURE));
                    calibrationPressureTV.setText(String.valueOf(Constants.STANDARD_PRESSURE));
                }
            }

        });

        checkPermissions(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        checkPermissions(this);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        //Liberate sensor listeners when closing app
        if(pressureSensorClass != null) {
            pressureSensorClass.stop();
            pressureSensorClass=null;
        }
        FileUtil.closeOutputStream();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.item:
                startActivity(new Intent(this, LogActivity.class));
                break;
        }
        return true;
    }


    //--------------------------------------------------------------------------
    // UI METHODS
    //--------------------------------------------------------------------------

    /**
     * Update TextView with new pressure values from the sources
     * @param sensorValue
     */
    public void updatePressureUI(double sensorValue){
        sensorPressure= sensorValue;
        pressureBaroTV.setText(String.format(Locale.ENGLISH, Constants.DECIMAL_FORMAT,sensorValue));
    }

    /**
     * Update TextView with calculated height
     * @param sensorValue
     */
    public void updateHeightUI(double sensorValue){
        heightBaroTV.setText(String.format(Locale.ENGLISH, Constants.DECIMAL_FORMAT,sensorValue));
    }

    public void receiveFromService(String pressureString){
        //Stop progress bar
        if(serviceProgressBar.getVisibility() != View.GONE) {
            serviceProgressBar.setVisibility(View.GONE);
        }

        calibrationPressureTV.setText(pressureString);

        double sensorHeight = PressureToHeightClass.calculate(getApplicationContext(), sensorPressure);
        updateHeightUI(sensorHeight);
    }

    //--------------------------------------------------------------------------
    // OTHERS
    //--------------------------------------------------------------------------

    public static boolean checkPermissions(final Activity activity){
        //From here https://stackoverflow.com/questions/30719047/android-m-check-runtime-permission-how-to-determine-if-the-user-checked-nev/35495893#35495893
        //Check all permissions in a row
        if (ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) ) {

                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                permissionsAlertDialog = alertDialogBuilder.setTitle("Permissions Required")
                        .setMessage("You have forcefully denied some of the required permissions " +
                                "for the app. Please open settings, go to permissions and allow them.")
                        .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Open permissions settings
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", activity.getApplicationContext().getPackageName(), null));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                activity.startActivityForResult(intent,10);
                                permissionsAlertDialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                permissionsAlertDialog.dismiss();
                                if (ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                                        ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                                        ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                    checkPermissions(activity);
                                }
                            }
                        })
                        .setCancelable(false)
                        .create();
                if(!permissionsAlertDialog.isShowing()) {
                    permissionsAlertDialog.show();
                }

            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
            }
        }else{
            return true; //Permission granted
        }
        return false; //Permission denied
    }

    private boolean checkGPSandConnection(){
        Context context = getApplicationContext();
        LocationManager service = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean enabled = activeNetwork != null && activeNetwork.isConnectedOrConnecting() //Internet connection
                && service.isProviderEnabled(LocationManager.GPS_PROVIDER); //GPS enabled

        if(!enabled)
        {
            Log.e("Location", "Internet or GPS is not activated.");
        }
        return enabled;
    }

}

