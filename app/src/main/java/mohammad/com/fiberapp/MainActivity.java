package mohammad.com.fiberapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.multi.SnackbarOnAnyDeniedMultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mohammad.com.fiberapp.utility.SampleErrorListener;
import mohammad.com.fiberapp.utility.SampleMultiplePermissionListener;

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 100;

    @BindView(R.id.root) View mRootView;
    @BindView(R.id.tvInternet) TextView tvInternet;
    @BindView(R.id.tvLocation) TextView tvLocation;
    @BindView(R.id.tvRdStorage) TextView tvRdStorage;
    @BindView(R.id.tvWrStorage) TextView tvWrStorage;
    @BindView(android.R.id.content) View contentView;
    private MultiplePermissionsListener allPermissionsListener;
    private PermissionRequestErrorListener errorListener;

    @NonNull
    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        createPermissionListeners();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivityForResult(buildSignInIntent(/*link=*/null), RC_SIGN_IN);
        } else {
            onAllPermissionsButtonClicked();
            //startSignedInActivity();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null && getIntent().getExtras() == null) {
            startSignedInActivity(null);
            //finish();
        }
    }

    private void startSignedInActivity(@Nullable IdpResponse response) {
        Log.d(TAG, "start Singned in");
        //startActivity(MapsActivity.createIntent(this, response));
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            handleSignInResponse(resultCode, data);
        }
    }

    // Choose authentication providers
    private List<AuthUI.IdpConfig> getSelectedProviders() {
        List<AuthUI.IdpConfig> selectedProviders = new ArrayList<>();

        //if (mUseEmailProvider.isChecked())
        {
            selectedProviders.add(new AuthUI.IdpConfig.EmailBuilder()
                    .setRequireName(false)
                    .setAllowNewAccounts(false)
                    .build());
        }
        return selectedProviders;
    }

    public Intent buildSignInIntent(String link) {
        AuthUI.SignInIntentBuilder builder = AuthUI.getInstance().createSignInIntentBuilder()
                //.setTheme(R.style.PurpleTheme)
                .setLogo(R.drawable.ic_launcher)
                .setAvailableProviders(getSelectedProviders());
        /*if (getSelectedTosUrl() != null && getSelectedPrivacyPolicyUrl() != null) {
            builder.setTosAndPrivacyPolicyUrls(
                    getSelectedTosUrl(),
                    getSelectedPrivacyPolicyUrl());
        }*/
        FirebaseAuth auth = FirebaseAuth.getInstance();
        /*if (auth.getCurrentUser() != null && auth.getCurrentUser().isAnonymous()) {
            builder.enableAnonymousUsersAutoUpgrade();
        }*/
        return builder.build();
    }

    private final String TAG = "FiberApp";
    private void handleSignInResponse(int resultCode, @Nullable Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        // Successfully signed in
        if (resultCode == RESULT_OK) {

            onAllPermissionsButtonClicked();
            //startSignedInActivity(response);
            //finish();
        } else {
            // Sign in failed
            if (response == null) {
                // User pressed back button
                showSnackbar(R.string.sign_in_cancelled);
                return;
            }

            if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                showSnackbar(R.string.no_internet_connection);
                return;
            }

            /*if (response.getError().getErrorCode() == ErrorCodes.ANONYMOUS_UPGRADE_MERGE_CONFLICT) {
                Intent intent = new Intent(this, AnonymousUpgradeActivity.class).putExtra
                        (ExtraConstants.IDP_RESPONSE, response);
                startActivity(intent);
            }*/

            if (response.getError().getErrorCode() == ErrorCodes.ERROR_USER_DISABLED) {
                showSnackbar(R.string.account_disabled);
                return;
            }

            showSnackbar(R.string.unknown_error);
            Log.e(TAG, "Sign-in error: ", response.getError());
        }
    }

    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    @OnClick(R.id.btnRequestAll) public void onAllPermissionsButtonClicked() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(allPermissionsListener)
                .withErrorListener(errorListener)
                .check();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void showPermissionRationale(final PermissionToken token) {
        new AlertDialog.Builder(this).setTitle(R.string.permission_rationale_title)
                .setMessage(R.string.permission_rationale_message)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        token.cancelPermissionRequest();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        token.continuePermissionRequest();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override public void onDismiss(DialogInterface dialog) {
                        token.cancelPermissionRequest();
                    }
                })
                .show();
    }

    public void showPermissionGranted(String permission) {
        TextView feedbackView = getFeedbackViewForPermission(permission);
        feedbackView.setTextColor(ContextCompat.getColor(this, R.color.permission_granted));
    }

    public void showPermissionDenied(String permission, boolean isPermanentlyDenied) {
        TextView feedbackView = getFeedbackViewForPermission(permission);
        feedbackView.setTextColor(ContextCompat.getColor(this, R.color.permission_denied));
    }

    private void createPermissionListeners() {
        //PermissionListener feedbackViewPermissionListener = new SamplePermissionListener(this);
        MultiplePermissionsListener feedbackViewMultiplePermissionListener =
                new SampleMultiplePermissionListener(this);

        allPermissionsListener =
                new CompositeMultiplePermissionsListener(feedbackViewMultiplePermissionListener,
                        SnackbarOnAnyDeniedMultiplePermissionsListener.Builder.with(contentView,
                                R.string.all_permissions_denied_feedback)
                                .withOpenSettingsButton(R.string.permission_rationale_settings_button_text)
                                .build());



        errorListener = new SampleErrorListener();
    }

    private TextView getFeedbackViewForPermission(String name) {
        TextView feedbackView;

        switch (name) {
            case Manifest.permission.INTERNET:
                feedbackView = tvInternet;
                break;
            case Manifest.permission.ACCESS_FINE_LOCATION:
                feedbackView = tvLocation;
                break;
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                feedbackView = tvRdStorage;
                break;
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                feedbackView = tvWrStorage;
                break;
            default:
                throw new RuntimeException("No feedback view for this permission");
        }

        return feedbackView;
    }

}
