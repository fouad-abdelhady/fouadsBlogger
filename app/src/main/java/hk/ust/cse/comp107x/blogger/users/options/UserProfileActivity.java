package hk.ust.cse.comp107x.blogger.users.options;

import android.app.SearchManager;
import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


import com.google.firebase.auth.FirebaseAuth;

import hk.ust.cse.comp107x.blogger.R;
import hk.ust.cse.comp107x.blogger.authentication.LoginActivity;

public class UserProfileActivity extends AppCompatActivity {
    private Toolbar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        setToolBarAsActionBar();
    }

    private void setToolBarAsActionBar() {
        actionBar = findViewById(R.id.profileActionBar);
        actionBar.setTitle("Profile");
        setSupportActionBar(actionBar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_profile_menu, menu);
        /*  setSearchItemStyle(menu.findItem(R.id.profileSearchItem));*/
        return true;
    }

    private void setSearchItemStyle(MenuItem searchItem) {
        SearchView searchView = (SearchView) searchItem.getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(this.SEARCH_SERVICE);
       /* if(searchItem != null){
            searchView.set
        }*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profileSignOutItem:
                FirebaseAuth.getInstance().signOut();
                moveToLogin();
                return true;
            case R.id.profileAccountSettingsItem:
                moveToAccountSettings();
                return true;
            default:
                return false;
        }
    }

    private void moveToLogin() {
        Intent login = new Intent(this, LoginActivity.class);
        startActivity(login);
        finish();
    }

    private void moveToAccountSettings() {
        Intent accountSettings = new Intent(UserProfileActivity.this,
                AccountSettingsActivity.class);
        startActivity(accountSettings);

    }
}
