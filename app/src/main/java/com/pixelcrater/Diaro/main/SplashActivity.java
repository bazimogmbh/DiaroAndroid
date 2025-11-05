package com.pixelcrater.Diaro.main;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.entries.viewedit.EntryViewEditActivity;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.Static;

import java.util.Objects;

public class SplashActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        AppLog.d("intent: " + intent);

        if (intent.hasExtra("widget")) {
            AppLog.d("intent.getAction(): " + intent.getAction());

            Intent newIntent = new Intent(this, EntryViewEditActivity.class);
            int flags = Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
            flags |= Intent.FLAG_ACTIVITY_TASK_ON_HOME;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
            } else {
                flags |= Intent.FLAG_ACTIVITY_NEW_TASK;
            }
            newIntent.setFlags(flags);
            newIntent.setAction(intent.getAction());
            newIntent.putExtra("widget", true);
            if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_SKIP_SECURITY_CODE_FOR_WIDGET, true)) {
                newIntent.putExtra(Static.EXTRA_SKIP_SC, true);
            }

            if (Objects.requireNonNull(intent.getAction()).startsWith("ACTION_PHOTO")) {
                newIntent.putExtra("selectPhoto", true);
            } else if (intent.getAction().startsWith("ACTION_CAMERA")) {
                newIntent.putExtra("capturePhoto", true);
            }

            startActivity(newIntent);
            finish();
        } else {
            Intent newIntent = new Intent(this, AppMainActivity.class);
            startActivity(newIntent);
            finish();
        }
    }
}
