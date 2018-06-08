package melsion.sansa.joan.pressurealtimeter;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.File;

public class LogActivity extends AppCompatActivity {

    private TextView logTV;

    //--------------------------------------------------------------------------
    // LIFE CYCLE
    //--------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Logs");
        logTV = findViewById(R.id.log_tv);

        String logs = FileUtil.getStringLogs();
        logTV.setText(logs);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.logs_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.save_item:
                FileUtil.saveFile(this);
                break;
            case R.id.send_item:
                sendFileByMail();
                break;
            case R.id.restart_item:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Are you sure?")
                        .setMessage("You are going to clear all collected data and preferences.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferencesUtils.clearSharedPreferences(getApplicationContext());
                                //Restart app:
                                Intent i = getBaseContext().getPackageManager()
                                        .getLaunchIntentForPackage( getBaseContext().getPackageName())
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create()
                        .show();
                break;
            default:
                onBackPressed();
        }
        return true;
    }

    //--------------------------------------------------------------------------
    // OTHERS
    //--------------------------------------------------------------------------

    public void sendFileByMail(){
        String fileName = Constants.FILE_NAME+Constants.FILE_EXTENSION;
        File file = new File(getExternalFilesDir(null),fileName);
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name)+": "+fileName)
                .putExtra(Intent.EXTRA_EMAIL, new String[] {getString(R.string.author_email)})
                .setType("message/rfc822")
                .putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        startActivity(Intent.createChooser(emailIntent, "Select email app..."));
    }
}