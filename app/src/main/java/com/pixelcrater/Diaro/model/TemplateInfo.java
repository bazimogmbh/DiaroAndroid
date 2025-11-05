package com.pixelcrater.Diaro.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.pixelcrater.Diaro.storage.Tables;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TemplateInfo {

    // {"uid":"9939b31b8c268c446da7915816a65731","name":"Mijju","text":"Ggy","title":"","date_created":1588161054000}

    static DateTimeFormatter formatter = DateTimeFormat.forPattern( "yyyy-MM-dd HH:mm:ss");

    public static String createJsonString(Cursor cursor) throws Exception {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(Tables.KEY_UID, cursor.getString(cursor.getColumnIndex(Tables.KEY_UID)));
        jsonObject.put(Tables.KEY_TEMPLATE_NAME, cursor.getString(cursor.getColumnIndex(Tables.KEY_TEMPLATE_NAME)));
        jsonObject.put(Tables.KEY_TEMPLATE_TEXT, cursor.getString(cursor.getColumnIndex(Tables.KEY_TEMPLATE_TEXT)));
        jsonObject.put(Tables.KEY_TEMPLATE_TITLE, cursor.getString(cursor.getColumnIndex(Tables.KEY_TEMPLATE_TITLE)));

        String dateCreatedString = cursor.getString(cursor.getColumnIndex(Tables.KEY_TEMPLATE_DATE_CREATED));
        // dateCreatedString is in yyyy-MM-dd HH:mm:ssm, convert to unix timestamp
        DateTime dt = formatter.parseDateTime(dateCreatedString);
        jsonObject.put(Tables.KEY_TEMPLATE_DATE_CREATED, dt.getMillis());

        return jsonObject.toString();
    }


    public static ContentValues createRowCvFromJsonString(String jsonString) throws Exception {
        JSONObject jsonObject = new JSONObject(jsonString);
        ContentValues cv = new ContentValues();

        cv.put(Tables.KEY_UID, jsonObject.getString(Tables.KEY_UID));
        if (jsonObject.has(Tables.KEY_TEMPLATE_TITLE)) {
            cv.put(Tables.KEY_TEMPLATE_TITLE, jsonObject.getString(Tables.KEY_TEMPLATE_TITLE));
        }
        if (jsonObject.has(Tables.KEY_TEMPLATE_TEXT)) {
            cv.put(Tables.KEY_TEMPLATE_TEXT, jsonObject.getString(Tables.KEY_TEMPLATE_TEXT));
        }
        if (jsonObject.has(Tables.KEY_TEMPLATE_NAME)) {
            cv.put(Tables.KEY_TEMPLATE_NAME, jsonObject.getString(Tables.KEY_TEMPLATE_NAME));
        }
        if (jsonObject.has(Tables.KEY_TEMPLATE_DATE_CREATED)) {

            long dateCreatedLong = jsonObject.getLong(Tables.KEY_TEMPLATE_DATE_CREATED);
            // Convert 1588161054000 to 2020-04-29 08:23:19
            String dateCreatedString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(dateCreatedLong));
            cv.put(Tables.KEY_TEMPLATE_DATE_CREATED, dateCreatedString);
        }

        return cv;
    }


}
