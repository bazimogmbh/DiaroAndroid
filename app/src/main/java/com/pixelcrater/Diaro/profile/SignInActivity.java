package com.pixelcrater.Diaro.profile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.activitytypes.TypeActivity;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyDevice;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.Static;

public class SignInActivity extends TypeActivity implements OnClickListener {

    protected GoogleSignInClient mGoogleApiClient;
    private BroadcastReceiver brReceiver = new BrReceiver();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(addViewToContentContainer(R.layout.sign_in_responsive));
        activityState.setLayoutBackground();
        activityState.setActionBarTitle(getSupportActionBar(), getString(R.string.sign_in));

        // Handle bottom insets for edge-to-edge on Android 15+
        applyBottomInsets(findViewById(R.id.layout_container));

        // Sign in with Diaro account button
        AppCompatButton signInWithDiaroAccountButton = findViewById(R.id.sign_in_with_diaro_account_button);
        signInWithDiaroAccountButton.setTransformationMethod(null);
        signInWithDiaroAccountButton.setOnClickListener(this);

        // Sign in with Google button
        AppCompatButton signInWithGoogleButton = findViewById(R.id.sign_in_with_google_button);
        signInWithGoogleButton.setTransformationMethod(null);
        signInWithGoogleButton.setOnClickListener(this);

        // Check if Google Play Services available on the device
        if (MyDevice.getInstance().isGooglePlayServicesAvailable()) {
            signInWithGoogleButton.setVisibility(View.VISIBLE);

            // Configure sign-in to request the user's ID, email address, and basic
            // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();

            // Build a GoogleApiClient with access to the Google Sign-In API and the
            // options specified by gso.
            mGoogleApiClient = GoogleSignIn.getClient(this, gso);
        }

        // Sign up link
        View signUpLink = findViewById(R.id.sign_up_link);
        signUpLink.setOnClickListener(arg0 -> showSignUpDialog());

        ((ImageView) findViewById(R.id.cloud_backup_restore_icon)).setImageResource(MyThemesUtils.getDrawableResId("ic_cloud_backup_restore_%s_36dp"));
        ((ImageView) findViewById(R.id.cloud_profile_photo_icon)).setImageResource(MyThemesUtils.getDrawableResId("ic_cloud_profile_photo_%s_36dp"));
        ((ImageView) findViewById(R.id.diaro_pro_on_all_devices_icon)).setImageResource(MyThemesUtils.getDrawableResId("ic_multiple_devices_%s_36dp"));

        // Register broadcast receiver
        ContextCompat.registerReceiver(this,brReceiver, new IntentFilter(Static.BR_IN_SIGN_IN), ContextCompat.RECEIVER_EXPORTED);

        // Restore please wait dialogs of active AsyncTasks
        restorePleaseWaitDialogs();

        // Restore active dialog listeners
        restoreDialogListeners(savedInstanceState);
    }

    private void restorePleaseWaitDialogs() {
        if (MyApp.getInstance().asyncsMgr.isAsyncRunning(MyApp.getInstance().asyncsMgr.signInAsync)) {
            MyApp.getInstance().asyncsMgr.signInAsync.showPleaseWaitDialog(SignInActivity.this);
        }

        if (MyApp.getInstance().asyncsMgr.isAsyncRunning(MyApp.getInstance().asyncsMgr.signUpAsync)) {
            MyApp.getInstance().asyncsMgr.signUpAsync.showPleaseWaitDialog(SignInActivity.this);
        }
    }

    private void restoreDialogListeners(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            SignInDialog dialog1 = (SignInDialog) getSupportFragmentManager().findFragmentByTag(Static.DIALOG_SIGN_IN);
            if (dialog1 != null) setSignInDialogListener(dialog1);

            SignUpDialog dialog2 = (SignUpDialog) getSupportFragmentManager().findFragmentByTag(Static.DIALOG_SIGN_UP);
            if (dialog2 != null) setSignUpDialogListener(dialog2);
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
    protected void onStop() {
        super.onStop();

//        if (mGoogleApiClient != null) {
//            mGoogleApiClient.disconnect();
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(brReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkStatus();
    }

    private void checkStatus() {
        // Finish if signed in
        if (MyApp.getInstance().userMgr.isSignedIn()) {
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        // Sign in
        if (viewId == R.id.sign_in_with_diaro_account_button) {
            if (!MyApp.getInstance().userMgr.isSignedIn()) {
                showSignInDialog();
            }
        }

        else if (viewId == R.id.sign_in_with_google_button) {
            if (!MyApp.getInstance().networkStateMgr.isConnectedToInternet()) {
                // Show error toast
                Static.showToastError(getString(R.string.error_internet_connection));
                return;
            }

            if (!MyApp.getInstance().userMgr.isSignedIn()) {
                signInWithGoogle();
            }
        }
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleApiClient.getSignInIntent();
        startActivityForResult(signInIntent, Static.REQUEST_SIGN_IN_WITH_GOOGLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AppLog.e("requestCode: " + requestCode + ", resultCode: " + resultCode);

        if (requestCode == Static.REQUEST_SIGN_IN_WITH_GOOGLE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            // Sign in succeeded, proceed with account
            GoogleSignInAccount result = null;
            try {
                result = task.getResult(ApiException.class);
                handleSignInResult(result);
            } catch (ApiException e) {
                Static.showToastError(getString(R.string.signin_with_google_failed));
                AppLog.e("signInResult:failed code=" + e.getStatusCode());
                // Sign in failed, handle failure and update UI
            }
        }

    }

    private void handleSignInResult(GoogleSignInAccount acct) {
        // Signed in successfully, show authenticated UI.
        String email = acct.getEmail();
        String googleId = acct.getId();
        String fullName = acct.getDisplayName();
        String name = acct.getGivenName();
        String surname = acct.getFamilyName();
        String gender = "";
        String birthday = "";
        String imageUrl = acct.getPhotoUrl() == null ? "" : acct.getPhotoUrl().getPath();

        if (name == null && fullName != null) {
            name = fullName;
        }

        googleId = (googleId == null) ? "" : googleId;
        name = (name == null) ? "" : name;
        surname = (surname == null) ? "" : surname;
        gender = (gender == null) ? "" : gender;
        birthday = (birthday == null) ? "" : birthday;

        AppLog.d("email: " + email + ", googleId: " + googleId + ", name: " + name + ", imageUrl: " + imageUrl);

        MyApp.getInstance().asyncsMgr.executeSignInAsync(SignInActivity.this, email, "", googleId, name, surname, gender, birthday);
    }

    private void showSignInDialog() {
        String dialogTag = Static.DIALOG_SIGN_IN;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            SignInDialog dialog = new SignInDialog();
            dialog.show(getSupportFragmentManager(), dialogTag);

            // Set dialog listener
            setSignInDialogListener(dialog);
        }
    }

    private void setSignInDialogListener(final SignInDialog dialog) {
        dialog.setDialogPositiveClickListener((email, password) -> {
            AppLog.d("email: " + email); //  + ", password: " + password
            MyApp.getInstance().asyncsMgr.executeSignInAsync(SignInActivity.this, email, password, "", "", "", "", "");

        });
    }

    private void showSignUpDialog() {
        String dialogTag = Static.DIALOG_SIGN_UP;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            SignUpDialog dialog = new SignUpDialog();
            dialog.show(getSupportFragmentManager(), dialogTag);

            // Set dialog listener
            setSignUpDialogListener(dialog);
        }
    }

    private void setSignUpDialogListener(final SignUpDialog dialog) {
        dialog.setDialogPositiveClickListener((email, password) -> {
            AppLog.d("email: " + email + ", password: " + password);

            MyApp.getInstance().asyncsMgr.executeSignUpAsync(SignInActivity.this, email, password);

        });
    }


    private class BrReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String doWhat = intent.getStringExtra(Static.BROADCAST_DO);

            switch (doWhat) {
                // - Check status -
                case Static.DO_CHECK_STATUS:
                    checkStatus();
                    break;
                // - Dismiss sign in dialog -
                case Static.DO_DISMISS_SIGNIN_DIALOG:
                    DialogFragment signInDialog = (DialogFragment) getSupportFragmentManager().findFragmentByTag(Static.DIALOG_SIGN_IN);
                    if (signInDialog != null && signInDialog.isVisible()) {
                        signInDialog.dismiss();
                    }
                    break;
                // - Dismiss sign in dialog -
                case Static.DO_DISMISS_SIGNUP_DIALOG:
                    DialogFragment signUpDialog = (DialogFragment) getSupportFragmentManager().findFragmentByTag(Static.DIALOG_SIGN_UP);
                    if (signUpDialog != null && signUpDialog.isVisible()) {
                        signUpDialog.dismiss();
                    }
                    break;
            }
        }
    }
}
