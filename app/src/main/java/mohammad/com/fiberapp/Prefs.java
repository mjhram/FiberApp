package mohammad.com.fiberapp;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mohammad.com.fiberapp.model.myFileInfo;

public class Prefs {
    static private String SP_NAME = "AppSettings";
    public static final String FLIST_KEY = "File_List";

    /*public static void putFList(FileList fList, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        int ss = fList.getFiles().size();
        if(ss != fList.getFdates().size()) {
            //bad fileList.do nothing
        } else {
            editor.putInt("size", ss);
            for (int i = 0; i < ss; i++) {
                editor.putString("fn"+i, fList.getFiles().get(i));
                editor.putLong("fd"+i, fList.getFdates().get(i));
            }
            editor.apply();
        }
    }*/

    public static void saveFList(Context context, List<myFileInfo> flist) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();

        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(flist);

        editor.putString(FLIST_KEY, jsonFavorites);
        editor.commit();
    }

    public static ArrayList<myFileInfo> loadFList(Context context) {
        SharedPreferences settings;
        List<myFileInfo> flist;

        settings = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        if (settings.contains(FLIST_KEY)) {
            String jsonFavorites = settings.getString(FLIST_KEY, null);
            Gson gson = new Gson();
            myFileInfo[] favoriteItems = gson.fromJson(jsonFavorites,
                    myFileInfo[].class);

            flist = Arrays.asList(favoriteItems);
            flist = new ArrayList<myFileInfo>(flist);
        } else
            return null;

        return (ArrayList<myFileInfo>) flist;
    }

    /*public static FileList `(Context mContext) {
        //if size =0, return
        //files.get(i) may equal ""
        //fdates.get(i) may equal 0
        SharedPreferences prefs = mContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        //SharedPreferences.Editor editor = prefs.edit();
        FileList fList = new FileList();
        ArrayList<String> files = new ArrayList<String>();
        ArrayList<Long> fdates = new ArrayList<Long>();
        fList.setFiles(files);
        fList.setFdates(fdates);
        int ss = prefs.getInt("size", 0);
        if(ss == 0) return fList;

        for (int i = 0; i < ss; i++) {
            files.add(prefs.getString("fn"+i,""));
            fdates.add(prefs.getLong("fd"+i,0));
        }

        return fList;
    }*/

    public static void setPrefs(String key, String value, Context context){
        SharedPreferences sharedpreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getPrefs(String key, Context context){
        SharedPreferences sharedpreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sharedpreferences.getString(key, "");
    }

    public static void setStringArrayPrefs(String arrayName, List<String> array, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(arrayName +"_size", array.size());
        for(int i=0;i<array.size();i++)
            editor.putString(arrayName + "_" + i, array.get(i));
        editor.apply();
    }

    public static List<String> getStringArrayPrefs(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        int size = prefs.getInt(arrayName + "_size", 0);
        ArrayList<String> array = new ArrayList<>(size);
        for(int i=0;i<size;i++)
            array.add(prefs.getString(arrayName + "_" + i, ""));
        return array;
    }

    public static void setLongArrayPrefs(String arrayName, List<Long> array, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(arrayName +"_size", array.size());
        for(int i=0;i<array.size();i++)
            editor.putLong(arrayName + "_" + i, array.get(i));
        editor.apply();
    }

    public static List<Long> getLongArrayPrefs(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        int size = prefs.getInt(arrayName + "_size", 0);
        ArrayList<Long> array = new ArrayList<>(size);
        for(int i=0;i<size;i++)
            array.add(prefs.getLong(arrayName + "_" + i, 0));
        return array;
    }

    public static void setStringLongArrayPrefs(List<String> fname, List<Long> fdate, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        int ss = fname.size();
        if(ss != fdate.size()) {

        } else {
            editor.putInt("size", ss);
            for (int i = 0; i < ss; i++)
                editor.putLong(fname.get(i), fdate.get(i));
            editor.apply();
        }
    }

    public static long getFDate(String fname, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        long l = prefs.getLong(fname, 0);
        return l;
    }
}