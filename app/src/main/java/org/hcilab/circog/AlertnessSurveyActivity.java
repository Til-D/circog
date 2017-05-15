package org.hcilab.circog;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;

public class AlertnessSurveyActivity extends Activity {


    private Button buttonSubmit;
    private RadioGroup radioAlertness;
    private CheckBox caffeination;
    private CardView errorMessage;
    private CardView studyCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alertness_survey);

        //check whether choice is made and log it
        buttonSubmit = (Button) findViewById(R.id.survey_send);
        radioAlertness = (RadioGroup) findViewById(R.id.survey_rg_1);
        caffeination = (CheckBox) findViewById(R.id.survey_checkbox_caffeination);
        errorMessage = (CardView) findViewById(R.id.error_message);
        studyCompleted = (CardView) findViewById(R.id.cardview_study_completed);

        //reset to -1 in case dialog is dismissed
        Util.putInt(getApplicationContext(), CircogPrefs.LEVEL_ALERTNESS, -1);

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int alertness = Util.getRating(radioAlertness);

                Util.putInt(getApplicationContext(),CircogPrefs.LEVEL_ALERTNESS,alertness);

                Util.putBool(getApplicationContext(), CircogPrefs.CAFFEINATED, caffeination.isChecked());

                if(alertness==-1) {
                    errorMessage.setVisibility(View.VISIBLE);
                } else {
                    finish();
                }
            }
        });

        //check if studycompleted
        boolean completed = Util.studyCompleted(getApplicationContext());
        if(completed) {
            studyCompleted.setVisibility(View.VISIBLE);
        }
    }
}
