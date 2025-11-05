package com.pixelcrater.Diaro.entries.viewedit;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.pixelcrater.Diaro.BuildConfig;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.entries.attachments.AttachmentsStatic;
import com.pixelcrater.Diaro.model.AttachmentInfo;
import com.pixelcrater.Diaro.model.EntryInfo;
import com.pixelcrater.Diaro.utils.MyDateTimeUtils;
import com.pixelcrater.Diaro.utils.Static;
import org.joda.time.DateTime;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.pixelcrater.Diaro.config.GlobalConstants.PHOTO;

public class EntryFragmentHelper {


    /**
     * Gets touched offset position of TextView
     */
    public static int getOffset(View v, MotionEvent event) {
        int offset = 0;

        Layout layout = ((TextView) v).getLayout();
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (layout != null) {
            int line = layout.getLineForVertical(y);
            offset = layout.getOffsetForHorizontal(line, x);

            if (offset < 0) {
                offset = 0;
            } else if (offset > ((TextView) v).length()) {
                offset = ((TextView) v).length();
            }
        }

        return offset;
    }

    /**
     * Returns link on cursor position
     */
    public static String getLinkOnCursor(TextView textView, int cursorPos) {
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(textView.getText());
        URLSpan[] urlSpans = strBuilder.getSpans(0, strBuilder.length(), URLSpan.class);
        for (final URLSpan span : urlSpans) {
            int start = strBuilder.getSpanStart(span);
            int end = strBuilder.getSpanEnd(span);
            if (cursorPos >= start && cursorPos < end) {
                return span.getURL();
            }
        }
        return null;
    }

    /**
     * Opens detected link (email, web url, phone number or address)
     */
     static void openDetectedLink(Activity activity, String linkOnCursor) {
        if (linkOnCursor == null) {
            return;
        }

        boolean isEmail = linkOnCursor.startsWith("mailto:");
        boolean isWebUrl = linkOnCursor.startsWith("http://") || linkOnCursor.startsWith("https://");
        boolean isPhoneNumber = linkOnCursor.startsWith("tel:");
        boolean isAddress = linkOnCursor.startsWith("geo:");
        // AppLog.d("linkOnCursor: "+linkOnCursor+", isEmail: "+isEmail+", isWebUrl: "+isWebUrl+", isPhoneNumber: "+isPhoneNumber+", isAddress: "+isAddress);
        try {
            // Web URL
            if (isWebUrl) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(linkOnCursor));
                activity.startActivityForResult(intent, Static.REQUEST_WEB_URL);
            }
            // E-mail
            else if (isEmail) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                String[] recipients = new String[]{linkOnCursor};
                intent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
                intent.setType("message/rfc822");
                activity.startActivityForResult(Intent.createChooser(intent, activity.getApplicationContext().getString(R.string.choose_email_app)), Static.REQUEST_SEND_EMAIL);
            }
            // Phone number
            else if (isPhoneNumber) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(linkOnCursor));
                activity.startActivityForResult(intent, Static.REQUEST_DIAL_NUMBER);
            }
            // Address
            else if (isAddress) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(linkOnCursor));
                activity.startActivityForResult(intent, Static.REQUEST_OPEN_MAP);
            }
        } catch (Exception e) {
            // Show error toast
            Static.showToast(e.getMessage(), Toast.LENGTH_SHORT);
        }
    }


    public static void shareEntry(Activity activity, EntryInfo entryInfo) {

        DateTime localDt = entryInfo.getLocalDt();

        // Day of month
        String dateD = Static.getDigitWithFrontZero(localDt.getDayOfMonth());

        // Month
        int month = localDt.getMonthOfYear();
        String dateM = Static.getMonthTitle(month).toUpperCase(Locale.ENGLISH);

        // Year
        String dateY = String.valueOf(localDt.getYear());
      //  dateYearField.setText(dateY);

        // Time
        String time = localDt.toString(MyDateTimeUtils.getTimeFormat());

        Intent intent = new Intent();
     //   intent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name);
        intent.putExtra(Intent.EXTRA_TEXT, dateD + " " + dateM + " " + dateY + ", " + time + "\n\n" + entryInfo.title + "\n\n" + entryInfo.text);

        ArrayList<AttachmentInfo> entryPhotosArrayList = AttachmentsStatic.getEntryAttachmentsArrayList(entryInfo.uid, PHOTO);

        if (entryPhotosArrayList.size() > 0) {
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);

            ArrayList<Uri> uris = new ArrayList<>();

            for (AttachmentInfo o : entryPhotosArrayList) {
                File file = new File(o.getFilePath());

                Uri uri;

                try {
                    uri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", file);

                } catch (Exception e) {
                    if (Build.VERSION.SDK_INT >= 24) {
                        try {
                            Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                            m.invoke(null);
                        } catch (Exception ee) {
                            ee.printStackTrace();
                        }
                    }
                    uri = Uri.fromFile(file);
                }

                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

                } else {
                    List<ResolveInfo> resInfoList = activity.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        activity.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                }

                uris.add(uri);
            }

            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

        } else {
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
        }

        activity.startActivityForResult(Intent.createChooser(intent, activity.getText(R.string.app_title) + " - " + activity.getText(R.string.share)), Static.REQUEST_SHARE_ENTRY);
    }

}
