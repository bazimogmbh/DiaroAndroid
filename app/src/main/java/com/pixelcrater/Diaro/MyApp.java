package com.pixelcrater.Diaro;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;

import androidx.multidex.MultiDexApplication;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.pixelcrater.Diaro.appupgrades.AppUpgradeMgr;
import com.pixelcrater.Diaro.asynctasks.AsyncsMgr;
import com.pixelcrater.Diaro.autobackup.AutoBackupAlarmBrReceiver;
import com.pixelcrater.Diaro.autobackup.BackupRestore;
import com.pixelcrater.Diaro.brreceivers.TimeToWriteAlarmBrReceiver;
import com.pixelcrater.Diaro.config.AppConfig;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.notifications.NotificationsMgr;
import com.pixelcrater.Diaro.profile.UserMgr;
import com.pixelcrater.Diaro.securitycode.SecurityCodeMgr;
import com.pixelcrater.Diaro.settings.PreferencesHelper;
import com.pixelcrater.Diaro.storage.StorageMgr;
import com.pixelcrater.Diaro.storage.dropbox.SyncService;
import com.pixelcrater.Diaro.storage.dropbox.SyncWorker;
import com.pixelcrater.Diaro.utils.ApiUtils;
import com.pixelcrater.Diaro.utils.AppLaunchHelper;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyAppGuard;
import com.pixelcrater.Diaro.utils.MyDateTimeUtils;
import com.pixelcrater.Diaro.utils.NetworkStateMgr;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumConfig;
import com.yariksoffice.lingver.Lingver;
import com.pixelcrater.Diaro.utils.glide.MediaLoader;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MyApp extends MultiDexApplication {

    private static ThreadPoolExecutor executor;
    private static MyApp instance;

    public final Handler handler = new Handler();
    public SharedPreferences prefs;
    public StorageMgr storageMgr;
    public SecurityCodeMgr securityCodeMgr;
    public UserMgr userMgr;
    public AsyncsMgr asyncsMgr;
    public AppUpgradeMgr appUpgradeMgr;
    public NetworkStateMgr networkStateMgr;
    public NotificationsMgr notificationsMgr;
    public boolean openedCounterIncreased;
    private boolean isAppVisible;
    private onAppStateChangedListener mServiceListener;

    private Runnable goToPause_r = new Runnable() {
        @Override
        public void run() {
            isAppVisible = false;
            checkAppState();
            networkStateMgr.unregisterOnNetworkStateChangeReceiver();
        }
    };

    public MyApp() {
        instance = this;
    }

    public static MyApp getInstance() {
        return instance;
    }

    public static void executeInBackground(Runnable task) {
        executor.execute(task);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Get preferences, do not move this to later point
        prefs = getSharedPreferences(Prefs.NAME, Context.MODE_PRIVATE);

        final int processors = Runtime.getRuntime().availableProcessors();
        final int NON_CORE_THREADS_KEEP_ALIVE_TIME_SECONDS = 1;
        executor = new ThreadPoolExecutor(processors, processors, NON_CORE_THREADS_KEEP_ALIVE_TIME_SECONDS, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        asyncsMgr = new AsyncsMgr();
        storageMgr = new StorageMgr();
        appUpgradeMgr = new AppUpgradeMgr();
        userMgr = new UserMgr();
        networkStateMgr = new NetworkStateMgr();

        AsyncTask.execute(() -> {
            Lingver.init(this, PreferencesHelper.getCurrentLocaleAsCode(this));
            // Refresh locale
            Static.refreshLocale();
        });

        // Initialize Joda Time Android library
        AsyncTask.execute(MyDateTimeUtils::fixTimeZone);

        Album.initialize(AlbumConfig.newBuilder(this).setAlbumLoader(new MediaLoader()).build());

        // Delete and create cache directory
        try {
            AppLifetimeStorageUtils.deleteCacheDirectory();
            AppLifetimeStorageUtils.createCacheDirectory();
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }

        if (AppConfig.COPY_PREFS_AND_DB_FILES_ON_LAUNCH) {
            try {
                Static.copyPrefsAndDatabaseFiles();
            } catch (Exception e) {
                AppLog.e("Exception: " + e);
            }
        }

        if (AppConfig.CREATE_BACKUP_ON_LAUNCH) {
            try {
                BackupRestore.createBackup("auto", false, true);
            } catch (Exception e) {
                AppLog.e("Exception: " + e);
            }
        }

        securityCodeMgr = new SecurityCodeMgr();
        notificationsMgr = new NotificationsMgr();

        AsyncTask.execute(() -> {
            // Schedule next auto backup in AlarmManager
            AutoBackupAlarmBrReceiver.scheduleNextAutoBackupAlarm();

            // Schedule next 'Time to write' notification in AlarmManager
            TimeToWriteAlarmBrReceiver.scheduleNextTimeToWriteAlarm();

            ApiUtils.ping();

            // set default unit to c/f based on usa
            PreferencesHelper.setPrefUnitDefault();

            MyAppGuard.killIfInvalidSignature();
        });

        // Admob
        MobileAds.initialize(this, initializationStatus -> {});

        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(86400)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        AppLaunchHelper.deleteAttachmentsOfNotExistingEntriesOnce(this.getApplicationContext());

        // copy the demo entry images to media path
        AsyncTask.execute(AppLaunchHelper::copyAssets);
    }

    public void test() {

        if (BuildConfig.DEBUG) {
           // AppLaunchHelper.setDropboxID(this.getApplicationContext(), "", "");
            // AppLaunchHelper.uploadDbtoDropboxOnce(true);
        }
    }

    public boolean isAppVisible() {
        return isAppVisible;
    }

    public void resumeUsingApp() {
        handler.removeCallbacks(goToPause_r);
        isAppVisible = true;
        checkAppState();
        networkStateMgr.registerOnNetworkStateChangeReceiver();

        if (asyncsMgr.dataIntegrityAsync == null) {
            // Start indexing async
            asyncsMgr.executeDataIntegrityAsync();
        }
    }

    public void pauseUsingApp() {
        handler.postDelayed(goToPause_r, 2000);
    }

    public void checkAppState() {
      //  AppLog.d("isAppVisible: " + isAppVisible);
        if (mServiceListener != null) {
            mServiceListener.onAppStateChanged();
        }
    }

    public void setOnAppStateChangedListener(SyncService onAppStateChangedListener) {
        mServiceListener = onAppStateChangedListener;
    }

    public void setOnAppStateChangedListenerNew(SyncWorker onAppStateChangedListener) {
        mServiceListener = onAppStateChangedListener;
    }

    public interface onAppStateChangedListener {
        void onAppStateChanged();
    }
}