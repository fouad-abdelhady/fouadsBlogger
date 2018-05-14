package hk.ust.cse.comp107x.blogger.authentication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import hk.ust.cse.comp107x.blogger.R;
import hk.ust.cse.comp107x.blogger.users.options.AccountSettingsActivity;

public class CreateAccountActivity extends AppCompatActivity {
    public static final String COME_FROM = "from";
    public static final int CREATE_ACCOUNT = 1;
    private EditText email;
    private EditText password;
    private EditText confirmPassword;

    private String Email;
    private String Password;
    private ProgressBar loading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        setViews();
    }

    private void setViews() {
        email = findViewById(R.id.createAccountEmail);
        password = findViewById(R.id.createAccountPassword);
        confirmPassword = findViewById(R.id.confirmPassword);
        loading = findViewById(R.id.createAccountLoading);
    }

    public void signUserUp(View view) {
        loading.setVisibility(View.VISIBLE);
        if(!allRight()){
            return;
        }
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(Email, Password).
                addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                loading.setVisibility(View.INVISIBLE);
                Intent accountSettings = new Intent(CreateAccountActivity.this,
                        AccountSettingsActivity.class);
                accountSettings.putExtra(COME_FROM, CREATE_ACCOUNT);
                startActivity(accountSettings);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loading.setVisibility(View.INVISIBLE);
                Toast.makeText(CreateAccountActivity.
                        this, e.getMessage(),
                        Toast.LENGTH_LONG).
                        show();
            }
        });
    }

    private boolean allRight() {
        String message = "This Field Required";
        if(email.getText().toString().isEmpty()){
           setErrorMessage(email, message);
           return false;
        }
        Email = email.getText().toString();
        setErrorMessage(email, null);

        if(password.getText().toString().isEmpty()){
            setErrorMessage(password, message);
            return false;
        }
        setErrorMessage(password, null);

        if(confirmPassword.getText().toString().isEmpty()){
            setErrorMessage(confirmPassword, message);
            return false;
        }
        setErrorMessage(confirmPassword, null);

        if(!confirmPassword.getText().toString().equals(password.getText().toString())){
            message = "those dose not match";
            setErrorMessage(password, message);
            setErrorMessage(confirmPassword, message);
            return false;
        }
        setErrorMessage(password, null);
        setErrorMessage(confirmPassword, null);
        Password = password.getText().toString();
        return true;
    }
    private void setErrorMessage(EditText editText, String message){
        editText.setError(message);
    }

    public void backToLogin(View view) {
        Intent locinActivity = new Intent(this, LoginActivity.class);
        startActivity(locinActivity);
        finish();
    }
}
