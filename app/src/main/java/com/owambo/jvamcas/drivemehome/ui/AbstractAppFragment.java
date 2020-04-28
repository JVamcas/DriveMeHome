package com.owambo.jvamcas.drivemehome.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.ActivityNavigator;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;

import com.owambo.jvamcas.drivemehome.R;
import com.owambo.jvamcas.drivemehome.viewmodel.UserViewModel;

public abstract class AbstractAppFragment extends Fragment{
    public NavController navController;
    private ProgressDialog mProgressBar;
    public UserViewModel user_vModel;

    @Override
    public void onCreate(Bundle savedBundleInstance) {
        super.onCreate(savedBundleInstance);
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        user_vModel = ViewModelProviders.of(requireActivity()).get(UserViewModel.class);

    }@Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstance) {

        user_vModel.getAuthState().observe(getViewLifecycleOwner(), authState -> {

            NavDestination currentDestination = navController.getCurrentDestination();

            if (currentDestination.getId() != R.id.loginFragment && authState == UserViewModel.AuthState.UNAUTHENTICATED)
                navController.navigate(R.id.action_global_loginFragment);

        });
        return null;
    }

    public void showToast(String msg) {
        Toast.makeText(requireActivity(), msg, Toast.LENGTH_LONG).show();
    }

    public void showProgressBar(String message){
        if(mProgressBar == null) {
            mProgressBar = new ProgressDialog(requireContext());
            mProgressBar.setCancelable(false);
            mProgressBar.setMessage(message);
            mProgressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressBar.show();
        }
    }
    public void endProgressBar(){
        if(mProgressBar != null)
            mProgressBar.cancel();
    }
}
