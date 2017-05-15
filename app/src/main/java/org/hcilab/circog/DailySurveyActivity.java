package org.hcilab.circog;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DailySurveyActivity extends AppCompatActivity {

    private static final String	TAG	= DailySurveyActivity.class.getSimpleName();

    private static final int DEFAULT_HOUR = 8;
    private static final int DEFAULT_MINUTE = 0;

    private static final int HOURS_SLEPT_MIN = 1;
    private static final int HOURS_SLEPT_MAX = 12;

    private DialogFragment wakeupTimePicker;
    private int hoursSlept;
    private CardView errorView;
    private TextView errorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_survey);

        final Spinner spinnerHoursSlept       = (Spinner)     findViewById(R.id.spinner_hours_slept);
        final RadioGroup radioSleepQuality    = (RadioGroup) findViewById(R.id.survey_rg_1);
        Button buttonSubmit             = (Button)     findViewById(R.id.survey_send);
        errorView = (CardView) findViewById(R.id.error_card_view);
        errorMessage = (TextView) findViewById(R.id.error_message);

        wakeupTimePicker = new TimePickerFragment();
        Util.putBool(getApplicationContext(), CircogPrefs.LAST_WAKEUP_SET, false);

        //populate hours slept spinner
        List<String> list = new ArrayList<String>();
        for (int i=HOURS_SLEPT_MIN; i<=HOURS_SLEPT_MAX; i++) {
            list.add(Integer.toString(i));
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerHoursSlept.setAdapter(dataAdapter);

        //init hours slept with last selection made
        hoursSlept = Util.getInt(getApplicationContext(), CircogPrefs.LAST_HOURS_SLEPT, -1);
        for(int i=0; i<spinnerHoursSlept.getCount(); i++) {
            int itemValue = Integer.parseInt(spinnerHoursSlept.getItemAtPosition(i).toString());
            if (itemValue==hoursSlept) {
                spinnerHoursSlept.setSelection(i);
            }
        }

        spinnerHoursSlept.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                hoursSlept = Integer.parseInt(spinnerHoursSlept.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                hoursSlept = -1;
            }
        });

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int sleepQuality = Util.getRating(radioSleepQuality);

                //force users to put in a time or take a default
                boolean wakeupTimeSet = Util.getBool(getApplicationContext(), CircogPrefs.LAST_WAKEUP_SET, false);
                if (!wakeupTimeSet) {
                    errorMessage.setText(R.string.daily_survey_error_wakeup_time);
                    errorView.setVisibility(View.VISIBLE);
                    return;
                }

                //force users to indicate their hours slept
                if (hoursSlept==-1) {
                    errorMessage.setText(R.string.daily_survey_error_hours_slept);
                    errorView.setVisibility(View.VISIBLE);
//                    Util.showErrorDialog(getApplicationContext(), R.string.daily_survey_error_hours_slept);
                    return;
                }

                //force users to rate their sleep
                if (sleepQuality == -1) {
                    errorMessage.setText(R.string.daily_survey_error_sleep_quality);
                    errorView.setVisibility(View.VISIBLE);
//                    Util.showErrorDialog(getApplicationContext(), R.string.daily_survey_error_sleep_quality);
                    return;
                }

                int wakeupHour = Util.getInt(getApplicationContext(), CircogPrefs.LAST_WAKEUP_HOUR, -1);
                int wakeupMinute = Util.getInt(getApplicationContext(), CircogPrefs.LAST_WAKEUP_MINUTE, -1);

                if (CircogPrefs.DEBUG_MODE) {
                    Log.i(TAG, "sleep quality rating: " + sleepQuality);
                    Log.i(TAG, "woke up at: " + wakeupHour + ":" + wakeupMinute);
                    Log.i(TAG, "slept for hours: " + hoursSlept);
                }

                //update last survey date
                Util.putLong(getApplicationContext(), CircogPrefs.DATE_LAST_DAILY_SURVEY_MS, System.currentTimeMillis());

                //update last hours slept selection
                Util.putInt(getApplicationContext(), CircogPrefs.LAST_HOURS_SLEPT, hoursSlept);
//
                //log daily survey: wakeupHour, wakeupMinute
                LogManager.logDailySurveyFilledIn(wakeupHour, wakeupMinute, hoursSlept, sleepQuality);

                finish();

                Toast.makeText(DailySurveyActivity.this, R.string.survey_thanks, Toast.LENGTH_SHORT).show();

            }
        });

    }

    public void showTimePickerDialog(View v) {
        wakeupTimePicker.show(getSupportFragmentManager(), "wakeupTimePicker");
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            //use last entered value
            int initHour = Util.getInt(getActivity(), CircogPrefs.LAST_WAKEUP_HOUR, DEFAULT_HOUR);
            int initMinute = Util.getInt(getActivity(), CircogPrefs.LAST_WAKEUP_MINUTE, DEFAULT_MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, initHour, initMinute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            //save picked time
            Util.putInt(getActivity(), CircogPrefs.LAST_WAKEUP_HOUR, hourOfDay);
            Util.putInt(getActivity(), CircogPrefs.LAST_WAKEUP_MINUTE, minute);
            Util.putBool(getActivity(), CircogPrefs.LAST_WAKEUP_SET, true);
        }
    }

}

