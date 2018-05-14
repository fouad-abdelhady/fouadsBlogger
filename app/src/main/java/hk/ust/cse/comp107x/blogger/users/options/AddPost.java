package hk.ust.cse.comp107x.blogger.users.options;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import hk.ust.cse.comp107x.blogger.R;

public class AddPost extends AppCompatActivity {
    private static final int REQUEST_CODE = 25;
    private PermissionsManager pm;
    private ImageView postImage;
    private EditText description;
    private Toolbar actionBar;

    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        setToolBarAsActionBar();
        setViews();
    }
    private void setToolBarAsActionBar() {
        actionBar = findViewById(R.id.add_post_action_bar);
        actionBar.setTitle("Add New Post");
        setSupportActionBar(actionBar);
    }

    private void setViews() {
        postImage = findViewById(R.id.add_post_post_image);
        description = findViewById(R.id.add_post_post_description);
        pm = new PermissionsManager();
    }

    public void selectImage(View view) {
        begin();
    }

    private void begin() {
        if(checkPermissionState()){
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON).
                    setAspectRatio(2,1)
                    .start(this);
        }
    }

    private boolean checkPermissionState() {
        String [] permissions = new String[]{PermissionsManager.WRITE_EXTERNAL_SRORAGE,
                PermissionsManager.READ_EXTERNAL_STORAGE};
       return pm.checkPermissions(permissions, this, REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE)
            for (int state : grantResults) {
                if (state == PackageManager.PERMISSION_DENIED){
                    Toast.makeText(AddPost.this,
                            "Sorry we need these permissions to operate",
                            Toast.LENGTH_LONG).
                            show();
                    finish();
                    return;
                }
            }
           begin();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                imageUri = result.getUri();
                postImage.setImageURI(imageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
    public void addPost(View view) {
        if(checkInputs()){
            Toast.makeText(this, "Everything is good.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkInputs() {
        if(description.getText().toString().isEmpty()){
            description.setError("this field required");
            return false;
        }
        description.setError(null);
        if(imageUri == null){
            Toast.makeText(this, "select photo please", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

}
