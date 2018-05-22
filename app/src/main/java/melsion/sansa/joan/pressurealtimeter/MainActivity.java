package melsion.sansa.joan.pressurealtimeter;

import android.Manifest;
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
import android.support.v4.content.ContextCompat;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    //--------------------------------------------------------------------------
    // VARIABLES
    //--------------------------------------------------------------------------

    private PressureSensorClass pressureSensorClass;
    private WindooSensorClass windooSensorClass;
    private TextView pressureWindooTV, pressureBaroTV, heightWindooTV, heightBaroTV, calibrationPressureTV, calibrationTempTV;
    private ToggleButton windooButton, barometerButton;
    private Button callServiceButton;
    private Spinner apiSpinner, formulaSpinner;
    private CheckBox tempCheckBox;

    private double sensorPressure, windooPressure;
    private String selectedService;

    //--------------------------------------------------------------------------
    // LIFE CYCLE
    //--------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pressureWindooTV = findViewById(R.id.pressure_windoo_tv);
        pressureBaroTV = findViewById(R.id.pressure_baro_tv);
        heightWindooTV = findViewById(R.id.height_windoo_tv);
        heightBaroTV = findViewById(R.id.height_baro_tv);
        windooButton = findViewById(R.id.windoo_btn);
        barometerButton = findViewById(R.id.baro_btn);
        callServiceButton = findViewById(R.id.call_service_btn);
        calibrationPressureTV = findViewById(R.id.calibration_pressure_tv);
        apiSpinner = findViewById(R.id.api_spinner);
        formulaSpinner = findViewById(R.id.formula_spinner);
        calibrationTempTV = findViewById(R.id.calibration_temp_tv);
        tempCheckBox = findViewById(R.id.temp_checkbox);

        FileUtil.createFile(getApplicationContext());

        String calibrationPressure = SharedPreferencesUtils.getString(this, Constants.CALIBRATION_PRESSURE,String.valueOf(Constants.STANDARD_PRESSURE));
        String calibrationTemp = SharedPreferencesUtils.getString(this, Constants.CALIBRATION_TEMPERATURE,String.valueOf(Constants.STANDARD_TEMPERATURE));
        calibrationPressureTV.setText(calibrationPressure);
        calibrationTempTV.setText(calibrationTemp);
        windooButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean buttonIsChecked = windooButton.isChecked();
                if(buttonIsChecked){
                    //Check that the jack volume is the maximum
                    final int VOLUME_STREAM= AudioManager.STREAM_MUSIC;
                    AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    int maxVolume = audio.getStreamMaxVolume(VOLUME_STREAM);
                    int currentVolume = audio.getStreamVolume(VOLUME_STREAM);
                    if(maxVolume==currentVolume || !audio.isVolumeFixed()){
                        //Set volume to the maximum
                        audio.setStreamVolume(VOLUME_STREAM, maxVolume, 0);
                        // https://stackoverflow.com/questions/28539717/android-startrecording-called-on-an-uninitialized-audiorecord-when-samplerate/28539778
                        //Instantiate jdcWindooManager and start observing sensor changes
                        if (windooSensorClass == null) {
                            windooSensorClass = new WindooSensorClass(MainActivity.this);
                            windooSensorClass.start();
                        }
                    } else {
                        //Show dialog
                        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getApplicationContext());
                        alertDialogBuilder.setTitle("Audio volume must be maximum")
                                .setMessage("You must turn up the volume.")
                                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .create()
                                .show();
                    }
                } else{
                    //Liberate sensor listeners
                    if (windooSensorClass !=null) {
                        windooSensorClass.stop();
                        windooSensorClass=null;
                    }
                }
            }
        });

        /*windooButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {// The toggle is enabled

                } else {// The toggle is disabled

                }
            }
        });*/

        barometerButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    //Start smartphone sensor listener
                    if(pressureSensorClass == null) {
                        pressureSensorClass = new PressureSensorClass(MainActivity.this);
                        pressureSensorClass.start();
                    }

                } else {
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
                if(selectedService.equals(Constants.DEFAULT)) {
                    SharedPreferencesUtils.setString(getApplicationContext(), Constants.CALIBRATION_PRESSURE, String.valueOf(Constants.STANDARD_PRESSURE));
                    calibrationPressureTV.setText(String.valueOf(Constants.STANDARD_PRESSURE));
                    SharedPreferencesUtils.setString(getApplicationContext(), Constants.CALIBRATION_TEMPERATURE, String.valueOf(Constants.STANDARD_TEMPERATURE));
                    calibrationTempTV.setText(String.valueOf(Constants.STANDARD_TEMPERATURE));

                    FileUtil.saveToFile("Calibrated:"+SharedPreferencesUtils.getString(getApplicationContext(),Constants.CALIBRATION_PRESSURE,String.valueOf(Constants.STANDARD_PRESSURE))
                            +"-"+SharedPreferencesUtils.getString(getApplicationContext(),Constants.CALIBRATION_TEMPERATURE,String.valueOf(Constants.STANDARD_TEMPERATURE))
                            ,"","");
                    receiveFromService(String.valueOf(Constants.STANDARD_PRESSURE), String.valueOf(Constants.STANDARD_TEMPERATURE));
                } else {
                    //ToDo: start AlarmManager to calibrate every X minutes - NOT THAT OBVIOUS (maybe it must only calibrate at the starting point)
                    boolean neededConfig = checkGPSandConnection();
                    if (neededConfig) {
                        new LocationHelper(MainActivity.this);
                    } else {
                        //Show dialog
                        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getApplicationContext());
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

                        //Use standard pressure and temperature if gps is not enabled
                        SharedPreferencesUtils.setString(getApplicationContext(), Constants.CALIBRATION_PRESSURE, String.valueOf(Constants.STANDARD_PRESSURE));
                        calibrationPressureTV.setText(String.valueOf(Constants.STANDARD_PRESSURE));
                        SharedPreferencesUtils.setString(getApplicationContext(), Constants.CALIBRATION_TEMPERATURE, String.valueOf(Constants.STANDARD_TEMPERATURE));
                        calibrationTempTV.setText(String.valueOf(Constants.STANDARD_TEMPERATURE));
                    }
                }
            }
        });

        final String[] services= new String[]{
                Constants.DEFAULT,
                Constants.ACCUWEATHER,
                //Constants.OPENWEATHERMAP, //I decided to deactivate this option as it gives bad results (negative altitude)
                Constants.DARKSKY
        };
        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, services);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        apiSpinner.setAdapter(adaptador);
        apiSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent,android.view.View v, int position, long id) {
                        selectedService = (String)parent.getItemAtPosition(position);
                        SharedPreferencesUtils.setString(getApplicationContext(), Constants.SELECTED_SERVICE,selectedService);
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        selectedService = (String)parent.getItemAtPosition(0);
                        SharedPreferencesUtils.setString(getApplicationContext(), Constants.SELECTED_SERVICE,selectedService);
                    }
                });

        final String[] formulas= new String[]{
                Constants.ANDROID_SENSORMANAGER,
                Constants.KEISAN_CASIO,
                Constants.WIKIPEDIA0,
                Constants.WIKIPEDIA1,
                Constants.WIKIPEDIA2,
                Constants.WIKIPEDIACAT,
                Constants.WIKIPEDIACAT2,
                Constants.GEOPOTENTIAL_INVERSE,
                Constants.METEOCAT,
                Constants.WEATHER_GOV
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, formulas);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formulaSpinner.setAdapter(adapter);
        formulaSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent,
                                               android.view.View v, int position, long id) {
                        String formula = (String)parent.getItemAtPosition(position);
                        SharedPreferencesUtils.setString(getApplicationContext(), Constants.SELECTED_FORMULA, formula);

                        FileUtil.saveToFile("selectedFormula="+formula.replace(" ","_"),"","");

                        double sensorHeight = PressureToHeightClass.calculate(getApplicationContext(), sensorPressure);
                        double windooHeight = PressureToHeightClass.calculate(getApplicationContext(), windooPressure);
                        if(sensorPressure != 0) {
                            updateHeightUI(sensorHeight, 0);
                        }
                        if(windooPressure != 0){
                            updateHeightUI(0, windooHeight);
                        }
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        String formula = (String)parent.getItemAtPosition(0);
                        SharedPreferencesUtils.setString(getApplicationContext(), Constants.SELECTED_FORMULA, formula);
                    }
                });

        tempCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferencesUtils.setBoolean(getApplicationContext(),Constants.TEMP_CHECKED, tempCheckBox.isChecked());
                if(tempCheckBox.isChecked()){
                    SharedPreferencesUtils.setString(getApplicationContext(),Constants.CALIBRATION_TEMPERATURE, String.valueOf(Constants.STANDARD_TEMPERATURE));
                }
                else{
                    String serviceTemp= SharedPreferencesUtils.getString(getApplicationContext(),Constants.SERVICE_TEMPERATURE,String.valueOf(Constants.STANDARD_TEMPERATURE));
                    SharedPreferencesUtils.setString(getApplicationContext(),Constants.CALIBRATION_TEMPERATURE, serviceTemp);
                }

                String calibrationPressure = SharedPreferencesUtils.getString(getApplicationContext(),Constants.CALIBRATION_PRESSURE,String.valueOf(Constants.STANDARD_PRESSURE));
                String calibrationTemp= SharedPreferencesUtils.getString(getApplicationContext(),Constants.CALIBRATION_TEMPERATURE,String.valueOf(Constants.STANDARD_TEMPERATURE));
                FileUtil.saveToFile("Calibrated:"+calibrationPressure+"-"+calibrationTemp,"","");
                receiveFromService(calibrationPressure, calibrationTemp);
            }
        });

        checkPermissions();
    }

    @Override
    public void onResume(){
        super.onResume();
        checkPermissions();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        //Liberate sensor listeners when closing app
        if(pressureSensorClass != null) {
            pressureSensorClass.stop();
            pressureSensorClass=null;
        }
        if(windooSensorClass != null) {
            windooSensorClass.stop();
            windooSensorClass = null;
        }
        FileUtil.closeOutputStream();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.dev_menu, menu);
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
     * Update TextView with new pressure values from the two sources
     * @param sensorValue
     * @param windooValue
     */
    public void updatePressureUI(double sensorValue, double windooValue){
        if(sensorValue != 0){
            sensorPressure= sensorValue;
            pressureBaroTV.setText(Constants.DECIMAL_FORMAT.format(sensorValue));
        } else if(windooValue != 0){
            windooPressure= windooValue;
            pressureWindooTV.setText(Constants.DECIMAL_FORMAT.format(windooValue));
        }
    }

    /**
     * Update TextView with calculated height
     * @param sensorValue
     * @param windooValue
     */
    public void updateHeightUI(double sensorValue, double windooValue){
        if(sensorValue != 0){
            heightBaroTV.setText(Constants.DECIMAL_FORMAT.format(sensorValue));
        } else if(windooValue != 0){
            heightWindooTV.setText(Constants.DECIMAL_FORMAT.format(windooValue));
        }
    }

    public void receiveFromService(String pressureString, String temperatureString){
        calibrationPressureTV.setText(pressureString);
        calibrationTempTV.setText(temperatureString);

        double sensorHeight = PressureToHeightClass.calculate(getApplicationContext(), sensorPressure);
        double windooHeight = PressureToHeightClass.calculate(getApplicationContext(), windooPressure);
        if(sensorPressure != 0) {
            updateHeightUI(sensorHeight, 0);
        }
        if(windooPressure != 0){
            updateHeightUI(0, windooHeight);
        }
    }

    //--------------------------------------------------------------------------
    // OTHERS
    //--------------------------------------------------------------------------

    private void checkPermissions(){
        //From here https://stackoverflow.com/questions/30719047/android-m-check-runtime-permission-how-to-determine-if-the-user-checked-nev/35495893#35495893
        //Check all permissions in a row
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.RECORD_AUDIO) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) ) {

                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Permissions Required")
                        .setMessage("You have forcefully denied some of the required permissions " +
                                "for the app. Please open settings, go to permissions and allow them.")
                        .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Open permissions settings
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", getPackageName(), null));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivityForResult(intent,10);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                checkPermissions();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();

            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 123);
            }
        }
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

