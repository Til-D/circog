package org.hcilab.log;

public interface Keys {

    String	KEY_PID						= "PID";
    String	UID							= "uid";
    String	KEY_DATE					= "date";

    // App Interactions
    String  APP_LAUNCH                  = "AppLaunch";
    String SESSION_FINISHED             = "SessionFinished";
    String	MODE					    = "Mode";

    // Survey
    String  SURVEY_RESPONSE             = "SurveyResponse";

    // Tasks/Conditions
    String	CONDITION_SWITCH			= "ConditionSwitch";

    // Notifications
    String	NOTIF_IGNORED				= "NotifIgnored";
    String	NOTIF_DISMISSED             = "NotifDismissed";
    String  NOTIF_INTERACTION           = "NotificationInteraction";

    // Sensor Status
    String	SENSOR_ID					= "SensId";
    String	SENSOR_VALUE				= "SensVal";

    // Misc
    String	EMPTY						= "";
    String	KEY_VC						= "vc";
    String	KEY_T						= "t";
    String	KEY_UPTIME_SINCE_START_S	= "up";
}

