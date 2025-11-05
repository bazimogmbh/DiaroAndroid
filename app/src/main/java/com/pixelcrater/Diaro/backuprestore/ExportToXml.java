package com.pixelcrater.Diaro.backuprestore;

import android.database.Cursor;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.AppLog;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ExportToXml {

    private Exporter exporter;

    public ExportToXml(String xmlFilePath) {
        AppLog.d("--- ExportToXml() ---");
        AppLog.d("xmlFilePath: " + xmlFilePath);

        try {
            // Create a file on the SD card
            File myXmlFile = new File(xmlFilePath);
            myXmlFile.createNewFile();

            FileOutputStream fos = new FileOutputStream(myXmlFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            exporter = new Exporter(bos);
            exporter.startDbExport();

            ArrayList<String> databaseTablesArrayList = new ArrayList<>();
            databaseTablesArrayList.add(Tables.TABLE_FOLDERS);
            databaseTablesArrayList.add(Tables.TABLE_TAGS);
            databaseTablesArrayList.add(Tables.TABLE_MOODS);
            databaseTablesArrayList.add(Tables.TABLE_LOCATIONS);
            databaseTablesArrayList.add(Tables.TABLE_ENTRIES);
            databaseTablesArrayList.add(Tables.TABLE_ATTACHMENTS);
            databaseTablesArrayList.add(Tables.TABLE_TEMPLATES);

            for (String fullTableName : databaseTablesArrayList) {
                // Export table to xml
                exportTable(fullTableName);
            }
            exporter.endDbExport();
            exporter.close();
        } catch (Exception e) {
            AppLog.d("Export e: " + e);
        }
    }

    private void exportTable(String fullTableName) throws Exception {
        String fullWhere = (fullTableName.equals(Tables.TABLE_ENTRIES)) ? "WHERE " + Tables.KEY_ENTRY_ARCHIVED + "=0" : "";

        // Get only uid fields to avoid 1MB cursor limit
        Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getRowsUidsCursor(fullTableName, fullWhere, null);
        int entryUidColumnIndex = cursor.getColumnIndex(Tables.KEY_UID);

        // Move through the table, creating rows and adding each column with name and value to the row
        if (cursor.getCount() > 0) {
            exporter.startTable(fullTableName);
            while (cursor.moveToNext()) {

                // Get single row cursor
                Cursor rowCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleRowCursorByUid(fullTableName, cursor.getString(entryUidColumnIndex));

                if (rowCursor != null) {
                    exporter.startRow();
                    int columnCount = rowCursor.getColumnCount();

                    for (int c = 0; c < columnCount; c++) {
                        // Add only allowed fields to XML
                        if (!rowCursor.getColumnName(c).equals(Tables.KEY_ENTRY_ARCHIVED) && !StringUtils.isEmpty(Tables.getFieldType(fullTableName,
                                rowCursor.getColumnName(c)))) {
                            exporter.addColumn(rowCursor.getColumnName(c), rowCursor.getString(c));
                        }
                    }
                    rowCursor.close();
                    exporter.endRow();
                }
            }
            exporter.endTable();
        }
        cursor.close();
    }

    private class Exporter {
        private BufferedOutputStream bos;

        public Exporter(BufferedOutputStream bos) {
            this.bos = bos;
        }

        public void close() throws IOException {
            if (bos != null) {
                bos.close();
            }
        }

        public void startDbExport() throws IOException {
            bos.write("<data version=\"2\">".getBytes());
        }

        public void endDbExport() throws IOException {
            bos.write("\n</data>".getBytes());
        }

        public void startTable(String tableName) throws IOException {
            String stg = "\n<table name=\"" + tableName + "\">";
            bos.write(stg.getBytes());
        }

        public void endTable() throws IOException {
            bos.write("\n</table>".getBytes());
        }

        public void startRow() throws IOException {
            bos.write("\n<r>".getBytes());
        }

        public void endRow() throws IOException {
            bos.write("\n</r>".getBytes());
        }

        public void addColumn(String name, String val) throws IOException {
            if (name.equals(Tables.KEY_ENTRY_TITLE) ||
                    name.equals(Tables.KEY_ENTRY_TEXT) ||
                    name.equals(Tables.KEY_FOLDER_TITLE) ||
                    name.equals(Tables.KEY_TAG_TITLE) ||
                    name.equals(Tables.KEY_LOCATION_TITLE) ||
                    name.equals(Tables.KEY_LOCATION_ADDRESS) ||
                    name.equals(Tables.KEY_ATTACHMENT_FILENAME) ||
                    name.equals(Tables.KEY_TEMPLATE_TITLE) ||
                    name.equals(Tables.KEY_TEMPLATE_TEXT) ||
                    name.equals(Tables.KEY_TEMPLATE_TITLE) ) {
                val = StringEscapeUtils.escapeXml11(val);
            }

            bos.write(String.valueOf("\n   <" + name + ">" + val + "</" + name + ">").getBytes());
        }
    }
}
