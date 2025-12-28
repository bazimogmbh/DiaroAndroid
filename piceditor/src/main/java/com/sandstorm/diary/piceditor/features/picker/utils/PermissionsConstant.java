package com.sandstorm.diary.piceditor.features.picker.utils;

import android.Manifest;
import android.os.Build;

public class PermissionsConstant {
    // For Android 14+ partial media access
    public static final String READ_MEDIA_VISUAL_USER_SELECTED = "android.permission.READ_MEDIA_VISUAL_USER_SELECTED";

    public static final String[] PERMISSIONS_CAMERA = {"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};

    public static String[] getReadStoragePermissions() {
        if (Build.VERSION.SDK_INT >= 34) {
            // Android 14+: Include partial access permission
            return new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    READ_MEDIA_VISUAL_USER_SELECTED
            };
        } else if (Build.VERSION.SDK_INT >= 33) {
            // Android 13: Use granular media permissions
            return new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO};
        } else {
            // Android 12 and below
            return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }
    }

    public static String[] getWriteStoragePermissions() {
        if (Build.VERSION.SDK_INT >= 34) {
            // Android 14+: Include partial access permission
            return new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    READ_MEDIA_VISUAL_USER_SELECTED
            };
        } else if (Build.VERSION.SDK_INT >= 33) {
            // Android 13: Use granular media permissions
            return new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO};
        } else if (Build.VERSION.SDK_INT >= 29) {
            // Android 10-12: Scoped storage, read is sufficient
            return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        } else {
            // Android 9 and below
            return new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }
    }

    // Legacy constants for backward compatibility
    @Deprecated
    public static final String[] PERMISSIONS_EXTERNAL_READ = {Manifest.permission.READ_EXTERNAL_STORAGE};
    @Deprecated
    public static final String[] PERMISSIONS_EXTERNAL_WRITE = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
}
