package com.pixelcrater.Diaro.storage.dropbox;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import androidx.preference.PreferenceManager;

import com.dropbox.core.v2.DbxPathV2;
import com.dropbox.core.v2.files.Metadata;
import com.pixelcrater.Diaro.config.AppConfig;
import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.model.MoodInfo;
import com.pixelcrater.Diaro.model.TemplateInfo;
import com.pixelcrater.Diaro.templates.Template;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.model.AttachmentInfo;
import com.pixelcrater.Diaro.model.EntryInfo;
import com.pixelcrater.Diaro.model.FolderInfo;
import com.pixelcrater.Diaro.model.LocationInfo;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.model.TagInfo;
import com.pixelcrater.Diaro.utils.AppLog;

import org.json.JSONObject;

import java.util.Objects;

import static com.pixelcrater.Diaro.config.GlobalConstants.DROPBOX_PATH_BACKUP;
import static com.pixelcrater.Diaro.config.GlobalConstants.DROPBOX_PATH_DATA_ATTACHMENTS;
import static com.pixelcrater.Diaro.config.GlobalConstants.DROPBOX_PATH_DATA_ENTRIES;
import static com.pixelcrater.Diaro.config.GlobalConstants.DROPBOX_PATH_DATA_FOLDERS;
import static com.pixelcrater.Diaro.config.GlobalConstants.DROPBOX_PATH_DATA_LOCATIONS;
import static com.pixelcrater.Diaro.config.GlobalConstants.DROPBOX_PATH_DATA_MOODS;
import static com.pixelcrater.Diaro.config.GlobalConstants.DROPBOX_PATH_DATA_TAGS;
import static com.pixelcrater.Diaro.config.GlobalConstants.DROPBOX_PATH_DATA_TEMPLATES;
import static com.pixelcrater.Diaro.config.GlobalConstants.DROPBOX_PATH_MEDIA;
import static com.pixelcrater.Diaro.config.GlobalConstants.DROPBOX_PATH_PROFILE;

public class DropboxStatic {

    public static String getEncryptionKey(Context context) {
        if (DropboxAccountManager.isLoggedIn(context)) {
            String uid = PreferenceManager.getDefaultSharedPreferences(context).getString(DropboxAccountManager.PREF_DROPBOX_UID_V1, null);
            if (uid != null) {
                return Static.md5(uid);
            } else {
                return null;
            }
        }
        return null;
    }

    public static String getJsonFilePrefix(String fullTableName) {
        switch (fullTableName) {
            case Tables.TABLE_ENTRIES:
                return "entry_";

            case Tables.TABLE_ATTACHMENTS:
                return "attachment_";

            case Tables.TABLE_FOLDERS:
                return "folder_";

            case Tables.TABLE_TAGS:
                return "tag_";

            case Tables.TABLE_MOODS:
                return "mood_";

            case Tables.TABLE_LOCATIONS:
                return "location_";

            case Tables.TABLE_TEMPLATES:
                return "template_";
        }

        return null;
    }

    public static String getFullTableNameFromJsonFilename(String jsonFilename) {
        if (jsonFilename.startsWith("entry_")) {
            return Tables.TABLE_ENTRIES;
        } else if (jsonFilename.startsWith("attachment_")) {
            return Tables.TABLE_ATTACHMENTS;
        } else if (jsonFilename.startsWith("folder_")) {
            return Tables.TABLE_FOLDERS;
        } else if (jsonFilename.startsWith("tag_")) {
            return Tables.TABLE_TAGS;
        } else if (jsonFilename.startsWith("mood_")) {
            return Tables.TABLE_MOODS;
        }else if (jsonFilename.startsWith("location_")) {
            return Tables.TABLE_LOCATIONS;
        } else if (jsonFilename.startsWith("template_")) {
            return Tables.TABLE_TEMPLATES;
        }

        return null;
    }

    public static boolean isJsonFilenameCorrect(String fullTableName, String jsonFilename) {
        int prefixLength = Objects.requireNonNull(getJsonFilePrefix(fullTableName)).length();
        int extensionLength = AppConfig.USE_PLAIN_JSON ? 5 : 0;

        return !(AppConfig.USE_PLAIN_JSON && !Static.getFileExtension(jsonFilename).equals("json")) &&
                jsonFilename.startsWith(DropboxStatic.getJsonFilePrefix(fullTableName)) &&
                jsonFilename.length() == prefixLength + 32 + extensionLength &&
                DropboxStatic.getRowUidFromJsonFilename(fullTableName, jsonFilename).length() == 32;
    }

    public static String getRowUidFromJsonFilename(String fullTableName, String jsonFilename) {
        try {
            int prefixLength = Objects.requireNonNull(getJsonFilePrefix(fullTableName)).length();
            return jsonFilename.substring(prefixLength, prefixLength + 32);
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }

        return "";
    }

    public static String getDbxJsonFilePath(String fullTableName, String rowUid) {
        return DropboxStatic.getDbxDataPath(fullTableName) + "/" + DropboxStatic.getJsonFilePrefix(fullTableName) + rowUid + (AppConfig.USE_PLAIN_JSON ? ".json" : "");
    }

    public static String getDbxDataPath(String fullTableName) {
        switch (fullTableName) {
            case Tables.TABLE_ENTRIES:
                return DROPBOX_PATH_DATA_ENTRIES;

            case Tables.TABLE_FOLDERS:
                return DROPBOX_PATH_DATA_FOLDERS;

            case Tables.TABLE_TAGS:
                return DROPBOX_PATH_DATA_TAGS;

            case Tables.TABLE_MOODS:
                return DROPBOX_PATH_DATA_MOODS;

            case Tables.TABLE_LOCATIONS:
                return DROPBOX_PATH_DATA_LOCATIONS;

            case Tables.TABLE_ATTACHMENTS:
                return DROPBOX_PATH_DATA_ATTACHMENTS;

            case Tables.TABLE_TEMPLATES:
                return DROPBOX_PATH_DATA_TEMPLATES;
        }

        return null;
    }

    public static String createJsonString(String fullTableName, Cursor cursor) throws Exception {
        switch (fullTableName) {
            case Tables.TABLE_ENTRIES:
                return EntryInfo.createJsonString(cursor);

            case Tables.TABLE_FOLDERS:
                return FolderInfo.createJsonString(cursor);

            case Tables.TABLE_TAGS:
                return TagInfo.createJsonString(cursor);

            case Tables.TABLE_MOODS:
                return MoodInfo.createJsonString(cursor);

            case Tables.TABLE_LOCATIONS:
                return LocationInfo.createJsonString(cursor);

            case Tables.TABLE_ATTACHMENTS:
                return AttachmentInfo.createJsonString(cursor);

            case Tables.TABLE_TEMPLATES:
                return TemplateInfo.createJsonString(cursor);
        }

        return null;
    }

    public static ContentValues createRowCvFromJsonString(String fullTableName, String jsonString)
            throws Exception {

        switch (fullTableName) {
            case Tables.TABLE_ENTRIES:
                return EntryInfo.createRowCvFromJsonString(jsonString);

            case Tables.TABLE_FOLDERS:
                return FolderInfo.createRowCvFromJsonString(jsonString);

            case Tables.TABLE_TAGS:
                return TagInfo.createRowCvFromJsonString(jsonString);

            case Tables.TABLE_MOODS:
                return MoodInfo.createRowCvFromJsonString(jsonString);

            case Tables.TABLE_LOCATIONS:
                return LocationInfo.createRowCvFromJsonString(jsonString);

            case Tables.TABLE_ATTACHMENTS:
                return AttachmentInfo.createRowCvFromJsonString(jsonString);

            case Tables.TABLE_TEMPLATES:
                return TemplateInfo.createRowCvFromJsonString(jsonString);
        }

        return null;
    }


    public enum DIARO_FILETYPE {
        INVALID_PATH,

        BACKUP,

        DATA_ATTACHMENTS,
        DATA_ENTRIES,
        DATA_FOLDERS,
        DATA_TAGS,
        DATA_MOODS,
        DATA_LOCATIONS,

        DATA_TEMPLATES,

        MEDIA_PHOTO,
        PROFILE,
    }

    public static DIARO_FILETYPE getFileType(Metadata metadata) {

        String filePath = metadata.getPathLower();
        String parent = DbxPathV2.getParent(filePath);

        if (parent.compareToIgnoreCase(DROPBOX_PATH_BACKUP) == 0) {
            return DIARO_FILETYPE.BACKUP;
        }

        if (parent.compareToIgnoreCase(DROPBOX_PATH_DATA_ATTACHMENTS) == 0) {
            return DIARO_FILETYPE.DATA_ATTACHMENTS;
        }
        if (parent.compareToIgnoreCase(DROPBOX_PATH_DATA_ENTRIES) == 0) {
            return DIARO_FILETYPE.DATA_ENTRIES;
        }
        if (parent.compareToIgnoreCase(DROPBOX_PATH_DATA_FOLDERS) == 0) {
            return DIARO_FILETYPE.DATA_FOLDERS;
        }
        if (parent.compareToIgnoreCase(DROPBOX_PATH_DATA_TAGS) == 0) {
            return DIARO_FILETYPE.DATA_TAGS;
        }
        if (parent.compareToIgnoreCase(DROPBOX_PATH_DATA_MOODS) == 0) {
            return DIARO_FILETYPE.DATA_MOODS;
        }

        if (parent.compareToIgnoreCase(DROPBOX_PATH_DATA_LOCATIONS) == 0) {
            return DIARO_FILETYPE.DATA_LOCATIONS;
        }

        if (parent.compareToIgnoreCase(DROPBOX_PATH_DATA_TEMPLATES) == 0) {
            return DIARO_FILETYPE.DATA_TEMPLATES;
        }

        if (parent.compareToIgnoreCase(DROPBOX_PATH_MEDIA + "/" + GlobalConstants.PHOTO) == 0) {
            return DIARO_FILETYPE.MEDIA_PHOTO;
        }
        if (parent.compareToIgnoreCase(DROPBOX_PATH_PROFILE) == 0) {
            return DIARO_FILETYPE.PROFILE;
        }

        return DIARO_FILETYPE.INVALID_PATH;


    }
}
