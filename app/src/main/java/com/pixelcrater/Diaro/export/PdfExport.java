package com.pixelcrater.Diaro.export;

import static com.pixelcrater.Diaro.utils.Static.EXTRA_SKIP_SC;
import static com.pixelcrater.Diaro.utils.Static.REQUEST_GET_PRO;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.analytics.AnalyticsConstants;
import com.pixelcrater.Diaro.premium.PremiumActivity;
import com.pixelcrater.Diaro.settings.PreferencesHelper;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.storage.StorageUtils;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PdfExport {

    private static final String HTML_TEMPLATE_FILE = "pdf_template.html";
    private static final String BASE_URL_FOR_ASSET = "file:///android_asset/";

    @RequiresApi(19)
    public static void export(ArrayList<String> uids, ExportOptions options, ExportSummary summary, Activity activity) {
        FirebaseAnalytics.getInstance(activity).logEvent(AnalyticsConstants.EVENT_LOG_PRINT_PDF, new Bundle());

        if (Static.isProUser()) {
            LayoutInflater inflater = activity.getLayoutInflater();
            View print_pdf_preview = inflater.inflate(R.layout.print_pdf_preview, null);

            ProgressBar progressBar = print_pdf_preview.findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.VISIBLE);

            final WebView webview = print_pdf_preview.findViewById(R.id.webView);
            webview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

            WebSettings webSetting = webview.getSettings();
            webSetting.setJavaScriptEnabled(true);
             webSetting.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            webSetting.setDatabaseEnabled(true);
            webSetting.setAllowFileAccess(true);
            webSetting.setLoadWithOverviewMode(false);
            webSetting.setUseWideViewPort(false);
            webSetting.setSupportZoom(true);
            webSetting.setBuiltInZoomControls(true);
            webSetting.setDisplayZoomControls(false);

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setView(print_pdf_preview);
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int arg1) {
                    dialog.dismiss();
                  //  PrintManager printManager = (PrintManager) activity.getSystemService(Context.PRINT_SERVICE);
                    webview.stopLoading();
                    webview.destroy();
                }});

            AlertDialog alert = builder.create();
            builder.setOnDismissListener(dialog -> {
            });


            final String filename = ExportUtils.generatePdfFileName();

            webview.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    progressBar.setVisibility(View.GONE);
                    alert.dismiss();

                    // Create a wrapper PrintDocumentAdapter to clean up when done.
                    PrintDocumentAdapter printAdapter;
                    printAdapter = new PrintDocumentAdapter() {
                        private final PrintDocumentAdapter mWrappedInstance = webview.createPrintDocumentAdapter(filename);

                        @Override
                        public void onStart() {
                            mWrappedInstance.onStart();
                        }

                        @Override
                        public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
                            mWrappedInstance.onLayout(oldAttributes, newAttributes, cancellationSignal, callback, extras);
                        }

                        @Override
                        public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
                            mWrappedInstance.onWrite(pages, destination, cancellationSignal, callback);
                        }

                        @Override
                        public void onFinish() {
                            mWrappedInstance.onFinish();
                            webview.destroy();
                            if (MyApp.getInstance().securityCodeMgr.isSecurityCodeSet()) {
                                MyApp.getInstance().securityCodeMgr.setUnlocked();
                            }
                        }
                    };

                    PrintManager printManager = (PrintManager) activity.getSystemService(Context.PRINT_SERVICE);
                    if (printManager != null) {
                        PrintJob printJob = printManager.print(filename, printAdapter, new PrintAttributes.Builder().build());
                    }
                }
            });
            alert.show();

             // TODO : cacel this on abort

            AsyncTask.execute(() -> {
                boolean withAttachments = true;
                if (options.photo_height.isEmpty()) {
                    withAttachments = false;
                }
                List<ExportEntry> entriesList = ExportUtils.getExportEntries(uids, withAttachments, activity);

                // Load template
                String templateString = StorageUtils.getStringFromAsset(activity, HTML_TEMPLATE_FILE);
                Template mustacheTemplate = Mustache.compiler().compile(templateString);
                String currentFont = PreferencesHelper.getPrefFont();
                boolean includeLogo =   PreferencesHelper.getIncludeLogo();

                // Prepare data
                HashMap<String, Object> scopes = new HashMap<String, Object>();
                scopes.put("options", options);
                scopes.put("summary", summary);
                scopes.put("entries", entriesList);
                scopes.put("fontname", currentFont);
                scopes.put("logo", includeLogo);

                String htmlDocument = mustacheTemplate.execute(scopes);
                webview.post(() -> webview.loadDataWithBaseURL(BASE_URL_FOR_ASSET, htmlDocument, "text/HTML", "UTF-8", null));
            });

        } else {
            Intent intent = new Intent(activity, PremiumActivity.class);
            intent.putExtra(EXTRA_SKIP_SC, true);
            activity.startActivityForResult(intent, REQUEST_GET_PRO);
        }

    }


}
