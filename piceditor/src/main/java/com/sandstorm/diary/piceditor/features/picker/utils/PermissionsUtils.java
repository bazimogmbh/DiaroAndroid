package com.sandstorm.diary.piceditor.features.picker.utils;

import android.Manifest;
import android.app.Activity;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;

public class PermissionsUtils {

    public static boolean checkReadStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= 34) {
            // Android 14+: Check for partial access or full access
            boolean hasImages = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES) == PermissionChecker.PERMISSION_GRANTED;
            boolean hasPartialAccess = ContextCompat.checkSelfPermission(activity, PermissionsConstant.READ_MEDIA_VISUAL_USER_SELECTED) == PermissionChecker.PERMISSION_GRANTED;

            if (hasImages || hasPartialAccess) {
                return true;
            }
            // Request permissions
            ActivityCompat.requestPermissions(activity, PermissionsConstant.getReadStoragePermissions(), 2);
            return false;
        } else if (Build.VERSION.SDK_INT >= 33) {
            // Android 13: Check for media permissions
            boolean hasImages = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES) == PermissionChecker.PERMISSION_GRANTED;
            if (hasImages) {
                return true;
            }
            ActivityCompat.requestPermissions(activity, PermissionsConstant.getReadStoragePermissions(), 2);
            return false;
        } else {
            // Android 12 and below
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
                return true;
            }
            ActivityCompat.requestPermissions(activity, PermissionsConstant.getReadStoragePermissions(), 2);
            return false;
        }
    }

    public static boolean checkWriteStoragePermission(Fragment fragment) {
        if (fragment.getContext() == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= 34) {
            // Android 14+: Check for partial access or full access
            boolean hasImages = ContextCompat.checkSelfPermission(fragment.getContext(), Manifest.permission.READ_MEDIA_IMAGES) == PermissionChecker.PERMISSION_GRANTED;
            boolean hasPartialAccess = ContextCompat.checkSelfPermission(fragment.getContext(), PermissionsConstant.READ_MEDIA_VISUAL_USER_SELECTED) == PermissionChecker.PERMISSION_GRANTED;

            if (hasImages || hasPartialAccess) {
                return true;
            }
            fragment.requestPermissions(PermissionsConstant.getWriteStoragePermissions(), 3);
            return false;
        } else if (Build.VERSION.SDK_INT >= 33) {
            // Android 13: Check for media permissions
            boolean hasImages = ContextCompat.checkSelfPermission(fragment.getContext(), Manifest.permission.READ_MEDIA_IMAGES) == PermissionChecker.PERMISSION_GRANTED;
            if (hasImages) {
                return true;
            }
            fragment.requestPermissions(PermissionsConstant.getWriteStoragePermissions(), 3);
            return false;
        } else if (Build.VERSION.SDK_INT >= 29) {
            // Android 10-12: Only need read for scoped storage
            boolean hasRead = ContextCompat.checkSelfPermission(fragment.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED;
            if (hasRead) {
                return true;
            }
            fragment.requestPermissions(PermissionsConstant.getWriteStoragePermissions(), 3);
            return false;
        } else {
            // Android 9 and below
            boolean hasWrite = ContextCompat.checkSelfPermission(fragment.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED;
            if (hasWrite) {
                return true;
            }
            fragment.requestPermissions(PermissionsConstant.getWriteStoragePermissions(), 3);
            return false;
        }
    }

    public static boolean checkCameraPermission(Fragment fragment) {
        if (fragment.getContext() == null) {
            return false;
        }
        boolean z = ContextCompat.checkSelfPermission(fragment.getContext(), Manifest.permission.CAMERA) == PermissionChecker.PERMISSION_GRANTED;
        if (!z) {
            fragment.requestPermissions(PermissionsConstant.PERMISSIONS_CAMERA, 1);
        }
        return z;
    }

    public static boolean checkCameraPermission(Activity activity) {
        boolean z = ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.CAMERA) == PermissionChecker.PERMISSION_GRANTED;
        if (!z) {
            ActivityCompat.requestPermissions(activity, PermissionsConstant.PERMISSIONS_CAMERA, 1);
        }
        return z;
    }

    public static boolean checkWriteStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= 34) {
            // Android 14+: Check for partial access or full access
            boolean hasImages = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES) == PermissionChecker.PERMISSION_GRANTED;
            boolean hasPartialAccess = ContextCompat.checkSelfPermission(activity, PermissionsConstant.READ_MEDIA_VISUAL_USER_SELECTED) == PermissionChecker.PERMISSION_GRANTED;

            if (hasImages || hasPartialAccess) {
                return true;
            }
            ActivityCompat.requestPermissions(activity, PermissionsConstant.getWriteStoragePermissions(), 3);
            return false;
        } else if (Build.VERSION.SDK_INT >= 33) {
            // Android 13: Check for media permissions
            boolean hasImages = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES) == PermissionChecker.PERMISSION_GRANTED;
            if (hasImages) {
                return true;
            }
            ActivityCompat.requestPermissions(activity, PermissionsConstant.getWriteStoragePermissions(), 3);
            return false;
        } else if (Build.VERSION.SDK_INT >= 29) {
            // Android 10-12: Only need read for scoped storage
            boolean hasRead = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED;
            if (hasRead) {
                return true;
            }
            ActivityCompat.requestPermissions(activity, PermissionsConstant.getWriteStoragePermissions(), 3);
            return false;
        } else {
            // Android 9 and below
            boolean hasWrite = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED;
            if (hasWrite) {
                return true;
            }
            ActivityCompat.requestPermissions(activity, PermissionsConstant.getWriteStoragePermissions(), 3);
            return false;
        }
    }

    /**
     * Check if the permission result indicates success for storage permissions.
     * On Android 14+, partial access is considered successful.
     */
    public static boolean isStoragePermissionGranted(String[] permissions, int[] grantResults) {
        if (permissions.length != grantResults.length) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= 34) {
            // On Android 14+, check if partial access is granted
            for (int i = 0; i < permissions.length; i++) {
                if (PermissionsConstant.READ_MEDIA_VISUAL_USER_SELECTED.equals(permissions[i])) {
                    if (grantResults[i] == PermissionChecker.PERMISSION_GRANTED) {
                        return true;
                    }
                } else if (Manifest.permission.READ_MEDIA_IMAGES.equals(permissions[i])) {
                    if (grantResults[i] == PermissionChecker.PERMISSION_GRANTED) {
                        return true;
                    }
                }
            }
            return false;
        }

        // For older versions, all permissions must be granted
        for (int result : grantResults) {
            if (result != PermissionChecker.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
