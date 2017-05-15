package org.hcilab.circog;

import android.util.Log;

import org.hcilab.log.Keys;
import org.hcilab.log.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LogManager {

    private static final String	TAG	= LogManager.class.getSimpleName();

    //APP Stats
    public static final String KEY_APP_LAUNCH               = "appLaunch";

    //Notification
    public static final String KEY_NOTIFICATION             = "notification";
    public static final String KEY_NOTIFICATION_STATUS      = "notificationStatus";
    public static final String KEY_TIME_SINCE_POSTED        = "timeSincePosted";

    public static final String KEY_TASK                     = "task";

    //POT
    public static final String KEY_PVT              = "PVT";
    public static final String KEY_MEASUREMENTS     = "measurements";
    public static final String KEY_NUM_TAPS         = "numberOfTaps";
    public static final String KEY_TASK_COMPLETED   = "taskCompleted";
    public static final String KEY_TASK_SEQUENCE_COMPLETED   = "taskSequenceCompleted";
    public static final String KEY_TASK_START_TIME  = "startTaskTime";  //ms
    public static final String KEY_TASK_END_TIME    = "endTaskTime";    //ms
    public static final String KEY_TASK_DURATION    = "taskDuration";   //ms

    //GNG
    public static final String KEY_GNG              = "GNG";
    public static final String KEY_CORRECT_TAPS     = "hit";
    public static final String KEY_FALSE_TAPS       = "falseAlarms";
    public static final String KEY_HIT_MEASUREMENTS = "hitMeasurements";
    public static final String KEY_FALSE_ALARM_MEASUREMENTS = "falseAlarmMeasurements";
    public static final String KEY_CORRECT_MISSES   = "correctRejections";
    public static final String KEY_FALSE_MISSES     = "miss";
    public static final String KEY_PREMATURE_TAPS   = "prematureTaps";

    //MOT
    public static final String KEY_MOT              = "MOT";
    public static final String KEY_NUM_TASKS        = "numberOfTasks";
    public static final String KEY_CORRECT_SELECTIONS = "correctSelections";
    public static final String KEY_TOTAL_TARGETS_SEEN = "totalTargetsSeen";

    //Daily Survey
    public static final String KEY_DAILY_SURVEY                 = "dailySurvey";
    public static final String KEY_DAILY_SURVEY_WAKEUP_HOUR     = "wakeupHour";
    public static final String KEY_DAILY_SURVEY_WAKEUP_MIN      = "wakeupMinute";
    public static final String KEY_DAILY_SURVEY_HOURS_SLEPT     = "hoursSlept";
    public static final String KEY_DAILY_SURVEY_SLEEP_QUALITY   = "sleepQuality";

    //Task Survey
    public static final String KEY_TASK_SURVEY                  = "taskSurvey";
    public static final String KEY_TASK_SURVEY_ALERTNESS        = "alertness";
    public static final String KEY_TASK_SURVEY_CAFFEINATED      = "caffeinated";

    /**
     * logs the launch of the app
     */
    public static void logAppLaunch(boolean notifTriggered) {
        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "-logAppLaunch()");
        }

        JSONObject json = new JSONObject();
        try {

            JSONObject values = new JSONObject();
            values.put(KEY_NOTIFICATION, notifTriggered);

            json.put(Keys.SENSOR_ID, KEY_APP_LAUNCH);
            json.put(Keys.SENSOR_VALUE, values);

        } catch (JSONException e) {
            if(CircogPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR putting POT data into json object");
            }
            e.printStackTrace();
        }

        Logger.logSensorSnapshot(json);
    }

    /**
     * logs the completion of a full task sequence
     */
    public static void taskSequenceCompleted() {
        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "-taskSequenceCompleted()");
        }

        JSONObject json = new JSONObject();
        try {

            json.put(Keys.SENSOR_ID, KEY_TASK_SEQUENCE_COMPLETED);
            json.put(Keys.SENSOR_VALUE, System.currentTimeMillis());

        } catch (JSONException e) {
            if(CircogPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR logging taskSequenceCompleted");
            }
            e.printStackTrace();
        }

        Logger.logSensorSnapshot(json);
    }

    /**
     * logs Psychomotor Vigilance Test (PVT) results both locally and writes it to server log
     * @param measurements
     * @param numberOfTaps
     */
    public static void logPVT(ArrayList<Long> measurements, int numberOfTaps, long startTasksTime, long endTasksTime, boolean taskCompleted, int alertness, boolean caffeinated) {

        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "-logPVT()");
        }

        JSONObject json = new JSONObject();
        try {
            JSONObject values = new JSONObject();

            values.put(KEY_MEASUREMENTS, measurements);
            values.put(KEY_NUM_TAPS, numberOfTaps);
            values.put(KEY_TASK_START_TIME, startTasksTime);
            values.put(KEY_TASK_END_TIME, endTasksTime);
            values.put(KEY_TASK_DURATION, endTasksTime-startTasksTime);
            values.put(KEY_TASK_COMPLETED, taskCompleted);
            values.put(KEY_TASK_SURVEY_ALERTNESS, alertness);
            values.put(KEY_TASK_SURVEY_CAFFEINATED, caffeinated);

            json.put(Keys.SENSOR_ID, KEY_PVT);
            json.put(Keys.SENSOR_VALUE, values);
        } catch (JSONException e) {
            if(CircogPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR putting POT data into json object");
            }
            e.printStackTrace();
        }

        Logger.logSensorSnapshot(json);
    }

    /**
     * logs Go/No-Go Task results both locally and writes it to server log
     * @param correctTaps
     * @param falseTaps
     * @param correctMisses
     * @param falseMisses
     * @param numberOfTaps
     * @param hitMeasurements
     * @param falseAlarmMeasurements
     */
    public static void logGNG(int correctTaps, int falseTaps, int correctMisses, int falseMisses, int numberOfTaps, ArrayList<Long> hitMeasurements, ArrayList<Long> falseAlarmMeasurements, long startTasksTime, long endTasksTime, boolean taskCompleted, int alertness, boolean caffeinated) {

        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "-logGNG()");
        }

        JSONObject json = new JSONObject();
        try {
            JSONObject values = new JSONObject();

            values.put(KEY_CORRECT_TAPS, correctTaps);
            values.put(KEY_FALSE_TAPS, falseTaps);
            values.put(KEY_CORRECT_MISSES, correctMisses);
            values.put(KEY_FALSE_MISSES, falseMisses);
            values.put(KEY_NUM_TAPS, numberOfTaps);

            values.put(KEY_HIT_MEASUREMENTS, hitMeasurements);
            values.put(KEY_FALSE_ALARM_MEASUREMENTS, falseAlarmMeasurements);

            values.put(KEY_TASK_START_TIME, startTasksTime);
            values.put(KEY_TASK_END_TIME, endTasksTime);
            values.put(KEY_TASK_DURATION, endTasksTime-startTasksTime);
            values.put(KEY_TASK_COMPLETED, taskCompleted);
            values.put(KEY_TASK_SURVEY_ALERTNESS, alertness);
            values.put(KEY_TASK_SURVEY_CAFFEINATED, caffeinated);

            json.put(Keys.SENSOR_ID, KEY_GNG);
            json.put(Keys.SENSOR_VALUE, values);
        } catch (JSONException e) {
            if(CircogPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR putting GNG data into json object");
            }
            e.printStackTrace();
        }

        Logger.logSensorSnapshot(json);
    }

    public static void logMOT(int taskCount, int correctSelections, int totalTargetsSeen, long startTasksTime, long endTasksTime, boolean taskCompleted, int alertness, boolean caffeinated) {

        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "-logMOT()");
        }

        JSONObject json = new JSONObject();
        try {
            JSONObject values = new JSONObject();

            values.put(KEY_NUM_TASKS, taskCount);
            values.put(KEY_CORRECT_SELECTIONS, correctSelections);
            values.put(KEY_TOTAL_TARGETS_SEEN, totalTargetsSeen);
            values.put(KEY_TASK_START_TIME, startTasksTime);
            values.put(KEY_TASK_END_TIME, endTasksTime);
            values.put(KEY_TASK_DURATION, endTasksTime-startTasksTime);
            values.put(KEY_TASK_COMPLETED, taskCompleted);
            values.put(KEY_TASK_SURVEY_ALERTNESS, alertness);
            values.put(KEY_TASK_SURVEY_CAFFEINATED, caffeinated);

            json.put(Keys.SENSOR_ID, KEY_MOT);
            json.put(Keys.SENSOR_VALUE, values);
        } catch (JSONException e) {
            if(CircogPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR putting MOT data into json object");
            }
            e.printStackTrace();
        }

        Logger.logSensorSnapshot(json);
    }

    public static void logDailySurveyFilledIn(int wakeupHour, int wakeupMinute, int hoursSlept, int sleepQuality) {

        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "-logDailySurveyFilledIn()");
        }

        JSONObject json = new JSONObject();
        try {
            JSONObject values = new JSONObject();

            values.put(KEY_DAILY_SURVEY_WAKEUP_HOUR, wakeupHour);
            values.put(KEY_DAILY_SURVEY_WAKEUP_MIN, wakeupMinute);
            values.put(KEY_DAILY_SURVEY_HOURS_SLEPT, hoursSlept);
            values.put(KEY_DAILY_SURVEY_SLEEP_QUALITY, sleepQuality);

            json.put(Keys.SENSOR_ID, KEY_DAILY_SURVEY);
            json.put(Keys.SENSOR_VALUE, values);
        } catch (JSONException e) {
            if(CircogPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR putting Notification data into json object");
            }
            e.printStackTrace();
        }

        Logger.logSensorSnapshot(json);

    }
}
