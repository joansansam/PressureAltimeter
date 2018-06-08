package melsion.sansa.joan.pressurealtimeter;

import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {

    private static FileOutputStream outputStream;
    private static double baroAvgAux;
    private static double fromWindooAux;
    private static File file;

    public static void createFile(Context context){
        if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return;
        }

        //Save file to ...\Phone\Android\data\melsion.sansa.joan.pressurealtimeter\files
        file = new File(context.getExternalFilesDir(null),Constants.FILE_NAME+Constants.FILE_EXTENSION);

        if(file.exists())
            file.delete();

        try {
            outputStream = new FileOutputStream(file);
            String sequence= "date instant_height averaged_height windoo_heigh \n";
            outputStream.write(sequence.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("FileUtil", "PROBLEMS SAVING TO FILE");
            Log.e("FileUtil", e.getMessage());
        }
    }

    //This method does not write the zeros between measures in the .csv file
    /*public static void addToFile(double aux, double baroAvg, double fromWindoo){
        try {
            if(baroAvg==0){
                fromWindooAux=fromWindoo;
            } else if(fromWindoo==0) {
                baroAvgAux = baroAvg;
            }
            if(fromWindooAux!=0 && baroAvgAux!=0) {
                String date = DateFormat.format("dd/MM/yyyy-HH:mm:ss", new java.util.Date()).toString();
                String sequence = date + " " + aux + " " + baroAvgAux + " " + fromWindooAux + "\n";
                outputStream.write(sequence.replace(".", ",").getBytes());
                //outputStream.close();
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

    public static void addToFile(String baroHeight, String baroAvg, String fromWindoo){
        try {
            String date = DateFormat.format("dd/MM/yyyy-HH:mm:ss", new java.util.Date()).toString();

            String sequence = date + " " + baroHeight + " " + baroAvg + " " + fromWindoo + "\n";

            outputStream.write(sequence.replace(".", ",").getBytes());

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("FileUtil", "PROBLEMS SAVING TO FILE");
            Log.e("FileUtil", e.getMessage());
        }
    }

    public static void closeOutputStream(){
        try {
            if(outputStream != null){
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("FileUtil", "PROBLEMS SAVING TO FILE");
            Log.e("FileUtil", e.getMessage());
        }
    }

    /**
     * https://www.androidcode.ninja/copy-or-move-file-from-one-directory-to/
     * @param context
     */
    public static void saveFile(Context context){
        String date = DateFormat.format("dd-MM-yyyy_HH:mm:ss", new java.util.Date()).toString();
        String targetFileName = Constants.FILE_NAME+"_"+date+Constants.FILE_EXTENSION;
        File targetLocation = new File(context.getApplicationContext().getExternalFilesDir(null),targetFileName);

        try {
            InputStream in = new FileInputStream(file);
            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;

            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            in.close();
            out.close();

            Log.d("FileUtil", "File successfully saved");
        } catch(IOException e){
            Log.e("FileUtil", "IOException: "+e.getMessage());
        }

    }

    public static String getStringLogs(){
        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
            return text.toString();
        }
        catch (IOException e) {
            Log.e("FileUtil",e.getMessage());
            return "";
        }
    }

}