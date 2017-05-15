package org.hcilab.circog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.hcilab.log.Logger;

/**
 * @author Tilman Dingler
 * @see https://github.com/til-d/circog
 * @version 1.0
 * @since 05/2017
 */
public class MainActivity extends AppCompatActivity {

    private static final String	TAG	= MainActivity.class.getSimpleName();

    public static final int[] COMPLETE_TASKLIST = {PVTActivity.TASK_ID, GoNoGoActivity.TASK_ID, MOTActivity.TASK_ID}; //{MOTActivity.TASK_ID};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "+ onCreate()");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TaskList.initTaskList(getApplicationContext());
    }

    @Override
    public void onResume() {
        super.onResume();

        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "+ onResume()");
        }

        Logger.startLogger(getApplicationContext());

        //track whether app has been opened through a notification or explicit app launch
        boolean notifTriggered = Util.getBool(getApplicationContext(), CircogPrefs.NOTIF_CLICKED, false);
        LogManager.logAppLaunch(notifTriggered);
        Util.putBool(getApplicationContext(), CircogPrefs.NOTIF_CLICKED, false);

        //make sure NotificationTriggerService is running
        startService(new Intent(this, NotificationTriggerService.class));

        //check whether demographics have been recorded
        boolean provided = Util.getBool(getApplicationContext(), CircogPrefs.DEMOGRAPHICS_PROVIDED, false);

        //check whether consent has been given
        if (!Util.getBool(this, CircogPrefs.PREF_CONSENT_GIVEN, false)) {
            final Intent intent = new Intent(this, ConsentActivity.class);
            startActivity(intent);
            finish();
        } else {

            launchNextTask();

            if(!provided) {
                //launch demographics activity
                final Intent intent = new Intent(this, EnterDemographicsActivity.class);
                startActivity(intent);
                finish();
            }

        }

    }

    private void launchNextTask() {
        //launch task according to tasklist
        int task = TaskList.getCurrentTask(getApplicationContext());
        if(task==PVTActivity.TASK_ID) {
            launchPVT(findViewById(android.R.id.content));
        }
        else if(task==GoNoGoActivity.TASK_ID) {
            launchGoNoGo(findViewById(android.R.id.content));
        }
        else if(task==MOTActivity.TASK_ID) {
            launchMOT(findViewById(android.R.id.content));
        }

        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        NotificationTriggerService.removeNotification(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void launchPVT (final View view) {
        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "launchPVT()");
        }
        Util.putString(getApplicationContext(), CircogPrefs.CURRENT_TASK, LogManager.KEY_PVT);
        final Intent intent = new Intent(this, PVTActivity.class);
        startActivity(intent);
//        finish();
    }

    public void launchGoNoGo(final View view) {
        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "launchGoNoGo()");
        }
        Util.putString(getApplicationContext(), CircogPrefs.CURRENT_TASK, LogManager.KEY_GNG);
        final Intent intent = new Intent(this, GoNoGoActivity.class);
        startActivity(intent);
    }

    public void launchMOT(final View view) {
        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "launchMOT()");
        }
        Util.putString(getApplicationContext(), CircogPrefs.CURRENT_TASK, LogManager.KEY_MOT);
        final Intent intent = new Intent(this, MOTActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
