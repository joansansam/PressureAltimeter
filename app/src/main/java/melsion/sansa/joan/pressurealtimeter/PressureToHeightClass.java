package melsion.sansa.joan.pressurealtimeter;

import android.hardware.SensorManager;

import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.pow;

public class PressureToHeightClass {
    //CONSTANTS
    public static double L=0.0065; //https://en.wikipedia.org/wiki/International_Standard_Atmosphere
    public static double M=0.0289644;
    public static double g=9.80665;
    public static double R=8.3144598;

    public static double calculate(String formula, double P, double P0, double T) {
        double h = 8888;
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
                double h0 = 0;
                double Rd = 287.05;
                //https://ca.wikipedia.org/wiki/Pressi%C3%B3_de_vapor_de_l%27aigua
                double e = (exp(20.386 - (5132 / Tk))) * 1.33322387415;
                //Tk=Tk*(1+0.378*(e/P)); %Compensació de la temperatura segons la pressió de vapor d'aigua (veure link meteocat)
                h = ((1 - (pow(P0 / P,-Rd * L / g))) * (Tk / L)) + h0;
                break;
            case Constants.WEATHER_GOV:
                //https://www.weather.gov/media/epz/wxcalc/pressureAltitude.pdf
                h = (1 - pow(P/P0,0.190284)) * 145366.45 * 0.3048;
                break;

        }
        return h;
    }
}