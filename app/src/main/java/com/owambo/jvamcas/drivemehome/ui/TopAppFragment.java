package com.owambo.jvamcas.drivemehome.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;

import com.owambo.jvamcas.drivemehome.R;

public abstract class TopAppFragment extends AbstractAppFragment {

    private AlertDialog mDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        handleBackPressed();
        return null;
    }

    private void handleBackPressed() {
        //handle back press
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        showExitDialog(false);
                    }
                });
    }

    protected void showExitDialog(boolean fromLogin) {
        if (mDialog != null && mDialog.isShowing())
            return;

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(requireContext());
        View mView = getLayoutInflater().inflate(R.layout.app_dismiss_dialog, null);
        mBuilder.setView(mView);
        mDialog = mBuilder.create();
        mDialog.setCancelable(false);

        mView.findViewById(R.id.ok_exit_btn).setOnClickListener(view -> {
            mDialog.dismiss();
            requireActivity().finish();
        });
        mView.findViewById(R.id.cancel_exit_btn).setOnClickListener(view -> {
            mDialog.dismiss();
            if(fromLogin)navController.navigate(R.id.action_global_loginFragment);//nav back to login
        });
        mDialog.show();
    }
}
