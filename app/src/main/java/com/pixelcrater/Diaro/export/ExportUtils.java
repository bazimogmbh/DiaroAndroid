package com.pixelcrater.Diaro.export;

import static com.pixelcrater.Diaro.config.GlobalConstants.PHOTO;

import android.content.Context;

import com.pixelcrater.Diaro.entries.attachments.AttachmentsStatic;
import com.pixelcrater.Diaro.model.AttachmentInfo;
import com.pixelcrater.Diaro.model.EntryInfo;
import com.pixelcrater.Diaro.model.TagInfo;
import com.pixelcrater.Diaro.settings.PreferencesHelper;
import com.pixelcrater.Diaro.storage.sqlite.SQLiteQueryHelper;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyDateTimeUtils;
import com.pixelcrater.Diaro.utils.Static;
import com.sandstorm.weather.WeatherHelper;

import org.apache.commons.text.WordUtils;
import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ExportUtils {

    private static final String DATE_TIME_PATTERN = "ddMMyyyy_hhmmss";
    private static final String FILE_NAME_PREFFIX = "Diaro_";
    private static SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_PATTERN, Locale.getDefault());

    private static HashMap<String, TagInfo> tagsUidTagInfoMap = new HashMap<>();

    public static List<ExportEntry> getExportEntries(final ArrayList<String> uidsList, Boolean withAttachments, Context ctx) {
        //long time = System.currentTimeMillis();
        List<ExportEntry> entriesList = new ArrayList<>();

        tagsUidTagInfoMap.clear();

        //1. Fetch all tags and attachment data at once so that we do not have to query them from DB for each entry!
        // 1a) Tags cache
        List<TagInfo> tagsInfo = SQLiteQueryHelper.getTagsInfos();
        for (TagInfo tagInfo : tagsInfo) {
            tagsUidTagInfoMap.put(tagInfo.uid, tagInfo);
        }

        // 1b) Photos attachment cache
        HashMap<String, ArrayList<AttachmentInfo>> attachmentsMap = new HashMap<>();
        if (withAttachments) {
            ArrayList<AttachmentInfo> entryPhotosArrayList = AttachmentsStatic.getEntryAttachmentsArrayList(null, PHOTO);
            AppLog.e("Total images found -> " + entryPhotosArrayList.size());
            for (AttachmentInfo entryPhoto : entryPhotosArrayList) {
                String entryUid = entryPhoto.entryUid;
                ArrayList<AttachmentInfo> entryPhotos = attachmentsMap.get(entryUid);
                if (entryPhotos == null) {
                    entryPhotos = new ArrayList<>();
                }
                entryPhotos.add(entryPhoto);
                attachmentsMap.put(entryUid, entryPhotos);
            }
        }

        // 2 Fetch max 1000 entries from DB at once to make sure 1mb cursor limit does not affect us
        int partitionSize = 1000;
        List<List<String>> partitions = new ArrayList<>();

        for (int i = 0; i < uidsList.size(); i += partitionSize) {
            partitions.add(uidsList.subList(i, Math.min(i + partitionSize, uidsList.size())));
        }

        // 3 Iterate through each partition and fetch entry data of those uidslist
        for (List<String> subList : partitions) {
            ArrayList<EntryInfo> entries = SQLiteQueryHelper.getEntriesByUids(subList, false);
            for (EntryInfo entryInfo : entries) {
                DateTime localDt = entryInfo.getLocalDt();
                // Day of month
                String dateD = Static.getDigitWithFrontZero(localDt.getDayOfMonth());
                // Day of week
                int dayOfWeek = localDt.getDayOfWeek();
                String dateWD = Static.getDayOfWeekTitle(Static.convertDayOfWeekFromJodaTimeToCalendar(dayOfWeek));
                // Month
                int month = localDt.getMonthOfYear();
                String dateM = Static.getMonthTitle(month);
                // Year
                String dateY = String.valueOf(localDt.getYear());
                // Time
                String dateHM = localDt.toString(MyDateTimeUtils.getTimeFormat());
                // Time with Seconds
                String dateHMS = localDt.toString(MyDateTimeUtils.getTimeFormatWithSeconds());

                ExportEntry exportEntry = new ExportEntry();
                exportEntry.uid = entryInfo.uid;

                //   AppLog.e(count++  + " - >" +  entryInfo.uid);
                exportEntry.title = entryInfo.title;
                exportEntry.text = entryInfo.text.replace("\n", "<br>");
                exportEntry.folder_color = entryInfo.folderColor;
                if (!entryInfo.folderTitle.isEmpty())
                    exportEntry.folder_title = entryInfo.folderTitle;

                String tags = getTagStringFromTagUids(entryInfo);
                if (!tags.isEmpty())
                    exportEntry.tags = tags;

                // Mood
                if (entryInfo.isMoodSet() && PreferencesHelper.isMoodsEnabled()) {
                    exportEntry.moodTitle = entryInfo.moodTitle;
                    exportEntry.moodIcon = entryInfo.moodIcon;
                    exportEntry.hasMood = true;
                }

                if (!entryInfo.getLocationTitle().isEmpty()) {
                    exportEntry.location = entryInfo.getLocationTitle() + ", (" + entryInfo.locationLatitude + "," + entryInfo.locationLongitude + ")";
                } else {
                    if (!entryInfo.locationLatitude.isEmpty() && !entryInfo.locationLongitude.isEmpty())
                        exportEntry.location = "(" + entryInfo.locationLatitude + "," + entryInfo.locationLongitude + ")";
                }

                exportEntry.day = Integer.parseInt(dateD);
                exportEntry.day_of_week_full = dateWD;
                exportEntry.month_name = dateM;
                exportEntry.time = dateHM;
                exportEntry.year = Integer.parseInt(dateY);

                if (withAttachments) {
                    ArrayList<AttachmentInfo> entryPhotosArrayList = attachmentsMap.get(entryInfo.uid);
                    if (entryPhotosArrayList != null) {
                        for (AttachmentInfo o : entryPhotosArrayList) {
                            exportEntry.attachmentsList.add((o.getFilePath()));
                        }
                    }
                }

                if (entryInfo.weatherInfo != null) {
                    if (!entryInfo.weatherInfo.getIcon().isEmpty() && !entryInfo.weatherInfo.getDescription().isEmpty()) {
                        exportEntry.weather_icon = entryInfo.weatherInfo.getIcon();
                        double temp = entryInfo.weatherInfo.getTemperature();
                        String suffix = WeatherHelper.STRING_CELSIUS;
                        if (PreferencesHelper.isPrefUnitFahrenheit()) {
                            temp = WeatherHelper.celsiusToFahrenheit(temp);
                            suffix = WeatherHelper.STRING_FAHRENHEIT;
                        }
                        exportEntry.unit_name = suffix;
                        exportEntry.weather_temperature_display = String.format("%.1f", temp);
                        if (entryInfo.weatherInfo.getLocalizedDescription() == 0) {
                            exportEntry.weather_description_display = (WordUtils.capitalize(entryInfo.weatherInfo.getDescription()));
                        } else {
                            exportEntry.weather_description_display = ctx.getString(entryInfo.weatherInfo.getLocalizedDescription());
                        }

                    }
                }
                entriesList.add(exportEntry);
            }
        }
        //   AppLog.e("Export process took " + (System.currentTimeMillis() - time) + " for " + entriesList.size() + " entries.");
        return entriesList;
    }

    private static String getTagStringFromTagUids(EntryInfo entryInfo) {
        if (entryInfo.tagCount == 0) {
            return "";
        }

        ArrayList<TagInfo> entryTagsArrayList = new ArrayList<>();
        String[] entryTagsArray = entryInfo.tags.split(",");
        StringBuilder entryTags = new StringBuilder();

        for (String tagUid : entryTagsArray) {
            if (!tagUid.equals("")) {
                TagInfo tagInfoCached = tagsUidTagInfoMap.get(tagUid);
                entryTagsArrayList.add(tagInfoCached);
            }
        }
        if (!entryTags.toString().equals("")) {
            entryTags.append(",");
        }

        Collections.sort(entryTagsArrayList, new Static.ComparatorTags());

        StringBuilder tags = new StringBuilder();
        for (TagInfo tagInfo : entryTagsArrayList) {
            if (tagInfo != null) {
                if (!tags.toString().equals("")) {
                    tags.append(", ");
                }
                tags.append(tagInfo.title);
            }
        }

        return tags.toString();
    }

    static String generatePdfFileName() {
        String currentDateAndTime = sdf.format(new Date());
        return FILE_NAME_PREFFIX + currentDateAndTime;
    }

    static String generateTxtFileName() {
        String currentDateAndTime = sdf.format(new Date());
        return FILE_NAME_PREFFIX + currentDateAndTime + ".txt";
    }

    static String generateCSVFileName() {
        String currentDateAndTime = sdf.format(new Date());
        return FILE_NAME_PREFFIX + currentDateAndTime + ".csv";
    }
}
