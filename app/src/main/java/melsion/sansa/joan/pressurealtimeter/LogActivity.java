package melsion.sansa.joan.pressurealtimeter;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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

        MainActivity.checkPermissions(this);

        //ToDo: NO VA. VEURE SI POSO UN IF

        String logs = FileUtil.getStringLogs();
        logTV.setText(logs);
    }

    @Override
    public void onResume(){
        super.onResume();
        MainActivity.checkPermissions(this);
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
                final View customView = getLayoutInflater().inflate(R.layout.save_file_alertdialog,null);
                final EditText editText = customView.findViewById(R.id.selected_name_et);
                String date = DateFormat.format("dd-MM-yyyy_HH:mm:ss", new java.util.Date()).toString();
                String defaultName = Constants.FILE_NAME+"_"+date;
                editText.setText(defaultName);

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Save logs file")
                .setMessage("Select file name")
                .setView(customView)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String selectedName = editText.getText().toString();
                        FileUtil.saveFile(getApplicationContext(),selectedName);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
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