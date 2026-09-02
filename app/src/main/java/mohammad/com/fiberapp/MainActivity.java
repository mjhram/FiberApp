package mohammad.com.fiberapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import mohammad.com.fiberapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private View contentView;
    private MultiplePermissionsListener allPermissionsListener;
    private PermissionRequestErrorListener errorListener;

    @NonNull
    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        contentView = findViewById(android.R.id.content);
        binding.btnRequestAll.setOnClickListener(v -> requestAllPermissions());
            requestAllPermissions();
    }

    private final String TAG = "FiberApp";

    public void requestAllPermissions() {
        String[] permissions = {Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION};
        Permissions.check(this, permissions, null, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                startActivity(MapsActivity.createIntent(MainActivity.this));
                finish();
                Toast.makeText(MainActivity.this, "granted....", Toast.LENGTH_LONG).show();
            }
        });
    }
}
