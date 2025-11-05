package com.pixelcrater.Diaro.tags;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.model.TagInfo;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.activitytypes.TypeActivity;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.AppLog;

public class TagAddEditActivity extends TypeActivity {

    private String tagUid;
    private EditText tagTitleEditText;
    private TextInputLayout tagTitleTextInputLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(addViewToContentContainer(R.layout.tag_addedit));
        activityState.setLayoutBackground();

        Bundle extras = getIntent().getExtras();
        tagUid = extras.getString("tagUid");

        // Activity title
        int titleResId = R.string.tag_add;
        if (tagUid != null) titleResId = R.string.tag_edit;
        activityState.setActionBarTitle(getSupportActionBar(), getString(titleResId));

        tagTitleEditText = (EditText) findViewById(R.id.title);
        tagTitleTextInputLayout = (TextInputLayout) tagTitleEditText.getParent().getParent();

        if (tagUid != null) {
            Cursor tagCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleTagCursorByUid(tagUid);

            // If not found
            if (tagCursor.getCount() == 0) {
                tagCursor.close();
                finish();
                return;
            }

            TagInfo tagInfo = new TagInfo(tagCursor);
            tagCursor.close();

            tagTitleEditText.setText(tagInfo.title);
            tagTitleEditText.setSelection(tagTitleEditText.getText().length());
        }

        // Show keyboard
        Static.showSoftKeyboard(tagTitleEditText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_tag_addedit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (activityState.isActivityPaused) {
            return true;
        }

        switch (item.getItemId()) {
            // Back
            case android.R.id.home:
                Static.hideSoftKeyboard(tagTitleEditText);
                tagTitleEditText.clearFocus();

                finish();
                return true;

            // Save tag
            case R.id.item_save:
                Static.hideSoftKeyboard(tagTitleEditText);
                tagTitleEditText.clearFocus();

                saveTag();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveTag() {
        AppLog.d("tagUid: " + tagUid);

        String tagTitle = tagTitleEditText.getText().toString().trim();

        boolean sameTagExists = MyApp.getInstance().storageMgr.getSQLiteAdapter().findSameTag(tagUid, tagTitle) != null;

        // Check tag title
        if (tagTitle.equals("")) {
            // Show error
            tagTitleTextInputLayout.setError(getString(R.string.tag_title_error));
        }
        // Check if the same tag does not exist already
        else if (sameTagExists) {
            // Show error
            tagTitleTextInputLayout.setError(getString(R.string.tag_the_same_error));
        } else {
            boolean created = false;

            ContentValues cv = new ContentValues();

            // Save title
            cv.put(Tables.KEY_TAG_TITLE, tagTitle);

            if (tagUid == null) {
                // Generate uid
                cv.put(Tables.KEY_UID, Static.generateRandomUid());

                String uid = MyApp.getInstance().storageMgr.insertRow(Tables.TABLE_TAGS, cv);
                if (uid != null) {
                    created = true;
                    tagUid = uid;
                }
            } else if (cv.size() > 0) {
                MyApp.getInstance().storageMgr.updateRowByUid(Tables.TABLE_TAGS, tagUid, cv);
            }

            MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners();

            // Finish
            Intent i = new Intent();
            i.putExtra("tagUid", tagUid);
            i.putExtra("created", created);
            setResult(Activity.RESULT_OK, i);
            finish();
        }
    }
}
