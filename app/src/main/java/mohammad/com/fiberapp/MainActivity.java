package mohammad.com.fiberapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 100;

    @BindView(R.id.root) View mRootView;

    @NonNull
    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        startActivityForResult(buildSignInIntent(/*link=*/null), RC_SIGN_IN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null && getIntent().getExtras() == null) {
            startSignedInActivity(null);
            finish();
        }
    }

    private void startSignedInActivity(@Nullable IdpResponse response) {
        startActivity(MapsActivity.createIntent(this, response));
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
            startSignedInActivity(response);
            finish();
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

}
