package com.pixelcrater.Diaro.profile;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.activitytypes.TypeBillingActivity;
import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.generaldialogs.ConfirmDialog;
import com.pixelcrater.Diaro.premium.billing.BillingUpdateListener;
import com.pixelcrater.Diaro.premium.billing.PaymentUtils;
import com.pixelcrater.Diaro.storage.SyncStatic;
import com.pixelcrater.Diaro.storage.dropbox.DbxUserInfo;
import com.pixelcrater.Diaro.storage.dropbox.DropboxAccountManager;
import com.pixelcrater.Diaro.storage.dropbox.DropboxLocalHelper;
import com.pixelcrater.Diaro.storage.dropbox.OnFsSyncStatusChangeListener;
import com.pixelcrater.Diaro.storage.dropbox.SyncService;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;
import com.pixelcrater.Diaro.utils.storage.StorageUtils;
import com.sandstorm.diary.piceditor.features.crop.CropDialogFragment;
import com.yalantis.ucrop.UCrop;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import cn.nekocode.badge.BadgeDrawable;


public class ProfileActivity extends TypeBillingActivity implements OnClickListener, OnFsSyncStatusChangeListener, BillingUpdateListener, CropDialogFragment.OnCropPhoto {

    private static final boolean SHOW_SYNC_DEBUG_INFO = false;

    private File profilePhotoFile;
    private TextView signedInUserEmailTextView;
    private TextView dropboxEmailTextView;
    private AppCompatButton dropboxConnectButton;
    private ImageButton dropboxDisconnectImageButton;
    private ImageButton userSignOutImageButton;
    private TextView wouldLikeToSyncTextView;
    private TextView getProLink;
    private View profilePhotoContainer;
    private ImageView profilePhotoView;
    private ViewGroup syncStatusContainer;
    private TextView fsSyncStatusTextView;
    private TextView syncErrorTextView;
    private ImageView syncIconImageView;
    private TextView debugFsInfoTextView;
    private ViewGroup whyToLinkWithDropboxViewGroup;
    private Animation rotateAnim;
    private HashMap<String, ProductDetails> mProductsMap = new HashMap<>();
    // *** Broadcast Receiver ***
    private BroadcastReceiver brReceiver = new BrReceiver();

    private final int CROP_REQUEST = 1001;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(addViewToContentContainer(R.layout.profile_responsive));
        activityState.setLayoutBackground();
        activityState.setActionBarTitle(getSupportActionBar(), getString(R.string.profile));

        // Separators
        View separator1 = findViewById(R.id.separator_1);
        View separator2 = findViewById(R.id.separator_2);
        separator1.setBackgroundColor(Color.parseColor(MyThemesUtils.getSeparatorColor()));
        separator2.setBackgroundColor(Color.parseColor(MyThemesUtils.getSeparatorColor()));

        signedInUserEmailTextView = (TextView) findViewById(R.id.signed_in_user_email);

        userSignOutImageButton = (ImageButton) findViewById(R.id.user_sign_out_button);
        userSignOutImageButton.setImageDrawable(getResources().getDrawable(MyThemesUtils.getDrawableResId("ic_close_%s_18dp")));
        userSignOutImageButton.setOnClickListener(this);

        dropboxEmailTextView = (TextView) findViewById(R.id.dropbox_email);
        dropboxEmailTextView.setCompoundDrawablesWithIntrinsicBounds(MyThemesUtils.getDrawableResId("ic_dropbox_%s_24dp"), 0, 0, 0);

        dropboxConnectButton = findViewById(R.id.dropbox_connect_button);
        dropboxConnectButton.setOnClickListener(this);

        dropboxDisconnectImageButton = (ImageButton) findViewById(R.id.dropbox_disconnect_button);
        dropboxDisconnectImageButton.setImageDrawable(getResources().getDrawable(MyThemesUtils.getDrawableResId("ic_close_%s_18dp")));
        dropboxDisconnectImageButton.setOnClickListener(this);

        // Sync status
        syncStatusContainer = (ViewGroup) findViewById(R.id.sync_status_container);
        syncStatusContainer.setOnClickListener(this);

        syncStatusContainer.setOnLongClickListener(v -> {
            new AlertDialog.Builder(ProfileActivity.this)
                    .setTitle("Reset Sync IDs?")
                    .setMessage("Are you sure you want to force sync?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        // clear the cursors in case of manual force sync
                        try {
                            Static.uploadDatabaseToDropbox();
                        } catch (Exception e) {
                            AppLog.e("Exception: " + e);
                        }
                        DropboxLocalHelper.clearAllFolderCursors();
                        MyApp.getInstance().storageMgr.getSQLiteAdapter().resetAllTablesSyncedField();
                        restartSync();
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
            return true;
        });

        ((TextView) findViewById(R.id.fs_sync_status_title)).setText(String.format("%s: ", getString(R.string.sync_status)));
        fsSyncStatusTextView = (TextView) findViewById(R.id.fs_sync_status);
        debugFsInfoTextView = (TextView) findViewById(R.id.debug_fs_info);

        // Sync error message
        syncErrorTextView = (TextView) findViewById(R.id.sync_error);

        // Sync icon
        syncIconImageView = (ImageView) findViewById(R.id.sync_icon);
        syncIconImageView.setImageDrawable(getResources().getDrawable(MyThemesUtils.getDrawableResId("ic_sync_%s_24dp")));
        rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate360_anim);

        // Debug
        if (SHOW_SYNC_DEBUG_INFO) {
            debugFsInfoTextView.setVisibility(View.VISIBLE);
        }

        // Why to link with Dropbox
        whyToLinkWithDropboxViewGroup = (ViewGroup) findViewById(R.id.why_to_link_with_dropbox);

        ((ImageView) findViewById(R.id.cloud_sync_icon)).setImageResource(MyThemesUtils.getDrawableResId("ic_cloud_sync_%s_36dp"));
        ((ImageView) findViewById(R.id.cloud_backup_restore_icon)).setImageResource(MyThemesUtils.getDrawableResId("ic_cloud_backup_restore_%s_36dp"));
        ((ImageView) findViewById(R.id.cloud_profile_photo_icon)).setImageResource(MyThemesUtils.getDrawableResId("ic_cloud_profile_photo_%s_36dp"));

        // Profile photo
        profilePhotoContainer = findViewById(R.id.profile_photo_container);

        View profilePhotoClickArea = findViewById(R.id.profile_photo_click_area);
        profilePhotoClickArea.setOnClickListener(this);

        profilePhotoView = (ImageView) findViewById(R.id.profile_photo);

        profilePhotoFile = new File(AppLifetimeStorageUtils.getProfilePhotoFilePath());

        wouldLikeToSyncTextView = (TextView) findViewById(R.id.would_like_to_sync);
        getProLink = (TextView) findViewById(R.id.get_pro_version_link);
        getProLink.setOnClickListener(v -> {
            // Go to get PRO activity
            Static.startProActivity(ProfileActivity.this, activityState);
        });

        if (!Static.isProUser()) {
            MyApp.getInstance().asyncsMgr.executeCheckProAsync(MyApp.getInstance().userMgr.getSignedInEmail());
        }

        setBillingHandler(this);

        // Register broadcast receiver
        ContextCompat.registerReceiver(this,brReceiver, new IntentFilter(Static.BR_IN_PROFILE), ContextCompat.RECEIVER_EXPORTED);

        // Restore active dialog listeners
        restoreDialogListeners(savedInstanceState);
    }

    private void restoreDialogListeners(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            ConfirmDialog dialog1 = (ConfirmDialog) getSupportFragmentManager().findFragmentByTag(Static.DIALOG_SIGN_OUT);
            if (dialog1 != null) {
                setSignOutConfirmDialogListener(dialog1);
            }

            ConfirmDialog dialog2 = (ConfirmDialog) getSupportFragmentManager().findFragmentByTag(Static.DIALOG_CONFIRM_DROPBOX_UNLINK);
            if (dialog2 != null) {
                setDropboxUnlinkConfirmDialogListener(dialog2);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (activityState.isActivityPaused) {
            return true;
        }
        // Back
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(brReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeDbxListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        addDbxListeners();

        // Check for Dropbox OAuth completion and capture UID
        // This is called after Auth.startOAuth2Authentication() completes
        String uid = com.dropbox.core.android.Auth.getUid();
        if (uid != null) {
            PreferenceManager.getDefaultSharedPreferences(this)
                .edit().putString(DropboxAccountManager.PREF_DROPBOX_UID_V1, uid).apply();
            AppLog.e("Dropbox UID V1 saved in onResume: " + uid);
        }

        DropboxLocalHelper.checkDropboxToken(this);
        updateUi();
        updateProfilePhoto();
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        // Profile photo click area
        if (viewId == R.id.profile_photo_click_area) {
            if (DropboxAccountManager.isLoggedIn(this)) {
                if (profilePhotoFile.exists()) {
                    startProfilePhotoActivity();
                } else {
                    showPhotoChooser();
                }
            }
        }

        // Sign out
        else if (viewId == R.id.user_sign_out_button) {
            showSignOutConfirmDialog();
        }

        // Dropbox connect
        else if (viewId == R.id.dropbox_connect_button) {
            if (!DropboxAccountManager.isLoggedIn(this)) {
                // Start Dropbox authentication
                DropboxAccountManager.link(this);
            }
        }

        // Dropbox unlink
        else if (viewId == R.id.dropbox_disconnect_button) {
            showDropboxUnlinkConfirmDialog();
        }

        // Sync container
        else if (viewId == R.id.sync_status_container) {
            DropboxLocalHelper.clearAllFolderCursors();
            restartSync();
        }
    }

    private void restartSync() {
        if (MyApp.getInstance().networkStateMgr.isConnectedToInternet() && MyApp.getInstance().storageMgr.isStorageDropbox()) {
            if (!MyApp.getInstance().asyncsMgr.isSyncAsyncRunning()) {
                SyncService.startService();
            } else {
                // Cancel sync and start again
                MyApp.getInstance().asyncsMgr.cancelSyncAsync();

                final Handler handler = new Handler();
                handler.postDelayed(SyncService::startService, 200);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            // Result from Dropbox link window
            case Static.REQUEST_LINK_TO_DBX:
                if (resultCode == Activity.RESULT_OK) {
                    // Reset sync status
                    MyApp.getInstance().storageMgr.getSQLiteAdapter().resetAllTablesSyncedField();

                    updateUi();
                    updateProfilePhoto();

                    Static.sendBroadcast(Static.BR_IN_MAIN, Static.DO_UPDATE_PROFILE_PHOTO, null);

                }
                break;

            // Result from photo select app
            case Static.REQUEST_SELECT_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    /** List<AspectRatio> ignoredAspectRations  =   Arrays.asList(AspectRatio.ASPECT_INS_STORY, AspectRatio.ASPECT_FACE_POST, AspectRatio.ASPECT_FACE_COVER, AspectRatio.ASPECT_PIN_POST, AspectRatio.ASPECT_YOU_COVER, AspectRatio.ASPECT_TWIT_POST, AspectRatio.ASPECT_TWIT_HEADER);
                     CropRequest cropRequest = new Auto(uri, CROP_REQUEST, StorageType.CACHE, ignoredAspectRations, new CroppyTheme(R.color.blue));
                     Croppy.INSTANCE.start(this, cropRequest);**/


                    String photoPath = Static.getPhotoFilePathFromUri(uri);
                    File photoFile = new File(photoPath);
                    Uri uriPAth = Uri.fromFile(photoFile);

                    UCrop.Options opts = new UCrop.Options();

                    UCrop.of(uriPAth, uriPAth)
                            .withMaxResultSize(512, 512)
                            .withOptions(opts)
                            .start(ProfileActivity.this);
                }
                break;

            case CROP_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        finishCrop(bitmap);
                    } catch (IOException e) {
                        Static.showToastError(String.format("%s:", e.getMessage()));
                    }
                }
                break;

            case UCrop.REQUEST_CROP:
                if (resultCode == Activity.RESULT_OK) {
                    final Uri uri = UCrop.getOutput(data);

                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        finishCrop(bitmap);
                    } catch (IOException e) {
                        Static.showToastError(String.format("%s:", e.getMessage()));
                    }
                }
        }
    }

    @Override
    public void finishCrop(Bitmap bitmap) {

        try {
            File tmpProfileImg = new File(getExternalCacheDir() + "/profileimage.jpg");
            FileOutputStream out = new FileOutputStream(tmpProfileImg);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Static.copyFileOrDirectory(tmpProfileImg, profilePhotoFile);
            StorageUtils.compressPhoto(profilePhotoFile.getPath(), 1024, 100);
            updateProfilePhoto();
            Static.sendBroadcast(Static.BR_IN_MAIN, Static.DO_UPDATE_PROFILE_PHOTO, null);
        } catch (Exception e) {
            // Show error toast
            Static.showToastError(String.format("%s: %s", getString(R.string.error_add_profile_photo), e.getMessage()));
        }
    }

    private void startProfilePhotoActivity() {
        if (activityState.isActivityPaused) {
            return;
        }

        Intent intent = new Intent(ProfileActivity.this, ProfilePhotoActivity.class);
        intent.putExtra(Static.EXTRA_SKIP_SC, true);
        startActivityForResult(intent, Static.REQUEST_PROFILE_PHOTO);
    }

    public void showPhotoChooser() {
        if (activityState.isActivityPaused) {
            return;
        }

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getText(R.string.select_photo)), Static.REQUEST_SELECT_PHOTO);
    }

    private void showDropboxUnlinkConfirmDialog() {
        String dialogTag = Static.DIALOG_CONFIRM_DROPBOX_UNLINK;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setTitle(getString(R.string.unlink));
            dialog.setMessage(getString(R.string.settings_disconnect_warning));
            dialog.show(getSupportFragmentManager(), dialogTag);
            setDropboxUnlinkConfirmDialogListener(dialog);
        }
    }

    private void setDropboxUnlinkConfirmDialogListener(ConfirmDialog dialog) {
        dialog.setDialogPositiveClickListener(() -> {
            // Cancel sync
            MyApp.getInstance().asyncsMgr.cancelSyncAsync();

            unlinkFromDropboxInBackground();
        });
    }

    private void unlinkFromDropboxInBackground() {
        if (DropboxAccountManager.isLoggedIn(this)) {
            MyApp.executeInBackground(() -> {
                // Unlink Dropbox
                DropboxAccountManager.unlink(ProfileActivity.this);

                runOnUiThread(() -> {
                    if (!activityState.isActivityPaused) {
                        updateUi();
                    }
                });
            });
        } else {
            updateUi();
        }

    }

    public void showSignOutConfirmDialog() {
        AppLog.d("");

        String dialogTag = Static.DIALOG_SIGN_OUT;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setTitle(getString(R.string.sign_out));
            dialog.setMessage(getString(R.string.sign_out_confirm));
            dialog.show(getSupportFragmentManager(), dialogTag);
            setSignOutConfirmDialogListener(dialog);
        }
    }

    private void setSignOutConfirmDialogListener(final ConfirmDialog dialog) {
        dialog.setDialogPositiveClickListener(() -> {
            if (!MyApp.getInstance().networkStateMgr.isConnectedToInternet()) {
                // Show error toast
                Static.showToastError(getString(R.string.error_internet_connection));
                return;
            }

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
            GoogleSignInClient mGoogleApiClient = GoogleSignIn.getClient(this, gso);
            mGoogleApiClient.signOut();

            // Unset signed in user
            MyApp.getInstance().userMgr.setSignedInUser(null, null);

            // Cancel sync
            MyApp.getInstance().asyncsMgr.cancelSyncAsync();

            Static.turnOffPro();
            Static.turnOffSubscribedCurrently();

            unlinkFromDropboxInBackground();
        });
    }

    private void updateUi() {
        AppLog.d("Static.isPremiumUser(): " + Static.isProUser());

        // Finish if not signed in
        if (!MyApp.getInstance().userMgr.isSignedIn()) {
            setResult(RESULT_OK);
            finish();
            return;
        }
        AppLog.d("MyApp.getInstance().userMgr.getSignedInAccountType(): " + MyApp.getInstance().userMgr.getSignedInAccountType());
        // Signed in user icon
        if (StringUtils.equals(MyApp.getInstance().userMgr.getSignedInAccountType(), UserMgr.SIGNED_IN_WITH_GOOGLE)) {
            signedInUserEmailTextView.setCompoundDrawablesWithIntrinsicBounds(MyThemesUtils.getDrawableResId("ic_google_logo_%s_24dp"), 0, 0, 0);
        } else {
            signedInUserEmailTextView.setCompoundDrawablesWithIntrinsicBounds(MyThemesUtils.getDrawableResId("ic_diaro_icon_%s_24dp"), 0, 0, 0);
        }

        BadgeDrawable drawable = new BadgeDrawable.Builder().type(BadgeDrawable.TYPE_ONLY_ONE_TEXT).badgeColor(MyThemesUtils.getAccentColor()).build();
        if (Static.isProUser()) {
            String text = "PRO";
            if (Static.isSubscribedCurrently())
                text = "PREMIUM";
            drawable.setText1(text);
        } else {
            drawable.setText1("BASIC");
        }

        SpannableString spannableString = new SpannableString(TextUtils.concat(MyApp.getInstance().userMgr.getSignedInEmail(), "  ", drawable.toSpannable(), " "));

        signedInUserEmailTextView.setText(spannableString);
        dropboxDisconnectImageButton.setVisibility(View.GONE);

        if (Static.isProUser()) {
            wouldLikeToSyncTextView.setVisibility(View.GONE);
            getProLink.setVisibility(View.GONE);
            // Hide Diaro PRO badge
            findViewById(R.id.pro_version_badge).setVisibility(View.GONE);
        } else {
            wouldLikeToSyncTextView.setVisibility(View.VISIBLE);
            getProLink.setVisibility(View.VISIBLE);
        }
        if (DropboxAccountManager.isLoggedIn(this)) {
            dropboxConnectButton.setVisibility(View.GONE);
            dropboxEmailTextView.setVisibility(View.VISIBLE);

            String dbxEmail = PreferenceManager.getDefaultSharedPreferences(this).getString(DropboxAccountManager.PREF_DROPBOX_EMAIL, "");
            dropboxEmailTextView.setText(dbxEmail);

            if (MyApp.getInstance().networkStateMgr.isConnectedToInternet()) {
                new DropboxAccountManager.GetCurrentUserAsyncTask(this) {
                    @Override
                    protected void onPostExecute(DbxUserInfo dbxUserInfo) {
                        super.onPostExecute(dbxUserInfo);
                        if (dbxUserInfo != null) {
                            dropboxDisconnectImageButton.setVisibility(View.VISIBLE);
                            dropboxEmailTextView.setText(dbxUserInfo.getFullaccount().getEmail() + "\n " + dbxUserInfo.getUsageInfo() + " used");

                        } else {
                            //
                            Log.e("DropboxAccountManager", "we had a invalid token, we need to relink " + DropboxAccountManager.getToken(ProfileActivity.this));
                            DropboxLocalHelper.checkDropboxToken(ProfileActivity.this);
                        }
                    }
                }.execute();

            }
            profilePhotoContainer.setVisibility(View.VISIBLE);
            whyToLinkWithDropboxViewGroup.setVisibility(View.GONE);
        } else {
            dropboxConnectButton.setVisibility(View.VISIBLE);
            dropboxEmailTextView.setVisibility(View.GONE);
            profilePhotoContainer.setVisibility(View.GONE);
            whyToLinkWithDropboxViewGroup.setVisibility(View.VISIBLE);
        }

        if (MyApp.getInstance().storageMgr.isStorageDropbox()) {
            syncStatusContainer.setVisibility(View.VISIBLE);
            updateFsSyncStatus();
        } else {
            syncStatusContainer.setVisibility(View.GONE);
        }
    }

    private void updateFsSyncStatus() {

        if (!MyApp.getInstance().storageMgr.isStorageDropbox()) {
            return;
        }

        // FS sync status
        int fsSyncStatus = SyncStatic.getFsSyncStatus();

        int fsSyncStatusColorResId = SyncStatic.getSyncStatusColorResId(fsSyncStatus);
        fsSyncStatusTextView.setText(SyncStatic.getFsSyncStatusText(fsSyncStatus));
        fsSyncStatusTextView.setTextColor(getResources().getColor(fsSyncStatusColorResId));

        if (fsSyncStatus == SyncStatic.STATUS_SYNCING) {
            if (!rotateAnim.hasStarted() || rotateAnim.hasEnded()) {
                // Rotate sync icon
                syncIconImageView.startAnimation(rotateAnim);
            }
        } else {
            // Stop rotate animation
            rotateAnim.cancel();
        }

        if (MyApp.getInstance().asyncsMgr.syncAsync != null && MyApp.getInstance().asyncsMgr.syncAsync.errorMessage != null) {
            syncErrorTextView.setText(MyApp.getInstance().asyncsMgr.syncAsync.errorMessage);
            syncErrorTextView.setVisibility(View.VISIBLE);
        } else {
            syncErrorTextView.setText("");
            syncErrorTextView.setVisibility(View.GONE);
        }

        // Debug info
        if (SHOW_SYNC_DEBUG_INFO) {
            String fsStatus = "Not supported";
            debugFsInfoTextView.setText(String.format("FS status: %s", fsStatus));
        }
    }

    public void updateProfilePhoto() {
        DropboxLocalHelper.setProfilePicChanged(true);

        if (MyApp.getInstance().networkStateMgr.isConnectedToInternet() && DropboxAccountManager.isLoggedIn(this)) {
            // Start sync service
            SyncService.startService();
        }

        if (DropboxAccountManager.isLoggedIn(this) && profilePhotoFile.exists() && profilePhotoFile.length() > 0) {
            // Show photo
            Glide.with(ProfileActivity.this).load(profilePhotoFile).signature(Static.getGlideSignature(profilePhotoFile)).centerCrop().error(R.drawable.ic_photo_red_24dp).into(profilePhotoView);
        } else {
            profilePhotoView.setImageResource(R.drawable.ic_profile_no_padding_white_disabled_48dp);
        }
    }

    @Override
    public void onFsSyncStatusChange() {
        runOnUiThread(this::updateFsSyncStatus);
    }

    private void addDbxListeners() {
        if (DropboxAccountManager.isLoggedIn(this)) {
            if (MyApp.getInstance().storageMgr.getDbxFsAdapter() != null) {
                MyApp.getInstance().storageMgr.getDbxFsAdapter().addOnFsSyncStatusChangeListener(this);
            }
        } else {
            removeDbxListeners();
        }
    }

    private void removeDbxListeners() {
        if (DropboxAccountManager.isLoggedIn(this)) {
            if (MyApp.getInstance().storageMgr.getDbxFsAdapter() != null) {
                MyApp.getInstance().storageMgr.getDbxFsAdapter().removeOnFsSyncStatusChangeListener(this);
            }
        }
    }

    @Override
    public void onBillingInitialized() {

        querySkuDetails(GlobalConstants.activeSubscriptionsList, BillingClient.SkuType.SUBS);

        queryPurchases(BillingClient.SkuType.SUBS);
    }

    @Override
    public void onBillingUnavailable(String debugMessage, int responseCode) {

    }

    @Override
    public void onAvailableProductsResponse(List<ProductDetails> availableProductsSkuList) {
        AppLog.e("Available products count -> " + availableProductsSkuList.size());

        for (ProductDetails productDetails : availableProductsSkuList) {
            AppLog.e("Product -> " + productDetails);
            String productId = productDetails.getProductId();
            if (mProductsMap.get(productId) == null) {
                mProductsMap.put(productId, productDetails);
            }

        }
    }

    @Override
    public void onOwnedProductsResponse(List<Purchase> ownedProductsList) {
        AppLog.e("Owned products count -> " + ownedProductsList.size());
        processPurchases(ownedProductsList);
    }

    @Override
    public void onPurchase(List<Purchase> purchasesList) {
    }

    private void processPurchases(@NonNull List<Purchase> purchasesList) {
        if (purchasesList.size() > 0) {
            // PURCHASES FOUND
            for (Purchase purchase : purchasesList) {
                AppLog.e(purchase.getProducts().get(0) + " -> " + purchase);

                ProductDetails productDetails = mProductsMap.get(purchase.getProducts().get(0));

                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    // Acknowledge the purchase if it hasn't already been acknowledged.
                    if (!purchase.isAcknowledged()) {
                        acknowledgePurchase(purchase.getPurchaseToken());
                    }
                }

                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    // Turn ON PRO
                    Static.turnOnSubscribedCurrently();

                    //  If signed in Send payment transaction information (purchased or canceled/refunded) to API
                    if (MyApp.getInstance().userMgr.isSignedIn()) {
                        PaymentUtils.sendGoogleInAppPaymentToAPI(purchase, productDetails);
                    }
                } else {
                    if (MyApp.getInstance().userMgr.isSignedIn()) {
                        MyApp.getInstance().asyncsMgr.executeCheckProAsync(MyApp.getInstance().userMgr.getSignedInEmail());
                    } else {
                        Static.turnOffSubscribedCurrently();
                    }
                }
            }


        } else {
            // NO PURCHASES FOUND


            // Do not turn of google play nbo
        }

    }


    private class BrReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String doWhat = intent.getStringExtra(Static.BROADCAST_DO);
            AppLog.d("doWhat: " + doWhat);

            // - Update UI -
            if (doWhat.equals(Static.DO_UPDATE_UI)) {
                updateUi();
            }
            // - Update profile photo -
            else if (doWhat.equals(Static.DO_UPDATE_PROFILE_PHOTO)) {
                updateProfilePhoto();
            }
        }
    }
}
