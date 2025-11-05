package com.pixelcrater.Diaro.imports;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.MyDateTimeUtils;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.model.AttachmentInfo;
import com.pixelcrater.Diaro.model.EntryInfo;
import com.pixelcrater.Diaro.model.FolderInfo;
import com.pixelcrater.Diaro.model.LocationInfo;
import com.pixelcrater.Diaro.model.PersistanceHelper;
import com.pixelcrater.Diaro.model.TagInfo;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;

import org.apache.commons.io.FilenameUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class EvernoteImport {

    //evernote enex nodes -> https://evernote.com/blog/how-evernotes-xml-export-format-works/
    private String NOTES = "/en-export/note";                                    //DIARO_KEY_ENTRY
    private String LATITUDE = "note-attributes/latitude";                        //DIARO_ KEY_ENTRY_LOCATION_LATITUDE
    private String LONGITUDE = "note-attributes/longitude";                      //DIARO_ KEY_ENTRY_LOCATION_LONGITUDE
    private String RESOURCE_ATTRIBUTE_FILENAME = "resource-attributes/file-name";//DIARO_KEY_ATTACHMENT_FILENAME
    private String TITLE = "title";                                              //DIARO_KEY_ENTRY_TITLE
    private String CONTENT = "content";                                          //DIARO_KEY_ENTRY_TEXT
    private String CREATED = "created";                                          //DIARO_KEY_ENTRY_DATE
    private String RESOURCE = "resource";                                        //DIARO_KEY_ATTACHMENT
    private String MIME = "mime";                                                //DIARO_KEY_ATTACHMENT_TYPE
    private String DATA = "data";                                                //DIARO_KEY_ATTACHMENT_DATA
    private String TAG = "tag";                                                  //DIARO_KEY_ENTRY_TAG

    private int DEFAULT_ZOOM = 11;
    private String OFFSET = "+00:00";

    //evernote folder
    private String FOLDER_TITLE = "Evernote";
    private String FOLDER_COLOR = "#F0B913";

    private SAXReader reader = new SAXReader();
    private Document document;

    private String locationFormat = "%.6f";

    public EvernoteImport(String filePath, Activity activity) {

        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

        ProgressDialog progress = new ProgressDialog(activity);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(false);
        progress.setTitle(R.string.importing_data);
     //   progress.setMessage(fileName);
        progress.setCancelable(false);
        progress.show();

        AsyncTask.execute(() -> {

            try {
                document = reader.read(new File(filePath));

                // Import folders
                FolderInfo folder = new FolderInfo(null, FOLDER_TITLE, FOLDER_COLOR);
                folder.setPattern("");
                String tmpfolderUid = PersistanceHelper.saveFolder(folder);

                //collecting all the nodes inside a list
                List<Node> nodeList = document.selectNodes(NOTES);
                AppLog.e("Total entries found -> " + nodeList.size());
                progress.setMax(nodeList.size());

                //   progress.setMessage("Found " + nodeList.size() + " entries");

                String title = "";
                String text = "";
                String locationUid = "";
                String folderUid = "";
                StringBuilder tags = new StringBuilder();

                int entriesCounter = 0;

                // Loop the entries
                for (Node node : nodeList) {

                    final int displayCount = ++entriesCounter;

                    activity.runOnUiThread(() -> progress.setProgress(displayCount));

                    title = "";
                    text = "";
                    locationUid = "";
                    folderUid = "";
                    tags = new StringBuilder();

                    // Import folder
                    folderUid = tmpfolderUid;

                    // Import Tags
                    ArrayList<String> tagUids = new ArrayList<>();
                    if (node.selectSingleNode(TAG) != null) {
                        List<Node> evernote_tags = node.selectNodes(TAG);

                        for (Node evenote_tag : evernote_tags) {
                            String tagTitle = evenote_tag.getText();

                            TagInfo tagInfo = new TagInfo(Static.generateRandomUid(), tagTitle);
                            String tagUid = PersistanceHelper.saveTag(tagInfo);
                            tagUids.add(tagUid);
                        }
                    }

                    // Import Location
                    if (node.selectSingleNode(LATITUDE) != null && node.selectSingleNode(LONGITUDE) != null) {
                        String latitude = String.format(Locale.ENGLISH, locationFormat, Double.parseDouble(node.selectSingleNode(LATITUDE).getText()));
                        String longitude = String.format(Locale.ENGLISH, locationFormat, Double.parseDouble(node.selectSingleNode(LONGITUDE).getText()));

                        LocationInfo locationInfo = new LocationInfo(Static.generateRandomUid(), "", "", latitude, longitude, DEFAULT_ZOOM);
                        locationUid = PersistanceHelper.saveLocation(locationInfo, false);
                    }

                    //Import Entry
                    if (node.selectSingleNode(TITLE) != null) {
                        title = node.selectSingleNode(TITLE).getText();
                    }
                    if (node.selectSingleNode(CONTENT) != null) {
                        String html = node.selectSingleNode(CONTENT).getText();

                        text = cleanPreserveLineBreaks(html);
                      //  AppLog.e(text);
                    }

                    long time = 0L;
                    if (node.selectSingleNode(CREATED) != null) {
                        // 20150721T121347Z ,  20200510T124009Z
                        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss'Z'").withZoneUTC();
                        try {
                            String dateCreated = node.selectSingleNode(CREATED).getText();
                            DateTime dt = formatter.parseDateTime(dateCreated);
                            time = dt.getMillis();
                            OFFSET = MyDateTimeUtils.getCurrentTimeZoneOffset(time);
                            //  AppLog.e("---> " + dateCreated +  "---> " +MyDateTimeUtils.getCurrentTimeZoneOffset(time) );

                        } catch (Exception e) {
                            AppLog.e("time exception" + e.getMessage());
                        }
                    }

                    EntryInfo entry = new EntryInfo();
                    entry.setUid(Static.generateRandomUid());
                    entry.setTzOffset(OFFSET);
                    entry.setDate(time);
                    entry.setTitle(title);
                    entry.setText(text);
                    entry.setFolderUid(folderUid);
                    entry.setLocationUid(locationUid);

                    for (int i = 0; i < tagUids.size(); i++) {
                        tags.append(",").append(tagUids.get(i));

                        if (i == (tagUids.size() - 1)) {
                            tags.append(",");
                        }
                    }
                    entry.setTags(tags.toString());

                    PersistanceHelper.saveEntry(entry);

                    // Import Attachments
                    if (node.selectSingleNode(RESOURCE) != null) {

                        List<Node> attachmentsForEachEntry = node.selectNodes(RESOURCE);

                        long position = 1;
                        byte[] decoded = null;
                        String type = "photo";

                        for (Node attachmentForeach : attachmentsForEachEntry) {

                            String mime = attachmentForeach.selectSingleNode(MIME).getText();
                            // check for a valid attachment
                            if (ImportHelper.checkIsValidMime(mime)) {
                                try {
                                    String fileName1 = attachmentForeach.selectSingleNode(RESOURCE_ATTRIBUTE_FILENAME).getText();
                                    String extension = FilenameUtils.getExtension(fileName1);
                                    String baseEncoding = attachmentForeach.selectSingleNode(DATA).getText();

                                    decoded = android.util.Base64.decode(baseEncoding, android.util.Base64.DEFAULT);

                                    String newFileName = ImportHelper.getNewFilename(extension, type);

                                    AttachmentInfo attachment = new AttachmentInfo(entry.uid, type, newFileName, position++);
                                    PersistanceHelper.saveAttachment(attachment);

                                    // Save the image
                                    ImportHelper.writeFileAndCompress(AppLifetimeStorageUtils.getMediaDirPath() + "/" + type + "/" + newFileName, decoded);
                                } catch (Exception e) {
                                    AppLog.e(e.getMessage());
                                    activity.runOnUiThread(() -> Static.showToast(e.getMessage(), Toast.LENGTH_LONG));
                                    decoded = null;
                                }

                            }
                        }

                    }

                }

                activity.runOnUiThread(() -> {
                    // Done importing
                    Static.showToastLong("Imported " + nodeList.size() + " entries successfully!");
                    MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners();
                    progress.cancel();
                });


            } catch (DocumentException e) {
                AppLog.e(Arrays.toString(e.getStackTrace()));

                activity.runOnUiThread(() -> {
                    Static.showToastLong("Import failed! " + e.getMessage());
                    progress.cancel();
                });

            }

        });


    }

    public static String cleanPreserveLineBreaks(String html) {

        // get pretty printed html with preserved br p and tags
        String cleanedJsoup = Jsoup.clean(html, "", Safelist.none().addTags("br", "p", "div"), new org.jsoup.nodes.Document.OutputSettings().prettyPrint(true));

        // iterate through div , remove them and add newlines
        org.jsoup.nodes.Document doc = Jsoup.parse(cleanedJsoup);
        org.jsoup.select.Elements divs = doc.select("div");

        StringBuilder stringBuilder = new StringBuilder();
        for(org.jsoup.nodes.Element elem : divs){

            String elementHtml = elem.html();
            if(!elementHtml.isEmpty()){
                elementHtml  = Jsoup.clean(elementHtml, "", Safelist.none(), new org.jsoup.nodes.Document.OutputSettings().prettyPrint(false));
                stringBuilder.append(elementHtml);
                stringBuilder.append("\n");
            }
        }

        String textWithNewLines = stringBuilder.toString();

        String lineWithBreaks = Jsoup.clean(textWithNewLines, "", Safelist.none(), new org.jsoup.nodes.Document.OutputSettings().prettyPrint(false));
        lineWithBreaks = lineWithBreaks.replaceAll("&nbsp;", " ");
        lineWithBreaks = lineWithBreaks.replaceAll("\n\n", "\n");


        AppLog.e("lineWithBreaks->" +  lineWithBreaks);
        return lineWithBreaks;
    }


}
