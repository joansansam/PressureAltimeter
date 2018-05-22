package melsion.sansa.joan.pressurealtimeter;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LogActivity extends AppCompatActivity {

    private TextView logTV;

    //--------------------------------------------------------------------------
    // LIFE CYCLE
    //--------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        logTV = findViewById(R.id.log_tv);

        String logs = readFile();
        logTV.setText(logs);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    //--------------------------------------------------------------------------
    // OTHERS
    //--------------------------------------------------------------------------

    private String readFile(){
        //Get the text file
        File file = new File(getExternalFilesDir(null),"altimeter_logs.csv");

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
            Log.e("LogActivity",e.getMessage());
            return "";
        }
    }
}
