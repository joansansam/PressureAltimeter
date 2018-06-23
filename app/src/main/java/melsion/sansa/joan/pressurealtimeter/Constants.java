package melsion.sansa.joan.pressurealtimeter;

/**
 * Created by joan.sansa.melsion on 14/05/2018.
 * https://github.com/joansansam/PressureAltimeter
 */

public class Constants {
    ////////////////////Variables////////////////////////
    //Values
    public final static double STANDARD_PRESSURE = 1013.25;
    public final static double STANDARD_TEMPERATURE = 15;
    public final static double PRESSURE_OFFSET_DEFAULT = 2;

    public static final String DECIMAL_FORMAT = "%.2f";
    public static final String DATE_FORMAT = "dd/MM/yyyy-HH:mm:ss";

    public static final String FILE_NAME="altimeter_logs";
    public static final String FILE_EXTENSION=".csv";

    //////////////////////SharedPreferences Keys///////////////////
    public final static String CALIBRATION_PRESSURE = "calibration_pressure";
    public final static String SELECTED_OFFSET = "selected_offset";
    public final static String SERVICE_CALLS = "service_calls";
    public final static String FIRST_SERVICE_CALL = "first_service_call";
}
