package com.altistek.cpl_handheld.permission;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;


import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

public class PermissionHelper {
    private final String TAG = "-PermissionHelper-";

    public static final int REQ_PERMISSION_CODE = 101;

    public static String[] mStoragePerms = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static String[] mLocationPerms = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public static boolean checkPermission(Context ctx, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionResult = PermissionChecker.checkSelfPermission(ctx, permission);
            if (permissionResult == PackageManager.PERMISSION_GRANTED)
                return true;
            else
                return false;
        }
        else
            return true;
    }

    public static void requestPermission(Activity activity, String[] permissions) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[0]))
            ActivityCompat.requestPermissions(activity, permissions, REQ_PERMISSION_CODE);
        else
            ActivityCompat.requestPermissions(activity, permissions, REQ_PERMISSION_CODE);
    }
}

