package mohammad.com.fiberapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.util.ExtraConstants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.maps.android.data.kml.KmlLayer;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import mohammad.com.fiberapp.model.FileInfo;
import mohammad.com.fiberapp.model.FileList;
import mohammad.com.fiberapp.service.RetrofitInstance;
import mohammad.com.fiberapp.utility.Decompress2;
import okhttp3.ResponseBody;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.widget.Toast.LENGTH_LONG;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final int PERMISSION_REQUEST_CODE = 200;
    private GoogleMap mMap;
    private FileList localFList;
    private CompositeDisposable disposables = new CompositeDisposable();

    @NonNull
    public static Intent createIntent(@NonNull Context context, @Nullable IdpResponse response) {
        return new Intent().setClass(context, MapsActivity.class)
                .putExtra(ExtraConstants.IDP_RESPONSE, response);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localFList = Prefs.getFList(this);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(MainActivity.createIntent(this));
            finish();
            return;
        }
        //IdpResponse response = getIntent().getParcelableExtra(ExtraConstants.IDP_RESPONSE);

        setContentView(R.layout.activity_maps);
        if (!checkPermission()) {
            requestPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        LatLng bgd = new LatLng(33.2967658, 44.4707338);
        //mMap.addMarker(new MarkerOptions().position(bgd).title("Marker in Sydney"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(bgd, 10));
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

    private void requestPermission() {
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
    }

    private boolean processKmzResponse(ResponseBody body, final String filename) {
        KmlLayer kmlLayer = null;
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
                        int extensionIndex = filename.lastIndexOf(".");
                        String fname;
                        if (extensionIndex != -1)
                            fname = filename;
                        fname = filename.substring(0, extensionIndex);
                        fname += ".kml";
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

/*
                            while (true) {
                                int read = inputStream.read(fileReader);
                                if (read == -1) {
                                    break;
                                }

                                outputStream.write(fileReader, 0, read);

                                fileSizeDownloaded += read;

                                Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                            }

                outputStream.flush();*/

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
  /*  void downloadAFile(String filename, long filedate) {
        Call<ResponseBody> call2 = RetrofitInstance.getService().downloadFile(filename);

        call2.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "server contacted and has file");
                    boolean writtenToDisk = processKmzResponse(response.body(), filename);
                    Log.d(TAG, "file download was a success? " + writtenToDisk);
                } else {
                    Log.d(TAG, "server contact failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "error");
            }
        });
    }
*/
    private Observable<FileInfo> getPostsObservable(){
        return RetrofitInstance.getService()
                .getResults()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Function<List<FileInfo>, ObservableSource<FileInfo>>() {
                    @Override
                    public ObservableSource<FileInfo> apply(final List<FileInfo> posts) throws Exception {
                        return Observable.fromIterable(posts)
                                .subscribeOn(Schedulers.io());
                    }
                });
    }

    /*void getFileList() {
        Call<FileList> call= RetrofitInstance.getService().getResults();
        call.enqueue(new Callback<FileList>() {
            @Override
            public void onResponse(Call<FileList> call, Response<FileList> response) {
                //agendaArrayList = response.body();
                FileList remoteFList = response.body();
                if(remoteFList == null) {
                    if(getApplicationContext() != null) {
                        Toast.makeText(getApplicationContext(), "No data available. Use existing data", Toast.LENGTH_LONG).show();
                    }
                } else {
                    for (int i=0; i<remoteFList.getFiles().size(); i++) {
                        String filename = remoteFList.getFiles().get(i);
                        Long fdate = remoteFList.getFdates().get(i);
                        //if(localFList.getFiles().indexOf(filename) == -1 || localFList.getFdates().indexOf(fdate) == -1){
                        if(localFList.getFiles().indexOf(filename) == -1){//file not exist on local
                            //add
                            downloadAFile(filename, fdate);
                        } else if(localFList.getFdates().indexOf(fdate) == -1){//file exist, but old
                            //overwrite
                            downloadAFile(filename, fdate);
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<FileList> call, Throwable t) {
                Toast.makeText(getApplicationContext(),"Failure", Toast.LENGTH_SHORT).show();
            }
        });
        }
*/


    private void retrieveFileFromResource() {
        try {
            KmlLayer kmlLayer = new KmlLayer(mMap, R.raw.to_am, this);
            kmlLayer.addLayerToMap();
            //moveCameraToKml(kmlLayer);
            // Add a marker in Sydney and move the camera

        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }


    private Observable<FileInfo> getCommentsObservable(final FileInfo post){
        return RetrofitInstance.getService()
                .downloadFile(post.getFn())
                .map(new Function<ResponseBody, FileInfo>() {
                    @Override
                    public FileInfo apply(ResponseBody body) throws Exception {
                        Log.d(TAG, "downloaded. saving...");
                        post.error = processKmzResponse(body, post.getFn())?0:-1;
                        //post.setComments(comments);
                        return post;
                    }
                })
                .subscribeOn(Schedulers.io());

    }

    public void onBtnClicked(View v) {
        getPostsObservable()
                .subscribeOn(Schedulers.io())
                .flatMap(new Function<FileInfo, ObservableSource<FileInfo>>() {
                    @Override
                    public ObservableSource<FileInfo> apply(FileInfo post) throws Exception {
                        return getCommentsObservable(post);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<FileInfo>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(FileInfo post) {
                        Log.d(TAG, "onNext: "+post);
                        //updatePost(post);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "onComplete");
                    }
                });
        //getFileList();
        //new Decompress2("/storage/emulated/0/Download/to am.kmz", "/storage/emulated/0/Download/", this).execute();

    }
}
