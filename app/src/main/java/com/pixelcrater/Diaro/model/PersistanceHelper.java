package com.pixelcrater.Diaro.model;

import android.content.ContentValues;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.templates.Template;
import com.pixelcrater.Diaro.utils.Static;

import org.apache.commons.lang3.StringUtils;

public class PersistanceHelper {

    public static String saveFolder(FolderInfo folder) {

        // folder title must be unique
        String uid = MyApp.getInstance().storageMgr.getSQLiteAdapter().findFolderByTitle(folder.title);

        if (uid == null) {
            // no tag with this title exists, create one and save it
            ContentValues cv = new ContentValues();

            if (folder.uid == null)
                cv.put(Tables.KEY_UID, Static.generateRandomUid());
            else
                cv.put(Tables.KEY_UID, folder.uid);

            cv.put(Tables.KEY_FOLDER_TITLE, folder.title);
            cv.put(Tables.KEY_FOLDER_COLOR, folder.color);
            cv.put(Tables.KEY_FOLDER_PATTERN, folder.pattern);

            uid = MyApp.getInstance().storageMgr.insertRow(Tables.TABLE_FOLDERS, cv);
        }

        return uid;
    }

    public static String saveTag(TagInfo tag) {
        // tag title must be unique
        String uid = MyApp.getInstance().storageMgr.getSQLiteAdapter().findTagByTitle(tag.title);
        if (uid == null) {
            // no tag with this title exists, create one and save it
            ContentValues cv = new ContentValues();
            cv.put(Tables.KEY_TAG_TITLE, tag.title);

            if (tag.uid == null)
                cv.put(Tables.KEY_UID, Static.generateRandomUid());
            else
                cv.put(Tables.KEY_UID, tag.uid);

            uid = MyApp.getInstance().storageMgr.insertRow(Tables.TABLE_TAGS, cv);
        }

        return uid;
    }

    /**
     * Save or update location info ( if same lat / long exists) and return its uid
     *
     * @param location
     * @return
     */
    public static String saveLocation(LocationInfo location, boolean update) {
        //  location lat / lng combination must be unique
        String locationUidExisiting = MyApp.getInstance().storageMgr.getSQLiteAdapter().findLocationByLatLng(location.latitude, location.longitude, location.title);

        // we just want the uid of existing location
        if (!update && locationUidExisiting != null) {
            return locationUidExisiting;
        }

        ContentValues cv = new ContentValues();
        cv.put(Tables.KEY_TAG_TITLE, location.title);
        cv.put(Tables.KEY_LOCATION_ADDRESS, location.address);
        cv.put(Tables.KEY_LOCATION_LATITUDE, location.latitude);
        cv.put(Tables.KEY_LOCATION_LONGITUDE, location.longitude);
        cv.put(Tables.KEY_LOCATION_ZOOM, location.zoom);

        if (locationUidExisiting == null) {
            // no lat long combination found, INSERT
            if (StringUtils.isEmpty(location.uid)) {
                locationUidExisiting = Static.generateRandomUid();
            } else {
                locationUidExisiting = location.uid;
            }

            cv.put(Tables.KEY_UID, locationUidExisiting);
            MyApp.getInstance().storageMgr.insertRow(Tables.TABLE_LOCATIONS, cv);

            //     AppLog.e("location with uid " + locationUidExisiting + " created");

        } else {
            // lat long combination found, UPDATE
            //   AppLog.e("location with uid " + locationUidExisiting + " existed");
            MyApp.getInstance().storageMgr.updateRowByUid(Tables.TABLE_LOCATIONS, locationUidExisiting, cv);
        }

        return locationUidExisiting;
    }

    public static String saveEntry(EntryInfo entryInfo) {
        String uid = "";

        ContentValues cv = new ContentValues();

        if (entryInfo.uid == null)
            cv.put(Tables.KEY_UID, Static.generateRandomUid());
        else
            cv.put(Tables.KEY_UID, entryInfo.uid);

        // Add archived field
        cv.put(Tables.KEY_ENTRY_ARCHIVED, entryInfo.archived);
        cv.put(Tables.KEY_ENTRY_TITLE, entryInfo.title);
        cv.put(Tables.KEY_ENTRY_TEXT, entryInfo.text);
        cv.put(Tables.KEY_ENTRY_DATE, entryInfo.unixEpochMillis);
        cv.put(Tables.KEY_ENTRY_TZ_OFFSET, entryInfo.tzOffset);
        cv.put(Tables.KEY_ENTRY_FOLDER_UID, entryInfo.folderUid);
        cv.put(Tables.KEY_ENTRY_LOCATION_UID, entryInfo.locationUid);
        cv.put(Tables.KEY_ENTRY_TAGS, entryInfo.tags);
        cv.put(Tables.KEY_ENTRY_MOOD_UID, entryInfo.moodUid);

        if (entryInfo.weatherInfo != null) {
            cv.put(Tables.KEY_ENTRY_WEATHER_TEMPERATURE, entryInfo.weatherInfo.getTemperature());
            cv.put(Tables.KEY_ENTRY_WEATHER_ICON, entryInfo.weatherInfo.getIcon());
            cv.put(Tables.KEY_ENTRY_WEATHER_DESCRIPTION, entryInfo.weatherInfo.getDescription());
        } else {
            cv.put(Tables.KEY_ENTRY_WEATHER_TEMPERATURE, 0.0);
            cv.put(Tables.KEY_ENTRY_WEATHER_ICON, "");
            cv.put(Tables.KEY_ENTRY_WEATHER_DESCRIPTION, "");
        }

        uid = MyApp.getInstance().storageMgr.insertRow(Tables.TABLE_ENTRIES, cv);

        return uid;
    }


    public static String saveAttachment(AttachmentInfo attachmentInfo) {

        String uid = "";

        ContentValues cv = new ContentValues();

        if (attachmentInfo.uid == null)
            cv.put(Tables.KEY_UID, Static.generateRandomUid());
        else
            cv.put(Tables.KEY_UID, attachmentInfo.uid);

        cv.put(Tables.KEY_ATTACHMENT_ENTRY_UID, attachmentInfo.entryUid);
        cv.put(Tables.KEY_ATTACHMENT_TYPE, attachmentInfo.type);
        cv.put(Tables.KEY_ATTACHMENT_FILENAME, attachmentInfo.filename);
        cv.put(Tables.KEY_ATTACHMENT_POSITION, attachmentInfo.position);

        // Insert attachment to storage
        uid = MyApp.getInstance().storageMgr.insertRow(Tables.TABLE_ATTACHMENTS, cv);

        return uid;
    }

    public static String addTemplate(Template template) {
        String uid = "";

        ContentValues cv = new ContentValues();
        if (StringUtils.isEmpty(template.getUid())) {
            cv.put(Tables.KEY_UID, Static.generateRandomUid());
        } else
            cv.put(Tables.KEY_UID, template.getUid());

        cv.put(Tables.KEY_TEMPLATE_NAME, template.getName());
        cv.put(Tables.KEY_TEMPLATE_TITLE, template.getTitle());
        cv.put(Tables.KEY_TEMPLATE_TEXT, template.getText());

        // Insert attachment to storage
        uid = MyApp.getInstance().storageMgr.insertRow(Tables.TABLE_TEMPLATES, cv);

        return uid;
    }

    public static String updateTemplate(Template template) {
        String uid = template.getUid();

        ContentValues cv = new ContentValues();
        cv.put(Tables.KEY_UID, template.getUid());
        cv.put(Tables.KEY_TEMPLATE_NAME, template.getName());
        cv.put(Tables.KEY_TEMPLATE_TITLE, template.getTitle());
        cv.put(Tables.KEY_TEMPLATE_TEXT, template.getText());

        MyApp.getInstance().storageMgr.updateRowByUid(Tables.TABLE_TEMPLATES, uid, cv);

        return uid;
    }

    public static void deleteTemplate(String uid) {
        MyApp.getInstance().storageMgr.deleteRowByUid(Tables.TABLE_TEMPLATES, uid);
    }

}