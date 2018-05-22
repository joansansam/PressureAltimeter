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
                double height = PressureToHeightClass.calculate(context, pressureValue);
                //FileUtil.saveToFile(height,"","");

                //Averaging and sending to UI and file
                averaging(pressureValue);
            }
        }
    };

    //This method averages between each X (25) input values
    private void averaging(double value) {
        acum += value;
        n++;
        if (n == 6*8) { //For every 48 measurements (8 seconds), get the average //ToDo: calculate how many samples must be averaged
            average = acum / n;

            activity.updatePressureUI(average, 0);

            double height = PressureToHeightClass.calculate(context, average);

            //To check measures, save them to a file
            FileUtil.saveToFile("",String.valueOf(height),"");

            activity.updateHeightUI(height,0);

            n = 0;
            acum = 0;
            average = 0;
        }
    }
}

