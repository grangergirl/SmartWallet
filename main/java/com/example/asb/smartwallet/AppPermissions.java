package com.example.asb.smartwallet;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest.permission;
import android.app.Activity;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

/**
 * Utility for interacting with the permission system.
 */
public final class AppPermissions {

    private final Activity activity;
    public AppPermissions(Activity activity) {
        this.activity = activity;
    }

    /** Returns true if the app has all needed permissions. */
    public boolean checkPermissions() {
        int writeExternalStoragePermission =
                ContextCompat.checkSelfPermission(activity, permission.WRITE_EXTERNAL_STORAGE);
        int btPermission =
                ContextCompat.checkSelfPermission(activity, permission.BLUETOOTH);
        int btAdminPermission =
                ContextCompat.checkSelfPermission(activity, permission.BLUETOOTH_ADMIN);
        int readPhoneStatePermission =
                ContextCompat.checkSelfPermission(activity, permission.READ_PHONE_STATE);
        return writeExternalStoragePermission == PERMISSION_GRANTED && btPermission == PERMISSION_GRANTED && btAdminPermission == PERMISSION_GRANTED && readPhoneStatePermission == PERMISSION_GRANTED;
    }

    /** Request permissions from the user. */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestPermissions(int requestCode) {
        activity.requestPermissions(new String[]{ permission.WRITE_EXTERNAL_STORAGE, permission.BLUETOOTH, permission.BLUETOOTH_ADMIN, permission.READ_PHONE_STATE},requestCode);
    }

    /**
     * Call this from the activity's onRequestPermissionResult for the previously
     * given requestCode. Returns true if the permissions have been granted.
     * Terminates the activity and shows usage if the permissions were not granted.
     */
    public boolean onRequestPermissionResult(int[] grantResults) {
        boolean hasPermissions = grantResults.length > 3
                && grantResults[0] == PERMISSION_GRANTED
                && grantResults[1] == PERMISSION_GRANTED
                && grantResults[2] == PERMISSION_GRANTED
                && grantResults[3] == PERMISSION_GRANTED;
        if (!hasPermissions) {
            Toast.makeText(activity,"Storage and BT permissions are required to use the app",Toast.LENGTH_LONG).show();
            activity.finish();
        }
        return hasPermissions;
    }

}
