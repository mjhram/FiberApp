package mohammad.com.fiberapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.util.ExtraConstants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.kml.KmlLayer;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import mohammad.com.fiberapp.model.myFileInfo;
import mohammad.com.fiberapp.service.RetrofitInstance;
import mohammad.com.fiberapp.utility.Decompress2;
import okhttp3.ResponseBody;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, AdapterView.OnItemSelectedListener {
    //private static final int PERMISSION_REQUEST_CODE = 200;
    private GoogleMap mMap;
    private ArrayList<myFileInfo> localFList;
    private CompositeDisposable disposables = new CompositeDisposable();

    @BindView(R.id.toolbar)
    Toolbar myToolbar;
    @BindView(R.id.spinner)
    Spinner mySpinner;
    @BindView(R.id.llDesc)
    LinearLayout llDesc;
    @BindView(R.id.tvDesc)
    TextView tvDesc;

    @NonNull
    public static Intent createIntent(@NonNull Context context, @Nullable IdpResponse response) {
        return new Intent().setClass(context, MapsActivity.class)
                .putExtra(ExtraConstants.IDP_RESPONSE, response);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        localFList = Prefs.loadFList(this);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || !checkPermission()) {
            startActivity(MainActivity.createIntent(this));
            finish();
            return;
        }

        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        setSupportActionBar(myToolbar);
        mySpinner.setOnItemSelectedListener(this);
        /*if (!checkPermission()) {
            requestPermission();
        } else */
        {
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mMap.setPadding(0,myToolbar.getHeight(),0,llDesc.getHeight());
            }
        });
        LatLng bgd = new LatLng(33.2967658, 44.4707338);
        //mMap.addMarker(new MarkerOptions().position(bgd).title("Marker in Sydney"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(bgd, 10));
        updateData();
        //retrieveFileFromResource();
        //getFileList();


    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), INTERNET);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int result3 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);

        return result == PERMISSION_GRANTED && result1 == PERMISSION_GRANTED &&
                result2 == PERMISSION_GRANTED && result3 == PERMISSION_GRANTED
                ;
    }

    /*private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, INTERNET, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean locationAccepted = grantResults[0] == PERMISSION_GRANTED;
                    boolean inetAccepted = grantResults[1] == PERMISSION_GRANTED;
                    boolean readAccepted = grantResults[2] == PERMISSION_GRANTED;
                    boolean writeAccepted = grantResults[3] == PERMISSION_GRANTED;

                    if (locationAccepted && inetAccepted && readAccepted && writeAccepted) {
                        Toast.makeText(this, "Permission Granted. Now you can use the app.", LENGTH_LONG).show();
                    } else {
                    //if (!locationAccepted) {
                        //Toast.makeText(this, "Location Permission is not granted. You cannot use the app.", LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) ||
                                    shouldShowRequestPermissionRationale(INTERNET) ||
                                    shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE) ||
                                    shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)
                            ) {
                                showMessageOKCancel("You need to allow access to permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{ACCESS_FINE_LOCATION, INTERNET, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }

                }


                break;
        }
    }*/

    private boolean processGeojsonResponse(ResponseBody body, final String filename) {
        try {
            InputStream inputStream = null;
            BufferedInputStream bis=null;

            try {
                inputStream = body.byteStream();
                bis = new BufferedInputStream(inputStream);
                // Remove the extension.
                int extensionIndex = filename.lastIndexOf(".");
                String fname;
                if (extensionIndex != -1)
                    fname = filename;
                fname = filename.substring(0, extensionIndex);
                fname += ".geojson";
                Decompress2.extractEntry("/storage/emulated/0/Download/", null, bis, fname);
                return true;
            } catch (IOException e) {
                return false;
            } /*catch (XmlPullParserException e) {
                e.printStackTrace();
                return false;
            } */finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (bis != null) {
                    bis.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    private String changeExt(String filename) {
        int extensionIndex = filename.lastIndexOf(".");
        String fname;
        if (extensionIndex != -1)
            fname = filename;
        fname = filename.substring(0, extensionIndex);
        fname += ".kml";
        return fname;
    }
    private boolean processKmzResponse(ResponseBody body, final String filename) {
        //KmlLayer kmlLayer = null;
        try {
            // todo change the file location/name according to your needs
            //File outFile = new File(getExternalFilesDir(null) + File.separator + "1.kmz");

            InputStream inputStream = null;
            //OutputStream outputStream = null;
            ZipInputStream zipInputStream=null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));

                //FileOutputStream outputStream = this.openFileOutput(filename, Context.MODE_PRIVATE);
                    //outputStream = new FileOutputStream(outFile);
                ZipEntry zipEntry;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    if (!zipEntry.isDirectory() && zipEntry.getName().endsWith(".kml")) {
                        // Remove the extension.
                        String fname = changeExt(filename);

                        /*int extensionIndex = filename.lastIndexOf(".");
                        if (extensionIndex != -1)
                            fname = filename;
                        fname = filename.substring(0, extensionIndex);
                        fname += ".kml";*/
                        Decompress2.extractEntry("/storage/emulated/0/Download/", zipEntry, zipInputStream, fname);

                        /*String fileName = zipEntry.getName();
                        if (fileName.endsWith(".kml")) {
                            kmlLayer = new KmlLayer(mMap, zipInputStream, MapsActivity.this);
                            kmlLayer.addLayerToMap();
                            kmlLayer.setOnFeatureClickListener(new KmlLayer.OnFeatureClickListener() {
                                @Override
                                public void onFeatureClick(Feature feature) {
                                    String ss = feature.getProperty("description");//getId();
                                    Toast.makeText(MapsActivity.this,
                                            "Coming soon" ,
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                        }*/
                    }
                    zipInputStream.closeEntry();
                }

                return true;
            } catch (IOException e) {
                return false;
            } /*catch (XmlPullParserException e) {
                e.printStackTrace();
                return false;
            } */finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (zipInputStream != null) {
                    zipInputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    final String TAG = "FiberApp";
    ArrayList<myFileInfo> remoteFileInfo;

    public void syncFlist() {
        //1. already the updated fies are overwritten
        //2.add the new files
        int sz = remoteFileInfo.size();
        for(int i=0; i<sz; i++) {
            myFileInfo rfi = remoteFileInfo.get(i);
            boolean exist = false;
            for(int j=0; j<localFList.size(); j++) {
                myFileInfo lfi = localFList.get(j);
                if(rfi.getFn().equalsIgnoreCase(lfi.getFn()) && rfi.getDt().equalsIgnoreCase(lfi.getDt())){
                    exist = true;
                    break;
                }
            }
            if(!exist) {
                //add the new fileInfo to local list
                localFList.add(rfi);
            }
        }
        for(int j=localFList.size()-1; j>=0; j--) {
            myFileInfo lfi = localFList.get(j);
            boolean exist = false;
            for(int i=0; i<remoteFileInfo.size(); i++) {
                myFileInfo rfi = remoteFileInfo.get(i);
                if(rfi.getFn().equalsIgnoreCase(lfi.getFn()) && rfi.getDt().equalsIgnoreCase(lfi.getDt())){
                    exist = true;
                    break;
                }
            }
            if(!exist) {
                //remove the new fileInfo from local list
                String tmp ="/storage/emulated/0/Download/"+localFList.get(j).getFn();
                new File(tmp).delete();
                localFList.remove(j);
            }
        }
    }

    private Observable<myFileInfo> getPostsObservable(){
        return RetrofitInstance.getService()
                .getResults()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Function<List<myFileInfo>, ObservableSource<myFileInfo>>() {
                    @Override
                    public ObservableSource<myFileInfo> apply(final List<myFileInfo> flist) throws Exception {
                        remoteFileInfo = (ArrayList<myFileInfo>) flist;
                        return Observable.fromIterable(flist)
                                .subscribeOn(Schedulers.io());
                    }
                });
    }

    private Observable<myFileInfo> getCommentsObservable(final myFileInfo post){
        return RetrofitInstance.getService()
                .downloadFile(post.getFn())
                .map(new Function<ResponseBody, myFileInfo>() {
                    @Override
                    public myFileInfo apply(ResponseBody body) throws Exception {
                        Log.d(TAG, "downloaded. saving...");
                        String tmp = post.getFn();
                        String extension = tmp.substring(tmp.lastIndexOf("."));
                        if(extension.equalsIgnoreCase(".kmz")) {
                            post.error = processKmzResponse(body, post.getFn()) ? 0 : -1;
                        } else if (extension.equalsIgnoreCase(".geojson")) {
                            post.error = processGeojsonResponse(body, post.getFn()) ? 0 : -1;
                        }
                        //post.setComments(comments);
                        return post;
                    }
                })
                .subscribeOn(Schedulers.io());

    }

    int findFileInfo(myFileInfo fileInfo) {
        for(int i=0; i<localFList.size(); i++) {
            myFileInfo f = localFList.get(i);
            if(f.getFn()==fileInfo.getFn() && f.getDt()==fileInfo.getDt()) {
                //already exist
                return i;
            }
        }
        return -1;
    }
    JSONObject getJsonFromFile (String fname) {
        try {

            BufferedReader input = null;
            FileInputStream contentfile = null;
            try {
                contentfile = new FileInputStream("/storage/emulated/0/Download/" + fname);
                input = new BufferedReader(new InputStreamReader(contentfile));
                String line;
                StringBuffer content = new StringBuffer();
                char[] buffer = new char[1024];
                int num;
                while ((num = input.read(buffer)) > 0) {
                    content.append(buffer, 0, num);
                }
                JSONObject jsonObject = new JSONObject(content.toString());
                return jsonObject;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (contentfile != null) {
                    contentfile.close();
                }
                if (input != null) {
                    input.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    void updateData() {
        getPostsObservable()
                .subscribeOn(Schedulers.io())
                .flatMap(new Function<myFileInfo, ObservableSource<myFileInfo>>() {
                    @Override
                    public ObservableSource<myFileInfo> apply(myFileInfo post) throws Exception {

                        return getCommentsObservable(post);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<myFileInfo>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(myFileInfo fileInfo) {
                        Log.d(TAG, "onNext: "+fileInfo);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }

                    @Override
                    public void onComplete() {
                        syncFlist();
                        Prefs.saveFList(MapsActivity.this,localFList);
                        ArrayList<String> tmp = new ArrayList<>();
                        for(int i=0; i<localFList.size(); i++) {
                            String fname = localFList.get(i).getFn();
                            int ext = fname.lastIndexOf(".");
                            tmp.add(fname.substring(0,ext));
                        }
                        //ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MapsActivity.this, android.R.layout.simple_spinner_item, tmp);
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MapsActivity.this, R.layout.item, R.id.tvItem, tmp);
                        mySpinner.setAdapter(arrayAdapter);

                        Log.e(TAG, "onComplete");
                    }
                });
    }

    public void onBtnClicked(View v) {
        updateData();
        //getFileList();
        //new Decompress2("/storage/emulated/0/Download/to am.kmz", "/storage/emulated/0/Download/", this).execute();

    }

    private void loadLayer(String fname, String extension) {
        FileInputStream is = null;
        BufferedInputStream bis = null;
        try {
            if(extension.equalsIgnoreCase(".kmz"))
                fname += ".kml";
            else if(extension.equalsIgnoreCase(".geojson"))
                fname += ".geojson";

            is = new FileInputStream("/storage/emulated/0/Download/"+fname);
            bis = new BufferedInputStream(is);
            if(extension.equalsIgnoreCase(".kmz")) {
                KmlLayer kmlLayer = null;
                kmlLayer = new KmlLayer(mMap, bis, MapsActivity.this);
                kmlLayer.addLayerToMap();
                kmlLayer.setOnFeatureClickListener(new KmlLayer.OnFeatureClickListener() {
                    @Override
                    public void onFeatureClick(Feature feature) {
                        if (feature == null) {
                            Log.d(TAG, "feature is null");
                            return;
                        }
                        String ss = feature.getProperty("description");//getId();
                        tvDesc.setText(ss);
                        /*Toast.makeText(MapsActivity.this,
                                "Coming soon",
                                Toast.LENGTH_SHORT).show();*/
                    }
                });
            } else if(extension.equalsIgnoreCase(".geojson")) {
                GeoJsonLayer theLayer = null;
                theLayer = new GeoJsonLayer(mMap, getJsonFromFile(fname));
                theLayer.addLayerToMap();
                //layers.add(kmlLayer);
                theLayer.setOnFeatureClickListener(new KmlLayer.OnFeatureClickListener() {
                    @Override
                    public void onFeatureClick(Feature feature) {
                        if (feature == null) {
                            Log.d(TAG, "feature is null");
                            return;
                        }
                        String ss = feature.getProperty("description");//getId();
                        Toast.makeText(MapsActivity.this,
                                "Coming soon",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (is != null)
                    is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //updatePost(post);
    }
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        String item = adapterView.getItemAtPosition(position).toString();
        Log.d(TAG,"Selected: "+item);
        mMap.clear();
        loadLayer(item, ".kmz");
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_settings:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}


