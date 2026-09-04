package mohammad.com.fiberapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.documentfile.provider.DocumentFile;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.kml.KmlLayer;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import mohammad.com.fiberapp.databinding.ActivityMapsBinding;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, AdapterView.OnItemSelectedListener, OnMapsSdkInitializedCallback {

    private static final int RC_PICK_FOLDER = 200;
    private static final String PREF_FOLDER_URI = "map_folder_uri";
    // Bundled with the app so there's always something to show, even before any
    // folder is picked. Lives at app/src/main/assets/to_am.kmz.
    private static final String DEFAULT_ASSET_NAME = "to_am.kmz";
    final String TAG = "FiberApp";

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private int currentPosition = -1;

    // Everything available to show in the spinner: the bundled default first,
    // then any .kmz/.geojson files found in a user-picked folder.
    private final ArrayList<LayerSource> mapFiles = new ArrayList<>();

    /**
     * A single map layer FiberApp can load: either bundled inside the app (an asset)
     * or picked by the user from a device folder (a DocumentFile via the Storage
     * Access Framework). Either way, nothing is ever uploaded anywhere.
     */
    private static class LayerSource {
        final String displayName;
        final boolean isAsset;
        final String assetPath;
        final DocumentFile documentFile;
        final String lowerName;

        private LayerSource(String displayName, boolean isAsset, String assetPath,
                             DocumentFile documentFile, String lowerName) {
            this.displayName = displayName;
            this.isAsset = isAsset;
            this.assetPath = assetPath;
            this.documentFile = documentFile;
            this.lowerName = lowerName;
        }

        static LayerSource fromAsset(String assetFileName) {
            return new LayerSource(stripExtension(assetFileName), true, assetFileName, null,
                    assetFileName.toLowerCase());
        }

        static LayerSource fromDocument(DocumentFile file) {
            String name = file.getName();
            return new LayerSource(stripExtension(name), false, null, file, name.toLowerCase());
        }

        private static String stripExtension(String name) {
            int dot = name.lastIndexOf('.');
            return dot != -1 ? name.substring(0, dot) : name;
        }
    }

    @NonNull
    public static Intent createIntent(@NonNull Context context) {
        return new Intent().setClass(context, MapsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Android 15+ (targetSdk 35+) draws edge-to-edge by default; request it explicitly
        // for consistent behavior on older versions too, and handle insets ourselves below.
        EdgeToEdge.enable(this);

        // Request the modern Maps renderer explicitly; the legacy renderer is deprecated.
        MapsInitializer.initialize(getApplicationContext(), MapsInitializer.Renderer.LATEST, this);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.spinner.setOnItemSelectedListener(this);

        // Keep the toolbar below the status bar and the description panel above the
        // navigation bar / gesture area, now that the app draws behind the system bars.
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.toolbar.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            binding.llDesc.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }

    @Override
    public void onMapsSdkInitialized(@NonNull MapsInitializer.Renderer renderer) {
        Log.d(TAG, "Maps renderer initialized: " + renderer);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mMap.setPadding(0,binding.toolbar.getHeight(),0,binding.llDesc.getHeight());
            }
        });
        LatLng bgd = new LatLng(33.2967658, 44.4707338);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(bgd, 10));

        loadSavedFolderOrPrompt();
    }

    /**
     * Tries to reuse a previously picked folder in addition to the bundled default.
     * Unlike before, there's no forced folder-pick prompt  the bundled map already
     * gives the app something to show right after install.
     */
    private void loadSavedFolderOrPrompt() {
        String saved = Prefs.getPrefs(PREF_FOLDER_URI, this);
        Uri treeUri = null;
        if (saved != null && !saved.isEmpty()) {
            Uri candidate = Uri.parse(saved);
            DocumentFile dir = DocumentFile.fromTreeUri(this, candidate);
            if (dir != null && dir.canRead()) {
                treeUri = candidate;
                                                }
                                            }
        scanFolder(treeUri);
                        }

    /**
     * Launches the system folder picker (Storage Access Framework) so the user can choose
     * where their .kmz / .geojson files live on the device.
     */
    private void pickFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, RC_PICK_FOLDER);
                    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PICK_FOLDER && resultCode == RESULT_OK && data != null) {
            Uri treeUri = data.getData();
            if (treeUri == null) return;

            // Keep access to this folder across app restarts.
            getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Prefs.setPrefs(PREF_FOLDER_URI, treeUri.toString(), this);
            scanFolder(treeUri);
                }
        }

    /**
     * Builds the spinner's list of layers: the bundled default first, then any
     * .kmz/.geojson files found in the given folder (if one has been picked).
     * No network call is made - everything is read directly off the device.
     */
    private void scanFolder(@Nullable Uri treeUri) {
        mapFiles.clear();
        mapFiles.add(LayerSource.fromAsset(DEFAULT_ASSET_NAME));

        if (treeUri != null) {
        DocumentFile dir = DocumentFile.fromTreeUri(this, treeUri);
        if (dir != null && dir.isDirectory()) {
            for (DocumentFile f : dir.listFiles()) {
                if (!f.isFile()) continue;
                String name = f.getName();
                if (name == null) continue;
                String lower = name.toLowerCase();
                if (lower.endsWith(".kmz") || lower.endsWith(".geojson")) {
                        mapFiles.add(LayerSource.fromDocument(f));
                    }
                }
            }
            if (mapFiles.size() == 1) {
                Toast.makeText(this, "No .kmz or .geojson files found in the selected folder", Toast.LENGTH_LONG).show();
            }
    }

        ArrayList<String> displayNames = new ArrayList<>();
        for (LayerSource s : mapFiles) {
            displayNames.add(s.displayName);
    }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.item, R.id.tvItem, displayNames);
        binding.spinner.setAdapter(arrayAdapter);

        // Show the first layer right away rather than waiting for the spinner's
        // selection callback to fire.
        binding.spinner.setSelection(0);
        currentPosition = 0;
        mMap.clear();
        loadLayer(mapFiles.get(0));
                }

    /**
     * Reads the given layer - whether bundled in the app or picked from a device folder -
     * and renders it on the map. .kmz files are unzipped in-memory to find the embedded
     * .kml; .geojson files are parsed directly. Nothing is downloaded or uploaded anywhere.
     */
    private void loadLayer(LayerSource source) {
        try (InputStream is = source.isAsset
                ? getAssets().open(source.assetPath)
                : getContentResolver().openInputStream(source.documentFile.getUri())) {
            if (is == null) return;

            if (source.lowerName.endsWith(".kmz")) {
                loadKmz(is);
            } else if (source.lowerName.endsWith(".geojson")) {
                loadGeoJson(is);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to open file: " + source.displayName, e);
            Toast.makeText(this, "Could not open " + source.displayName, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadKmz(InputStream is) {
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is))) {
            ZipEntry entry;
            boolean found = false;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".kml")) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int n;
                    while ((n = zis.read(buffer)) > 0) {
                        baos.write(buffer, 0, n);
    }

                    KmlLayer kmlLayer = new KmlLayer(mMap, new ByteArrayInputStream(baos.toByteArray()), MapsActivity.this);
                    kmlLayer.addLayerToMap();
                    kmlLayer.setOnFeatureClickListener(new KmlLayer.OnFeatureClickListener() {
                    @Override
                        public void onFeatureClick(Feature feature) {
                            if (feature == null) {
                                Log.d(TAG, "feature is null");
                                return;
                            }
                            binding.tvDesc.setText(feature.getProperty("description"));
                    }
                });
                    found = true;
                    break;
    }
                zis.closeEntry();
                    }
            if (!found) {
                Toast.makeText(this, "No .kml found inside the .kmz file", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException | XmlPullParserException e) {
            Log.e(TAG, "Failed to load KMZ", e);
            Toast.makeText(this, "Failed to load KMZ file", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadGeoJson(InputStream is) {
            try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int n;
            while ((n = is.read(buffer)) > 0) {
                baos.write(buffer, 0, n);
                    }

            JSONObject json = new JSONObject(baos.toString("UTF-8"));
            GeoJsonLayer layer = new GeoJsonLayer(mMap, json);
            layer.addLayerToMap();
            layer.setOnFeatureClickListener(new KmlLayer.OnFeatureClickListener() {
                    @Override
                    public void onFeatureClick(Feature feature) {
                        if (feature == null) {
                            Log.d(TAG, "feature is null");
                            return;
                        }
                    Toast.makeText(MapsActivity.this, "Coming soon", Toast.LENGTH_SHORT).show();
                    }
                });
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Failed to load GeoJSON", e);
            Toast.makeText(this, "Failed to load GeoJSON file", Toast.LENGTH_SHORT).show();
                        }
            }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        if (position < 0 || position >= mapFiles.size()) return;
        if (position == currentPosition) return; // already showing this one (e.g. initial auto-selection)
        currentPosition = position;
        Log.d(TAG, "Selected: " + adapterView.getItemAtPosition(position));
        mMap.clear();
        loadLayer(mapFiles.get(position));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Log.d(TAG,"nothing selected");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    private static final String PRIVACY_POLICY_URL = "https://sites.google.com/view/fiberapp/home";

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_choose_folder) {
            pickFolder();
            return true;
        } else if (itemId == R.id.action_privacy_policy) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}


