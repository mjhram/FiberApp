package mohammad.com.fiberapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Entry point. The app currently requires no dangerous runtime permissions
 * (Storage Access Framework is used for file selection and doesn't need any),
 * so this simply forwards to the map screen.
 */
public class MainActivity extends AppCompatActivity {

    @NonNull
    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(MapsActivity.createIntent(this));
                finish();
    }
}
