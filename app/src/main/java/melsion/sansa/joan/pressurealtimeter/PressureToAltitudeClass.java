package melsion.sansa.joan.pressurealtimeter;

import android.content.Context;

import static java.lang.Math.log;

/**
 * Created by joan.sansa.melsion on 14/05/2018.
 * https://github.com/joansansam/PressureAltimeter
 */

public class PressureToAltitudeClass {
    //CONSTANTS
    private static double M=0.0289644;
    private static double g=9.80665;
    private static double R=8.3144598;

    public static double calculate(Context context, double P) {
        double P0 = Double.valueOf(SharedPreferencesUtils.getString(context,Constants.CALIBRATION_PRESSURE, String.valueOf(Constants.STANDARD_PRESSURE)));
        double T = Constants.STANDARD_TEMPERATURE;

        double h;
        double Tk = T + 273.15;

        //https://ca.wikipedia.org/wiki/F%C3%B3rmula_barom%C3%A8trica#Establiment_de_l'equaci%C3%B3_barom%C3%A8trica
        //Same as http://www.hills-database.co.uk/altim.html
        double hs = R * Tk / (M * g);
        h = hs * log(P0 / P);

        return h;
    }
}
