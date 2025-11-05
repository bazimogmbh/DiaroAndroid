package com.pixelcrater.Diaro.securitycode;

import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.activitytypes.TypeActivity;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.generaldialogs.ConfirmDialog;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.Static;

import java.util.concurrent.Executor;

public class SecurityCodeActivity extends TypeActivity {

    public static final int MODE_LOCK_SCREEN = 0;
    public static final int MODE_STEP1_NEW = 1;
    public static final int MODE_STEP2_REPEAT = 2;
    public static final int MODE_STEP3_FORGOT_EMAIL = 3;
    public static final int MODE_REMOVE = 4;

    // State vars
    private final String MODE_STATE_KEY = "MODE_STATE_KEY";
    private final String NEW_SECURITY_CODE_STATE_KEY = "NEW_SECURITY_CODE_STATE_KEY";

    public int mode;
    private String newSecurityCode = "";
    private ViewGroup layoutContainer;
    private ViewGroup lockscreen12;
    private ViewGroup lockscreenStep3;
    private ImageView logo;
    private TextView infoText;
    private ImageButton backspaceButton;
    private ImageButton okButton;
    private ImageView fingerprintImageView;
    private EditText securityCodeEditText;
    private TextView forgotLink;
    private EditText forgotEmailEditText;
    private boolean isListeningForFingerprint;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Get intent extras
        Bundle extras = getIntent().getExtras();
        mode = extras.getInt("mode");

        super.onCreate(savedInstanceState);
        AppLog.d("savedInstanceState: " + savedInstanceState);

        executor = ContextCompat.getMainExecutor(this);

        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode != BiometricPrompt.ERROR_CANCELED && errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON)
                    Static.showToastError(errorCode + " " + errString.toString());
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                exitOnSuccess();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Static.showToastError("Authentication failed");
            }
        });


        setContentView(addViewToContentContainer(R.layout.lockscreen_responsive));
        activityState.setLayoutBackground();

        layoutContainer = (ViewGroup) findViewById(R.id.layout_container);

        // Get UI color
        layoutContainer.setBackgroundColor(MyThemesUtils.getPrimaryColor());

        lockscreen12 = (ViewGroup) findViewById(R.id.lockscreen_12);
        lockscreenStep3 = (ViewGroup) findViewById(R.id.lockscreen_step_3);

        logo = (ImageView) findViewById(R.id.logo);
        infoText = (TextView) findViewById(R.id.info_text);

        securityCodeEditText = (EditText) findViewById(R.id.security_code);

        LinearLayout numpad = (LinearLayout) findViewById(R.id.numpad);

        forgotEmailEditText = (EditText) findViewById(R.id.forgot_email);

        // Get vars from savedInstanceState
        if (savedInstanceState != null) {
            mode = savedInstanceState.getInt(MODE_STATE_KEY);
            newSecurityCode = savedInstanceState.getString(NEW_SECURITY_CODE_STATE_KEY);
        } else {
            if (mode == MODE_STEP1_NEW || mode == MODE_STEP2_REPEAT || mode == MODE_STEP3_FORGOT_EMAIL) {

                if (MyApp.getInstance().userMgr.isSignedIn()) {
                    forgotEmailEditText.setText(MyApp.getInstance().userMgr.getSignedInEmail());

                    // Set edit text cursor to the end
                    forgotEmailEditText.setSelection(forgotEmailEditText.getText().toString().length());
                }
            }
        }
        AppLog.d("mode: " + mode);

        if (mode == MODE_LOCK_SCREEN) {
            if (MyApp.getInstance().securityCodeMgr.isSecurityCodeSet()) {
                getSupportActionBar().hide();
                logo.setVisibility(View.VISIBLE);
            } else {
                finish();
                return;
            }
        } else {
            activityState.setActionBarTitle(getSupportActionBar(), getString(R.string.settings_security_code));
            infoText.setVisibility(View.VISIBLE);
        }

        // Set listener for numpad buttons
        for (int i = 0; i < 4; i++) {
            LinearLayout numpadRow = (LinearLayout) numpad.getChildAt(i);

            for (int j = 0; j < 3; j++) {
                View numView = numpadRow.getChildAt(j);

                if (numView instanceof TextView && !((TextView) numView).getText().toString().equals("")) {
                    numView.setOnClickListener(v -> numKeyPressed(Integer.parseInt(((TextView) v).getText().toString())));
                }
            }
        }

        fingerprintImageView = (ImageView) findViewById(R.id.fingerprint);
        fingerprintImageView.setOnClickListener(view -> listenForBiometrics());

        // Set listener for OK button
        okButton = (ImageButton) findViewById(R.id.ok);
        if (mode == MODE_LOCK_SCREEN) {
            okButton.setOnClickListener(v -> checkSecurityCode(true));
        }

        // Delete
        backspaceButton = (ImageButton) findViewById(R.id.delete);
        backspaceButton.setOnClickListener(v -> backspacePressed());

        backspaceButton.setOnLongClickListener(v -> {
            setSecurityCodeText("");

            return false;
        });

        // Forgot security code
        forgotLink = (TextView) findViewById(R.id.forgot_link);

        updateOkAndBackspaceButtons();
        updateUi();

        // Restore please wait dialogs of active AsyncTasks
        restorePleaseWaitDialogs();

        // Restore active dialog listeners
        restoreDialogListeners(savedInstanceState);
    }

    private void backspacePressed() {
        // Delete last symbol
        if (getEnteredSecurityCode().length() > 0) {
            String textWithoutLastSymbol = getEnteredSecurityCode().substring(0, getEnteredSecurityCode().length() - 1);
            setSecurityCodeText(textWithoutLastSymbol);
        }

        // Set edit text cursor to the end
        securityCodeEditText.setSelection(getEnteredSecurityCode().length());
    }

    private void numKeyPressed(int clickedNumber) {
//        AppLog.d("clickedNumber: " + clickedNumber);

        if (getEnteredSecurityCode().length() < 8) {
            setSecurityCodeText(getEnteredSecurityCode() + clickedNumber);

            // Set edit text cursor to the end
            securityCodeEditText.setSelection(getEnteredSecurityCode().length());

            // Check security code and enter automatically if it is correct
            if (mode == MODE_LOCK_SCREEN) {
                checkSecurityCode(false);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        AppLog.d("keyCode: " + keyCode);

        if (!isFinishing() && (mode == MODE_STEP1_NEW || mode == MODE_STEP2_REPEAT || mode == MODE_LOCK_SCREEN || mode == MODE_REMOVE)) {

            // If number pressed
            switch (keyCode) {
                // Backspace key
                case KeyEvent.KEYCODE_DEL:
                    backspacePressed();
                    break;
                // Enter key
                case KeyEvent.KEYCODE_ENTER:
                    if (mode == MODE_LOCK_SCREEN) {
                        checkSecurityCode(true);
                    }
                    break;
                case KeyEvent.KEYCODE_0:
                    numKeyPressed(0);
                    break;
                case KeyEvent.KEYCODE_1:
                    numKeyPressed(1);
                    break;
                case KeyEvent.KEYCODE_2:
                    numKeyPressed(2);
                    break;
                case KeyEvent.KEYCODE_3:
                    numKeyPressed(3);
                    break;
                case KeyEvent.KEYCODE_4:
                    numKeyPressed(4);
                    break;
                case KeyEvent.KEYCODE_5:
                    numKeyPressed(5);
                    break;
                case KeyEvent.KEYCODE_6:
                    numKeyPressed(6);
                    break;
                case KeyEvent.KEYCODE_7:
                    numKeyPressed(7);
                    break;
                case KeyEvent.KEYCODE_8:
                    numKeyPressed(8);
                    break;
                case KeyEvent.KEYCODE_9:
                    numKeyPressed(9);
                    break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void restorePleaseWaitDialogs() {
        if (MyApp.getInstance().asyncsMgr.isAsyncRunning(MyApp.getInstance().asyncsMgr.forgotSecurityCodeAsync)) {
            MyApp.getInstance().asyncsMgr.forgotSecurityCodeAsync.showPleaseWaitDialog(SecurityCodeActivity.this);
        }
    }

    private void restoreDialogListeners(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            ConfirmDialog dialog1 = (ConfirmDialog) getSupportFragmentManager().findFragmentByTag(Static.DIALOG_FORGOT_SECURITY_CODE);
            if (dialog1 != null) setForgotConfirmDialogListener(dialog1);
        }
    }

    public void showForgotConfirmDialog() {
        String dialogTag = Static.DIALOG_FORGOT_SECURITY_CODE;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setTitle(getString(R.string.forgot_security_code));
            dialog.setMessage(getString(R.string.security_code_will_be_sent_to_email));
            dialog.show(getSupportFragmentManager(), dialogTag);

            // Set dialog listener
            setForgotConfirmDialogListener(dialog);
        }
    }

    private void setForgotConfirmDialogListener(final ConfirmDialog dialog) {
        dialog.setDialogPositiveClickListener(() -> {
            if (!MyApp.getInstance().networkStateMgr.isConnectedToInternet()) {
                // Show error toast
                Static.showToastError(getString(R.string.error_internet_connection));
                return;
            }

            // Send security code
            String email = MyApp.getInstance().securityCodeMgr.getForgotEmail();
            String securityCode = MyApp.getInstance().securityCodeMgr.getSecurityCode();

            if (email != null && securityCode != null) {
                MyApp.getInstance().asyncsMgr.executeForgotSecurityCodeAsync(SecurityCodeActivity.this, email, securityCode);
            }
        });
    }

    private void setSecurityCodeText(String text) {
        securityCodeEditText.setText(text);
        updateOkAndBackspaceButtons();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_security_code, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu != null) {
            showHideMenuIcons(menu);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    private void showHideMenuIcons(Menu menu) {
        menu.findItem(R.id.item_next).setVisible(false);
        menu.findItem(R.id.item_finish).setVisible(false);

        switch (mode) {
            // Lock screen
            case MODE_LOCK_SCREEN:
                break;

            // Step 1. Enter security code
            case MODE_STEP1_NEW:
                menu.findItem(R.id.item_next).setVisible(true);
                break;

            // Step 2. Repeat security code
            case MODE_STEP2_REPEAT:
                menu.findItem(R.id.item_next).setVisible(true);
                break;

            // Step 3. Repeat security code
            case MODE_STEP3_FORGOT_EMAIL:
                menu.findItem(R.id.item_finish).setVisible(true);
                break;

            // Enter security code to remove it
            case MODE_REMOVE:
                menu.findItem(R.id.item_finish).setVisible(true);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AppLog.d("item: " + item);
        if (activityState.isActivityPaused) {
            return true;
        }

        // Handle presses on the action bar items
        switch (item.getItemId()) {
            // Back
            case android.R.id.home:
                if (mode == SecurityCodeActivity.MODE_LOCK_SCREEN) {
                    moveTaskToBack(true);
                } else {
                    finish();
                }

                return true;

            // Go to next step
            case R.id.item_next:
                checkSecurityCode(true);
                return true;

            // Finish setting security code
            case R.id.item_finish:
                checkSecurityCode(true);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isListeningForFingerprint) {
            biometricPrompt.cancelAuthentication();
            isListeningForFingerprint = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mode == MODE_LOCK_SCREEN) {
            if (!MyApp.getInstance().securityCodeMgr.isSecurityCodeSet()) {
                finish();
                return;
            } else if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ALLOW_FINGERPRINT, true)) {
                // Listen for biometrics
                listenForBiometrics();
                // Listen for fingerprint
                // listenForFingerprint();
            }
        }

        updateOkAndBackspaceButtons();
    }

    private void listenForBiometrics() {
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                AppLog.e("----App can authenticate using biometrics.");
                promptInfo = new BiometricPrompt.PromptInfo.Builder()
                        //TODO : extract to strings
                        .setTitle("Biometric login")
                        // .setSubtitle("Log in using your biometric credential")
                        .setNegativeButtonText(getString(R.string.settings_security_code))
                        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                        .setConfirmationRequired(false)
                        .build();

                biometricPrompt.authenticate(promptInfo);
                isListeningForFingerprint = true;
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                AppLog.e("----No biometric features available on this device.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                AppLog.e("----Biometric features are currently unavailable.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.
                AppLog.e("---None enrolled.");
                /** final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                 enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
                 startActivityForResult(enrollIntent, 1000);**/
                break;
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                break;
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                break;
            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                break;
        }
    }

    private void listenForFingerprint() {
        AppLog.d("");

        /** if (Reprint.isHardwarePresent() && Reprint.hasFingerprintRegistered()) {
         Reprint.authenticate(new AuthenticationListener() {
        @Override public void onSuccess(int moduleTag) {
        //                AppLog.d("moduleTag: " + moduleTag);
        //                Static.showToast("Fingerprint OK! moduleTag: " + moduleTag, Toast.LENGTH_SHORT);
        exitOnSuccess();
        }

        @Override public void onFailure(AuthenticationFailureReason failureReason, boolean fatal, CharSequence errorMessage, int moduleTag, int errorCode) {
        AppLog.d("failureReason: " + failureReason + ", fatal: " + fatal + ", errorMessage: " + errorMessage + ", errorCode: " + errorCode);
        if (failureReason == AuthenticationFailureReason.LOCKED_OUT) {
        Static.showToastError(String.format("%s!", getString(R.string.unable_to_use_fingerprint_sensor)));
        } else if (errorMessage != null) {
        if (!errorMessage.toString().isEmpty())
        Static.showToastError(errorMessage.toString());
        }
        }
        });

         isListeningForFingerprint = true;
         }**/
    }

    private void exitOnSuccess() {
        fingerprintImageView.setVisibility(View.GONE);
        okButton.setVisibility(View.VISIBLE);

        setResult(RESULT_OK);
        finish();
        MyApp.getInstance().securityCodeMgr.setUnlocked();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(MODE_STATE_KEY, mode);
        outState.putString(NEW_SECURITY_CODE_STATE_KEY, newSecurityCode);
    }

    @Override
    public void onBackPressed() {
        if (mode == SecurityCodeActivity.MODE_LOCK_SCREEN) {
            moveTaskToBack(true);
        } else {
            super.onBackPressed();
        }
    }

    private void updateOkAndBackspaceButtons() {
        if (getEnteredSecurityCode().length() > 0) {
            backspaceButton.setImageResource(R.drawable.ic_backspace_white_24dp);
            backspaceButton.setEnabled(true);

            if (mode == MODE_LOCK_SCREEN) {
                fingerprintImageView.setVisibility(View.GONE);
                okButton.setVisibility(View.VISIBLE);
            }
        } else {
            backspaceButton.setImageResource(R.drawable.ic_backspace_white_disabled_24dp);
            backspaceButton.setEnabled(false);
            okButton.setVisibility(View.GONE);
            if (isListeningForFingerprint) {
                fingerprintImageView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateUi() {
        lockscreenStep3.setVisibility(View.GONE);

        switch (mode) {
            // Lock screen
            case MODE_LOCK_SCREEN:

                // Show forgot security code link
                if (MyApp.getInstance().securityCodeMgr.isForgotEmailSet()) {
                    forgotLink.setVisibility(View.VISIBLE);
                    forgotLink.setOnClickListener(v -> showForgotConfirmDialog());
                }

                break;

            // Step 1. Enter security code
            case MODE_STEP1_NEW:
                infoText.setText(R.string.settings_security_code_enter);

                break;

            // Step 2. Repeat security code
            case MODE_STEP2_REPEAT:
                infoText.setText(R.string.settings_security_code_repeat);

                break;

            // Step 3. Repeat security code
            case MODE_STEP3_FORGOT_EMAIL:
                lockscreen12.setVisibility(View.GONE);
                lockscreenStep3.setVisibility(View.VISIBLE);
                layoutContainer.setBackgroundColor(Color.TRANSPARENT);

                // Show keyboard
                Static.showSoftKeyboard(forgotEmailEditText);

                break;

            // Enter security code to remove it
            case MODE_REMOVE:
                infoText.setText(R.string.settings_enter_current_security_code_to_remove_it);

                break;
        }

        supportInvalidateOptionsMenu();
    }

    private void checkSecurityCode(boolean clearIfNotCorrect) {
        AppLog.d("mode: " + mode + ", newSecurityCode: " + newSecurityCode);

        switch (mode) {
            // Lock screen
            case MODE_LOCK_SCREEN:
                // If correct security code was entered
                if (getEnteredSecurityCode().equals(MyApp.getInstance().securityCodeMgr.getSecurityCode())) {
                    exitOnSuccess();
                    return;
                } else if (clearIfNotCorrect) {
                    setSecurityCodeText("");

                    // Shake logo animation
                    Animation shakeAnim = AnimationUtils.loadAnimation(SecurityCodeActivity.this, R.anim.shake);
                    logo.startAnimation(shakeAnim);
                }
                break;

            // Step 1. Enter security code
            case MODE_STEP1_NEW:
                if (getEnteredSecurityCode().length() > 0) {
                    // Go to step 2
                    mode = MODE_STEP2_REPEAT;

                    newSecurityCode = getEnteredSecurityCode();

                    setSecurityCodeText("");
                } else {
                    // Show error toast
                    Static.showToastError(getString(R.string.settings_security_code_enter));
                }
                break;

            // Step 2. Repeat security code
            case MODE_STEP2_REPEAT:
                if (getEnteredSecurityCode().length() > 0) {
                    // Check if repeated security code matches new security code
                    if (getEnteredSecurityCode().equals(newSecurityCode)) {
                        // Go to step 3
                        mode = MODE_STEP3_FORGOT_EMAIL;
                    } else {
                        // Go back to step 1
                        mode = MODE_STEP1_NEW;

                        newSecurityCode = "";
                        setSecurityCodeText("");

                        // Show error toast
                        Static.showToastError(getString(R.string.settings_security_codes_dont_match));
                    }
                } else {
                    // Show error toast
                    Static.showToastError(getString(R.string.settings_security_code_repeat));
                }
                break;

            // Step 3. Repeat security code
            case MODE_STEP3_FORGOT_EMAIL:
                if (!getEnteredEmail().equals("") && !Static.isValidEmail(getEnteredEmail())) {
                    // Show error toast
                    Static.showToastError(getString(R.string.invalid_email));
                } else {
                    MyApp.getInstance().securityCodeMgr.setSecurityCode(newSecurityCode);
                    MyApp.getInstance().securityCodeMgr.setForgotEmail(getEnteredEmail());

                    Static.showToastSuccess(getString(R.string.settings_security_code_was_set));

                    finish();
                    return;
                }
                break;

            // Enter security code to remove it
            case MODE_REMOVE:
                // If correct security code was entered
                if (getEnteredSecurityCode().equals(MyApp.getInstance().securityCodeMgr.getSecurityCode())) {
                    MyApp.getInstance().securityCodeMgr.setSecurityCode(null);
                    MyApp.getInstance().securityCodeMgr.setForgotEmail(null);

                    // Show error toast
                    Static.showToast(getString(R.string.settings_security_code_was_removed), Toast.LENGTH_SHORT);

                    finish();
                    return;
                } else {
                    setSecurityCodeText("");

                    // Show error toast
                    Static.showToastError(getString(R.string.settings_security_code_incorrect));
                }
                break;
        }

        updateUi();
    }

    private String getEnteredSecurityCode() {
        return securityCodeEditText.getText().toString();
    }

    private String getEnteredEmail() {
        return forgotEmailEditText.getText().toString();
    }
}
