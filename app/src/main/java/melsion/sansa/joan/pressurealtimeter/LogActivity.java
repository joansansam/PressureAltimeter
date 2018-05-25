package melsion.sansa.joan.pressurealtimeter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by joan.sansa.melsion on 22/05/2018.
 * https://github.com/joansansam/PressureAltimeter
 */

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

        //ToDo: send logs by mail

        //ToDo: save logs with an unique name (to not overwrite file)
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.dev_menu, menu);
        menu.getItem(R.id.item).setTitle("Send by mail");
        menu.getItem(R.id.invisible_item).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.item:
                //ToDo: start sending email intent
                break;
        }
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
