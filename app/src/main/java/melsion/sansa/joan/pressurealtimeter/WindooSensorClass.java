package melsion.sansa.joan.pressurealtimeter;

import android.content.Context;
import android.util.Log;

import java.util.Observable;
import java.util.Observer;

import ch.skywatch.windoo.api.JDCWindooEvent;
import ch.skywatch.windoo.api.JDCWindooManager;
import ch.skywatch.windoo.api.JDCWindooMeasurement;

/**
 * Created by joan.sansa.melsion on 18/04/2018. *
 * https://github.com/joansansam/PressureAltimeter
 * https://gist.github.com/jdc-electronic/4eb9b585bf27e5a222da
 */

public class WindooSensorClass implements Observer {
    private JDCWindooManager jdcWindooManager;
    private MainActivity activity;
    private Context context;

    public WindooSensorClass(MainActivity activity){
        this.activity=activity;
        this.context = activity.getApplicationContext();
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

                    //Send values to UI
                    activity.updatePressureUI(0, pressureValue);

                    double height = PressureToHeightClass.calculate(context, pressureValue);

                    //To check measures, save them to a file
                    FileUtil.saveToFile("","",String.valueOf(height));

                    activity.updateHeightUI(0, height);
                }
            }
        });
    }
}
