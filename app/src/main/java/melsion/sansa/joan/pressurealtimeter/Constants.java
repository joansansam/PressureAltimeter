package melsion.sansa.joan.pressurealtimeter;

/**
 * Created by joan.sansa.melsion on 14/05/2018.
 * https://github.com/joansansam/PressureAltimeter
 */

public class Constants {
    ////////////////////Variables////////////////////////
    //Values
    public final static double STANDARD_PRESSURE=1013.25;
    public final static double STANDARD_TEMPERATURE =15;

    public static final String DECIMAL_FORMAT = "%.2f";

    public static final String FILE_NAME="altimeter_logs";
    public static final String FILE_EXTENSION=".csv";

    //////////////////////SharedPreferences Keys///////////////////
    public final static String CALIBRATION_PRESSURE = "calibration_pressure";
    public final static String CALIBRATION_TEMPERATURE = "calibration_temperature";
}
