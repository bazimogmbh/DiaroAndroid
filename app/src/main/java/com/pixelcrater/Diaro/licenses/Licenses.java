package com.pixelcrater.Diaro.licenses;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

import java.util.ArrayList;

public class Licenses {

    private final ArrayList<Library> librariesArrayList;

    public Licenses(Activity activity, ViewGroup viewGroup) {
        librariesArrayList = new ArrayList<>();

        librariesArrayList.add(new Library("Album",
                "https://github.com/yanzhenjie/Album",
                "Zhenjie Yan"));

        librariesArrayList.add(new Library("Android - Inapp-Billing-v3",
                "https://github.com/anjlab/android-inapp-billing-v3",
                "AnjLab"));

        librariesArrayList.add(new Library("Android - Support-Preference-V7-Fix",
                "https://github.com/Gericop/Android-Support-Preference-V7-Fix",
                "Gergely Kőrössy"));

        librariesArrayList.add(new Library("AStickyHeader",
                "https://github.com/DWorkS/AStickyHeader",
                "Hari Krishna Dulipudi"));

        librariesArrayList.add(new Library("BottomDialogs",
                "https://github.com/javiersantos/BottomDialogs",
                "Javier Santos"));

        librariesArrayList.add(new Library("DragSortAdapter",
                "https://github.com/vinc3m1/DragSortAdapter",
                "Vincent Mi"));

        librariesArrayList.add(new Library("FloatingActionButton",
                "https://github.com/makovkastar/FloatingActionButton",
                "Oleksandr Melnykov"));

        librariesArrayList.add(new Library("Glide",
                "https://github.com/bumptech/glide",
                "Google, Inc"));

        librariesArrayList.add(new Library("HoloColorPicker",
                "https://github.com/LarsWerkman/HoloColorPicker",
                "Lars Werkman"));

        librariesArrayList.add(new Library("Joda-time-android",
                "https://github.com/dlew/joda-time-android",
                "Daniel Lew"));

        librariesArrayList.add(new Library("Reprint",
                "https://github.com/ajalt/reprint",
                "AJ Alt"));

        librariesArrayList.add(new Library("SQLCipher for Android",
                "https://www.zetetic.net/sqlcipher/open-source",
                "Zetetic LLC"));



        generateLicensesItems(activity, viewGroup);
    }

    private void generateLicensesItems(final Activity activity, ViewGroup viewGroup) {
        int px10 = Static.getPixelsFromDip(10);
//        int px2 = Static.getPixelsFromDip(2);

        for (final Library o : librariesArrayList) {
            // Title
            TextView titleTextView = new TextView(activity);
            titleTextView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            titleTextView.setPadding(0, px10, 0, 0);
            titleTextView.setTypeface(null, Typeface.BOLD);
            titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            titleTextView.setText(o.title);
            viewGroup.addView(titleTextView);

            // Author
            TextView authorTextView = new TextView(activity);
            authorTextView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            authorTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            authorTextView.setText(o.author);
            viewGroup.addView(authorTextView);

            // Url
            TextView urlTextView = new TextView(activity);
            urlTextView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            urlTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            urlTextView.setTextColor(MyThemesUtils.getDarkColor(MyThemesUtils.getPrimaryColorCode()));
            urlTextView.setText(o.url);
            urlTextView.setBackgroundResource(R.drawable.bg_ripple);
            viewGroup.addView(urlTextView);

            // Url OnClickListener
            urlTextView.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(o.url));
                activity.startActivityForResult(intent, Static.REQUEST_WEB_URL);
            });
        }
    }

    public class Library {
        public final String title;
        public final String url;
        public final String author;

        public Library(String title, String url, String author) {
            this.title = title;
            this.url = url;
            this.author = author;
        }
    }
}
