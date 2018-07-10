package com.example.asb.smartwallet;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.example.asb.smartwallet.AppPermissions;


/**
 * A simple activity that acquires permissions, decides which API to use,
 * and then starts the corresponding activity.
 */
public final class GatewayActivity extends Activity {

    private static final int REQUEST_CODE = 1;

    private AppPermissions appPermissions;
    private boolean isModeApi1;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appPermissions = new AppPermissions(this);
        if (appPermissions.checkPermissions()) {
            startMainActivity();
        } else {
            appPermissions.requestPermissions(REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (appPermissions.onRequestPermissionResult(grantResults)) {
                startMainActivity();
            }
        }
    }

    private void startMainActivity() {
        finish();
        startActivity(new Intent(this, MainActivity.class));
    }
}
