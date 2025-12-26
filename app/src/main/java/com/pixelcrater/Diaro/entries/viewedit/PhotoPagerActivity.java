package com.pixelcrater.Diaro.entries.viewedit;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import com.pixelcrater.Diaro.BuildConfig;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.entries.async.RotatePhotoAsync;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.analytics.AnalyticsConstants;
import com.pixelcrater.Diaro.activitytypes.TypeActivity;
import com.pixelcrater.Diaro.model.AttachmentInfo;
import com.pixelcrater.Diaro.entries.EntriesStatic;
import com.pixelcrater.Diaro.entries.attachments.AttachmentsStatic;
import com.pixelcrater.Diaro.generaldialogs.ConfirmDialog;
import com.pixelcrater.Diaro.storage.OnStorageDataChangeListener;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.AppLog;
import com.sandstorm.diary.piceditor.activities.ImageEditorActivity;
import com.sandstorm.diary.piceditor.features.picker.PhotoPicker;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.pixelcrater.Diaro.config.GlobalConstants.PHOTO;

public class PhotoPagerActivity extends TypeActivity implements OnStorageDataChangeListener {

    public String entryUid;
    public PhotoPagerAdapter photoPagerAdapter;
    private ArrayList<AttachmentInfo> entryPhotosArrayList;
    private boolean openedFromPhotoGrid;
    private String primaryPhotoUid = "";

    private ViewPager pager;
    private TextView noPhotosFoundTextView;
    private TextView photoNumberTextView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(addViewToContentContainer(R.layout.photo_pager));
        activityState.setLayoutBackground();

        // Handle bottom insets for edge-to-edge on Android 15+
        applyBottomInsets(findViewById(R.id.layout_container));

        Bundle bundle = getIntent().getExtras();
        entryUid = bundle.getString("entryUid");
        openedFromPhotoGrid = bundle.getBoolean("openedFromPhotoGrid");

        photoNumberTextView = (TextView) LayoutInflater.from(this).inflate(R.layout.photo_numbering, null);

        // Add custom view to ActionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(photoNumberTextView);

        pager = (ViewPager) findViewById(R.id.view_pager);
        noPhotosFoundTextView = (TextView) findViewById(R.id.no_photos_found);

        pager.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int pos) {
                // Update photo number in action bar
                updatePhotoNumber();
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        photoPagerAdapter = new PhotoPagerAdapter(PhotoPagerActivity.this);
        pager.setAdapter(photoPagerAdapter);

        if (!getEntryData()) {
            finish();
            return;
        }

        if (savedInstanceState == null) {
            int activePhotoPosition = bundle.getInt("position", 0);
            pager.setCurrentItem(activePhotoPosition, false);
        }

        // Restore active dialog listeners
        restoreDialogListeners(savedInstanceState);

        MyApp.getInstance().storageMgr.addOnStorageDataChangeListener(this);
    }

    private void restoreDialogListeners(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            ConfirmDialog dialog1 = (ConfirmDialog) getSupportFragmentManager().findFragmentByTag(Static.DIALOG_CONFIRM_PHOTO_DELETE);
            if (dialog1 != null) {
                setPhotoDeleteConfirmDialogListener(dialog1);
            }
        }
    }

    private boolean getEntryData() {
        // Get primary photo uid
        Cursor entryCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleRowCursorByUid(Tables.TABLE_ENTRIES, entryUid);
        if (entryCursor != null && entryCursor.getCount() > 0) {
            primaryPhotoUid = entryCursor.getString(entryCursor.getColumnIndex(Tables.KEY_ENTRY_PRIMARY_PHOTO_UID));

            entryPhotosArrayList = AttachmentsStatic.getEntryAttachmentsArrayList(entryUid, PHOTO);
            photoPagerAdapter.setEntryPhotosArrayList(entryPhotosArrayList);

            // Update photo number in action bar
            updatePhotoNumber();

            entryCursor.close();
            return true;
        }
        entryCursor.close();
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_photo_pager, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        showHideMenuIcons(menu);

        return super.onPrepareOptionsMenu(menu);
    }

    private void showHideMenuIcons(Menu menu) {
        boolean hasPhotos = entryPhotosArrayList.size() > 0;
        // 'Free up device storage' pref

        menu.findItem(R.id.item_show_grid).setVisible(hasPhotos);
        menu.findItem(R.id.item_set_as_primary).setVisible(hasPhotos);
        menu.findItem(R.id.item_edit).setVisible(hasPhotos );
        menu.findItem(R.id.item_rotate_left).setVisible(hasPhotos );
        menu.findItem(R.id.item_rotate_right).setVisible(hasPhotos);
        menu.findItem(R.id.item_details).setVisible(hasPhotos );
        menu.findItem(R.id.item_share).setVisible(hasPhotos );
        menu.findItem(R.id.item_delete).setVisible(hasPhotos);

        if (!hasPhotos) {
            return;
        }

        boolean isPrimary = false;
        if (entryPhotosArrayList.size() >= pager.getCurrentItem()) {
            isPrimary = primaryPhotoUid.equals(entryPhotosArrayList.get(pager.getCurrentItem()).uid);
        }

        menu.findItem(R.id.item_set_as_primary).setEnabled(!isPrimary);

        if (isPrimary) {
            menu.findItem(R.id.item_set_as_primary).setIcon(R.drawable.ic_ok_white_disabled_24dp);
        } else {
            menu.findItem(R.id.item_set_as_primary).setIcon(R.drawable.ic_ok_white_24dp);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AppLog.d("item: " + item);
        if (activityState.isActivityPaused) {
            return true;
        }

        // Handle presses on the action bar items
        int itemId = item.getItemId();

        // Back
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }

        // Show grid
        else if (itemId == R.id.item_show_grid) {
            if (!openedFromPhotoGrid) {
                // Show all photos grid in PhotoGridActivity
                Intent intent = new Intent(PhotoPagerActivity.this, PhotoGridActivity.class);
                intent.putExtra(Static.EXTRA_SKIP_SC, true);
                intent.putExtra("entryUid", entryUid);
                startActivityForResult(intent, Static.REQUEST_PHOTO_GRID);
            }
            finish();
            return true;
        }

        // Set as primary
        else if (itemId == R.id.item_set_as_primary) {
            String photoUid = entryPhotosArrayList.get(pager.getCurrentItem()).uid;
            AttachmentsStatic.setPhotoAsPrimary(findViewById(R.id.layout_container), photoUid, entryUid);
            primaryPhotoUid = photoUid;
            supportInvalidateOptionsMenu();
            return true;
        }

        //Edit
        else if (itemId == R.id.item_edit) {
            activityState.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_PHOTO_EDIT);

            AttachmentInfo attachmentInfo = entryPhotosArrayList.get(pager.getCurrentItem());
            Uri uri = Uri.fromFile(new File(attachmentInfo.getFilePath()));

            Intent intent = new Intent(this, ImageEditorActivity.class);
            intent.putExtra(PhotoPicker.KEY_SELECTED_PHOTOS, uri.getPath());
            startActivityForResult(intent, ImageEditorActivity.REQUEST_IMG_EDIT);
            return true;
        }

        // Photo details
        else if (itemId == R.id.item_rotate_left) {
            activityState.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_PHOTO_ROTATE);
            rotatePhotoInBackground(-90);
            return true;
        }

        // Photo details
        else if (itemId == R.id.item_rotate_right) {
            activityState.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_PHOTO_ROTATE);
            rotatePhotoInBackground(90);
            return true;
        }

        // Photo details
        else if (itemId == R.id.item_details) {
            activityState.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_PHOTO_DETAILS_VIEW);
            showPhotoDetailsDialog();
            return true;
        }

        // Share photo
        else if (itemId == R.id.item_share) {
            sharePhoto();
            return true;
        }

        // Delete
        else if (itemId == R.id.item_delete) {
            showDeletePhotoConfirmation();
            return true;
        }

        else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void rotatePhotoInBackground(int degree) {
        AttachmentInfo attachmentInfo = entryPhotosArrayList.get(pager.getCurrentItem());
        if (new File(attachmentInfo.getFilePath()).exists()) {
            RotatePhotoAsync rotatePhotoAsync = new RotatePhotoAsync( attachmentInfo, degree);
            rotatePhotoAsync.execute();
        } else {
            Static.showToast(getString(R.string.file_not_found), Toast.LENGTH_SHORT);
        }
    }

    public int getPhotoPosition(AttachmentInfo attachmentInfo) {
        for (int i = 0; i < entryPhotosArrayList.size(); i++) {
            if (entryPhotosArrayList.get(i).uid.equals(attachmentInfo.uid)) {
                return i;
            }
        }

        return 0;
    }

    private void updatePhotoNumber() {
        String text = "";
        int photoCount = entryPhotosArrayList.size();
        AppLog.d("dayPhotoCount: " + photoCount);

        if (photoCount > 0) {
            int photoPos = pager.getCurrentItem() + 1;
            text = photoPos + "/" + photoCount;
            pager.setVisibility(View.VISIBLE);
            noPhotosFoundTextView.setVisibility(View.GONE);
        } else {
            pager.setVisibility(View.GONE);
            noPhotosFoundTextView.setVisibility(View.VISIBLE);
        }
        photoNumberTextView.setText(text);

        // Redraw action bar to show/hide delete icon
        supportInvalidateOptionsMenu();
    }

    private void showDeletePhotoConfirmation() {
        String dialogTag = Static.DIALOG_CONFIRM_PHOTO_DELETE;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setTitle(getString(R.string.delete));
            dialog.setMessage(getString(R.string.delete_selected_photo));
            dialog.show(getSupportFragmentManager(), dialogTag);

            // Set dialog listener
            setPhotoDeleteConfirmDialogListener(dialog);
        }
    }

    private void setPhotoDeleteConfirmDialogListener(final ConfirmDialog dialog) {
        dialog.setDialogPositiveClickListener(() -> {
            deletePhoto();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        MyApp.getInstance().storageMgr.removeOnStorageDataChangeListener(this);
    }

    private void deletePhoto() {
        try {
            ArrayList<AttachmentInfo> photosToDeleteArrayList = new ArrayList<>();
            AttachmentInfo o = entryPhotosArrayList.get(pager.getCurrentItem());
            photosToDeleteArrayList.add(o);

            AttachmentsStatic.deleteAttachments(photosToDeleteArrayList);

            // Clear entry primary photo uid field if this was entry primary photo
            EntriesStatic.clearEntryPrimaryPhotoUidOnPhotoDelete(entryUid, o.uid);

            // Remove photo from the pager
            entryPhotosArrayList.remove(pager.getCurrentItem());
            photoPagerAdapter.notifyDataSetChanged();

            // Update photo number in action bar
            updatePhotoNumber();
        } catch (Exception e) {
            // Show error toast
            Static.showToast(String.format("%s: %s", getString(R.string.error), e.getMessage()), Toast.LENGTH_SHORT);
        }
    }

    public void sharePhoto() {
        if (activityState.isActivityPaused) {
            return;
        }

        AttachmentInfo photoAttachmentInfo = entryPhotosArrayList.get(pager.getCurrentItem());
        if (new File(photoAttachmentInfo.getFilePath()).exists()) {

            Intent intent = new Intent();

            Uri uri;

            try {
                uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", new File(photoAttachmentInfo.getFilePath()));
            } catch (Exception e) {

                //TODO : fix & log message
               // Fatal Exception: android.os.FileUriExposedException
               // file:///storage/0123-4567/Android/data/com.pixelcrater.Diaro/files/media/photo/photo_20181215_839148.jpg exposed beyond app through ClipData.Item.getUri()
                //com.pixelcrater.Diaro.entries.viewedit.PhotoPagerActivity.sharePhoto

                if (Build.VERSION.SDK_INT >= 24) {
                    try {
                        Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                        m.invoke(null);
                    } catch (Exception ee) {
                        ee.printStackTrace();
                    }
                }
                uri = Uri.fromFile(new File(photoAttachmentInfo.getFilePath()));

            }

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            } else {
                List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    AppLog.d("packageName: " + packageName);
                    grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            }

            intent.setAction(Intent.ACTION_SEND);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_STREAM, uri);

            startActivityForResult(Intent.createChooser(intent, getText(R.string.app_title) + " - " + getText(R.string.share)), Static.REQUEST_SHARE_PHOTO);

        } else {
            Static.showToast(getString(R.string.file_not_found), Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onStorageDataChange() {
        int oldPos = 0;
        AttachmentInfo attachmentInfo = null;

        if (entryPhotosArrayList.size() > 0) {
            oldPos = pager.getCurrentItem();
            attachmentInfo = entryPhotosArrayList.get(oldPos);
        }

        if (!getEntryData()) {
            finish();
            return;
        }

        if (entryPhotosArrayList.size() > 0 && attachmentInfo != null) {
            // Go to viewed photo
            int newPos = getPhotoPosition(attachmentInfo);
            if (newPos != oldPos) {
                pager.setCurrentItem(newPos, false);
            }
        }
    }

    private void showPhotoDetailsDialog() {
        String dialogTag = Static.DIALOG_PHOTO_DETAILS;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            String filePath = entryPhotosArrayList.get(pager.getCurrentItem()).getFilePath();
            // Show dialog
            PhotoDetailsDialog dialog = new PhotoDetailsDialog();
            dialog.setPhotoFilePath(filePath);
            dialog.show(getSupportFragmentManager(), dialogTag);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
          super.onActivityResult(requestCode, resultCode, data);

          AppLog.e("onActivityResult" + requestCode +" , " + resultCode + data );

        if (resultCode == RESULT_OK && requestCode == ImageEditorActivity.REQUEST_IMG_EDIT) {
           // final Uri resultUri = UCrop.getOutput(data);

            String photoPath = data.getStringExtra("path");
            Log.e("photoPath" , photoPath);

            photoPagerAdapter.notifyDataSetChanged();
        }
    }
}
