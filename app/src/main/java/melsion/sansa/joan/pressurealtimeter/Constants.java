package melsion.sansa.joan.pressurealtimeter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

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
    public final static String GEOPOTENTIAL_INVERSE="Geopotential-inverse";
    public final static String METEOCAT="Meteocat";
    public final static String WEATHER_GOV="Weather.gov";
    //Values
    public final static double STANDARD_PRESSURE=1013.25;
    public final static double STANDARD_TEMPERATURE=20;
    //public final static NumberFormat DECIMAL_FORMAT = NumberFormat.getInstance(Locale.ENGLISH);//TODO
    private static DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.ENGLISH);
    public final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0"+symbols.getDecimalSeparator()+"00");

    //////////////////////SharedPreferences Keys///////////////////
    public final static String SELECTED_SERVICE = "selected_service";
    public final static String SELECTED_FORMULA = "selected_formula";
    public final static String CALIBRATION_PRESSURE = "calibration_pressure";
    public final static String CALIBRATION_TEMPERATURE = "calibration_temperature";
    public final static String SERVICE_TEMPERATURE = "service_temperature";
    public final static String TEMP_CHECKED = "temp_checked";
}
