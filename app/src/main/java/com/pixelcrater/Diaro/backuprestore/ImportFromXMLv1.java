package com.pixelcrater.Diaro.backuprestore;

import android.content.ContentValues;
import android.provider.BaseColumns;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.locations.LocationsStatic;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyDateTimeUtils;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;
import com.pixelcrater.Diaro.utils.storage.StorageUtils;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Deprecated backup xml format
 */
public class ImportFromXMLv1 {

    private NodeList entriesNodes = null;
    private NodeList foldersNodes = null;
    private NodeList tagsNodes = null;
    private NodeList attachmentsNodes = null;

    public ImportFromXMLv1(Document doc) throws Exception {
        AppLog.d("");

        ArrayList<String> xmlTablesArrayList = new ArrayList<>();
        xmlTablesArrayList.add(Tables.TABLE_FOLDERS);
        xmlTablesArrayList.add("categories");
        xmlTablesArrayList.add(Tables.TABLE_TAGS);
        xmlTablesArrayList.add(Tables.TABLE_ENTRIES);
        xmlTablesArrayList.add(Tables.TABLE_ATTACHMENTS);

        // --- 1. Get tables data from xml ---
        for (int t = 0; t < xmlTablesArrayList.size(); t++) {
            String xmlFullTableName = xmlTablesArrayList.get(t);

            if (xmlFullTableName.equals(Tables.TABLE_FOLDERS)
                    || (xmlFullTableName.equals("categories") && foldersNodes == null)) {
                foldersNodes = getTableRowsFromXml(doc, xmlFullTableName);
            }

            if (xmlFullTableName.equals(Tables.TABLE_TAGS)) {
                tagsNodes = getTableRowsFromXml(doc, xmlFullTableName);
            }

            if (xmlFullTableName.equals(Tables.TABLE_ENTRIES)) {
                entriesNodes = getTableRowsFromXml(doc, xmlFullTableName);
            }

            if (xmlFullTableName.equals(Tables.TABLE_ATTACHMENTS)) {
                attachmentsNodes = getTableRowsFromXml(doc, xmlFullTableName);
            }
        }

        // --- 2. Import tables data ---

        // Import folders
        if (foldersNodes != null) {
            importFolders();
        }

        // Import tags
        if (tagsNodes != null) {
            importTags();
        }

        // Import entries
        if (entriesNodes != null) {
            importEntries();
        }

        // Import attachments
        if (attachmentsNodes != null) {
            importAttachments();
        }

        MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners();
    }

    private void importFolders() throws Exception {
        AppLog.d("");

        // Go through xml elements 'row'
        for (int i = 0; i < foldersNodes.getLength(); i++) {
            ContentValues cv = getRowCv(foldersNodes, i, Tables.TABLE_FOLDERS);

            String xmlFolderId = cv.getAsString(BaseColumns._ID);
            String xmlFolderUid = cv.getAsString(Tables.KEY_UID);
            String xmlFolderTitle = cv.getAsString(Tables.KEY_FOLDER_TITLE);

            // If not 'Other'
            if (!StringUtils.equals(xmlFolderUid, "0") &&
                    !StringUtils.equals(xmlFolderId, "0")) {
                // If uid does not exist, generate it
                if (xmlFolderUid == null) {
                    // Generate uid
                    xmlFolderUid = Static.generateRandomUid();

                    cv.put(Tables.KEY_UID, xmlFolderUid);
                }

                // Look for the same folder in database (by uid or title)
                String matchedFolderUid = MyApp.getInstance().storageMgr.getSQLiteAdapter()
                        .findSameFolder(xmlFolderUid, xmlFolderTitle);

                if (matchedFolderUid != null) {
                    AppLog.d("sameFolderExists xmlFolderUid: " + xmlFolderUid +
                            ", matchedFolderUid: " + matchedFolderUid);

                    // Update folderUid in NodeList from xml for all entries to matchedFolderUid
                    updateEntriesFolderUidInNodeList(xmlFolderUid, xmlFolderId, matchedFolderUid);
                } else {
                    // Insert new folder to database
                    executeInsertSql(Tables.TABLE_FOLDERS, cv);

                    // Update entry folderUid from id to newly inserted uid
                    if (xmlFolderId != null) {
                        updateEntriesFolderUidInNodeList(xmlFolderUid, xmlFolderId, xmlFolderUid);
                    }
                }
            }
        }
    }

    private void importTags() throws Exception {
        AppLog.d("");

        // Go through xml elements 'row'
        for (int i = 0; i < tagsNodes.getLength(); i++) {
            ContentValues cv = getRowCv(tagsNodes, i, Tables.TABLE_TAGS);

            String xmlTagUid = cv.getAsString(Tables.KEY_UID);
            String xmlTagTitle = cv.getAsString(Tables.KEY_TAG_TITLE);

            if (xmlTagTitle != null && !xmlTagTitle.equals("")) {
                // If uid does not exist, generate
                if (xmlTagUid == null) {
                    // Generate uid
                    xmlTagUid = Static.generateRandomUid();

                    cv.put(Tables.KEY_UID, xmlTagUid);
                }

//				AppLog.d("xmlTagUid: " + xmlTagUid);

                // Look for the same tag in database (by uid or name)
                String matchedTagUid = MyApp.getInstance().storageMgr.getSQLiteAdapter().
                        findSameTag(xmlTagUid, xmlTagTitle);
//				AppLog.d("matchedTagUid: " + matchedTagUid);

                if (matchedTagUid != null) {
                    // Update entries tags field in NodeList from xml to matchedTagUid
                    updateEntriesTagInNodeList(xmlTagUid, matchedTagUid);
                } else {
                    // Insert new tag to DB
                    executeInsertSql(Tables.TABLE_TAGS, cv);
                }
            }
        }
    }

    private void importEntries() throws Exception {
        AppLog.d("");

        // AppLog.d("entriesNodes.getLength(): " + entriesNodes.getLength());

        // Go through xml elements 'row'
        for (int i = 0; i < entriesNodes.getLength(); i++) {
            ContentValues cv = getRowCv(entriesNodes, i, Tables.TABLE_ENTRIES);
//			AppLog.d("cv: " + cv);

            String xmlEntryId = cv.getAsString(BaseColumns._ID);
            String xmlEntryUid = cv.getAsString(Tables.KEY_UID);

            // If uid does not exist, generate
            if (xmlEntryUid == null) {
                // Generate uid
                xmlEntryUid = Static.generateRandomUid();

                cv.put(Tables.KEY_UID, xmlEntryUid);

                // Rename photo directory from OLD id to uid in TEMP MEDIA folder
                renameOldEntryDirectoryFromIdtoUid(xmlEntryId, xmlEntryUid);
            }

            // Clear folderUid if incorrect length
            if (cv.containsKey(Tables.KEY_ENTRY_FOLDER_UID) && cv.getAsString(Tables.KEY_ENTRY_FOLDER_UID).length() != 32) {
                cv.put(Tables.KEY_ENTRY_FOLDER_UID, "");
            }

            // Update entry tag titles to uids and count tags
            replaceTagTitles(cv);
//            AppLog.d("cv: " + cv);

            // Insert location and update entry row
            String location = cv.getAsString("location");
            if (location == null) {
                location = "";
            }

            String locationCoords = cv.getAsString("location_coords");
            if (locationCoords == null) {
                locationCoords = "";
            }

            cv.remove("location");
            cv.remove("location_coords");

            // If tz_offset not found
            if (!cv.containsKey(Tables.KEY_ENTRY_TZ_OFFSET)) {
                cv.put(Tables.KEY_ENTRY_TZ_OFFSET, MyDateTimeUtils.getCurrentTimeZoneOffset(cv.getAsLong(Tables.KEY_ENTRY_DATE)));
            }

            // Insert new entry to DB
            executeInsertSql(Tables.TABLE_ENTRIES, cv);

            // Insert location to locations table
            LocationsStatic.insertLocationAndUpdateEntry(location, locationCoords, xmlEntryUid);
        }
    }

    /**
     * Renames entry photo directory from id to uid
     */
    public void renameOldEntryDirectoryFromIdtoUid(String xmlItemId, String itemUid) {
//		log("xmlItemId: " + xmlItemId + ", itemUid: " + itemUid);

        if (xmlItemId == null || itemUid == null) return;

        File photosDirFile = new File(AppLifetimeStorageUtils.getDeprecatedCacheRestoreMediaPhotosDirPath() + "/" + xmlItemId);
        File newPhotosDirFile = new File(AppLifetimeStorageUtils.getDeprecatedCacheRestoreMediaPhotosDirPath() + "/" + itemUid);

        // Rename
        boolean fileRenamed = photosDirFile.renameTo(newPhotosDirFile);
//		log("fileRenamed: " + fileRenamed);

        if (!fileRenamed) {
            StorageUtils.deleteFileOrDirectory(photosDirFile);
        }
    }

    private void importAttachments() throws Exception {
        AppLog.d("");

        // Go through xml elements 'row'
        for (int i = 0; i < attachmentsNodes.getLength(); i++) {
            ContentValues cv = getRowCv(attachmentsNodes, i, Tables.TABLE_ATTACHMENTS);

            String xmlAttachmentUid = cv.getAsString(Tables.KEY_UID);

            // If uid does not exist, generate
            if (xmlAttachmentUid == null) {
                // Generate uid
                xmlAttachmentUid = Static.generateRandomUid();

                cv.put(Tables.KEY_UID, xmlAttachmentUid);
            }

            // Insert new attachment to DB
            executeInsertSql(Tables.TABLE_ATTACHMENTS, cv);
        }
    }

    /**
     * Gets any table all rows from xml
     */
    private NodeList getTableRowsFromXml(Document doc, String fullTableName) {
        NodeList nodeLst = doc.getElementsByTagName("table");

        if (nodeLst != null && nodeLst.getLength() > 0) {
            for (int i = 0; i < nodeLst.getLength(); i++) {
                Element el = (Element) nodeLst.item(i);

                // Get table name from xml
                if (fullTableName.equals(el.getAttribute("name"))) {
                    // AppLog.d("fullTableName: " + fullTableName);

                    NodeList nodeLstSub = el.getElementsByTagName("row");
                    // Check for shorter xml tag
                    if (nodeLstSub.getLength() == 0) {
                        nodeLstSub = el.getElementsByTagName("r");
                    }

                    return nodeLstSub;
                }
            }
        }

        return null;
    }

    /**
     * Goes through all entries in NodeList xml and updates parent to matchedFolderUid
     */
    private void updateEntriesFolderUidInNodeList(String xmlFolderUid, String xmlFolderId, String matchedFolderUid) {
        AppLog.d("xmlFolderUid: " + xmlFolderUid + ", xmlFolderId: " + xmlFolderId + ", matchedFolderUid: " + matchedFolderUid);

        if (entriesNodes != null && entriesNodes.getLength() > 0) {
            // Go through xml elements 'row'
            for (int i = 0; i < entriesNodes.getLength(); i++) {
                ContentValues cv = getRowCv(entriesNodes, i, Tables.TABLE_ENTRIES);

                // Update entry folder
                if (StringUtils.equals(xmlFolderId, cv.getAsString(Tables.KEY_ENTRY_FOLDER_UID)) ||
                        StringUtils.equals(xmlFolderUid, cv.getAsString(Tables.KEY_ENTRY_FOLDER_UID))) {
                    // Update entry folder_uid in xml
                    updateEntryColValueInNodeList(i, Tables.KEY_ENTRY_FOLDER_UID, matchedFolderUid);

                    // Update entry parent in xml
                    updateEntryColValueInNodeList(i, "parent", matchedFolderUid);
                }
            }
        }
    }

    /**
     * Goes through all entries in NodeList from xml and updates tags to matchedFolderUid
     */
    private void updateEntriesTagInNodeList(String fromTagUid, String toTagUid) {
        // AppLog.d("fromTagUid: " + fromTagUid + ", toTagUid: " + toTagUid);

        if (fromTagUid == null || toTagUid == null) return;

        if (entriesNodes != null && entriesNodes.getLength() > 0) {
            // Go through xml elements 'row'
            for (int i = 0; i < entriesNodes.getLength(); i++) {
                ContentValues cv = getRowCv(entriesNodes, i, Tables.TABLE_ENTRIES);

                // Get entry tags uids
                String newTagsUids = "";
                String tagsUids = cv.getAsString(Tables.KEY_ENTRY_TAGS);
                if (tagsUids == null) tagsUids = "";

                ArrayList<String> tagsUidsArrayList = new ArrayList<>(Arrays.asList(tagsUids.split(",")));

                int size = tagsUidsArrayList.size();
                for (int z = 0; z < size; z++) {
                    String tagUid = tagsUidsArrayList.get(z);
                    if (!tagUid.equals("")) {
                        if (tagUid.equals(fromTagUid)) newTagsUids += "," + toTagUid;
                        else newTagsUids += "," + tagUid;
                    }
                }

                if (!newTagsUids.equals("")) {
                    newTagsUids += ",";

                    // Update entry tags in NodeList from xml
                    updateEntryColValueInNodeList(i, Tables.KEY_ENTRY_TAGS, newTagsUids);
                }
            }
        }
    }

    /**
     * Updates given entry column value in NodeList from xml to new given value
     */
    private void updateEntryColValueInNodeList(int rowIndex, String columnName, String columnNewValue) {
        // AppLog.d("rowIndex: " + rowIndex + ", columnName: " + columnName +
        // ", columnNewValue: " + columnNewValue);

        Element elRow = (Element) entriesNodes.item(rowIndex);

        NodeList nodeLstSubSub = elRow.getElementsByTagName("col");
        // Check for shorter xml element
        if (nodeLstSubSub.getLength() == 0) nodeLstSubSub = elRow.getElementsByTagName("c");

        // Go through xml elems 'col'
        for (int z = 0; z < nodeLstSubSub.getLength(); z++) {
            Element elCol = (Element) nodeLstSubSub.item(z);
            Node elNode = nodeLstSubSub.item(z);

            // Get col name and value
            String colName = elCol.getAttribute("name");
            // Check for shorter xml tag
            if (colName.length() == 0) colName = elCol.getAttribute("n");

            // Update node value
            if (colName.equals(columnName)) {
                elNode.getFirstChild().setNodeValue(columnNewValue);
                // AppLog.d("equals colName: " + colName + ", columnNewValue: " +
                // columnNewValue + ", elNode: " + elNode.getFirstChild().getNodeValue());
                break;
            }
        }
    }

    private ContentValues getRowCv(NodeList nodeLstSub, int rowIndex, String fullTableName) {
        ContentValues cv = new ContentValues();
        Element elRow = (Element) nodeLstSub.item(rowIndex);

        NodeList nodeLstSubSub = elRow.getElementsByTagName("col");
        // Check for shorter xml element
        if (nodeLstSubSub.getLength() == 0) {
            nodeLstSubSub = elRow.getElementsByTagName("c");
        }

        // Go through xml elems 'col'
        for (int z = 0; z < nodeLstSubSub.getLength(); z++) {
            Element elCol = (Element) nodeLstSubSub.item(z);
            Node elNode = nodeLstSubSub.item(z);

            // Get col name and value
            String colName = elCol.getAttribute("name");
            // Check for shorter xml element
            if (colName.length() == 0) {
                colName = elCol.getAttribute("n");
            }

            String colValue = "";
            try {
//                AppLog.d("colName: " + colName);
                colValue = elNode.getFirstChild().getNodeValue();
//                AppLog.d("colName: " + colName + ", colValue: " + colValue);
            } catch (Exception e) {
//                AppLog.e("Exception: " + e);
            }

            if (StringUtils.isNotEmpty(colName) && !StringUtils.equals(colValue, "null")) {
                if (columnExists(fullTableName, colName)) {
                    cv.put(colName, colValue);
                }
            }
        }

        // Change deprecated fields
        switch (fullTableName) {
            case Tables.TABLE_ENTRIES:
                changeDeprecatedCV(cv, "entry_name", Tables.KEY_ENTRY_TITLE);
                changeDeprecatedCV(cv, "parent", Tables.KEY_ENTRY_FOLDER_UID);
                break;

            case Tables.TABLE_FOLDERS:
                changeDeprecatedCV(cv, "category_name", Tables.KEY_FOLDER_TITLE);
                changeDeprecatedCV(cv, "category_color", Tables.KEY_FOLDER_COLOR);
                break;

            case Tables.TABLE_TAGS:
                changeDeprecatedCV(cv, "tag_name", Tables.KEY_TAG_TITLE);
                break;
        }

//        AppLog.d("cv: " + cv);

        return cv;
    }

    /**
     * Replaces tag titles in old xml to uids
     */
    private void replaceTagTitles(ContentValues cv) {
//		AppLog.d("cv: " + cv);

        String tags = cv.getAsString(Tables.KEY_ENTRY_TAGS);
//		AppLog.d("tags: " + tags);

        String newTags = "";

        if (tags != null) {
            ArrayList<String> tagsArrayList = new ArrayList<>(Arrays.asList(tags.split(",")));

            for (int i = 0; i < tagsArrayList.size(); i++) {
                String tagUid = tagsArrayList.get(i);
                if (!tagUid.equals("")) {
                    // If exists
                    if (MyApp.getInstance().storageMgr.getSQLiteAdapter().rowExists(Tables.TABLE_TAGS, tagUid)) {
                        newTags += "," + tagUid;
                    }
                }
            }
            if (!newTags.equals("")) newTags += ",";
        }

        cv.put(Tables.KEY_ENTRY_TAGS, newTags);
    }

    /**
     * Insert item to storage
     */
    public void executeInsertSql(String fullTableName, ContentValues cv) {
        //		AppLog.d("cv: " + cv + ", fullTableName: " + fullTableName);

        // Remove _id from cv
        cv.remove(BaseColumns._ID);

        MyApp.getInstance().storageMgr.insertRow(fullTableName, cv);
    }

    /**
     * Changes deprecated xml key to the new one to fit database field
     */
    private void changeDeprecatedCV(ContentValues cv, String oldKey, String newKey) {
        String value = cv.getAsString(oldKey);
        cv.remove(oldKey);
        if (value != null) {
            cv.put(newKey, value);
        }
    }

    /**
     * Checks if given column from xml is allowed to be inserted to database
     */
    private boolean columnExists(String fullTableName, String colName) {
        ArrayList<String> columnsArrayList = new ArrayList<>();

        columnsArrayList.add(BaseColumns._ID);
        columnsArrayList.add(Tables.KEY_UID);

        switch (fullTableName) {
            case Tables.TABLE_ENTRIES:
                columnsArrayList.add(Tables.KEY_ENTRY_DATE);
                columnsArrayList.add(Tables.KEY_ENTRY_TITLE);
                columnsArrayList.add("entry_name"); // support deprecated

                columnsArrayList.add(Tables.KEY_ENTRY_TEXT);
                columnsArrayList.add(Tables.KEY_ENTRY_FOLDER_UID);
                columnsArrayList.add("parent"); // support deprecated

                columnsArrayList.add("location");
                columnsArrayList.add("location_coords");
                columnsArrayList.add(Tables.KEY_ENTRY_TAGS);
                columnsArrayList.add(Tables.KEY_ENTRY_PRIMARY_PHOTO_UID);
                break;

            case Tables.TABLE_FOLDERS:
                columnsArrayList.add(Tables.KEY_FOLDER_TITLE);
                columnsArrayList.add("category_name"); // support deprecated

                columnsArrayList.add(Tables.KEY_FOLDER_COLOR);
                columnsArrayList.add("category_color"); // support deprecated

                columnsArrayList.add(Tables.KEY_FOLDER_PATTERN);
                break;

            case Tables.TABLE_TAGS:
                columnsArrayList.add(Tables.KEY_TAG_TITLE);
                columnsArrayList.add("tag_name"); // support deprecated

                break;

            case Tables.TABLE_ATTACHMENTS:
                columnsArrayList.add(Tables.KEY_ATTACHMENT_ENTRY_UID);
                columnsArrayList.add(Tables.KEY_ATTACHMENT_TYPE);
                columnsArrayList.add(Tables.KEY_ATTACHMENT_FILENAME);
                columnsArrayList.add(Tables.KEY_ATTACHMENT_POSITION);
                break;
        }

        return columnsArrayList.contains(colName);
    }
}
