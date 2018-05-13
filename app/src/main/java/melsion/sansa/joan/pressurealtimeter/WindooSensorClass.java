package melsion.sansa.joan.pressurealtimeter;

import android.util.Log;

import java.util.Observable;
import java.util.Observer;

import ch.skywatch.windoo.api.JDCWindooEvent;
import ch.skywatch.windoo.api.JDCWindooManager;
import ch.skywatch.windoo.api.JDCWindooMeasurement;

/**
 * Created by joan.sansa.melsion on 18/04/2018.
 * https://gist.github.com/jdc-electronic/4eb9b585bf27e5a222da
 */


public class WindooSensorClass implements Observer {
    private JDCWindooManager jdcWindooManager;
    private MainActivity activity;

    public WindooSensorClass(MainActivity activity){
        this.activity=activity;
        this.jdcWindooManager = JDCWindooManager.getInstance();
    }

    public void start(){
        jdcWindooManager.addObserver(this);
        jdcWindooManager.enable(activity);
    }

    public void stop(){
        jdcWindooManager.disable(activity);
        jdcWindooManager.deleteObserver(this);
    }

    @Override
    public void update(Observable observable, final Object object) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                JDCWindooEvent event = (JDCWindooEvent) object;
                if (event.getType() == JDCWindooEvent.JDCWindooAvailable) {
                    Log.d("WindooSensorClass", "Windoo available");
                } else if (event.getType() == JDCWindooEvent.JDCWindooNewPressureValue) {
                    //Two ways to get the pressure value:
                    double pressureEvent= (double)event.getData();
                    JDCWindooMeasurement measurement = jdcWindooManager.getLive();
                    double pressureValue = measurement.getPressure();

                    Log.d("WindooSensorClass", "Pressure received : " + pressureValue);

                    //If temperature is needed for calibration:
                    double temperature= measurement.getTemperature();

                    //To check measures, save them to a file
                    //FileUtil.saveToFile(temperature,0,pressureEvent);

                    //Send values to UI
                    activity.updatePressureUI(0, pressureValue);

                    String formula = SharedPreferencesUtils.getString(activity,Constants.SELECTED_FORMULA,Constants.ANDROID_SENSORMANAGER);
                    double P0 = Double.valueOf(SharedPreferencesUtils.getString(activity,Constants.CALIBRATION_PRESSURE, String.valueOf(Constants.STANDARD_PRESSURE)));
                    double T = Double.valueOf(SharedPreferencesUtils.getString(activity,Constants.CALIBRATION_TEMPERATURE, String.valueOf(Constants.STANDARD_TEMPERATURE)));
                    double height = PressureToHeightClass.calculate(formula, pressureValue, P0,T);
                    activity.updateHeightUI(0, height);
                }
            }
        });
    }
}