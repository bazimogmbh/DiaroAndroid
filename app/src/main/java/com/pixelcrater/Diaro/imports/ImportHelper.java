package com.pixelcrater.Diaro.imports;

import android.os.AsyncTask;
import android.util.Log;

import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.entries.attachments.AttachmentsStatic;
import com.pixelcrater.Diaro.utils.storage.StorageUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ImportHelper {


    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static String concatLatLng(String lat, String lng) {
        return String.format("(%s , %s)", lat, lng);
    }

    public static boolean checkIsValidImageFile(String extension) {
        return "png".equals(extension) || "gif".equals(extension) || "jpg".equals(extension) || "jpeg".equals(extension) || "sticker".equals(extension);
    }

    public static boolean checkIsValidMime(String mime) {
        return mime.equals("image/jpeg") || mime.equals("image/jpg") || mime.equals("image/gif") || mime.equals("image/png");
    }

    public static boolean checkPhotosExtension(ZipEntry zipEntry) {
        return zipEntry.getName().endsWith(".png") || zipEntry.getName().endsWith(".jpg") || zipEntry.getName().endsWith(".gif") || zipEntry.getName().endsWith(".jpeg");
    }

    public static String getNewFilename(String extension, String type) {
        DateTime dateTime = new DateTime();
        String millis = String.valueOf(dateTime.getMillis());
        String newFileName = type + "_" + dateTime.toString("yyyyMMdd") + "_" + millis.substring(millis.length() - 6) + "." + extension;
        newFileName = AttachmentsStatic.getNewAttachmentFilenameIfExists(newFileName, type);
        return newFileName;
    }

    public static boolean isNullOrEmpty(JSONObject rootJsonObject, String key) {
        if (rootJsonObject.isNull(key)) {
            return true;
        }

        try {
            if (rootJsonObject.get(key) instanceof String) {
                //check if rootJsonObject is a String
                return rootJsonObject.getString(key).isEmpty();

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static void writeFileAndCompress(String filePath, byte[] decoded) throws Exception {
        File targetFile = new File(filePath);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(targetFile));
        bos.write(decoded);
        bos.flush();
        bos.close();

        // Compress the image
        AsyncTask.execute(() -> {
            try {
                StorageUtils.compressPhoto(filePath,  GlobalConstants.IMAGE_MAX_W_H, GlobalConstants.IMAGE_COMPRESS_QUALITY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    public static byte[] getBytecodeForZipEntry(ZipEntry entry, ZipFile zipFile) {
        int entrySize = (int) entry.getSize();
        try {
            BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
            byte[] finalByteArray = new byte[entrySize];

            int bufferSize = 2048;
            byte[] buffer = new byte[2048];
            int chunkSize = 0;
            int bytesRead = 0;

            while (true) {
                //Read chunk to buffer
                chunkSize = bis.read(buffer, 0, bufferSize); //read() returns the number of bytes read
                if (chunkSize == -1) {
                    //read() returns -1 if the end of the stream has been reached
                    break;
                }

                //Write that chunk to the finalByteArray
                System.arraycopy(buffer, 0, finalByteArray, bytesRead, chunkSize);

                bytesRead += chunkSize;
            }

            bis.close();
            return finalByteArray;
        } catch (IOException e) {
            Log.e("Diaro", "No zip entry found");
            return null;
        }
    }


    /**
     * This method merges multiple jsons if present
     *
     * @param jsonArrays List of JSONArrays
     * @return JSONArray
     * @throws JSONException
     */
    public static JSONArray mergeJsonArrays(ArrayList<JSONArray> jsonArrays) throws JSONException {
        JSONArray mergedJsonArrays = new JSONArray();
        for (JSONArray tmpArray : jsonArrays) {
            for (int i = 0; i < tmpArray.length(); i++) {
                mergedJsonArrays.put(tmpArray.get(i));
            }
        }
        return mergedJsonArrays;
    }

    /**
     * @param dateFormat correct date format
     * @param timeZone   Joda dateTimeZone
     * @param dateString Date as String
     * @return UTC Offset
     */
    public static String getUTCOffset(String dateFormat, DateTimeZone timeZone, String dateString) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(dateFormat).withZone(timeZone);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateString);
        DateTimeFormatter offset = DateTimeFormat.forPattern("Z");
        String dateTimeOffsetString = dateTime.toString(offset);
        return dateTimeOffsetString.substring(0, 3) + (":") + dateTimeOffsetString.substring(3, 5);
    }


    //weather icon info
    public static HashMap<String, String> iconAndNameMap = new HashMap<String, String>() {
        {
            put("01d", "day-sunny");
            put("01n", "night-clear");
            put("02d", "day-cloudy-gusts");
            put("02n", "night-alt-cloudy-gusts");
            put("03d", "day-cloudy-gusts");
            put("03n", "night-alt-cloudy-gusts");
            put("04d", "day-sunny-overcast");
            put("04n", "night-alt-cloudy");
            put("09d", "day-showers");
            put("09n", "night-alt-showers");
            put("10d", "day-sprinkle");
            put("10n", "night-alt-sprinkle");
            put("11d", "day-lightning");
            put("11n", "night-alt-lightning");
            put("13d", "day-snow");
            put("13n", "night-alt-snow");
            put("50d", "day-fog");
            put("50n", "night-fog");
        }
    };


    /**
     * converts icon code to name
     *
     * @param icon                 weather icon code
     * @param weatherIconToNameMap map with weather code and name
     * @return weather icon name
     */
    public static String iconCodeToName(String icon, Map<String, String> weatherIconToNameMap) {
        if (!icon.isEmpty() && weatherIconToNameMap.containsKey(icon)) {
            return weatherIconToNameMap.get(icon);
        }
        return "";
    }
}


