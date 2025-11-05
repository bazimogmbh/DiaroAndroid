package com.pixelcrater.Diaro.utils;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.generaldialogs.ConfirmDialog;

public class PermissionsUtils {

    public static void askForPermission(AppCompatActivity activity, String permission, int requestCode, String dialogTag, int rationaleTextResId) {
        // Should we show an explanation? If the device is not running the M Preview, always returns false.
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) && dialogTag != null) {
            // Explain to the user why we need to read the contacts
            AppLog.d("Explain to the user why we need to read the contacts");

            showConfirmRationaleDialog(activity, permission, requestCode, dialogTag, rationaleTextResId);

        } else {
            // If the device is not running the M Preview, invokes the callback method in ActivityCompat.OnRequestPermissionsResultCallback.
            // Passes PERMISSION_GRANTED if the app already has the specified permission, or PERMISSION_DENIED if it does no
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
        }
    }

    private static void showConfirmRationaleDialog(AppCompatActivity activity, String permission, int requestCode, String dialogTag, int textResId) {
        if (activity.getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setMessage(activity.getString(textResId));
            dialog.show(activity.getSupportFragmentManager(), dialogTag);

            // Set dialog listener
            setConfirmRationaleDialogListener(activity, permission, requestCode, dialog);
        }
    }

    public static void setConfirmRationaleDialogListener(final AppCompatActivity activity, final String permission, final int requestCode, final ConfirmDialog dialog) {
        dialog.setDialogPositiveClickListener(() -> ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode));
    }

    public static void showDeniedOpenSettingsDialog(AppCompatActivity activity, String dialogTag, int textResId){

        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setMessage(activity.getString(textResId));
        dialog.show(activity.getSupportFragmentManager(), dialogTag);
        dialog.setPositiveButtonText(activity.getString(R.string.settings));
        dialog.setDialogPositiveClickListener(() -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
            intent.setData(uri);
            activity.startActivity(intent);
        });
    }
}
