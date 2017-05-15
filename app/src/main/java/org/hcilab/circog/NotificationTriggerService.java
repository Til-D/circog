package org.hcilab.circog;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationTriggerService extends Service implements CircogPrefs {

    private static final String TAG = NotificationTriggerService.class.getSimpleName();

    private static final boolean DISABLE_NOTIFICATIONS = false;
    private static final boolean MAKE_NOISE = true;
    public static final String NOTIFICATION_TAG = "TASK";
    public static final int    NOTIFICATION_ID  = 0;

    private static NotificationTriggerService instance;
    private Timer showNotifTimer;
    private Timer cancelNotifTimer;

    public static boolean notificationIsShown;
    public static boolean notificationIsScheduled;

    // Hours in which the experience sampling may be active
    private static final int START_HOUR = 8;
    private static final int END_HOUR = 21;

    private static final int MIN_DELAY = 10; //seconds
    private static final int MAX_DELAY = 5 * 60; //seconds

    private static final int MIN_TIME_SINCE_LAST_NOTIFICATION = 45; //20; // in minutes

    protected static final int		MIN_MS							= 1000 * 60; //milliseconds per minute
    protected static final int 		SEC_MS 							= 1000;	//milliseconds per second
    protected static final int      EVERY_HOUR                      = 60 * 60 * SEC_MS;
    protected static final int      EVERY_MINUTE                    = 60 * SEC_MS;

    private static final int		CANCEL_NOTIFICATION_DELAY_MS	= MIN_MS * 5; //5

    public NotificationTriggerService() {
        instance = this;
    }

    public static NotificationTriggerService getInstance() {
        return instance;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if(DEBUG_MODE) {
            Log.i(TAG, "onBind()");
        }
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(DEBUG_MODE) {
            Log.i(TAG, "onCreate()");
        }
        notificationIsScheduled = false;
        notificationIsShown = false;

        scheduleNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(DEBUG_MODE) {
            Log.i(TAG, "onStartCommand()");
        }

//        scheduleNotification();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        startService(new Intent(this, NotificationTriggerService.class));

    }

    /**
     * Schedules a notification ahead of time
     */
    public boolean notificationAllowed() {

        if (DISABLE_NOTIFICATIONS) {
            if(DEBUG_MODE) {
                Log.i(TAG, "notification aborted - disable_esm_notifs == TRUE");
            }
            return false;
        }

        if (!Util.getBool(getApplicationContext(), PREF_CONSENT_GIVEN, false)) {
            if(DEBUG_MODE) {
                Log.i(TAG, "notification aborted - still waiting for consent");
            }
            return false;
        }

        if (notificationIsShown) {
            if(DEBUG_MODE) {
                Log.i(TAG, "notification aborted - already shown");
            }
            return false;
        }

        if (!isTimingAllowed()) {
            if(DEBUG_MODE) {
                Log.i(TAG, "notification aborted - not allowed at this time -- IGNORED");
            }
            return false;
        }

        if (!isMinTimeElapsed()) {
            if(DEBUG_MODE) {
                Log.i(TAG, "notification aborted - not enough time elapsed");
            }
            return false;
        }

        if (applicationIsOpen()) {
            if(DEBUG_MODE) {
                Log.i(TAG, "notification aborted - application already in foreground");
            }
            return false;
        }

        if (dailyTasksCompleted()) {
            if(DEBUG_MODE) {
                Log.i(TAG, "notification aborted - daily tasks completed: " + MAX_DAILY_TASKS);
            }
            return false;
        }

        return true;
    }

    private void scheduleNotification() {

        notificationIsScheduled = true;

        //TODO: spread this more across day?
        int delayMs;
        int randomDelay = Util.randInt(0, SEC_MS * 60 * 30); //0 - 30mins
        if(DEBUG_MODE) {
            delayMs = EVERY_MINUTE/2;
        } else {
            delayMs = EVERY_HOUR/2 + randomDelay;
        }

        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "scheduling notification - due in " + Util.format((double) delayMs / MIN_MS) + " min");
        }

        if (showNotifTimer != null) {
            showNotifTimer.cancel();
            showNotifTimer.purge();
        }

        showNotifTimer = new Timer();
        showNotifTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                notificationIsScheduled = false;
                onNotificationTimerFired();
            }

        }, delayMs);

    }

    private void onNotificationTimerFired() {
        if(DEBUG_MODE) {
            Log.i(TAG, "onNotificationTimerFired()");
        }

        if(notificationAllowed()) {

            notificationIsShown = true;
            Util.putLong(getApplicationContext(), LAST_NOTIFICATION_POSTED_MS, System.currentTimeMillis());
            triggerNotification();
        }

        scheduleNotification();

    }

    public static void removeNotification(Context context) {
        NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancelAll();
        notificationIsShown = false;
        Util.putBool(context, NOTIF_POSTED, false);
    }

    /**
     *
     * makes sure that notifications are only triggered during awake times
     *
     * @return
     */
    protected static boolean isTimingAllowed() {
        if(DEBUG_MODE) {
            Log.i(TAG, "-isTimingAllowed");
            return true;
        }

        Calendar cal = Calendar.getInstance();
        Date now = new Date(System.currentTimeMillis());
        cal.setTime(now);
        int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);

        // Log.i(TAG, "hour of the day is " + hourOfDay);

        if (hourOfDay >= START_HOUR && hourOfDay <= (END_HOUR - 1)) { return true; }

        return false;
    }

    /**
     *
     * @returns true when both screen is turned on and application is in foreground
     */
    protected boolean applicationIsOpen() {
        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "applicationIsOpen()");
        }

        return false;
    }

    public void triggerNotification() {
        if(DEBUG_MODE) {
            Log.i(TAG, "triggerNotification");
        }

        //update PREF_LAST_NOTIFICATION_POSTED
        Util.putLong(getApplicationContext(), NOTIF_POSTED_MILLIS, System.currentTimeMillis());
        Util.putBool(getApplicationContext(), NOTIF_POSTED, true);

        showNotification();
    }

    private void showNotification() {

        if(DEBUG_MODE) {
            Log.i(TAG, "showNotification");
        }

        notificationIsShown = false;

        // Build notification
        Notification.Builder builder = new Notification.Builder(getApplicationContext())
//                .setContentIntent(pendingIntentMain)
                .setSmallIcon(R.drawable.circog_notificon)
                .setContentTitle(getApplicationContext().getString(R.string.notif_title))
                .setContentText(getApplicationContext().getString(R.string.notif_content))
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setPriority(Notification.PRIORITY_HIGH);

        // Intent to open the MainActivity
        Intent intentMain = new Intent(getApplicationContext(), MainActivity.class);

        Util.putBool(getApplicationContext(), CircogPrefs.NOTIF_CLICKED, true);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intentMain);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);

        // Sound/vibrate?
        if(MAKE_NOISE) {
            builder.setDefaults(Notification.DEFAULT_ALL);
        }

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public boolean isMinTimeElapsed() {
        if(DEBUG_MODE) {
            return true;
        }
        Date now = new Date();
        //Date minMinutesAgo = new Date(now.getTime() - (MIN_TIME_SINCE_LAST_NOTIFICATION * StudyManager.ONE_MINUTE_IN_MILLIS));
        long minMinutesAgo = now.getTime() - (MIN_TIME_SINCE_LAST_NOTIFICATION * MIN_MS);
        Date lastScheduled = Util.getDateFromTimestamp(Util.getLong(getApplicationContext(), LAST_NOTIFICATION_POSTED_MS, minMinutesAgo)); //Util.getDateFromString("Mon Sep 14 13:08:43 MESZ 2015"); //
        long minutesPassed = ((now.getTime()/MIN_MS) - (lastScheduled.getTime()/MIN_MS));
        boolean notificationTimeoutPassed = (minutesPassed >= MIN_TIME_SINCE_LAST_NOTIFICATION);

        if(DEBUG_MODE) {
            Log.i(TAG, "isMinTimeElapsed: " + notificationTimeoutPassed + " (last notification posted " + minutesPassed + " Minutes ago. Required: " + MIN_TIME_SINCE_LAST_NOTIFICATION + " minutes)");
        }
        return notificationTimeoutPassed;
    }

    /**
     *
     * @returns boolean: whether tasks were all completed for today (as defined as MAX_DAILY_TASKS)
     */
    private boolean dailyTasksCompleted() {
        int dailyTaskCount = TaskList.getDailyTaskCount(getApplicationContext());
        return dailyTaskCount>=MAX_DAILY_TASKS;
    }
}