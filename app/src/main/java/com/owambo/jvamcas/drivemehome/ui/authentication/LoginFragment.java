package com.owambo.jvamcas.drivemehome.ui.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.firebase.ui.auth.AuthUI;
import com.owambo.jvamcas.drivemehome.R;
import com.owambo.jvamcas.drivemehome.databinding.FragmentLoginBinding;
import com.owambo.jvamcas.drivemehome.ui.TopAppFragment;
import com.owambo.jvamcas.drivemehome.utils.Const;
import com.owambo.jvamcas.drivemehome.viewmodel.UserViewModel;

import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 */
public class LoginFragment extends TopAppFragment {

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState);
        FragmentLoginBinding loginBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false);

        handleAuth();

        return loginBinding.getRoot();
    }

    private void handleAuth() {
        user_vModel.getAuthState().observe(getViewLifecycleOwner(), authState -> {
            switch (authState) {
                case AUTHENTICATED:
                    navController.navigate(R.id.action_global_homeFragment);
                    //go to home fragment
                    break;
                case UNAUTHENTICATED:
                    authenticate();
                    break;
                case AUTH_CANCELLED:
                    user_vModel.cancelAuth(getViewLifecycleOwner());
                    showExitDialog(true);
                    break;
                case UNKNOWN_ERROR:
                    showToast(requireActivity().getResources().getString(R.string.authentication_error));
                    user_vModel.cancelAuth(getViewLifecycleOwner());
                    break;
                case NETWORK_ERROR:
                    showToast(requireActivity().getResources().getString(R.string.no_internet));
                    user_vModel.cancelAuth(getViewLifecycleOwner());
                    break;
            }
        });
    }
    private void authenticate() {
        // [START auth_fui_create_intent]
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                Const.RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int RequestCode, int ResultCode, Intent data) {
        super.onActivityResult(RequestCode, ResultCode, data);
        user_vModel.authenticate(RequestCode, ResultCode, data);
    }
}
