package org.hcilab.circog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class ConsentActivity extends Activity {

    private static final String	TAG	= ConsentActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void startNextActivity() {
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void onGiveConsent(final View view) {
        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "onGiveConsent()");
        }
        Util.putBool(getApplicationContext(), CircogPrefs.PREF_CONSENT_GIVEN, true);
        Util.putLong(getApplicationContext(), CircogPrefs.PREF_REGISTRATION_TIMESTAMP, System.currentTimeMillis());
        startNextActivity();
    }

    public void onDenyConsent(final View view) {
        if(CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "onDenyConsent()");
        }
        finish();
    }
}
