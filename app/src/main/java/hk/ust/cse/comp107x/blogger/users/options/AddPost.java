package hk.ust.cse.comp107x.blogger.users.options;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import hk.ust.cse.comp107x.blogger.R;

public class AddPost extends AppCompatActivity {
    private static final int REQUEST_CODE = 25;
    private static final String POSTS_IMAGES = "Posts Images";
    private static final String POSTS_FILE = "Posts";

    private PermissionsManager pm;

    private ImageView postImage;
    private EditText description;
    private Toolbar actionBar;

    private Uri imageUri = null;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;

    private AlertDialog uploadDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        setToolBarAsActionBar();
        setViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setFirebaseObjects();
    }

    private void setFirebaseObjects() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    private void setToolBarAsActionBar() {
        actionBar = findViewById(R.id.add_post_action_bar);
        actionBar.setTitle("Add New Post");
        setSupportActionBar(actionBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
           startUploadingData();
        }
    }

    private void startUploadingData() {
        setloadingDialog("Uploading Data","Please Wait");
        String imageName = auth.getCurrentUser().getUid()+ FieldValue.serverTimestamp()+".jpg";
        StorageReference imageRef = storageReference.child(POSTS_IMAGES).child(imageName);
        imageRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                setImageLinkToPost(taskSnapshot.getDownloadUrl());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                uploadDialog.dismiss();
                Toast.makeText(AddPost.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setImageLinkToPost(Uri downloadUrl) {

        HashMap<String, Object> newPost = new HashMap<>();
        newPost.put("Posted By", auth.getCurrentUser().getUid());
        newPost.put("Description", description.getText().toString());
        newPost.put("Image Link", downloadUrl.toString());
        newPost.put("TimeStamp", FieldValue.serverTimestamp().toString());

        firestore.collection(POSTS_FILE).add(newPost).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(AddPost.this, "The post added successfully.", Toast.LENGTH_LONG).show();
                uploadDialog.dismiss();
                moveToHome();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddPost.this, e.getMessage(), Toast.LENGTH_LONG).show();
                uploadDialog.dismiss();
            }
        });
    }

    private void moveToHome() {
        Intent i = new Intent(this, HomeActivity.class);
        startActivity(i);
        finish();
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

    private void setloadingDialog(String Title, String message) {
        AlertDialog.Builder loadingBuilder = new AlertDialog.Builder(AddPost.this);

        View uploadDialogLayout = LayoutInflater.from(this).inflate(R.layout.loading_state_dialog, null);
        TextView loadingStateMessage = uploadDialogLayout.findViewById(R.id.upload_stateMessage);

        loadingStateMessage.setText(message);
        loadingBuilder.setTitle(Title);
        loadingBuilder.setCancelable(false);
        loadingBuilder.setView(uploadDialogLayout);

        uploadDialog = loadingBuilder.create();
        uploadDialog.show();
    }

}
