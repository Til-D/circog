package org.hcilab.circog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class EnterDemographicsActivity extends Activity {

    private static final String			TAG					= EnterDemographicsActivity.class.getSimpleName();

    private EditText					emailText;
    private EditText					ageText;
    private EditText					professionText;
    private Spinner						genderSelectionSpinner;
    private ArrayAdapter<CharSequence>	genderSelectionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_demographics);
        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "onCreate()");
        }

        emailText = (EditText) findViewById(R.id.em_user_email);
        ageText = (EditText) findViewById(R.id.em_age);
        professionText = (EditText) findViewById(R.id.em_user_demographics);

        genderSelectionSpinner = (Spinner) findViewById(R.id.em_gender);
        genderSelectionAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_array,
                android.R.layout.simple_spinner_item);
        genderSelectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSelectionSpinner.setAdapter(genderSelectionAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "onStart()");
        }
    }

    public void onNotNowPressed (final View view) {
        finish();
    }

    public void onDonePressed(final View view) {
        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "onDonePressed()");
        }

        int age = getAge();

        if(age == 0) {
            showErrorDialog(R.string.demographics_error_message_age);
            ageText.requestFocus();
            return;
        }

        String gender = genderSelectionSpinner.getSelectedItem().toString();
        int genderPos = genderSelectionSpinner.getSelectedItemPosition();
        if(genderSelectionSpinner.getSelectedItemPosition() == 0) {
            showErrorDialog(R.string.demographics_error_message_gender);
            genderSelectionSpinner.requestFocus();
            return;
        }

        String profession = professionText.getText().toString();
        if(profession.equals("")) {
            showErrorDialog(R.string.demographics_error_message_profession);
            professionText.requestFocus();
            return;
        }

        String email = emailText.getText().toString();

        Util.putInt(getApplicationContext(), CircogPrefs.PREF_AGE, age);
        Util.putString(getApplicationContext(), CircogPrefs.PREF_GENDER, gender);
        Util.putInt(getApplicationContext(), CircogPrefs.PREF_GENDER_POS, genderPos);
        Util.putString(getApplicationContext(), CircogPrefs.PREF_PROFESSION, profession);
        Util.putString(getApplicationContext(), CircogPrefs.PREF_EMAIL, email);

        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "** Demographics provided **");
            Log.i(TAG, "age: " + age);
            Log.i(TAG, "gender: " + gender);
            Log.i(TAG, "profession: " + profession);
            Log.i(TAG, "gender position: " + genderPos);
            Log.i(TAG, "email: " + email);

        }
        Util.putBool(getApplicationContext(), CircogPrefs.DEMOGRAPHICS_PROVIDED, true);
        finish();
    }

    private int getAge() {
        String age = ageText.getText().toString();
        try {
            return Integer.parseInt(age);
        } catch (Exception e) {
            if(CircogPrefs.DEBUG_MODE) {
                Log.e(TAG, e + "while parsing: '" + age + "'");
            }
            return 0;
        }
    }

    private void showErrorDialog(int messageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error);
        builder.setMessage(messageId);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.show();
    }

}
