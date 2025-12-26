package com.pixelcrater.Diaro.config;

import com.pixelcrater.Diaro.BuildConfig;

public class AppConfig {
    /**
     * Always change these vars to FALSE before release! **
     */

    // Allows to turn on PRO version
    public static final boolean DEV_ENABLE_LOGGING = true;

    // Allows to connect to Labs versions in dev builds
    public static final boolean DEV_LABS_MODE = false;

    // Allows to turn on PRO version
    public static final boolean DEVELOPER_MODE = false;

    // Allows to open the app without entering security code
    public static final boolean SKIP_SECURITY_CODE = false;

    // Use plain json files without encryption to sync with Dropbox, if false - data is encrypted
    public static final boolean USE_PLAIN_JSON = DEV_LABS_MODE;

    // SQLCipher: Use plaintext SQLite database, if false - database file is encrypted
    public static final boolean USE_PLAIN_SQLITE = false;

    // Copies prefs and database file on app launch
    public static final boolean COPY_PREFS_AND_DB_FILES_ON_LAUNCH = false;

    // Creates a backup file on app launch
    public static final boolean CREATE_BACKUP_ON_LAUNCH = false;

    // Google Play build
    public static final boolean GOOGLE_PLAY_BUILD = true;

    public static boolean isDeveloperMode() {
        return DEVELOPER_MODE && BuildConfig.DEBUG;
    }
}
