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
import hk.ust.cse.comp107x.blogger.users.options.HomeActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setViews();
    }

    private void setViews() {
        email = findViewById(R.id.createAccountEmail);
        password = findViewById(R.id.createAccountPassword);
        loading = findViewById(R.id.loginLoading);
    }

    public void signUserIn(View view) {
        loading.setVisibility(View.VISIBLE);
        if(!allRight())
            return;
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email.getText().toString(),
                password.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                loading.setVisibility(View.INVISIBLE);
                Intent  userProfile = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(userProfile);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loading.setVisibility(View.INVISIBLE);
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean allRight() {
        if(email.getText().toString().isEmpty()){
            email.setError("The email address is required");
            return false;
        }
        if(password.getText().toString().isEmpty()){
            password.setError("You cannot access without password");
            return  false;
        }

        email.setError(null);
        password.setError(null);
        return true;
    }

    public void moveToCreateAccount(View view) {
        Intent createAccount = new Intent(this, CreateAccountActivity.class);
        startActivity(createAccount);
    }
}
