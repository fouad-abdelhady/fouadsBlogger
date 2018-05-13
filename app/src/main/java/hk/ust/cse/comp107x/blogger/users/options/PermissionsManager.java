package hk.ust.cse.comp107x.blogger.users.options;

import  android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;

public class PermissionsManager {
    public static final String INTERNET = Manifest.permission.INTERNET;
    public static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String WRITE_EXTERNAL_SRORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private int requestCode;
    public PermissionsManager(){}

    public boolean checkPermissions(String [] permissions, Activity context, int requestCode){
        this.requestCode = requestCode;
        ArrayList<String> deniedPermissions = new ArrayList<>();

        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission);
            }
        }

        if (!deniedPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(context,
                    deniedPermissions.toArray(new String[deniedPermissions.size()]),
                    requestCode);
            return false;
        }
        return true;
    }



}
