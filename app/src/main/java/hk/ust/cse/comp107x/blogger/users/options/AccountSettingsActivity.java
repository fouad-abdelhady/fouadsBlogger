package hk.ust.cse.comp107x.blogger.users.options;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;
import hk.ust.cse.comp107x.blogger.MainActivity;
import hk.ust.cse.comp107x.blogger.R;

public class AccountSettingsActivity extends AppCompatActivity {
    private final static int REQUEST_CODE = 22;
    private  PermissionsManager pm;

    private CircleImageView profileImage;
    private EditText userName;
    private TextView clickToSetImage;

    private Uri imageUri= null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        setViews();
    }

    private void setViews() {
        profileImage = findViewById(R.id.profile_image);
        userName = findViewById(R.id.Username);
        clickToSetImage = findViewById(R.id.clickToSetImage);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setProfileImageClickListener();
    }

    private void setProfileImageClickListener() {
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pm = new PermissionsManager();
                String [] permissions = new String[]{PermissionsManager.READ_EXTERNAL_STORAGE,
                        PermissionsManager.WRITE_EXTERNAL_SRORAGE};
                if(pm.checkPermissions(permissions, AccountSettingsActivity.this,
                        REQUEST_CODE)){

                    selectProfileImage();
                }
                else{

                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE)
            for (int state : grantResults) {
                if (state == PackageManager.PERMISSION_DENIED){
                    Toast.makeText(AccountSettingsActivity.this,
                            "Sorry we need these permissions to operate",
                            Toast.LENGTH_LONG).
                            show();
                    finish();
                    return;
                }
            }
      selectProfileImage();
    }

    private void selectProfileImage(){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON).
                setAspectRatio(1,1)
                .start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                imageUri = result.getUri();
                profileImage.setImageURI(imageUri);
                clickToSetImage.setVisibility(View.INVISIBLE);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void checkAndontinue(View view) {
        if(check()){

        }
    }

    private boolean check() {
        if(userName.getText().toString().isEmpty()) {
            userName.setError("Enter User Name please");
            return  false;
        }
        userName.setError(null);
        if(imageUri == null){
            Toast.makeText(this, "Please Select profile image", Toast.LENGTH_LONG).
                    show();
            return false;
        }
        return true;
    }
}
