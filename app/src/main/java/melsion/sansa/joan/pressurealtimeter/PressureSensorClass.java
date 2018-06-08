package melsion.sansa.joan.pressurealtimeter;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Arrays;

/**
 * Created by joan.sansa.melsion on 18/04/2018.
 * https://github.com/joansansam/PressureAltimeter
 * References:
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
                //double height = PressureToHeightClass.calculate(context, pressureValue);
                //FileUtil.addToFile(height,"","");

                //Averaging and sending to UI and file
                averaging(pressureValue);
            }
        }
    };

    //This method averages between each "AVG_WINDOW" input values
    /*private void averaging(double value) {
        acum += value;
        n++;
        if (n == AVG_WINDOW) { //For every AVG_WINDOW measurements (AVG_TIME_SECONDS seconds), get the average
            average = acum / n;

            activity.updatePressureUI(average, 0);

            double height = PressureToHeightClass.calculate(context, average);

            //To check measures, save them to a file
            FileUtil.addToFile("",String.valueOf(height),"");

            activity.updateHeightUI(height,0);

            n = 0;
            acum = 0;
            average = 0;
        }
    }*/

    private final static int AVG_TIME_SECONDS = 4; //ToDo: check best window length
    private final static int AVG_WINDOW = 6*AVG_TIME_SECONDS;
    private double[] values = new double[AVG_WINDOW];
    private int pos=0;
    //Using sliding window median
    //https://stackoverflow.com/questions/11955728/how-to-calculate-the-median-of-an-array
    private void averaging(double value){
        System.arraycopy(values,0,values,1,AVG_WINDOW-1); //Slide window
        values[0]=value;

        if(values[values.length-1]!=0) { //Start with median when the window is filled
            double median;
            double[] ordered = values.clone(); //Copy array
            Arrays.sort(ordered); //order from small to large
            int middle = ordered.length / 2;
            if ((ordered.length % 2) == 0) {
                double left = ordered[middle - 1];
                double right = ordered[middle];
                median = (left + right) / 2;
            } else {
                median = ordered[middle];
            }

            activity.updatePressureUI(median, 0);
            double height = PressureToHeightClass.calculate(context, median);
            FileUtil.addToFile("", String.valueOf(height), "");
            activity.updateHeightUI(height, 0);
        }

        //fixed window median
        /*if(pos < AVG_WINDOW) {
            values[pos] = value;
            pos++;
        } else {
            pos=0;

            double median;
            Arrays.sort(values);
            int middle = values.length/2;
            if((values.length % 2) == 0) {
                double left = values[middle - 1];
                double right = values[middle];
                median = (left + right)/2;
            } else {
                median = values[middle];
            }

            activity.updatePressureUI(median, 0);
            double height = PressureToHeightClass.calculate(context, median);
            FileUtil.addToFile("",String.valueOf(height),"");
            activity.updateHeightUI(height,0);
        }*/
    }
}

