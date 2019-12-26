package mohammad.com.fiberapp.utility;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class Decompress2 extends AsyncTask<Void, Integer, Integer> {

    private final static String TAG = "Decompress";
    private String zipFile;
    private String location;

    ProgressDialog myProgressDialog;
    Context ctx;

    public Decompress2(String zipFile, String location, Context ctx) {
        super();
        this.zipFile = zipFile;
        this.location = location;
        this.ctx = ctx;
        dirChecker("");
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        myProgressDialog = new ProgressDialog(ctx);
        myProgressDialog.setMessage("Please Wait... Unzipping");
        myProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        myProgressDialog.setCancelable(false);
        myProgressDialog.show();
    }

    @Override
    protected Integer doInBackground(Void... params){
        int count = 0;
        try {
            readUsingZipInputStream(zipFile, location);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        myProgressDialog.setProgress(progress[0]); //Since it's an inner class, Bar should be able to be called directly
    }

    @Override
    protected void onPostExecute(Integer result) {
        Log.i(TAG, "Completed. Total size: "+result);
        if(myProgressDialog != null && myProgressDialog.isShowing()){
            myProgressDialog.dismiss();
        }
    }

    private void dirChecker(String dir)
    {
        File f = new File(location + dir);
        if(!f.isDirectory())
        {
            f.mkdirs();
        }
    }

    //Method1:
    private static void readUsingZipFile(String FILE_NAME, String OUTPUT_DIR) throws IOException {
        final ZipFile file = new ZipFile(FILE_NAME);
        System.out.println("Iterating over zip file : " + FILE_NAME);
        try {
            final Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                System.out.printf("File: %s Size %d Modified on %TD %n", entry.getName(), entry.getSize(), new Date(entry.getTime()));
                extractEntry(OUTPUT_DIR, entry, file.getInputStream(entry));
            } System.out.printf("Zip file %s extracted successfully in %s", FILE_NAME, OUTPUT_DIR);
        } finally {
            file.close();
        }
    }

    //Method2
    /* * Example of reading Zip file using ZipInputStream in Java. */
    private static void readUsingZipInputStream(String FILE_NAME, String OUTPUT_DIR) throws IOException { BufferedInputStream bis = new BufferedInputStream(new FileInputStream(FILE_NAME));
        final ZipInputStream is = new ZipInputStream(bis);
        try {
            ZipEntry entry;
            while ((entry = is.getNextEntry()) != null) {
                System.out.printf("File: %s Size %d Modified on %TD %n", entry.getName(), entry.getSize(), new Date(entry.getTime()));
                extractEntry(OUTPUT_DIR, entry, is);
            }
        }
        finally { is.close();
        }
    }



    private static final int BUFFER_SIZE = 8192;
    /* * Utility method to read data from InputStream */
    public static void extractEntry(final String OUTPUT_DIR,final ZipEntry entry, InputStream is) throws IOException {
        String exractedFile = OUTPUT_DIR + entry.getName();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(exractedFile);
            final byte[] buf = new byte[BUFFER_SIZE];
            int read = 0;
            int length;
            while ((length = is.read(buf, 0, buf.length)) >= 0) {
                fos.write(buf, 0, length);
            } } catch (IOException ioex) { fos.close();
        }
    }


}
