package hk.ust.cse.comp107x.blogger.users.options;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import hk.ust.cse.comp107x.blogger.MainActivity;
import hk.ust.cse.comp107x.blogger.R;
import hk.ust.cse.comp107x.blogger.authentication.CreateAccountActivity;

public class AccountSettingsActivity extends AppCompatActivity {
    private final static String SETTINGS_LOG_D = "accountSettingsDebug";
    private final static int REQUEST_CODE = 22;
    public static final String PROFILE_IMAGES = "Profile_images";
    public static final String USER_NAME = "User Name";
    public static final String USER_IMAGE = "Profile Image";
    public static final String USERS_FILE = "Users";
    private  PermissionsManager pm;

    private CircleImageView profileImage;
    private EditText userName;
    private TextView clickToSetImage;

    private AlertDialog uploadDialog = null;
    private ProgressBar loadingProgress = null;

    private FirebaseAuth auth;
    private StorageReference storage;
    private FirebaseFirestore firestore;

    private Uri imageUri= null;

    private  String userProfileImage;

    private int from = 0;
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
        Intent i = getIntent();
        from = i.getIntExtra(MainActivity.COME_FROM, -1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setProfileImageClickListener();
        setFirebaseObj();
        if(from == -1)
            checkUserData();
    }

    private void checkUserData() {

        setloadingDialog("Downloading your data ", "Downloading now...");
        String userID = auth.getCurrentUser().getUid();
        firestore.collection(USERS_FILE).document(userID).get().
                addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Log.d(SETTINGS_LOG_D,"in On complete now");
                if(task.isSuccessful()){
                    Log.d(SETTINGS_LOG_D,"Task is successful");
                    if(task.getResult().exists()){
                        clickToSetImage.setVisibility(View.INVISIBLE);
                        userName.setText(task.getResult().getString(USER_NAME));
                        userProfileImage = task.getResult().getString(USER_IMAGE);


                        Glide.with(AccountSettingsActivity.this).load(userProfileImage).into(profileImage);


                    }
                    uploadDialog.dismiss();
                }
                else{
                    Toast.makeText(AccountSettingsActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG);
                    uploadDialog.dismiss();
                }

            }
        });

    }

    private void setFirebaseObj() {
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();
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
                profileImage.setImageURI(result.getUri());
                clickToSetImage.setVisibility(View.INVISIBLE);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void checkAndContinue(View view) {
        if(check()){
           uploadImage();
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

    private void uploadImage(){
        setloadingDialog("Uploading Data", "Please Wait, uploading your data...");

        StorageReference image_ref = storage.child(PROFILE_IMAGES).
                child(auth.getCurrentUser().
                        getUid()+".jpg");
        image_ref.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                saveUserNameAndImageURI(task.getResult().getDownloadUrl());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.
                        makeText(AccountSettingsActivity.this,
                                e.getMessage(),
                                Toast.LENGTH_LONG).
                        show();
                uploadDialog.dismiss();
            }
        });
    }

    private void saveUserNameAndImageURI(Uri downloadUrl) {

        HashMap<String, Object> user = new HashMap<>();
        user.put(USER_NAME, userName.getText().toString());
        user.put(USER_IMAGE, downloadUrl.toString());

        firestore.
                collection(USERS_FILE).
                document(auth.getCurrentUser().getUid()).
                set(user).
                addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                uploadDialog.dismiss();
                Intent i = new Intent(AccountSettingsActivity.this,HomeActivity.class);
                startActivity(i);
                if(from == CreateAccountActivity.CREATE_ACCOUNT){
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.
                        makeText(AccountSettingsActivity.this, e.getMessage(), Toast.LENGTH_LONG).
                        show();

                uploadDialog.dismiss();
            }
        });

    }

    private void setloadingDialog(String Title, String message) {
       AlertDialog.Builder loadingBuilder = new AlertDialog.Builder(AccountSettingsActivity.this);

        View uploadDialogLayout = LayoutInflater.from(this).inflate(R.layout.loading_state_dialog, null);
        loadingProgress = uploadDialogLayout.findViewById(R.id.uploading_image);
        TextView loadingStateMessage = uploadDialogLayout.findViewById(R.id.upload_stateMessage);

        loadingStateMessage.setText(message);
        loadingBuilder.setTitle(Title);
        loadingBuilder.setCancelable(false);
        loadingBuilder.setView(uploadDialogLayout);

        uploadDialog = loadingBuilder.create();
        uploadDialog.show();
    }
}

