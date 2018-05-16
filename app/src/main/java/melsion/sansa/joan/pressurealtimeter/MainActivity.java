package melsion.sansa.joan.pressurealtimeter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

        String calibrationPressure = SharedPreferencesUtils.getString(this, Constants.CALIBRATION_PRESSURE,String.valueOf(Constants.STANDARD_PRESSURE));
        String calibrationTemp = SharedPreferencesUtils.getString(this, Constants.CALIBRATION_TEMPERATURE,String.valueOf(Constants.STANDARD_TEMPERATURE));
        calibrationPressureTV.setText(calibrationPressure);
        calibrationTempTV.setText(calibrationTemp);

        windooButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    //Check audio permissions in order to use Windoo jack sensor.
                    // https://stackoverflow.com/questions/28539717/android-startrecording-called-on-an-uninitialized-audiorecord-when-samplerate/28539778
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO},123);
                    }
                    else {
                        //Instantiate jdcWindooManager and start observing sensor changes
                        if (windooSensorClass == null) {
                            windooSensorClass = new WindooSensorClass(MainActivity.this);
                            windooSensorClass.start();

                            //If any of the sensors have not been initialized, create the file to store the measurements
                        /*if(pressureSensorClass == null){
                            FileUtil.createFile(getApplicationContext());
                        }*/
                        }
                    }
                } else {
                    // The toggle is disabled
                    //Liberate sensor listeners
                    if (windooSensorClass !=null) {
                        windooSensorClass.stop();
                    }
                    windooSensorClass=null;
                }
            }
        });

        barometerButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    //Start smartphone sensor listener
                    if(pressureSensorClass == null) {
                        pressureSensorClass = new PressureSensorClass(MainActivity.this);
                        pressureSensorClass.start();

                        //If any of the sensors have not been initialized, create the file to store the measurements
                        /*if(windooSensorClass == null){
                            FileUtil.createFile(getApplicationContext());
                        }*/
                    }

                } else {
                    // The toggle is disabled
                    //Liberate sensor listeners
                    if(pressureSensorClass!=null) {
                        pressureSensorClass.stop();
                    }
                    pressureSensorClass=null;
                }
            }
        });

        callServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Make the action only if permission is granted
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},321);
                }
                else {
                    if(selectedService.equals(Constants.DEFAULT)) {
                        SharedPreferencesUtils.setString(MainActivity.this, Constants.CALIBRATION_PRESSURE, String.valueOf(Constants.STANDARD_PRESSURE));
                        calibrationPressureTV.setText(String.valueOf(Constants.STANDARD_PRESSURE));
                        SharedPreferencesUtils.setString(MainActivity.this, Constants.CALIBRATION_TEMPERATURE, String.valueOf(Constants.STANDARD_TEMPERATURE));
                        calibrationTempTV.setText(String.valueOf(Constants.STANDARD_TEMPERATURE));
                    } else {
                        new LocationHelper(MainActivity.this);
                    }
                }
                //ToDo: maybe show dialog if GPS not enabled.
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
                        SharedPreferencesUtils.setString(MainActivity.this, Constants.SELECTED_SERVICE,selectedService);
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        selectedService = (String)parent.getItemAtPosition(0);
                        SharedPreferencesUtils.setString(MainActivity.this, Constants.SELECTED_SERVICE,selectedService);
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
                        SharedPreferencesUtils.setString(MainActivity.this, Constants.SELECTED_FORMULA, formula);
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        String formula = (String)parent.getItemAtPosition(0);
                        SharedPreferencesUtils.setString(MainActivity.this, Constants.SELECTED_FORMULA, formula);
                    }
                });

        //ToDo: gestionar millor permisos i config (volum, internet i gps)
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO},123);
        }
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
        if(sensorValue == 0){
            pressureWindooTV.setText(Constants.DECIMAL_FORMAT.format(windooValue));
        } else if(windooValue == 0){
            pressureBaroTV.setText(Constants.DECIMAL_FORMAT.format(sensorValue));
        }
    }

    /**
     * Update TextView with calculated height
     * @param sensorValue
     * @param windooValue
     */
    public void updateHeightUI(double sensorValue, double windooValue){
        if(sensorValue == 0){
            heightWindooTV.setText(Constants.DECIMAL_FORMAT.format(windooValue));
        } else if(windooValue == 0){
            heightBaroTV.setText(Constants.DECIMAL_FORMAT.format(windooValue));
        }
    }

    public void receiveFromService(String pressureString, String temperatureString){
        calibrationPressureTV.setText(pressureString);
        calibrationTempTV.setText(temperatureString);
    }
}
