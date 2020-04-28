package com.owambo.jvamcas.drivemehome.ui.home;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.google.android.material.textview.MaterialAutoCompleteTextView;
import com.owambo.jvamcas.drivemehome.R;
import com.owambo.jvamcas.drivemehome.data.model.User;
import com.owambo.jvamcas.drivemehome.databinding.FragmentUpdateUserProfileBinding;
import com.owambo.jvamcas.drivemehome.databinding.UserIconLayoutBinding;
import com.owambo.jvamcas.drivemehome.ui.camera.CameraCaptureActivity;
import com.owambo.jvamcas.drivemehome.utils.Const;
import com.owambo.jvamcas.drivemehome.utils.Results;
import com.squareup.picasso.Picasso;

import java.util.Objects;

/**
 *
 */
public class UpdateUserProfileFragment extends AbstractHomeFragmentHelper {


    private FragmentUpdateUserProfileBinding profileBinding;
    private final User[] mUser = {null};

    public UpdateUserProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState);
        profileBinding = FragmentUpdateUserProfileBinding.inflate(inflater, container, false);

        user_vModel.getUser().observe(getViewLifecycleOwner(), user -> {
            mUser[0] = user;
            profileBinding.setMUser(user);
            profileBinding.setErrorMsg(getString(R.string.empty_text_input_error));
        });

        handleLicenseCode();
        handleImageCapture();

        profileBinding.updateMyAccount.setOnClickListener(view -> {
            user_vModel.updateUserProfile(mUser[0]);
            user_vModel.getOpResults().observe(getViewLifecycleOwner(), opResults -> {
                if (opResults instanceof Results.Success) {
                    showToast(requireActivity().getResources().getString(R.string.profile_update_success));
                    navController.popBackStack();
                } else parseOpResults(opResults);
                user_vModel.resetOpResults(getViewLifecycleOwner());
            });
        });
        return profileBinding.getRoot();
    }

    private void handleLicenseCode() {
        MaterialAutoCompleteTextView licenseCodeDropDown = profileBinding.licenseCodeDropdown;
        ArrayAdapter<CharSequence> licenseCodeAdapter = ArrayAdapter.
                createFromResource(requireActivity(), R.array.license_code, R.layout.dropdown_menu_item);
        licenseCodeDropDown.setAdapter(licenseCodeAdapter);
    }

    private void handleImageCapture() {
        UserIconLayoutBinding iconBinding = profileBinding.userAvatar;
        iconBinding.takeNewPicture.setOnClickListener(view -> {
            Intent mIntent = new Intent(requireActivity(), CameraCaptureActivity.class);
            mIntent.putExtra(Const.ICON_PATH, Objects.requireNonNull(user_vModel.getUser().getValue()).getId());
            startActivityForResult(mIntent, Const.CAPTURE_PICTURE);
        });
    }

    @Override
    public void onActivityResult(int RequestCode, int ResultCode, Intent data) {
        if (ResultCode == Activity.RESULT_OK && RequestCode == Const.CAPTURE_PICTURE) {
            String absPath = data.getStringExtra(Const.ICON_PATH);

            //upload to storage image recently captured
            user_vModel.uploadImage(absPath);
            user_vModel.getOpResults().observe(getViewLifecycleOwner(), results -> {
                if ((results instanceof Results.Success)) {
                    Picasso.get().invalidate("file:"+absPath);//force Picasso to reload this image
                    //showToast(requireActivity().getString(R.string.profile_pic_update_success));
                }
                super.parseOpResults(results);
                user_vModel.resetOpResults(getViewLifecycleOwner());
            });
        }
    }
}
