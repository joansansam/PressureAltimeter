package melsion.sansa.joan.pressurealtimeter;

import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {

    private static FileOutputStream stream;
    private static double baroAvgAux;
    private static double fromWindooAux;

    public static void createFile(Context context){
        if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
            return;

        //Save file to ...\Phone\Android\data\melsion.sansa.joan.pressurealtimeter\files
        File file = new File(context.getExternalFilesDir(null)
                ,"altimeter_logs.csv");

        if(file.exists())
            file.delete();

        try {
            stream = new FileOutputStream(file);
            String sequence= "date instant_height averaged_height windoo_heigh \n";
            stream.write(sequence.getBytes());
            //stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("FileUtil", "PROBLEMS SAVING TO FILE");
            Log.e("FileUtil", e.getMessage());
        }
    }

    //This method does not write the zeros between measures in the .csv file
    /*public static void saveToFile(double aux, double baroAvg, double fromWindoo){
        try {
            if(baroAvg==0){
                fromWindooAux=fromWindoo;
            } else if(fromWindoo==0) {
                baroAvgAux = baroAvg;
            }
            if(fromWindooAux!=0 && baroAvgAux!=0) {
                String date = DateFormat.format("dd/MM/yyyy-HH:mm:ss", new java.util.Date()).toString();

                String sequence = date + " " + aux + " " + baroAvgAux + " " + fromWindooAux + "\n";
                stream.write(sequence.replace(".", ",").getBytes());
                //stream.close();
                fromWindooAux=0;
                baroAvgAux=0;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("FileUtil", "PROBLEMS SAVING TO FILE");
            Log.e("FileUtil", e.getMessage());
        }
    }*/

    //This method writes the raw data

    public static void saveToFile(double baroHeight, double baroAvg, double fromWindoo){
        try {
            String date = DateFormat.format("dd/MM/yyyy-HH:mm:ss", new java.util.Date()).toString();

            String sequence = date + " " + baroHeight + " " + baroAvg + " " + fromWindoo + "\n";

            stream.write(sequence.replace(".", ",").getBytes());

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("FileUtil", "PROBLEMS SAVING TO FILE");
            Log.e("FileUtil", e.getMessage());
        }
    }

    public static void closeOutputStream(){
        try {
            if(stream != null){
                stream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("FileUtil", "PROBLEMS SAVING TO FILE");
            Log.e("FileUtil", e.getMessage());
        }
    }

}
