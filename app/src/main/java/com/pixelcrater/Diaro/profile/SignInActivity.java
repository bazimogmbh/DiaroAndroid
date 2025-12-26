package com.pixelcrater.Diaro.profile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.fragment.app.DialogFragment;

import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.activitytypes.TypeActivity;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyDevice;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.Static;

import java.util.concurrent.Executors;

public class SignInActivity extends TypeActivity implements OnClickListener {

    private CredentialManager credentialManager;
    private CancellationSignal cancellationSignal;
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

            // Initialize Credential Manager for Google Sign-In
            credentialManager = CredentialManager.create(this);
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(brReceiver);
        if (cancellationSignal != null) {
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
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
        if (credentialManager == null) {
            Static.showToastError(getString(R.string.signin_with_google_failed));
            return;
        }

        // Cancel any existing sign-in request
        if (cancellationSignal != null) {
            cancellationSignal.cancel();
        }
        cancellationSignal = new CancellationSignal();

        // Build the Google Sign-In option
        // Web client ID from google-services.json (client_type: 3 in appinvite_service)
        String serverClientId = "994414072062-glai70igcc8eft7f49q2414pd1spn0i5.apps.googleusercontent.com";
        GetSignInWithGoogleOption googleSignInOption = new GetSignInWithGoogleOption.Builder(serverClientId)
                .build();

        // Build the credential request
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleSignInOption)
                .build();

        // Request credentials using Credential Manager
        credentialManager.getCredentialAsync(
                this,
                request,
                cancellationSignal,
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        runOnUiThread(() -> handleCredentialResponse(result));
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        runOnUiThread(() -> {
                            Static.showToastError(getString(R.string.signin_with_google_failed));
                            AppLog.e("signInResult:failed " + e.getType() + ": " + e.getMessage());
                        });
                    }
                }
        );
    }

    private void handleCredentialResponse(GetCredentialResponse response) {
        // Extract Google ID Token credential
        if (response.getCredential() instanceof androidx.credentials.CustomCredential) {
            androidx.credentials.CustomCredential customCredential =
                    (androidx.credentials.CustomCredential) response.getCredential();

            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(customCredential.getType())) {
                GoogleIdTokenCredential googleCredential =
                        GoogleIdTokenCredential.createFrom(customCredential.getData());
                handleSignInResult(googleCredential);
            } else {
                Static.showToastError(getString(R.string.signin_with_google_failed));
                AppLog.e("Unexpected credential type: " + customCredential.getType());
            }
        } else {
            Static.showToastError(getString(R.string.signin_with_google_failed));
            AppLog.e("Unexpected credential class: " + response.getCredential().getClass().getName());
        }
    }

    private void handleSignInResult(GoogleIdTokenCredential credential) {
        // Signed in successfully, extract user info
        String email = credential.getId(); // Email is the ID in GoogleIdTokenCredential
        String googleId = credential.getIdToken();
        String fullName = credential.getDisplayName();
        String name = credential.getGivenName();
        String surname = credential.getFamilyName();
        String gender = "";
        String birthday = "";
        String imageUrl = credential.getProfilePictureUri() == null ? "" : credential.getProfilePictureUri().toString();

        if (name == null && fullName != null) {
            name = fullName;
        }

        googleId = (googleId == null) ? "" : googleId;
        name = (name == null) ? "" : name;
        surname = (surname == null) ? "" : surname;

        AppLog.d("email: " + email + ", name: " + name + ", imageUrl: " + imageUrl);

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
