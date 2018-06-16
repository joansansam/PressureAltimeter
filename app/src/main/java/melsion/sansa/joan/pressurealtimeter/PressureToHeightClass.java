package melsion.sansa.joan.pressurealtimeter;

import android.content.Context;
import android.hardware.SensorManager;

import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.pow;

/**
 * Created by joan.sansa.melsion on 14/05/2018.
 * https://github.com/joansansam/PressureAltimeter
 */

public class PressureToHeightClass {
    //CONSTANTS
    public static double L=0.0065; //https://en.wikipedia.org/wiki/International_Standard_Atmosphere
    public static double M=0.0289644;
    public static double g=9.80665;
    public static double R=8.3144598;

    //for meteocat
    private static double h0 = 0;
    private static double Rd = 287.05;

    public static double calculate(Context context, double P) {
        String formula = SharedPreferencesUtils.getString(context,Constants.SELECTED_FORMULA,"");
        double P0 = Double.valueOf(SharedPreferencesUtils.getString(context,Constants.CALIBRATION_PRESSURE, String.valueOf(Constants.STANDARD_PRESSURE)));
        double T = Double.valueOf(SharedPreferencesUtils.getString(context,Constants.CALIBRATION_TEMPERATURE, String.valueOf(Constants.STANDARD_TEMPERATURE)));

        double h;
        double Tk = T + 273.15;

        switch (formula) {
            case Constants.ANDROID_SENSORMANAGER:
                h = SensorManager.getAltitude((float) P0, (float) P);
                break;
            case Constants.KEISAN_CASIO:
                //http://keisan.casio.com/exec/system/1224585971
                h = (((pow(P0 / P,1 / 5.267)) - 1) * (Tk)) / L;
                break;
            case Constants.WIKIPEDIA0:
                //https://en.wikipedia.org/wiki/Barometric_formula
                h = Tk / L * (1 - (pow(P / P0,(R * L) / (g * M))));
                break;
            case Constants.WIKIPEDIA1:
                //https://en.wikipedia.org/wiki/Barometric_formula
                double hb = 0;
                h = ((Tk / (pow(P / P0,(R * L) / (g * M)))) - Tk) * (1 / L) + hb;
                break;
            case Constants.WIKIPEDIA2:
                //https://en.wikipedia.org/wiki/Barometric_formula
                //when standard temperature lapse rate equals zero
                hb = 0;
                h = ((R * Tk) / (g * M)) * log(P0 / P) + hb;
                break;
            case Constants.WIKIPEDIACAT:
                //https://ca.wikipedia.org/wiki/F%C3%B3rmula_barom%C3%A8trica#Establiment_de_l'equaci%C3%B3_barom%C3%A8trica
                //La mateixa que http://www.hills-database.co.uk/altim.html
                double hs = R * Tk / (M * g);
                h = hs * log(P0 / P);
                break;
            case Constants.WIKIPEDIACAT2:
                //https://ca.wikipedia.org/wiki/F%C3%B3rmula_barom%C3%A8trica#F%C3%B3rmula_internacional_de_l'anivellament_barom%C3%A8tric
                h = (Tk / L) * (1 - pow(P/P0, 1/5.255));
                break;
            case Constants.GEOPOTENTIAL_INVERSE:
                double Rt = 6356.766;
                double Tst = Tk / pow(P / P0,-1/5.255877);
                double Gp = (Tk - Tst) / (L * 1000);
                h = (Gp * Rt / (Rt - Gp)) * 1000;
                break;
            case Constants.METEOCAT:
                //http://www.meteo.cat/wpweb/divulgacio/equipaments-meteorologics/estacions-meteorologiques-automatiques/xarxa-destacions-meteorologiques-automatiques-xema/informacio-sobre-les-dades-meteorologiques-de-les-ema-que-es-mostren-al-web/reduccio-de-la-pressio-atmosferica-a-un-nivell-de-referencia/
                //Si l'alçada de l'estació està a menys de 1500m es refereix a 0m. Si no, a 1500m.
                h = ((1 - (pow(P0 / P,-Rd * L / g))) * (Tk / L)) + h0;
                break;
            case Constants.METEOCAT_VAPOR:
                //http://www.meteo.cat/wpweb/divulgacio/equipaments-meteorologics/estacions-meteorologiques-automatiques/xarxa-destacions-meteorologiques-automatiques-xema/informacio-sobre-les-dades-meteorologiques-de-les-ema-que-es-mostren-al-web/reduccio-de-la-pressio-atmosferica-a-un-nivell-de-referencia/
                //Si l'alçada de l'estació està a menys de 1500m es refereix a 0m. Si no, a 1500m. ?? ToDo
                //https://ca.wikipedia.org/wiki/Pressi%C3%B3_de_vapor_de_l%27aigua
                double e = (exp(20.386 - (5132 / Tk))) * 1.33322387415;
                //Compensació de la temperatura segons la pressió de vapor d'aigua (veure link meteocat)
                Tk=Tk*(1+0.378*(e/P0)); //ToDo: comprovar si P o P0
                h = ((1 - (pow(P0 / P,-Rd * L / g))) * (Tk / L)) + h0;
                break;
            case Constants.WEATHER_GOV:
                //https://www.weather.gov/media/epz/wxcalc/pressureAltitude.pdf
                h = (1 - pow(P/P0,0.190284)) * 145366.45 * 0.3048;
                break;
            default:
                h = SensorManager.getAltitude((float) P0, (float) P);
                break;

        }
        return h;
    }
}
