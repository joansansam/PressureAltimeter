package melsion.sansa.joan.pressurealtimeter;

/**
 * Created by joan.sansa.melsion on 14/05/2018.
 * https://github.com/joansansam/PressureAltimeter
 */

public class Constants {
    ////////////////////Variables////////////////////////
    //Services
    public final static String DEFAULT="Default 1013,25 hPa, 20ºC";
    public final static String ACCUWEATHER="Accuweather";
    public final static String OPENWEATHERMAP="OpenWeatherMap";
    public final static String DARKSKY="DarkSky";
    //Formulas
    public final static String ANDROID_SENSORMANAGER="Android SensorManager";
    public final static String KEISAN_CASIO="Keisan-Casio";
    public final static String WIKIPEDIA0="Wikipedia0";
    public final static String WIKIPEDIA1="Wikipedia1";
    public final static String WIKIPEDIA2="Wikipedia2";
    public final static String WIKIPEDIACAT="WikipediaCAT";
    public final static String WIKIPEDIACAT2="WikipediaCAT2";
    public final static String GEOPOTENTIAL_INVERSE="Geopotential inverse";
    public final static String METEOCAT="Meteocat";
    public final static String METEOCAT_VAPOR="Meteocat vapor";
    public final static String WEATHER_GOV="Weather.gov";
    //Values
    public final static double STANDARD_PRESSURE=1013.25;
    public final static double STANDARD_TEMPERATURE=20;

    public static final String DECIMAL_FORMAT = "%.2f";

    public static final String FILE_NAME="altimeter_logs";
    public static final String FILE_EXTENSION=".csv";

    //////////////////////SharedPreferences Keys///////////////////
    public final static String SELECTED_SERVICE = "selected_service";
    public final static String SELECTED_FORMULA = "selected_formula";
    public final static String CALIBRATION_PRESSURE = "calibration_pressure";
    public final static String CALIBRATION_TEMPERATURE = "calibration_temperature";
    public final static String SERVICE_TEMPERATURE = "service_temperature";
    public final static String TEMP_CHECKED = "temp_checked";
}
