package org.hcilab.circog;

public interface CircogPrefs {

    // MAIN
    boolean DEBUG_MODE  = false;
    boolean PROVIDE_FEEDBACK = false;
    int MAX_DAILY_TASKS = 6;
    int MIN_STUDY_DAYS = 8;
    boolean RESTART_SERVICE_AFTER_REBOOT = true;
    String  PREFERENCES_NAME = "CircogPreferences";

    //STRINGS
    String PREF_UID				                = "uid";
    String CURRENT_VERSION_CODE                 = "CurrentVersionCode";
    String PREF_CONSENT_GIVEN                   = "Consent given";
    String DEMOGRAPHICS_PROVIDED                = "Demographics provided";
    String PREF_REGISTRATION_TIMESTAMP          = "registered_timestamp";
    String CURRENT_TASK                         = "current_task";
    String TASK_SEQUENCE                        = "tasks_sequence";
    String TASKLIST_DELIMITER                   = ", ";

    String DAILY_TASK_COUNT                     = "daily_task_count";
    String DATE_LAST_TASK_COMPLETED             = "day_last_task_completed";

    // Email and demographics
    String	PREF_EMAIL							= "email";
    String	PREF_AGE							= "age";
    String	PREF_GENDER_POS						= "gender_pos";
    String	PREF_GENDER 						= "gender";
    String	PREF_PROFESSION						= "profession";

    //Notifications
    String	NOTIF_CLICKED     					= "notif_clicked";
    String	NOTIF_POSTED    					= "notif_posted";
    String	NOTIF_POSTED_MILLIS					= "notif_posted_millis";
    String  LAST_NOTIFICATION_POSTED_MS         = "last_notif_posted_ms";

    //DAILY Survey
    String LAST_WAKEUP_HOUR                     = "last_wakeup_hour";
    String LAST_WAKEUP_MINUTE                   = "last_wakeup_minute";
    String LAST_WAKEUP_SET                      = "last_wakeup_set";
    String DATE_LAST_DAILY_SURVEY_MS            = "last_daily_survey_ms";
    String LAST_HOURS_SLEPT                     = "last_hours_slept";

    //TASK Survey
    String LEVEL_ALERTNESS                      = "alertness";
    String CAFFEINATED                          = "caffeinated";

    //FORMATTING
    String	CRLF						= "\r\n";
}