package org.hcilab.circog;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Manages the tasklist: creation, order, and updates
 */
public class TaskList {

    private static final String	TAG	= TaskList.class.getSimpleName();

    /**
     * initates task list with random task order
     */
    public static void initTaskList(Context context) {
        ArrayList<Integer> taskList = new ArrayList<Integer>();
        for (int index = 0; index < MainActivity.COMPLETE_TASKLIST.length; index++)
        {
            taskList.add(MainActivity.COMPLETE_TASKLIST[index]);
        }
        Collections.shuffle(taskList);

        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "tasklist initiated: " + taskList.toString());
        }
        saveCurrentTaskList(taskList, context);
    }

    /**
     * saves current tasklist as string list to shared preferences
     * @param taskList
     */
    private static void saveCurrentTaskList(ArrayList<Integer> taskList, Context context) {

        Util.putString(context, CircogPrefs.TASK_SEQUENCE, taskList.toString());

        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "tasklist saved: " + taskList.toString());
        }
    }

    public static int getCurrentTask(Context context) {
        ArrayList<Integer> taskList = getTaskList(context);
        if(taskList.size()>0) {
            return taskList.get(0);
        } else {
            return -1;
        }
    }

    /**
     *
     * @param context
     * @return removes current task and based on remaining number of tasks it returns a message string
     */
    public static String getNextTask(Context context) {

        ArrayList<Integer> taskList = getTaskList(context);

        if(taskList.size()>0) {
            taskList.remove(0);
        }

        String txt;

        switch (taskList.size()) {
            case(0):
                txt = context.getResources().getString(R.string.task_dialog_finish);
                break;
            case (1):
                txt = context.getResources().getString(R.string.task_dialog_next_task);
                break;
            default:
                txt = String.format(context.getResources().getString(R.string.task_dialog_next_tasks), taskList.size());
                break;
        }

        saveCurrentTaskList(taskList, context);
        return txt;
    }

    /**
     *
     * @param context
     * @returns current task list from shared preferences
     */
    public static ArrayList<Integer> getTaskList(Context context) {

        String rawTaskList = Util.getString(context, CircogPrefs.TASK_SEQUENCE, "");
        ArrayList<Integer> taskList = new ArrayList<Integer>();

        if(!rawTaskList.equals("") && !rawTaskList.equals("[]")) {
            //parse rawTaskList into String array
            String[] elements = rawTaskList.substring(1, rawTaskList.length() - 1).split(CircogPrefs.TASKLIST_DELIMITER);
            for (String item : elements) {
                taskList.add(Integer.valueOf(item));
            }
            if(CircogPrefs.DEBUG_MODE) {
                Log.i(TAG, "getTaskList: " + taskList.toString());
            }
        }
        return taskList;
    }

    /**
     *
     * @param context
     * @returns number of tasks completed today
     */
    public static int getDailyTaskCount(Context context) {

        int taskCount = Util.getInt(context, CircogPrefs.DAILY_TASK_COUNT, 0);
        Date lastTaskCompleted = Util.getDateFromTimestamp(Util.getLong(context, CircogPrefs.DATE_LAST_TASK_COMPLETED, 0));
        Date now = new Date(System.currentTimeMillis());

        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "getDailyTaskCount: " + taskCount + " (lastTaskCompleted: " + lastTaskCompleted.toString()+ ", now: " + now.toString() + ", sameDay: " + Util.isSameDay(lastTaskCompleted, now) + ")");
        }

        if(Util.isSameDay(lastTaskCompleted, now)) { //last task was completed today
            if(CircogPrefs.DEBUG_MODE) {
                Log.i(TAG, "getDailyTaskCount: " + taskCount);
            }
            return taskCount;
        } else {
            if(CircogPrefs.DEBUG_MODE) {
                Log.i(TAG, "getDailyTaskCount: none today");
            }
            return 0;
        }
    }

    /**
     * increments the daily task count by 1 and updates date
     * @param context
     */
    public static void incrementDailyTaskCount(Context context) {
        int dailyTaskCount = Util.getInt(context, CircogPrefs.DAILY_TASK_COUNT, 0);
        Date lastTaskCompleted = Util.getDateFromTimestamp(Util.getLong(context, CircogPrefs.DATE_LAST_TASK_COMPLETED, 0));
        long now = System.currentTimeMillis();
        Date nowDate = new Date(System.currentTimeMillis());

        if(Util.isSameDay(lastTaskCompleted, nowDate)) { //reset task count if last task was completed on any other day than today
            Util.putInt(context, CircogPrefs.DAILY_TASK_COUNT, ++dailyTaskCount);
        } else {
            dailyTaskCount = 1;
            Util.putInt(context, CircogPrefs.DAILY_TASK_COUNT, dailyTaskCount);
        }
        Util.putLong(context, CircogPrefs.DATE_LAST_TASK_COMPLETED, now);

        LogManager.taskSequenceCompleted();

        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "incrementDailyTaskCount to: " + dailyTaskCount + " (last update: " + lastTaskCompleted.toString() + ")");
        }
    }
}