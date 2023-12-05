package com.altistek.cpl_handheld.helpers;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileIO {
    @SuppressLint("SdCardPath")
    String state = Environment.getExternalStorageState(new File("/sdcard"));
    private File _file;

    protected boolean createFolder(String pathname) {
        boolean result = false;
        try
        {
            _file = new File(pathname);
            if (Environment.MEDIA_MOUNTED.equals(state))
            {
                if (!_file.exists())
                {
                    //Files.createDirectory(Paths.get(fileIO.getPath()));
                    _file.mkdirs();
                    result = true;
                }
            }
        }
        catch (Exception e)
        {
            Log.d("IO", e.toString());
            e.printStackTrace();
        }
        return result;
    }

    protected boolean createFile(String pathname) {
        boolean result = false;
        try
        {
            _file = new File(pathname);
            if (Environment.MEDIA_MOUNTED.equals(state))
            {
                if (!_file.exists())
                {
                    //Files.createFile(Paths.get(fileIO.getPath()));
                    _file.createNewFile();
                    result = true;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }

    protected boolean append(String pathname, String msg) {
        boolean result = false;
        _file = new File(pathname);
        //BufferedWriter for performance, true to set append to file flag
        try
        {
            BufferedWriter buf = new BufferedWriter(new FileWriter(_file, true));
            buf.append(msg);
            buf.newLine();
            buf.flush();
            buf.close();
            result = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }

    protected boolean clearContent(String pathname) {
        try
        {
            _file = new File(pathname);
            if (Environment.MEDIA_MOUNTED.equals(state))
            {
                if (_file.exists())
                {
//                    Path path = Paths.get(fileIO.getPath());
//                    Files.delete(path);
//                    Files.createFile(path);
                    if(_file.delete())
                        return _file.createNewFile();
                }
            }
            return false;
        }
        catch (Exception e)
        {
            //You'll need to add proper error handling here
            return false;
        }
    }

    protected String getContent(String pathname) {
        StringBuilder text = new StringBuilder();
        _file = new File(pathname);
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(_file));
            String line;

            while ((line = br.readLine()) != null)
            {
                text.append(line);
            }
            br.close();
            return text.toString();
        }
        catch (IOException e)
        {
            //You'll need to add proper error handling here
        }
        return "";
    }

    protected boolean isExist(String pathname) {
        _file = new File(pathname);
        return _file.exists();
        //return Files.exists(Paths.get(fileIO.getPath()));
    }
}

