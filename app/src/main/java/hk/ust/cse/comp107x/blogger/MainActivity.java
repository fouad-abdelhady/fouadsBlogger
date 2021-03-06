package hk.ust.cse.comp107x.blogger;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import hk.ust.cse.comp107x.blogger.authentication.LoginActivity;
import hk.ust.cse.comp107x.blogger.users.options.AccountSettingsActivity;
import hk.ust.cse.comp107x.blogger.users.options.HomeActivity;

public class MainActivity extends AppCompatActivity {

    private static final String[] PERMISSTIONS = new String[]{
            Manifest.permission.INTERNET
    };
    public static final String COME_FROM = "come from";
    public static final int MAIN_ACTIVTY = 2;
    private static final int PERMISSIONS_REQUEST_CODE = 1;

    private boolean permissionsGranted = false;
    private boolean isAcoountSettingsComplete = false;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setFirebaseObjects();
        begin();
    }

    private void setFirebaseObjects() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    private void begin() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            permissionsGranted = true;
        else
            checkPermissionsState();


        if (!permissionsGranted) {
            return;
        }
        checkUser();
    }

    private void checkPermissionsState() {
        ArrayList<String> permissions = new ArrayList<>();

        for (String permission : PERMISSTIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(permission);
            }
        }

        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    permissions.toArray(new String[permissions.size()]),
                    PERMISSIONS_REQUEST_CODE);
            return;
        }
        permissionsGranted = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE)
            for (int state : grantResults) {
                if (state == PackageManager.PERMISSION_DENIED){
                    permissionsGranted = false;
                    Toast.makeText(MainActivity.this,
                            "Sorry we need these permissions to operate",
                            Toast.LENGTH_LONG).
                            show();
                    finish();
                    return;
                }
            }
            permissionsGranted = true;
        begin();
    }

    private void checkUser() {
        Intent i;

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            i = new Intent(this, LoginActivity.class);
        }
        else{
            if(isAccountSettingsComplete(auth.getCurrentUser().getUid())){
                i = new Intent(this, AccountSettingsActivity.class);
                i.putExtra(COME_FROM, MAIN_ACTIVTY);
            }
            else
                i = new Intent(this, HomeActivity.class);
        }

        startActivity(i);
        finish();
    }

    private boolean isAccountSettingsComplete(String uid) {
        firestore.
                collection(AccountSettingsActivity.USERS_FILE).
                document(uid).get().
                addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.getResult().exists()){
                    isAcoountSettingsComplete = true;
                }
                else{
                    isAcoountSettingsComplete = false;
                }
            }
        });
        return isAcoountSettingsComplete;
    }
}
