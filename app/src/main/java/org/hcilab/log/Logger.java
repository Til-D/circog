package org.hcilab.log;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import org.hcilab.circog.CircogPrefs;
import org.hcilab.circog.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static org.hcilab.circog.Util.getTimeStringMySql;
import static org.hcilab.circog.Util.getUpTimeSec;
import static org.hcilab.log.Keys.KEY_DATE;
import static org.hcilab.log.Keys.KEY_PID;
import static org.hcilab.log.Keys.KEY_UPTIME_SINCE_START_S;

public class Logger {

    // Fields
    private static final String TAG = Logger.class.getSimpleName();
    private static LogThread logger;
    private static String pid;

    public static synchronized void startLogger(Context context) {

        if (logger == null) {

            if(CircogPrefs.DEBUG_MODE) {
                Log.i(TAG, "startLogger()");
            }

            pid = LogThread.getPid(context);

            logger = new LogThread(context);
            logger.onStart();

        } else {

            if(CircogPrefs.DEBUG_MODE) {
                Log.i(TAG, "startLogger() -- logger already initialized.");
            }
        }
    }

    private static synchronized JSONObject prepareJSONSnapshot() {

        JSONObject json = new JSONObject();
        try {
            json.put(Keys.KEY_T, Keys.EMPTY);
            json.put(KEY_UPTIME_SINCE_START_S, Keys.EMPTY);
            json.put(KEY_DATE, Keys.EMPTY);
            json.put(KEY_PID, pid);
        } catch (JSONException e) {
            if(CircogPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR creating json object!");
            }
            e.printStackTrace();
        }
        return json;
    }

    public static synchronized void logSensorSnapshot(JSONObject sensor) {
        JSONObject json = prepareJSONSnapshot();
        try {
            Iterator<?> keys = sensor.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                json.put(key, sensor.getString(key));
            }
        } catch (JSONException e) {
            if(CircogPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR adding sensor log data to json object");
            }
            e.printStackTrace();
        }
        logJSONSnapshot(json);
    }

    public static synchronized void logJSONSnapshot(JSONObject json) {
        try {
            if (logger != null && logger.isRunning()) {
                logger.log(pid, json);
            } else {
                if(CircogPrefs.DEBUG_MODE) {
                    Log.i(TAG, "logger not available or not running");
                }
            }

        } catch (Exception e) {
            if(CircogPrefs.DEBUG_MODE) {
                Log.w(TAG, e + " in createSnapshot()", e);
            }
        }
    }
}

class LogMessageQueue {

    private LinkedList<LogMessage> messages	= new LinkedList<LogMessage>();

    public synchronized void queue(LogMessage message) {
        this.messages.addLast(message);
        notifyAll();
    }

    public synchronized LogMessage peek() {
        try {
            while (messages.size() == 0) {
                wait();
            }
            return messages.getFirst();
        } catch (Exception e) {
            return null;
        }
    }

    public synchronized LogMessage dequeue() {
        try {
            while (messages.size() == 0) {
                wait();
            }
            return messages.removeFirst();
        } catch (Exception e) {
            return null;
        }
    }

    public synchronized int size() {
        return messages.size();
    }
}

class LogMessage {

    public String	fileName;
    public String	message;

    public LogMessage(String fileName, String message) {
        this.fileName = fileName;
        this.message = message;

    }
}

class LogThread implements Runnable {

    private LogMessageQueue queue;
    private String deviceUid;
    private File logDir;
    private Context context;
    private boolean running;
    private Thread runner;
    private static final Object	LOCK = new Object();

    public LogThread(Context context) {

        this.context = context;
        this.deviceUid = getUniqueId(context);
        this.queue = new LogMessageQueue();
        this.logDir = getLogDir();

    }

    public static String getPid(Context context) {

        String uuid = getUniqueId(context);
        return uuid.substring(0, 6);

    }

    public static String getUniqueId(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        String userId = sharedPreferences.getString(CircogPrefs.PREF_UID, "");

        // if user ID is still there, store it
        if (userId.length() > 0)
            return userId;

        // otherwise, create new user ID
        userId = fromAndroidId(context);

        // store user id in preferences
        Editor edit = sharedPreferences.edit();
        edit.putString(CircogPrefs.PREF_UID, userId);
        edit.commit();

        return userId;
    }

    private static String fromAndroidId(Context context) {
        String userId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        try {
            MessageDigest md5;
            md5 = MessageDigest.getInstance("MD5");
            md5.reset();
            md5.update(userId.getBytes());
            byte[] result = md5.digest();

            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < result.length; i++) {
                hexString.append(Integer.toHexString(0xFF & result[i]));
            }
            userId = hexString.toString();
        } catch (Exception e) {
            userId = "nullId";
        }

        // check if this is an infected user id and extend randomly
        if (userId.equals("cf95dc53f383f9a836fd749f3ef439cd")) {
            userId = userId + "-" + System.currentTimeMillis();
        }
        return userId;
    }

    public File getLogDir() {
        String app = context.getString(R.string.app_name);
        File root = new File(Environment.getExternalStorageDirectory(), "log");
        File logDir = new File(root, app);
        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "Data directory " + logDir);
        }

        if (!logDir.exists()) {
            boolean success = logDir.mkdirs();
            if (!success) {
                if(CircogPrefs.DEBUG_MODE) {
                    Log.e(TAG, "Could not create data directory @ " + logDir);
                } else {
                    Log.i(TAG, "data directory successfully created @ " + logDir);
                }
            }
        }

        return logDir;
    }

    private void writeToFile(LogMessage message) {
        try {

            File logFile = new File(logDir, message.fileName);

            synchronized (LOCK) {

                if (!logFile.exists()) logFile.createNewFile();

                BufferedWriter out = new BufferedWriter(new FileWriter(logFile, true));
                out.append(message.message);
                out.append(CircogPrefs.CRLF);
                out.close();
            }

        } catch (Exception e) {
            if(CircogPrefs.DEBUG_MODE) {
                Log.e(TAG, "could not write message to file", e);
            }
        }

    }

    public void log(Map<String, String> map) {
        updateTimeStamps(map);
        log(map.toString());
    }

    public void log(String fileName, Map<String, String> map) {
        updateTimeStamps(map);
        log(fileName, map.toString());
    }

    public void log(String filename, JSONObject json) {
        updateTimeStamps(json);
        log(filename, json.toString());
    }

    public void updateTimeStamps(Map<String, String> map) {
        final long now = System.currentTimeMillis();
        final String nowStr = getTimeStringMySql(now);
        final long uptimeSec = getUpTimeSec();

        map.put(Keys.UID, deviceUid);
        map.put(Keys.KEY_T, String.valueOf(now));
        map.put(Keys.KEY_UPTIME_SINCE_START_S, String.valueOf(uptimeSec));
        map.put(Keys.KEY_DATE, nowStr);
    }

    public void updateTimeStamps(JSONObject json) {
        final long now = System.currentTimeMillis();
        final String nowStr = getTimeStringMySql(now);
        final long uptimeSec = getUpTimeSec();

        try {
            json.put(Keys.KEY_T, String.valueOf(now));
            json.put(Keys.KEY_UPTIME_SINCE_START_S, String.valueOf(uptimeSec));
            json.put(Keys.KEY_DATE, nowStr);
        } catch (JSONException e) {
            if(CircogPrefs.DEBUG_MODE) {
                Log.e(TAG, "ERROR updating timestamps in json object", e);
            }
        }
    }

    public void log(String msg) {
        log(deviceUid, msg);
    }

    public void log(String fileName, String msg) {
        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "write log to: " + fileName + " (" + msg + ")");
        }
        LogMessage message = new LogMessage(fileName, msg);
        this.queue.queue(message);
    }

    @Override
    public void run() {

        do {
            try {
                LogMessage message = this.queue.peek();

                if (message != null) {

                    writeToFile(message);
                    this.queue.dequeue();

                }

            } catch (Exception e) {
                if(CircogPrefs.DEBUG_MODE) {
                    Log.e(TAG, "unexpected exception in run()", e);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {}
            }
        } while (running);
    }

    public void onStart() {
        if (!running) {
            this.running = true;
            this.runner = new Thread(this);
            this.runner.start();
        }
    }

    public void onStop() {
        if (running) {
            this.running = false;
            this.runner.interrupt();
            this.runner = null;
        }
    }

    public boolean isRunning() {
        return running;
    }
}


