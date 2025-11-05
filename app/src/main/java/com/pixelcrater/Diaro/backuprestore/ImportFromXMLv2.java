package com.pixelcrater.Diaro.backuprestore;

import android.content.ContentValues;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyDateTimeUtils;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

/**
 * Backup xml format with diaro_locations table and every row element starts and ends with its tag
 * (e.g.: <title>Business</title>)
 */
public class ImportFromXMLv2 {

    public ImportFromXMLv2(Document doc) throws Exception {
        AppLog.d("");

        ArrayList<String> xmlTablesArrayList = new ArrayList<>();
        xmlTablesArrayList.add(Tables.TABLE_FOLDERS);
        xmlTablesArrayList.add(Tables.TABLE_TAGS);
        xmlTablesArrayList.add(Tables.TABLE_MOODS);
        xmlTablesArrayList.add(Tables.TABLE_LOCATIONS);
        xmlTablesArrayList.add(Tables.TABLE_ENTRIES);
        xmlTablesArrayList.add(Tables.TABLE_ATTACHMENTS);
        xmlTablesArrayList.add(Tables.TABLE_TEMPLATES);

        // --- 1. Get tables data from xml ---
        NodeList foldersNodeList = null;
        NodeList tagsNodeList = null;
        NodeList moodsNodeList = null;
        NodeList locationsNodeList = null;
        NodeList entriesNodeList = null;
        NodeList attachmentsNodeList = null;
        NodeList templatesNodeList = null;

        for (int t = 0; t < xmlTablesArrayList.size(); t++) {
            String xmlFullTableName = xmlTablesArrayList.get(t);

            if (xmlFullTableName.equals(Tables.TABLE_FOLDERS)) {
                foldersNodeList = getTableRowsNodeListFromXml(doc, xmlFullTableName);
            }

            if (xmlFullTableName.equals(Tables.TABLE_TAGS)) {
                tagsNodeList = getTableRowsNodeListFromXml(doc, xmlFullTableName);
            }
            if (xmlFullTableName.equals(Tables.TABLE_MOODS)) {
                moodsNodeList = getTableRowsNodeListFromXml(doc, xmlFullTableName);
            }

            if (xmlFullTableName.equals(Tables.TABLE_LOCATIONS)) {
                locationsNodeList = getTableRowsNodeListFromXml(doc, xmlFullTableName);
            }

            if (xmlFullTableName.equals(Tables.TABLE_ENTRIES)) {
                entriesNodeList = getTableRowsNodeListFromXml(doc, xmlFullTableName);
            }

            if (xmlFullTableName.equals(Tables.TABLE_ATTACHMENTS)) {
                attachmentsNodeList = getTableRowsNodeListFromXml(doc, xmlFullTableName);
            }

            if (xmlFullTableName.equals(Tables.TABLE_TEMPLATES)) {
                templatesNodeList = getTableRowsNodeListFromXml(doc, xmlFullTableName);
            }
        }

        // --- 2. Import tables data ---

        // Import folders
        if (foldersNodeList != null) {
            importXmlTableRows(foldersNodeList, Tables.TABLE_FOLDERS);
        }

        // Import tags
        if (tagsNodeList != null) {
            importXmlTableRows(tagsNodeList, Tables.TABLE_TAGS);
        }

        // Import tags
        if (moodsNodeList != null) {
            importXmlTableRows(moodsNodeList, Tables.TABLE_MOODS);
        }

        // Import locations
        if (locationsNodeList != null) {
            importXmlTableRows(locationsNodeList, Tables.TABLE_LOCATIONS);
        }

        // Import entries
        if (entriesNodeList != null) {
            importXmlTableRows(entriesNodeList, Tables.TABLE_ENTRIES);
        }

        // Import attachments
        if (attachmentsNodeList != null) {
            importXmlTableRows(attachmentsNodeList, Tables.TABLE_ATTACHMENTS);
        }

        // Import templates
        if (templatesNodeList != null) {
            importXmlTableRows(templatesNodeList, Tables.TABLE_TEMPLATES);
        }

        MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners();
    }

    private NodeList getTableRowsNodeListFromXml(Document doc, String fullTableName) {
        NodeList tableNodeList = doc.getElementsByTagName("table");

        if (tableNodeList != null && tableNodeList.getLength() > 0) {
            for (int i = 0; i < tableNodeList.getLength(); i++) {
                Element tableElement = (Element) tableNodeList.item(i);

                // Get table name from xml
                if (fullTableName.equals(tableElement.getAttribute("name"))) {
                    AppLog.d("fullTableName: " + fullTableName);

                    return tableElement.getElementsByTagName("r");
                }
            }
        }

        return null;
    }

    private void importXmlTableRows(NodeList nodeList, String fullTableName) throws Exception {
        AppLog.d("fullTableName: " + fullTableName);

        // Go through xml elements 'r'
        for (int i = 0; i < nodeList.getLength(); i++) {
            ContentValues cv = getRowCv(nodeList, i, fullTableName);

            // If uid does not exist
            if (cv.getAsString(Tables.KEY_UID) == null) {
                // Generate uid
                cv.put(Tables.KEY_UID, Static.generateRandomUid());
            }

            switch (fullTableName) {
                case Tables.TABLE_ENTRIES:
                    // If tz_offset not found
                    if (!cv.containsKey(Tables.KEY_ENTRY_TZ_OFFSET)) {
                        cv.put(Tables.KEY_ENTRY_TZ_OFFSET, MyDateTimeUtils.getCurrentTimeZoneOffset(cv.getAsLong(Tables.KEY_ENTRY_DATE)));
                    }
                    break;

                case Tables.TABLE_LOCATIONS:
                    // Change field long to lng
                    if (cv.containsKey("long")) {
                        cv.put(Tables.KEY_LOCATION_LONGITUDE, cv.getAsString("long"));
                        cv.remove("long");
                    }
                    break;
            }

            // Insert to storage
            MyApp.getInstance().storageMgr.insertRow(fullTableName, cv);
        }
    }

    private ContentValues getRowCv(NodeList nodeList, int index, String fullTableName) {
        ContentValues cv = new ContentValues();
        Element rElement = (Element) nodeList.item(index);
//		AppLog.d("rElement: " + rElement);

        NodeList rChildNodeList = rElement.getChildNodes();

        for (int i = 0; i < rChildNodeList.getLength(); i++) {
            Node itemNode = rChildNodeList.item(i);
//			AppLog.d("itemNode: " + itemNode);

            String nodeName = itemNode.getNodeName();
//			AppLog.d("nodeName: " + nodeName);

            // If this column exists in storage table
            if (!StringUtils.isEmpty(Tables.getFieldType(fullTableName, nodeName))) {
                try {
                    String nodeValue = itemNode.getFirstChild().getNodeValue();
//					AppLog.d("nodeValue: " + nodeValue);

                    if (nodeValue != null && !nodeValue.equals("null")) {
                        cv.put(nodeName, nodeValue);
                    }
                } catch (Exception e) {
                }
            }
        }

//		AppLog.d("fullTableName: " + fullTableName + ", cv: " + cv);

        return cv;
    }
}
