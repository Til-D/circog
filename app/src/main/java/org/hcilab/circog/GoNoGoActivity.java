package org.hcilab.circog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

public class GoNoGoActivity extends AppCompatActivity {

    public static final int TASK_ID = 2;
    private static final String	TAG	= GoNoGoActivity.class.getSimpleName();

    //SETTINGS
    private final static int CIRCLE_PLANE = 0;
    private final static int CIRCLE_PATTERNED = 1;
    private static final int MIN_DELAY = 1000; //ms
    private static final int MAX_DELAY = 8000; //ms
    private static final int MIN_TIME_PASSED = 100; //ms
    private static final int MAX_TIME_PASSED = 3000; //ms
    private static final int MIN_TASKS = 8;
    private static final int MAX_TASKS = 12;

    private Button btnStartStop;
    private ImageView imgCircle;
    private ArrayList<Integer> tasklist;
    private int taskIndex;
    private ArrayList<Long> correctMeasurements;
    private ArrayList<Long> falseAlarmMeasurements;
    private long startTime;
    private boolean circleShowing;
    private int numberOfTaps;
    private int correctTaps;
    private int falseTaps;
    private int correctMisses;
    private int falseMisses;
    private boolean allTasksCompleted;
    private long startTasksTime;
    private long endTasksTime;
    private int total_tasks;

    //manages the appearance of the timer
    private Handler taskHandler = new Handler();
    private Runnable taskRunnable = new Runnable() {

        @Override
        public void run() {
            if(CircogPrefs.DEBUG_MODE) {
                Log.i(TAG, "+ circle task timer fired");
            }
            startTime = System.currentTimeMillis();
            showCurrentCircle();
            circleShowing = true;
        }
    };

    //manages the cancel timer after 3 seconds
    private Handler cancelHandler = new Handler();
    private Runnable cancelRunnable = new Runnable() {
        @Override
        public void run() {
            if(CircogPrefs.DEBUG_MODE) {
                Log.i(TAG, "- cancel task timer fired");
            }

            //assess correct / incorrect
            if(tasklist.get(taskIndex)==CIRCLE_PATTERNED) {
                correctMisses++;
            } else {
                falseMisses++;
            }
            taskIndex++;
            imgCircle.setImageResource(R.drawable.circle_target);
            circleShowing = false;
            scheduleNextTask();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_no_go);

        //make sure screen keeps turned on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        btnStartStop = (Button) findViewById(R.id.btn_start_stop);
        imgCircle = (ImageView) findViewById(R.id.img_circle);
        imgCircle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(circleShowing) {
                    if(CircogPrefs.DEBUG_MODE) {
                        Log.i(TAG, "- stopping cancel timer");
                    }
                    cancelHandler.removeCallbacks(cancelRunnable);
                    circleShowing = false;

                    long timePassed = System.currentTimeMillis() - startTime;

                    if(timePassed > MIN_TIME_PASSED) {
                        //assess correct / incorrect
                        if(tasklist.get(taskIndex)==CIRCLE_PLANE) {
                            correctTaps++;
                            correctMeasurements.add(timePassed);
                        } else {
                            falseTaps++;
                            falseAlarmMeasurements.add(timePassed);
                            Toast.makeText(GoNoGoActivity.this, R.string.GNG_false_alarm, Toast.LENGTH_SHORT).show();
                        }
                        taskIndex++;
                    }
                    scheduleNextTask();
                }
                return false;
            }
        });

        /**
         * collects numberOfTaps, i.e. premature taps
         */
        imgCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberOfTaps++;
            }
        });

        //check whether daily survey has been answered yet
        Date lastDailySurveyTaken = new Date(Util.getLong(getApplicationContext(), CircogPrefs.DATE_LAST_DAILY_SURVEY_MS, 0));
        Date now = new Date(System.currentTimeMillis());
        boolean dailySurveyAnswered = Util.isSameDay(lastDailySurveyTaken, now);
        if(!dailySurveyAnswered) {
            startActivity(new Intent(getApplicationContext(), DailySurveyActivity.class));
        }

        //show alert assessments
        ArrayList<Integer> tasklist = TaskList.getTaskList(getApplicationContext());
        if(tasklist.size()==MainActivity.COMPLETE_TASKLIST.length) {
            final Intent intent = new Intent(this, AlertnessSurveyActivity.class);
            startActivity(intent);
        }
    }

    public void StartStopButtonClicked(final View view) {
        if(btnStartStop.getVisibility()==View.VISIBLE && btnStartStop.getText().equals(getString(R.string.pvt_btn_stop))) { //stop task
            if(CircogPrefs.DEBUG_MODE) {
                Log.i(TAG, "- stop GNG task");
            }
            cancelTimers();
            btnStartStop.setText(R.string.pvt_btn_start);
            circleShowing = false;
            endTasksTime= System.currentTimeMillis();
            showFinishScreen();

        } else { //start timer
            if(CircogPrefs.DEBUG_MODE) {
                Log.i(TAG, "+ start GNG task");
            }
            startTasksTime = System.currentTimeMillis();
            scheduleNextTask();
            if(CircogPrefs.DEBUG_MODE) {
                btnStartStop.setText(R.string.pvt_btn_stop);
            } else {
                //don't allow stop button in production mode
                btnStartStop.setVisibility(View.GONE);
            }
        }
    }

    private void showCurrentCircle() {
        switch(tasklist.get(taskIndex)) {
            case CIRCLE_PLANE:
                imgCircle.setImageResource(R.drawable.circle_plain);
                break;
            case CIRCLE_PATTERNED:
                imgCircle.setImageResource(R.drawable.circle_patterned);
                break;
            default:
                imgCircle.setImageResource(R.drawable.circle_target);
                break;
        }
    }

    private void scheduleNextTask() {
        if(taskIndex < total_tasks) {
            Random rand = new Random();
            long interval = MIN_DELAY + rand.nextInt((MAX_DELAY - MIN_DELAY) + 1);
            long now = System.currentTimeMillis();
            taskHandler.postAtTime(taskRunnable, now + interval);
            taskHandler.postDelayed(taskRunnable, interval);
            cancelHandler.postAtTime(cancelRunnable, now + interval + MAX_TIME_PASSED);
            cancelHandler.postDelayed(cancelRunnable, interval + MAX_TIME_PASSED);
            imgCircle.setImageResource(R.drawable.circle_target);
            if(CircogPrefs.DEBUG_MODE) {
                Log.i(TAG, "+ task scheduled in " + interval + " ms");
            }
        } else {
            if(CircogPrefs.DEBUG_MODE) {
                Log.i(TAG, "- tasks finished: " + correctMeasurements.size() + " correctMeasurements.");
                Log.i(TAG, "- tasks finished: " + falseAlarmMeasurements.size() + " falseAlarmMeasurements.");
            }
            endTasksTime= System.currentTimeMillis();
            showFinishScreen();
        }
    }

    /**
     * inits tasklist with an equal number of target types and shuffles the task list
     */
    private void initTasklist() {
        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "initTaskList()");
        }
        tasklist = new ArrayList<Integer>();

        //random number of targets
        Random rand = new Random();
        if(CircogPrefs.DEBUG_MODE) {
            total_tasks = 1;
        } else {
            total_tasks = MIN_TASKS + rand.nextInt((MAX_TASKS - MIN_TASKS) + 1);
        }

        for(int i=0; i<total_tasks/2; i++)
        {
            tasklist.add(CIRCLE_PLANE);
        }
        for(int i=total_tasks/2+1; i<=total_tasks; i++)
        {
            tasklist.add(CIRCLE_PATTERNED);
        }
        Collections.shuffle(tasklist);

        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, tasklist.toString());
        }
        taskIndex = 0;
        correctMeasurements = new ArrayList<Long>();
        falseAlarmMeasurements = new ArrayList<Long>();
        circleShowing = false;
        numberOfTaps = 0;
        correctTaps = 0;
        falseTaps = 0;
        correctMisses = 0;
        falseMisses = 0;
        allTasksCompleted = false;

        imgCircle.setImageResource(android.R.color.transparent);
        btnStartStop.setVisibility(View.VISIBLE);
        btnStartStop.setText(R.string.pvt_btn_start);

        if(CircogPrefs.DEBUG_MODE) {
            if(CircogPrefs.DEBUG_MODE) {
                Log.i(TAG, "# of targets created: " + total_tasks);
            }
        }
    }

    /**
     * launches next task in tasklist and finishes current activity
     */
    private void launchNextTask() {
        int task = TaskList.getCurrentTask(getApplicationContext());
        if(task==PVTActivity.TASK_ID) {
            if(CircogPrefs.DEBUG_MODE) {
                Log.i(TAG, "launchPVT()");
            }
            Util.putString(getApplicationContext(), CircogPrefs.CURRENT_TASK, LogManager.KEY_PVT);
            final Intent intent = new Intent(this, PVTActivity.class);
            startActivity(intent);
        }
        else if(task==GoNoGoActivity.TASK_ID) {
            if(CircogPrefs.DEBUG_MODE) {
                Log.i(TAG, "launchGoNoGo()");
            }
            Util.putString(getApplicationContext(), CircogPrefs.CURRENT_TASK, LogManager.KEY_GNG);
            final Intent intent = new Intent(this, GoNoGoActivity.class);
            startActivity(intent);
        }
        else if(task==MOTActivity.TASK_ID) {
            if(CircogPrefs.DEBUG_MODE) {
                Log.i(TAG, "launchMOT()");
            }
            Util.putString(getApplicationContext(), CircogPrefs.CURRENT_TASK, LogManager.KEY_MOT);
            final Intent intent = new Intent(this, MOTActivity.class);
            startActivity(intent);
        }

        //check whether this is the last task
        ArrayList<Integer> tasklist = TaskList.getTaskList(getApplicationContext());
        if(tasklist.size()==0) {

            //increment DAILY_TASK_COUNT
            TaskList.incrementDailyTaskCount(getApplicationContext());

            //bye bye message
            Toast.makeText(GoNoGoActivity.this, R.string.task_toast_finish, Toast.LENGTH_LONG).show();
        }
        finish();
    }

    private void showFinishScreen() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        String avg = String.format("%.1f", Util.calculateAverage(correctMeasurements));
        int corrects = correctMisses + correctTaps;
        int total = correctMisses + correctTaps + falseTaps + falseMisses;

//        String txt = getString(R.string.gng_dialog_finish_no_feedback);
        String txt = TaskList.getNextTask(getApplicationContext());
        if(CircogPrefs.PROVIDE_FEEDBACK) {
            txt = String.format(getString(R.string.gng_dialog_finish), total, corrects, total, avg);
            if (total == 1) {
                txt = String.format(getString(R.string.gng_dialog_finish_single), total, correctTaps, total, avg);
            }
        }

        alertDialogBuilder.setMessage(txt);

        //TODO: set icon
        alertDialogBuilder.setPositiveButton(getString(R.string.gng_dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                launchNextTask();
            }
        });
        alertDialogBuilder.show();

        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "# of correct taps: " + correctTaps);
            Log.i(TAG, "# of false taps: " + falseTaps);
            Log.i(TAG, "# of correct misses: " + correctMisses);
            Log.i(TAG, "# of false misses: " + falseMisses);
            Log.i(TAG, "# of false premature taps: " + numberOfTaps);
            Log.i(TAG, "# of correctMeasurements: " + correctMeasurements.size() + ", avg: " + avg);
            Log.i(TAG, "time correctMeasurements: " + correctMeasurements.toString());
            Log.i(TAG, "time falseAlarmMeasurements: " + falseAlarmMeasurements.toString());
        }

        int alertness = Util.getInt(this, CircogPrefs.LEVEL_ALERTNESS, -1);
        boolean caffeinated = Util.getBool(this, CircogPrefs.CAFFEINATED, false);
        boolean taskCompleted = taskIndex==total_tasks;
        LogManager.logGNG(correctTaps, falseTaps, correctMisses, falseMisses, numberOfTaps, correctMeasurements, falseAlarmMeasurements, startTasksTime, endTasksTime, taskCompleted, alertness, caffeinated);
        allTasksCompleted = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        cancelTimers();

        if(!allTasksCompleted) {
            endTasksTime = System.currentTimeMillis();
            int alertness = Util.getInt(this, CircogPrefs.LEVEL_ALERTNESS, -1);
            boolean caffeinated = Util.getBool(this, CircogPrefs.CAFFEINATED, false);
            LogManager.logGNG(correctTaps, falseTaps, correctMisses, falseMisses, numberOfTaps, correctMeasurements, falseAlarmMeasurements, startTasksTime, endTasksTime, allTasksCompleted, alertness, caffeinated);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initTasklist();
    }

    private void cancelTimers() {
        taskHandler.removeCallbacks(taskRunnable);
        cancelHandler.removeCallbacks(cancelRunnable);
    }
}
