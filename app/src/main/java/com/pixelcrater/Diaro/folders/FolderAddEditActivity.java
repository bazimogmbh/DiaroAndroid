package com.pixelcrater.Diaro.folders;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.model.FolderInfo;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.activitytypes.TypeActivity;
import com.pixelcrater.Diaro.generaldialogs.ColorPickerDialog;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

public class FolderAddEditActivity extends TypeActivity {

    private String folderUid;
    private View colorView;
    private EditText folderTitleEditText;
    private String stateFolderColorCode;
    private int statePatternPosition = 0;
    private ViewGroup patternColor;
    private ViewGroup patternView;
    private TextInputLayout folderTitleTextInputLayout;
    private FolderColorsAdapter folderColorsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(addViewToContentContainer(R.layout.folder_addedit));
        activityState.setLayoutBackground();

        // Get intent extras
        Bundle extras = getIntent().getExtras();
        folderUid = extras.getString("folderUid");

        // Activity title
        int titleResId = R.string.folder_add;
        if (folderUid != null) {
            titleResId = R.string.folder_edit;
        }
        activityState.setActionBarTitle(getSupportActionBar(), getString(titleResId));

        folderTitleEditText = (EditText) findViewById(R.id.folder_title);
        folderTitleTextInputLayout = (TextInputLayout) folderTitleEditText.getParent().getParent();

        // Color line
        colorView = findViewById(R.id.color);

        // Folder colors gridview
        GridView colorsGridView = (GridView) findViewById(R.id.folder_colors_gridview);
        folderColorsAdapter = new FolderColorsAdapter(FolderAddEditActivity.this);
        folderColorsAdapter.setOnColorSelectedListener(this::onColorChange);
        colorsGridView.setAdapter(folderColorsAdapter);

        // More colors button
        Button moreColorButton = (Button) findViewById(R.id.more_colors);
        VectorDrawableCompat vectorDrawablePalette = VectorDrawableCompat.create(getResources(), R.drawable.ic_palette_24dp, null);
        moreColorButton.setCompoundDrawablesWithIntrinsicBounds(vectorDrawablePalette, null, null, null);
        moreColorButton.setOnClickListener(v -> showColorPickerDialog());

        if (savedInstanceState != null) {
            stateFolderColorCode = savedInstanceState.getString(FOLDER_COLOR_CODE_STATE_KEY);
            statePatternPosition = savedInstanceState.getInt(FOLDER_PATTERN_POSITION_STATE_KEY);
        } else if (folderUid != null) {
            Cursor folderCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleFolderCursorByUid(folderUid);

            // If not found
            if (folderCursor.getCount() == 0) {
                folderCursor.close();
                finish();
                return;
            }

            FolderInfo folderInfo = new FolderInfo(folderCursor);
            folderCursor.close();

            // Title
            folderTitleEditText.setText(folderInfo.title);
            folderTitleEditText.setSelection(folderTitleEditText.getText().length());

            stateFolderColorCode = folderInfo.color;

            // Pattern
            statePatternPosition = Static.getPatternPosition(folderInfo.pattern);
        }

        // Pattern
        ViewGroup patternClickView = (ViewGroup) findViewById(R.id.folder_pattern_click);
        patternClickView.setOnClickListener(v -> {
            showFolderPatternSelectDialog();
        });

        patternColor = (ViewGroup) findViewById(R.id.pattern_color);
        patternView = (ViewGroup) findViewById(R.id.folder_pattern);

        onColorChange(stateFolderColorCode);

        // Show keyboard
        Static.showSoftKeyboard(folderTitleEditText);

        // Restore active dialog listeners
        restoreDialogListeners(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_folder_addedit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (activityState.isActivityPaused) {
            return true;
        }

        int itemId = item.getItemId();

        // Back
        if (itemId == android.R.id.home) {
            Static.hideSoftKeyboard(folderTitleEditText);
            folderTitleEditText.clearFocus();

            finish();
            return true;
        }

        // Save folder
        else if (itemId == R.id.item_save) {
            Static.hideSoftKeyboard(folderTitleEditText);
            folderTitleEditText.clearFocus();

            saveFolder();
            return true;
        }

        else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void saveFolder() {
        String title = folderTitleEditText.getText().toString().trim();

        boolean sameFolderExists = MyApp.getInstance().storageMgr.getSQLiteAdapter().findSameFolder(folderUid, title) != null;

        // Check folder title
        if (title.equals("")) {
            // Show error
            folderTitleTextInputLayout.setError(getString(R.string.folder_title_error));
        }
        // Check if the same folder does not already exist
        else if (sameFolderExists) {
            // Show error
            folderTitleTextInputLayout.setError(getString(R.string.folder_the_same_error));
        } else {
            ContentValues cv = new ContentValues();
            cv.put(Tables.KEY_FOLDER_TITLE, title);
            cv.put(Tables.KEY_FOLDER_COLOR, stateFolderColorCode);
            String pattern = Static.getPatternsArrayList().get(statePatternPosition).patternTitle;
            cv.put(Tables.KEY_FOLDER_PATTERN, pattern);

            if (folderUid == null) {
                // Generate uid
                cv.put(Tables.KEY_UID, Static.generateRandomUid());

                // Insert row
                String uid = MyApp.getInstance().storageMgr.insertRow(Tables.TABLE_FOLDERS, cv);
                AppLog.d("INSERTED uid: " + uid);

                if (uid != null) {
                    folderUid = uid;
                }
            } else if (cv.size() > 0) {
                // Update row
                MyApp.getInstance().storageMgr.updateRowByUid(Tables.TABLE_FOLDERS, folderUid, cv);
            }

            MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners();

            // Finish
            Intent i = new Intent();
            setResult(RESULT_OK, i);
            finish();
        }
    }


    public void setColorPickerDialogListener(ColorPickerDialog dialog) {
        dialog.setOnOkButtonClickListener(color -> {
            onColorChange(MyThemesUtils.getHexColor(color));
        });
    }

    private void onColorChange(String colorCode) {
        // Try to parse color to check if its code is correct
        try {
            if (Color.parseColor(stateFolderColorCode) != -1) {
                stateFolderColorCode = colorCode;
            }
        } catch (Exception e) {
            AppLog.e("Exception: " + e);

            // Default folder color
            stateFolderColorCode = "#1cb5ff";
        }

        updateColorLineView();
        updatePatternPreview();

        folderColorsAdapter.setFolderColor(stateFolderColorCode);
        folderColorsAdapter.notifyDataSetChanged();
    }

    private void updateColorLineView() {
        colorView.setBackgroundColor(Color.parseColor(stateFolderColorCode));
    }

    private void setFolderPatternSelectDialogListener(FolderPatternSelectDialog dialog) {
        dialog.setDialogItemClickListener(position -> {
            statePatternPosition = position;
            updatePatternPreview();
        });
    }

    private void updatePatternPreview() {
        Static.setBgColorWithAlpha(Color.parseColor(stateFolderColorCode), patternColor);
        Static.setPattern(FolderAddEditActivity.this, statePatternPosition, patternView);
    }


    public void showColorPickerDialog() {
        String dialogTag = Static.DIALOG_PICKER_COLOR;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            ColorPickerDialog dialog = new ColorPickerDialog();
            dialog.setInitialColorCode(stateFolderColorCode);
            dialog.show(getSupportFragmentManager(), dialogTag);

            // Set dialog listener
            setColorPickerDialogListener(dialog);
        }
    }

    protected void showFolderPatternSelectDialog() {
        String dialogTag = Static.DIALOG_FOLDER_PATTERN_SELECT;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            FolderPatternSelectDialog dialog = new FolderPatternSelectDialog();
            dialog.setColor(Color.parseColor(stateFolderColorCode));
            dialog.show(getSupportFragmentManager(), dialogTag);

            // Set dialog listener
            setFolderPatternSelectDialogListener(dialog);
        }
    }

    // state restore
    private static final String FOLDER_COLOR_CODE_STATE_KEY = "FOLDER_COLOR_CODE_STATE_KEY";
    private static final String FOLDER_PATTERN_POSITION_STATE_KEY = "FOLDER_PATTERN_POSITION_STATE_KEY";

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(FOLDER_COLOR_CODE_STATE_KEY, stateFolderColorCode);
        outState.putInt(FOLDER_PATTERN_POSITION_STATE_KEY, statePatternPosition);
    }

    private void restoreDialogListeners(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            FolderPatternSelectDialog dialog1 = (FolderPatternSelectDialog)getSupportFragmentManager().findFragmentByTag(Static.DIALOG_FOLDER_PATTERN_SELECT);
            if (dialog1 != null) {
                setFolderPatternSelectDialogListener(dialog1);
            }

            ColorPickerDialog dialog2 = (ColorPickerDialog)getSupportFragmentManager().findFragmentByTag(Static.DIALOG_PICKER_COLOR);
            if (dialog2 != null) {
                setColorPickerDialogListener(dialog2);
            }
        }
    }
}
