package com.owambo.jvamcas.drivemehome.ui.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.owambo.jvamcas.drivemehome.R;
import com.owambo.jvamcas.drivemehome.databinding.ActivityMainBinding;
import com.owambo.jvamcas.drivemehome.databinding.NavHeaderMainBinding;
import com.owambo.jvamcas.drivemehome.utils.Const;
import com.owambo.jvamcas.drivemehome.utils.ParseUtil;
import com.owambo.jvamcas.drivemehome.viewmodel.UserViewModel;

import java.io.File;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private ActivityMainBinding mainBinding;
    private MaterialToolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private UserViewModel user_vModel;
    private NavController mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        handleViewModels();
        handleToolBar();
        handleNavigation();

        //create dir for storing profile pictures
        File parentDir = new File(getExternalFilesDir(null), Const.IMAGE_ROOT_PATH);
        if (!parentDir.exists())
            parentDir.mkdirs();
    }

    private void handleViewModels() {
        user_vModel = ViewModelProviders.of(this).get(UserViewModel.class);
    }

    private void handleToolBar() {
        mToolbar = mainBinding.appBarMain.mainToolbar;
        setSupportActionBar(mToolbar);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void handleNavigation() {
        mDrawerLayout = mainBinding.drawerLayout;

        mController = Navigation.findNavController(this, R.id.nav_host_fragment);
        AppBarConfiguration appConfig = new AppBarConfiguration.
                Builder(R.id.loginFragment, R.id.homeFragment)//indicates top level  view where three lines would show
                .setDrawerLayout(mDrawerLayout)
                .build();

        NavigationView mNavView = mainBinding.navView;
        NavigationUI.setupWithNavController(mToolbar, mController, appConfig);
        mNavView.setNavigationItemSelectedListener(this);

        NavHeaderMainBinding navHeaderBinding = NavHeaderMainBinding.bind(mNavView.getHeaderView(0));
        user_vModel.getUser().observe(this, user -> {
            if (user != null) {
                navHeaderBinding.setMUser(user);

                File mFile = new File(getExternalFilesDir(null), ParseUtil.iconPath(user.getId()));
                if (!mFile.exists())//if user profile pic not present in IMAGES/PROFILE_PICTURES --download it
                    user_vModel.downloadImage(mFile.getAbsolutePath());
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        mDrawerLayout.closeDrawer(GravityCompat.START);

        switch (item.getItemId()) {
            case R.id.nav_my_account:
                mController.navigate(R.id.action_global_myAccountFragment);
                break;
            case R.id.nav_current_trip:
                mController.navigate(R.id.action_global_currentTripFragment);
                break;
            case R.id.drive_requests:
                mController.navigate(R.id.action_global_driverRequestFragment);
                break;
        }
        return true;
    }

    public void onSignOut(View view) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        user_vModel.logout();
    }
}
