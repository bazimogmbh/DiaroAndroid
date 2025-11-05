package com.pixelcrater.Diaro.storage.dropbox;

import android.content.Context;
import android.text.TextUtils;

import androidx.preference.PreferenceManager;

import com.dropbox.core.DbxException;
import com.dropbox.core.android.Auth;
import com.dropbox.core.android.AuthActivity;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.DbxPathV2;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.utils.AppLog;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Goddchen on 03.01.2017.
 */

public class DropboxLocalHelper {

    private static final String PREF_DB_CURSOR_PREFIX = "db_cursor_default";
    private static final String PREF_DELETION_QUEUE = "db_deletion_queue";
    private static final String PREF_PROFILE_PIC_CHANGE = "db_profile_pic";


    public static void setProfilePicChanged(boolean value) {
        MyApp.getInstance().prefs.edit().putBoolean(PREF_PROFILE_PIC_CHANGE, value).apply();
    }

    public static boolean hasProfilePicChanged() {
        return MyApp.getInstance().prefs.getBoolean(PREF_PROFILE_PIC_CHANGE, false);

    }

    static void setLatestCursor(String cursor) {
        MyApp.getInstance().prefs.edit().putString(PREF_DB_CURSOR_PREFIX, cursor).apply();
    }

    static String getLatestCursor() {
        return MyApp.getInstance().prefs.getString(PREF_DB_CURSOR_PREFIX, null);
    }


    static void clearLocalDropboxData(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(DropboxAccountManager.PREF_DROPBOX_TOKEN).apply();
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(DropboxAccountManager.PREF_DROPBOX_UID_V2).apply();
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(DropboxAccountManager.PREF_DROPBOX_UID_V1).apply();
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(DropboxAccountManager.PREF_DROPBOX_EMAIL).apply();
        DropboxLocalHelper.clearAllFolderCursors();
    }

    public static void clearAllFolderCursors() {
        DropboxLocalHelper.setLatestCursor(null);
    }

    public static boolean exists(DbxClientV2 client, String path) throws DbxException {
        try {
            client.files().getMetadata(path);
            return true;
        } catch (GetMetadataErrorException e) {
            if (e.errorValue.isPath() && e.errorValue.getPathValue().isNotFound()) {
                return false;
            } else {
                throw e;
            }
        }
    }

    public static boolean exists(List<Metadata> entries, String path) throws DbxException {

        if (entries == null) {
            return exists(DropboxAccountManager.getDropboxClient(MyApp.getInstance()), path);
        } else {
            for (int i = 0; i < entries.size(); i++) {
                if (TextUtils.equals(entries.get(i).getName().toLowerCase(), DbxPathV2.getName(path).toLowerCase())) {
                    return true;
                }
            }
            return false;
        }
    }

    static Metadata getMetadata(List<Metadata> entries, String path) throws DbxException {
        if (entries == null) {
            return DropboxAccountManager.getDropboxClient(MyApp.getInstance()).files().getMetadata(path);
        } else {
            for (int i = 0; i < entries.size(); i++) {
                if (TextUtils.equals(entries.get(i).getName(), DbxPathV2.getName(path))) {
                    return entries.get(i);
                }
            }
            return DropboxAccountManager.getDropboxClient(MyApp.getInstance()).files().getMetadata(path);

        }
    }

    static List<Metadata> listFolder(String path) throws DbxException {
        DbxClientV2 dbxClient = DropboxAccountManager.getDropboxClient(MyApp.getInstance());
        List<Metadata> entries = new ArrayList<>();
        ListFolderResult listFolderResult = null;
        while (listFolderResult == null || listFolderResult.getHasMore()) {
            if (listFolderResult == null) {
                listFolderResult = dbxClient.files().listFolder(path);
            } else {
                listFolderResult = dbxClient.files().listFolderContinue(listFolderResult.getCursor());
            }
            entries.addAll(listFolderResult.getEntries());
        }
        return entries;
    }

    public static void markFileForDeletion(String dbxPath) {
        AppLog.d(String.format("Adding %s to deletion queue", dbxPath));
        try {
            JSONArray queue = new JSONArray(MyApp.getInstance().prefs.getString(PREF_DELETION_QUEUE, "[]"));
            queue.put(dbxPath);
            String json = queue.toString();
            AppLog.d(String.format("New deletion queue: %s", json));
            MyApp.getInstance().prefs.edit().putString(PREF_DELETION_QUEUE, json).apply();
        } catch (JSONException e) {
            AppLog.e(String.format("Error updating file deletion queue: %s", e.getMessage()));
        }
    }

    @SuppressWarnings("unused")
    public static void removeFileFromDeletion(String dbxPath) {
        try {
            JSONArray queue = new JSONArray(MyApp.getInstance().prefs.getString(PREF_DELETION_QUEUE, "[]"));
            for (int i = 0; i < queue.length(); i++) {
                if (StringUtils.equals(queue.getString(i), dbxPath)) {
                    queue.remove(i);
                    break;
                }
            }
            MyApp.getInstance().prefs.edit().putString(PREF_DELETION_QUEUE, queue.toString()).apply();
        } catch (JSONException e) {
            AppLog.e(String.format("Error updating file deletion queue: %s", e.getMessage()));
        }
    }

    static void clearDeletionQueue() {
        AppLog.d("Clearing deletion queue");
        MyApp.getInstance().prefs.edit().putString(PREF_DELETION_QUEUE, "[]").apply();
    }

    static List<String> getDeletionQueue() {
        try {
            String json = MyApp.getInstance().prefs.getString(PREF_DELETION_QUEUE, "[]");
            // AppLog.d("Deletion queue: " + json);
            JSONArray queue = new JSONArray(json);
            List<String> list = new ArrayList<>();
            for (int i = 0; i < queue.length(); i++) {
                list.add(queue.getString(i));
            }
            return list;
        } catch (JSONException e) {
            AppLog.e(String.format("Error getting deletion queue: %s", e.getMessage()));
            return null;
        }
    }

    public static void checkDropboxToken(final Context context) {

        String token = Auth.getOAuth2Token();
        //  Log.e("checkDropboxToken is ", token + "");

        if (token != null) {
            String lastRevokedToken = PreferenceManager.getDefaultSharedPreferences(context).getString(DropboxAccountManager.PREF_DROPBOX_LAST_REVOKED_TOKEN, "");
            if (lastRevokedToken.compareToIgnoreCase(token) == 0) {
                AppLog.e("Revoked token found " + lastRevokedToken);
                PreferenceManager.getDefaultSharedPreferences(context).edit().remove(DropboxAccountManager.PREF_DROPBOX_TOKEN).apply();
                return;
            } else {
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString(DropboxAccountManager.PREF_DROPBOX_TOKEN, token).apply();
            }

            new DropboxAccountManager.GetCurrentUserAsyncTask(context) {
                @Override
                protected void onPostExecute(DbxUserInfo dbxUserInfo) {
                    super.onPostExecute(dbxUserInfo);
                    if (dbxUserInfo != null) {
                        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(DropboxAccountManager.PREF_DROPBOX_UID_V2, dbxUserInfo.getFullaccount().getAccountId()).apply();

                    } else {
                        checkDropboxToken(context);
                    }
                }
            }.execute();
        }
        // callback from dbox oauth
      //  if (AuthActivity.result != null && AuthActivity.result.hasExtra(AuthActivity.EXTRA_UID)) {
     //      String uidV1 = AuthActivity.result.getStringExtra(AuthActivity.EXTRA_UID);
      //      PreferenceManager.getDefaultSharedPreferences(context).edit().putString(DropboxAccountManager.PREF_DROPBOX_UID_V1, uidV1).apply();
      //  }
    }

}
