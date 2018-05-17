package melsion.sansa.joan.pressurealtimeter;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by joan.sansa.melsion on 18/04/2018.
 * http://developer.samsung.com/technical-doc/view.do;jsessionid=1EAF2428DFF919537C6594B261B24A49?v=T000000127
 */

public class PressureSensorClass {
    private SensorManager sensorManager = null;
    private Context context;
    private MainActivity activity;
    private double pressureValue;
    private int n;
    private float average;
    private float acum;

    public PressureSensorClass(MainActivity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        n = 0;
        acum = 0;
    }

    public void start() {
        //Get SensorManager instance
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        //Register listener
        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stop() {
        sensorManager.unregisterListener(sensorListener);
    }

    private SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // when accuracy changed, this method will be called.
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            // when pressure value is changed, this method will be called.

            //Check sensor type.
            if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
                pressureValue = (double) event.values[0];

                //To check measures, save them to a file (Try to not use this value, TOO MUCH OVERFLOW IN THE LOG FILE)
                //FileUtil.saveToFile(0,pressureValue,0);

                //Averaging and sending to UI and file
                averaging(pressureValue);
            }
        }
    };

    //This method averages between each X (25) input values
    private void averaging(double value) {
        acum += value;
        n++;
        if (n == 25) { //For every 25 measurements, get the average //ToDo: calculate how many samples must be averaged
            average = acum / n;

            //To check measures, save them to a file
            //FileUtil.saveToFile(0,average,0);

            activity.updatePressureUI(average, 0);

            String formula = SharedPreferencesUtils.getString(context,Constants.SELECTED_FORMULA,"");
            double P0 = Double.valueOf(SharedPreferencesUtils.getString(context,Constants.CALIBRATION_PRESSURE, String.valueOf(Constants.STANDARD_PRESSURE)));
            double T = Double.valueOf(SharedPreferencesUtils.getString(context,Constants.CALIBRATION_TEMPERATURE, String.valueOf(Constants.STANDARD_TEMPERATURE)));
            double height = PressureToHeightClass.calculate(formula, average, P0,T);
            activity.updateHeightUI(height,0);

            n = 0;
            acum = 0;
            average = 0;
        }
    }
}

